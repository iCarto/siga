/*
 * Copyright (c) 2003 Objectix Pty Ltd  All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OBJECTIX PTY LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package au.com.objectix.jgridshift;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.CharEncoding;

/**
 * Models the NTv2 Sub Grid within a Grid Shift File
 *
 * @author Peter Yuill
 */
public class NTv2SubGrid implements Cloneable, Serializable {

  private static final long serialVersionUID = 1L;
  private static final int REC_SIZE = 16;

  private String subGridName;
  private String parentSubGridName;
  private String created;
  private String updated;
  private double minLat;
  private double maxLat;
  private double minLon;
  private double maxLon;
  private double latInterval;
  private double lonInterval;
  private int nodeCount;

  private int lonColumnCount;
  private int latRowCount;
  private float[] latShift;
  private float[] lonShift;
  private float[] latAccuracy;
  private float[] lonAccuracy;

  private RandomAccessFile raf;
  private long subGridOffset;
  boolean bigEndian;
  private NTv2SubGrid[] subGrid;

  /**
   * Construct a Sub Grid from an InputStream, loading the node data into
   * arrays in this object.
   *
   * @param in NTv2GridShiftFile InputStream
   * @param bigEndian is the file bigEndian?
   * @param loadAccuracy is the node Accuracy data to be loaded?
   * @throws IOException
   */
  public NTv2SubGrid(InputStream in, boolean bigEndian, boolean loadAccuracy) throws IOException {
    byte[] b8 = new byte[8];
    byte[] b4 = new byte[4];
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
        subGridName = new String(b8, CharEncoding.UTF_8).trim();
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
        parentSubGridName = new String(b8, CharEncoding.UTF_8).trim();
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
        created = new String(b8, CharEncoding.UTF_8);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
        updated = new String(b8, CharEncoding.UTF_8);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
    minLat = NTv2Util.getDouble(b8, bigEndian);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
    maxLat = NTv2Util.getDouble(b8, bigEndian);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
    minLon = NTv2Util.getDouble(b8, bigEndian);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
    maxLon = NTv2Util.getDouble(b8, bigEndian);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
    latInterval = NTv2Util.getDouble(b8, bigEndian);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
    lonInterval = NTv2Util.getDouble(b8, bigEndian);
    lonColumnCount = 1 + (int) ((maxLon - minLon) / lonInterval);
    latRowCount = 1 + (int) ((maxLat - minLat) / latInterval);
    NTv2Util.readBytes(in, b8);
    NTv2Util.readBytes(in, b8);
    nodeCount = NTv2Util.getInt(b8, bigEndian);
    if (nodeCount != lonColumnCount * latRowCount) {
      throw new IllegalStateException("NTv2SubGrid " + subGridName + " has inconsistent grid dimesions");
    }
    latShift = new float[nodeCount];
    lonShift = new float[nodeCount];
    if (loadAccuracy) {
      latAccuracy = new float[nodeCount];
      lonAccuracy = new float[nodeCount];
    }

    for (int i = 0; i < nodeCount; i++) {
      // Read the grid file byte after byte. This is a workaround about a bug in
      // certain VM which are not able to read byte blocks when the resource file is in a .jar file (Pieren)
      NTv2Util.readBytes(in, b4, 1);
      latShift[i] = NTv2Util.getFloat(b4, bigEndian);
      NTv2Util.readBytes(in, b4, 1);
      lonShift[i] = NTv2Util.getFloat(b4, bigEndian);
      NTv2Util.readBytes(in, b4, 1);
      if (loadAccuracy) {
        latAccuracy[i] = NTv2Util.getFloat(b4, bigEndian);
      }
      NTv2Util.readBytes(in, b4, 1);
      if (loadAccuracy) {
        lonAccuracy[i] = NTv2Util.getFloat(b4, bigEndian);
      }
    }
  }

  /**
   * Construct a Sub Grid from a RandomAccessFile. Only the headers
   * are loaded into this object, the node data is accessed directly
   * from the RandomAccessFile.
   *
   * @param in NTv2GridShiftFile RandomAccessFile
   * @param bigEndian is the file bigEndian?
   * @throws Exception
   */
  public NTv2SubGrid(RandomAccessFile raf, long subGridOffset, boolean bigEndian) throws IOException {
    this.raf = raf;
    this.subGridOffset = subGridOffset;
    this.bigEndian = bigEndian;
    raf.seek(subGridOffset);
    byte[] b8 = new byte[8];
    raf.read(b8);
    raf.read(b8);
    subGridName = new String(b8).trim();
    raf.read(b8);
    raf.read(b8);
    parentSubGridName = new String(b8).trim();
    raf.read(b8);
    raf.read(b8);
    created = new String(b8);
    raf.read(b8);
    raf.read(b8);
    updated = new String(b8);
    raf.read(b8);
    raf.read(b8);
    minLat = NTv2Util.getDouble(b8, bigEndian);
    raf.read(b8);
    raf.read(b8);
    maxLat = NTv2Util.getDouble(b8, bigEndian);
    raf.read(b8);
    raf.read(b8);
    minLon = NTv2Util.getDouble(b8, bigEndian);
    raf.read(b8);
    raf.read(b8);
    maxLon = NTv2Util.getDouble(b8, bigEndian);
    raf.read(b8);
    raf.read(b8);
    latInterval = NTv2Util.getDouble(b8, bigEndian);
    raf.read(b8);
    raf.read(b8);
    lonInterval = NTv2Util.getDouble(b8, bigEndian);
    lonColumnCount = 1 + (int)((maxLon - minLon) / lonInterval);
    latRowCount = 1 + (int)((maxLat - minLat) / latInterval);
    raf.read(b8);
    raf.read(b8);
    nodeCount = NTv2Util.getInt(b8, bigEndian);
    if (nodeCount != lonColumnCount * latRowCount) {
      throw new IllegalStateException("NTv2SubGrid " + subGridName + " has inconsistent grid dimesions");
    }
  }

