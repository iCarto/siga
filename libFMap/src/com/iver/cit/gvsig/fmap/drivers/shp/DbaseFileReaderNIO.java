/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    This file is based on an origional contained in the GISToolkit project:
 *    http://gistoolkit.sourceforge.net/
 *
 */
/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib��ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.fmap.drivers.shp;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Calendar;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geotools.resources.NIOUtilities;
import org.geotools.resources.NumberParser;

import com.iver.cit.gvsig.fmap.drivers.dbf.DbfEncodings;


/** A DbaseFileReader is used to read a dbase III format file.
 * <br>
 * The general use of this class is:
 * <CODE><PRE>
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * DbaseFileReader r = new DbaseFileReader( in )
 * Object[] fields = new Object[r.getHeader().getNumFields()];
 * while (r.hasNext()) {
 *    r.readEntry(fields);
 *    // do stuff
 * }
 * r.close();
 * </PRE></CODE>
 * For consumers who wish to be a bit more selective with their reading of rows,
 * the Row object has been added. The semantics are the same as using the
 * readEntry method, but remember that the Row object is always the same. The
 * values are parsed as they are read, so it pays to copy them out (as each call
 * to Row.read() will result in an expensive String parse).
 * <br><b>EACH CALL TO readEntry OR readRow ADVANCES THE FILE!</b><br>
 * An example of using the Row method of reading:
 * <CODE><PRE>
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * DbaseFileReader r = new DbaseFileReader( in )
 * int fields = r.getHeader().getNumFields();
 * while (r.hasNext()) {
 *   DbaseFileReader.Row row = r.readRow();
 *   for (int i = 0; i < fields; i++) {
 *     // do stuff
 *     Foo.bar( row.read(i) );
 *   }
 * }
 * r.close();
 * </PRE></CODE>
 *
 * @author Ian Schneider
 */
public class DbaseFileReaderNIO {

  public final class Row {
    public Object read(int column) throws IOException {
      int offset = getOffset(column);
      return readObject(offset, column);
    }
  }

  DbaseFileHeaderNIO header;
  ByteBuffer buffer;
  ReadableByteChannel channel;
  CharBuffer charBuffer;
  CharsetDecoder decoder;
  char[] fieldTypes;
  int[] fieldLengths;
  int cnt = 1;
  Row row;
  NumberParser numberParser = new NumberParser();

  /** Creates a new instance of DBaseFileReader
   * @param channel The readable channel to use.
   * @throws IOException If an error occurs while initializing.
   */
  public DbaseFileReaderNIO(ReadableByteChannel channel) throws IOException {
    this.channel = channel;

    header = new DbaseFileHeaderNIO();
    ///header.readHeader(channel);

    init();
  }

  private int fill(ByteBuffer buffer,ReadableByteChannel channel) throws IOException {
    int r = buffer.remaining();
    // channel reads return -1 when EOF or other error
    // because they a non-blocking reads, 0 is a valid return value!!
    while (buffer.remaining() > 0 && r != -1) {
      r = channel.read(buffer);
    }
    if (r == -1) {
      buffer.limit(buffer.position());
    }
    return r;
  }

  private void bufferCheck() throws IOException {
    // remaining is less than record length
    // compact the remaining data and read again
    if (!buffer.isReadOnly() && buffer.remaining() < header.getRecordLength()) {
      buffer.compact();
      fill(buffer,channel);
      buffer.position(0);
    }
  }

  private int getOffset(int column) {
    int offset = 0;
    for (int i = 0, ii = column; i < ii; i++) {
      offset += fieldLengths[i];
    }
    return offset;
  }

  private void init() throws IOException {
    // create the ByteBuffer
    // if we have a FileChannel, lets map it
    if (channel instanceof FileChannel) {
      FileChannel fc = (FileChannel) channel;
      buffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
      buffer.position((int) fc.position());
    } else {
      // Some other type of channel
      // start with a 8K buffer, should be more than adequate
      int size = 8 * 1024;
      // if for some reason its not, resize it
      size = header.getRecordLength() > size ? header.getRecordLength() : size;
      buffer = ByteBuffer.allocateDirect(size);
      // fill it and reset
      fill(buffer,channel);
      buffer.flip();
    }

    // The entire file is in little endian
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    // Set up some buffers and lookups for efficiency
    fieldTypes = new char[header.getNumFields()];
    fieldLengths = new int[header.getNumFields()];
    for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
      fieldTypes[i] = header.getFieldType(i);
      fieldLengths[i] = header.getFieldLength(i);
    }