  /**
   * Tests if a specified coordinate is within this Sub Grid
   * or one of its Sub Grids. If the coordinate is outside
   * this Sub Grid, null is returned. If the coordinate is
   * within this Sub Grid, but not within any of its Sub Grids,
   * this Sub Grid is returned. If the coordinate is within
   * one of this Sub Grid's Sub Grids, the method is called
   * recursively on the child Sub Grid.
   *
   * @param lon Longitude in Positive West Seconds
   * @param lat Latitude in Seconds
   * @return the Sub Grid containing the Coordinate or null
   */
  public NTv2SubGrid getSubGridForCoord(double lon, double lat) {
    if (isCoordWithin(lon, lat)) {
      if (subGrid == null) {
        return this;
      } else {
        for (NTv2SubGrid s : subGrid) {
          if (s.isCoordWithin(lon, lat)) {
            return s.getSubGridForCoord(lon, lat);
          }
        }
        return this;
      }
    } else {
      return null;
    }
  }

  /**
   * Tests if a specified coordinate is within this Sub Grid.
   * A coordinate on either outer edge (maximum Latitude or
   * maximum Longitude) is deemed to be outside the grid.
   *
   * @param lon Longitude in Positive West Seconds
   * @param lat Latitude in Seconds
   * @return true or false
   */
  private boolean isCoordWithin(double lon, double lat) {
    return (lon >= minLon) && (lon < maxLon) && (lat >= minLat) && (lat < maxLat);
  }

  /**
   * Bi-Linear interpolation of four nearest node values as described in
   * 'GDAit Software Architecture Manual' produced by the
   * <a href='http://www.dtpli.vic.gov.au/property-and-land-titles/geodesy/geocentric-datum-of-australia-1994-gda94/gda94-useful-tools'>
   * Geomatics Department of the University of Melbourne</a>
   * @param a value at the A node
   * @param b value at the B node
   * @param c value at the C node
   * @param d value at the D node
   * @param x Longitude factor
   * @param y Latitude factor
   * @return interpolated value
   */
  private double interpolate(float a, float b, float c, float d, double x, double y) {
    return a +
      (((double) b - (double) a) * x) +
      (((double) c - (double) a) * y) +
      (((double) a + (double) d - b - c) * x * y);
  }

  /**
   * Interpolate shift and accuracy values for a coordinate in the 'from' datum
   * of the NTv2GridShiftFile. The algorithm is described in 'GDAit Software Architecture Manual' produced by the
   * <a href='http://www.dtpli.vic.gov.au/property-and-land-titles/geodesy/geocentric-datum-of-australia-1994-gda94/gda94-useful-tools'>
   * Geomatics Department of the University of Melbourne</a>
   * <p>This method is thread safe for both memory based and file based node data.</p>
   * @param gs NTv2GridShift object containing the coordinate to shift and the shift values
   * @throws IOException
   */
  public void interpolateGridShift(NTv2GridShift gs) throws IOException {
    int lonIndex = (int) ((gs.getLonPositiveWestSeconds() - minLon) / lonInterval);
    int latIndex = (int) ((gs.getLatSeconds() - minLat) / latInterval);

    double x = (gs.getLonPositiveWestSeconds() - (minLon + (lonInterval * lonIndex))) / lonInterval;
    double y = (gs.getLatSeconds() - (minLat + (latInterval * latIndex))) / latInterval;

    // Find the nodes at the four corners of the cell

    int indexA = lonIndex + (latIndex * lonColumnCount);
    int indexB = indexA + 1;
    int indexC = indexA + lonColumnCount;
    int indexD = indexC + 1;

    if (raf == null) {
      gs.setLonShiftPositiveWestSeconds(interpolate(lonShift[indexA], lonShift[indexB], lonShift[indexC], lonShift[indexD], x, y));

      gs.setLatShiftSeconds(interpolate(latShift[indexA], latShift[indexB], latShift[indexC], latShift[indexD], x, y));

      if (lonAccuracy == null) {
        gs.setLonAccuracyAvailable(false);
      } else {
        gs.setLonAccuracyAvailable(true);
        gs.setLonAccuracySeconds(interpolate(lonAccuracy[indexA], lonAccuracy[indexB], lonAccuracy[indexC], lonAccuracy[indexD], x, y));
      }

      if (latAccuracy == null) {
        gs.setLatAccuracyAvailable(false);
      } else {
        gs.setLatAccuracyAvailable(true);
        gs.setLatAccuracySeconds(interpolate(latAccuracy[indexA], latAccuracy[indexB], latAccuracy[indexC], latAccuracy[indexD], x, y));
      }
    } else {
      synchronized(raf) {
        byte[] b4 = new byte[4];
        long nodeOffset = subGridOffset + (11 * REC_SIZE) + (indexA * REC_SIZE);
        raf.seek(nodeOffset);
        raf.read(b4);
        float latShiftA = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonShiftA = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float latAccuracyA = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonAccuracyA = NTv2Util.getFloat(b4, bigEndian);

        nodeOffset = subGridOffset + (11 * REC_SIZE) + (indexB * REC_SIZE);
        raf.seek(nodeOffset);
        raf.read(b4);
        float latShiftB = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonShiftB = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float latAccuracyB = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonAccuracyB = NTv2Util.getFloat(b4, bigEndian);

        nodeOffset = subGridOffset + (11 * REC_SIZE) + (indexC * REC_SIZE);
        raf.seek(nodeOffset);
        raf.read(b4);
        float latShiftC = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonShiftC = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float latAccuracyC = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonAccuracyC = NTv2Util.getFloat(b4, bigEndian);

        nodeOffset = subGridOffset + (11 * REC_SIZE) + (indexD * REC_SIZE);
        raf.seek(nodeOffset);
        raf.read(b4);
        float latShiftD = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonShiftD = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float latAccuracyD = NTv2Util.getFloat(b4, bigEndian);
        raf.read(b4);
        float lonAccuracyD = NTv2Util.getFloat(b4, bigEndian);

        gs.setLonShiftPositiveWestSeconds(interpolate(lonShiftA, lonShiftB, lonShiftC, lonShiftD, x, y));

        gs.setLatShiftSeconds(interpolate(latShiftA, latShiftB, latShiftC, latShiftD, x, y));

        gs.setLonAccuracyAvailable(true);
        gs.setLonAccuracySeconds(interpolate(lonAccuracyA, lonAccuracyB, lonAccuracyC, lonAccuracyD, x, y));

        gs.setLatAccuracyAvailable(true);
        gs.setLatAccuracySeconds(interpolate(latAccuracyA, latAccuracyB, latAccuracyC, latAccuracyD, x, y));
      }
    }
  }

  public String getParentSubGridName() {
    return parentSubGridName;
  }

  public String getSubGridName() {
    return subGridName;
  }

  public int getNodeCount() {
    return nodeCount;
  }

  public int getSubGridCount() {
    return (subGrid == null) ? 0 : subGrid.length;
  }

  public NTv2SubGrid getSubGrid(int index) {
    return (subGrid == null) ? null : subGrid[index];
  }

  /**
   * Set an array of Sub Grids of this sub grid
   * @param subGrid
   */
  public void setSubGridArray(NTv2SubGrid[] subGrid) {
    this.subGrid = subGrid == null ? null : Arrays.copyOf(subGrid, subGrid.length);
  }

  @Override
  public String toString() {
    return subGridName;
  }

  /**
   * Returns textual details about the sub grid.
   * @return textual details about the sub grid
   */
  public String getDetails() {
    return new StringBuilder("Sub Grid : ")
      .append(subGridName)
      .append("\nParent   : ")
      .append(parentSubGridName)
      .append("\nCreated  : ")
      .append(created)
      .append("\nUpdated  : ")
      .append(updated)
      .append("\nMin Lat  : ")
      .append(minLat)
      .append("\nMax Lat  : ")
      .append(maxLat)
      .append("\nMin Lon  : ")
      .append(minLon)
      .append("\nMax Lon  : ")
      .append(maxLon)
      .append("\nLat Intvl: ")
      .append(latInterval)
      .append("\nLon Intvl: ")
      .append(lonInterval)
      .append("\nNode Cnt : ")
      .append(nodeCount)
      .toString();
  }

  /**
   * Make a deep clone of this Sub Grid
   * @throws CloneNotSupportedException
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    NTv2SubGrid clone = (NTv2SubGrid) super.clone();
    // Do a deep clone of the sub grids
    if (subGrid != null) {
      clone.subGrid = new NTv2SubGrid[subGrid.length];
      for (int i = 0; i < subGrid.length; i++) {
        clone.subGrid[i] = (NTv2SubGrid) subGrid[i].clone();
      }
    }
    return clone;
  }

  /**
   * Get maximum latitude value
   * @return maximum latitude
   */
  public double getMaxLat() {
    return maxLat;
  }

  /**
   * Get maximum longitude value
   * @return maximum longitude
   */
  public double getMaxLon() {
    return maxLon;
  }

  /**
   * Get minimum latitude value
   * @return minimum latitude
   */
  public double getMinLat() {
    return minLat;
  }

  /**
   * Get minimum longitude value
   * @return minimum longitude
   */
  public double getMinLon() {
    return minLon;
  }
}