    charBuffer = CharBuffer.allocate(header.getRecordLength() - 1);
    String charSetName = DbfEncodings.getInstance().getCharsetForDbfId(header.getLanguageID());
	Charset charSet=null;
    if (charSetName == null)
	{
		charSet = Charset.defaultCharset();
	}
	else
	{
		if (charSetName.equalsIgnoreCase("UNKNOWN"))
			charSet = Charset.defaultCharset();
		else{
			try{
				charSet = Charset.forName(charSetName);
			}catch (UnsupportedCharsetException e) {
				JOptionPane.showMessageDialog(null, "Unsupported CharSet for the System");
				Logger.getLogger(this.getClass()).warn(e.getLocalizedMessage(), e);
				charSet = Charset.defaultCharset();
			}
		}
	}
//    Charset chars = Charset.forName("ISO-8859-1");
    decoder = charSet.newDecoder();

    row = new Row();
  }



  /** Get the header from this file. The header is read upon instantiation.
   * @return The header associated with this file or null if an error occurred.
   */
  public DbaseFileHeaderNIO getHeader() {
    return header;
  }

  /** Clean up all resources associated with this reader.<B>Highly recomended.</B>
   * @throws IOException If an error occurs.
   */
  public void close() throws IOException {
    if (channel.isOpen()) {
      channel.close();
    }
    if (buffer instanceof MappedByteBuffer) {
      NIOUtilities.clean(buffer);
    }
    buffer = null;
    channel = null;
    charBuffer = null;
    decoder = null;
    header = null;
    row = null;
  }

  /** Query the reader as to whether there is another record.
   * @return True if more records exist, false otherwise.
   */
  public boolean hasNext() {
    return cnt < header.getNumRecords() + 1;
  }

  /** Get the next record (entry). Will return a new array of values.
   * @throws IOException If an error occurs.
   * @return A new array of values.
   */
  public Object[] readEntry() throws IOException {
    return readEntry(new Object[header.getNumFields()]);
  }

  public Row readRow() throws IOException {
    read();
    return row;
  }

  /** Skip the next record.
   * @throws IOException If an error occurs.
   */
  public void skip() throws IOException {
    boolean foundRecord = false;
    while (!foundRecord) {

      bufferCheck();

      // read the deleted flag
      char tempDeleted = (char) buffer.get();

      // skip the next bytes
      buffer.position(buffer.position() + header.getRecordLength() - 1); //the 1 is for the deleted flag just read.

      // add the row if it is not deleted.
      if (tempDeleted != '*') {
        foundRecord = true;
      }
    }
  }

  /** Copy the next record into the array starting at offset.
   * @param entry Th array  to copy into.
   * @param offset The offset to start at
   * @throws IOException If an error occurs.
   * @return The same array passed in.
   */
  public Object[] readEntry(Object[] entry,final int offset) throws IOException {
    if (entry.length - offset < header.getNumFields()) {
      throw new ArrayIndexOutOfBoundsException();
    }

    read();

    // retrieve the record length
    final int numFields = header.getNumFields();

    int fieldOffset = 0;
    for (int j=0; j < numFields; j++){
      entry[j + offset] = readObject(fieldOffset,j);
      fieldOffset += fieldLengths[j];
    }

    return entry;
  }

  /**
   * Transfer, by bytes, the next record to the writer.
   */
  public void transferTo(DbaseFileWriterNIO writer) throws IOException {
      bufferCheck();
      buffer.limit(buffer.position() + header.getRecordLength());
      writer.channel.write(buffer);
      buffer.limit(buffer.capacity());

      cnt++;
  }

  private void read() throws IOException {
    boolean foundRecord = false;
    while (!foundRecord) {

      bufferCheck();

      // read the deleted flag
      char deleted = (char) buffer.get();
      if (deleted == '*') {
          continue;
      }

      charBuffer.position(0);
      buffer.limit(buffer.position() + header.getRecordLength() - 1);
      decoder.decode(buffer,charBuffer,true);
      buffer.limit(buffer.capacity());
      charBuffer.flip();

      foundRecord = true;
    }

    cnt++;
  }

  /** Copy the next entry into the array.
   * @param entry The array to copy into.
   * @throws IOException If an error occurs.
   * @return The same array passed in.
   */
  public Object[] readEntry(Object[] entry) throws IOException {
    return readEntry(entry,0);
  }

  private Object readObject(final int fieldOffset,final int fieldNum) throws IOException {
    final char type = fieldTypes[fieldNum];
    final int fieldLen = fieldLengths[fieldNum];
    Object object = null;

    //System.out.println( charBuffer.subSequence(fieldOffset,fieldOffset + fieldLen));

    if(fieldLen > 0) {

      switch (type){
        // (L)logical (T,t,F,f,Y,y,N,n)
        case 'l':
        case 'L':
          switch (charBuffer.charAt(fieldOffset)) {

            case 't': case 'T': case 'Y': case 'y':
              object = Boolean.TRUE;
              break;
            case 'f': case 'F': case 'N': case 'n':
              object = Boolean.FALSE;
              break;
            default:

              throw new IOException("Unknown logical value : '" + charBuffer.charAt(fieldOffset) + "'");
          }
          break;
          // (C)character (String)
        case 'c':
        case 'C':
          // oh, this seems like a lot of work to parse strings...but,
          // For some reason if zero characters ( (int) char == 0 ) are allowed
          // in these strings, they do not compare correctly later on down the
          // line....
          int start = fieldOffset;
          int end = fieldOffset + fieldLen - 1;
          // trim off whitespace and 'zero' chars
          while (start < end) {
            char c = charBuffer.get(start);
            if (c== 0 || Character.isWhitespace(c)) {
              start++;
            }
            else break;
          }
          while (end > start) {
            char c = charBuffer.get(end);
            if (c == 0 || Character.isWhitespace(c)) {
              end--;
            }
            else break;
          }
          // set up the new indexes for start and end
          charBuffer.position(start).limit(end + 1);
          String s = charBuffer.toString();
          // this resets the limit...
          charBuffer.clear();
          object = s;
          break;
          // (D)date (Date)
        case 'd':
        case 'D':
            try{
              String tempString = charBuffer.subSequence(fieldOffset,fieldOffset + 4).toString();
              int tempYear = Integer.parseInt(tempString);
              tempString = charBuffer.subSequence(fieldOffset + 4,fieldOffset + 6).toString();
              int tempMonth = Integer.parseInt(tempString) - 1;
              tempString = charBuffer.subSequence(fieldOffset + 6,fieldOffset + 8).toString();
              int tempDay = Integer.parseInt(tempString);
              Calendar cal = Calendar.getInstance();
              cal.clear();
              cal.set(cal.YEAR,tempYear);
              cal.set(cal.MONTH, tempMonth);
              cal.set(cal.DAY_OF_MONTH, tempDay);
              object = cal.getTime();
            }
            catch(NumberFormatException nfe){
                //todo: use progresslistener, this isn't a grave error.
            }
          break;

          // (F)floating (Double)
        case 'n':
        case 'N':
          try {
            if (header.getFieldDecimalCount(fieldNum) == 0) {
              object = new Integer(numberParser.parseInt(charBuffer, fieldOffset, fieldOffset + fieldLen - 1));
              break;
            }
            // else will fall through to the floating point number
          } catch (NumberFormatException e) {

            // todo: use progresslistener, this isn't a grave error.

            // don't do this!!! the Double parse will be attemted as we fall
            // through, so no need to create a new Object. -IanS
            //object = new Integer(0);

            // Lets try parsing a long instead...
            try {
                object = new Long(numberParser.parseLong(charBuffer,fieldOffset,fieldOffset + fieldLen - 1));
                break;
            } catch (NumberFormatException e2) {

            }
          }

        case 'f':
        case 'F': // floating point number
          try {


            object = new Double(numberParser.parseDouble(charBuffer,fieldOffset, fieldOffset + fieldLen - 1));
          } catch (NumberFormatException e) {
              // todo: use progresslistener, this isn't a grave error, though it
              // does indicate something is wrong

              // okay, now whatever we got was truly undigestable. Lets go with
              // a zero Double.
              object = new Double(0.0);
          }
          break;
        default:
          throw new IOException("Invalid field type : " + type);
      }

    }
    return object;
  }

  public static void main(String[] args) throws Exception {
  /*  FileChannel channel = new FileInputStream(args[0]).getChannel();
    DbaseFileReaderNIO reader = new DbaseFileReaderNIO(channel);
    System.out.println(reader.getHeader());
    int r = 0;
    while (reader.hasNext()) {
      System.out.println(++r + "," + java.util.Arrays.asList(reader.readEntry()));
    }
    */
  }

}
