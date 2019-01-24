package org.gvsig.mapsheets.print.series.test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;

import com.sun.pdfview.ImageInfo;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

public class PdfTest extends TestCase {

	public static String pdf_test_name = "sheet_E_6.pdf"; 
	
	public static File thepdf =
		new File(
				System.getProperty("user.home") + File.separator +
				"mapsheets_test" + File.separator + pdf_test_name);
	
	public void test() {
		
		AllTests.waitSome();
		AllTests.waitSome();
		AllTests.waitSome();
		
		FileInputStream fIn = null;
		FileChannel fChan = null;
		long fSize = 0;
		ByteBuffer mBuf = null;
		PDFFile pdf_file = null;
		PDFPage pdf_page = null;

	    try {
	    	System.out.println("Opening PDF file: " + thepdf.getAbsolutePath() + "...");
	        fIn = new FileInputStream(thepdf);
	        fChan = fIn.getChannel();
	        fSize = fChan.size();
	        
			AllTests.waitSome();
	    	System.out.println("Buffering file...");

	        mBuf = ByteBuffer.allocate((int) fSize);
	        fChan.read(mBuf);
	        mBuf.rewind();
	        
	        pdf_file = new PDFFile(mBuf);
	        pdf_page = pdf_file.getPage(0);
	        
	        int w = 842;
	        int h = 595;
	        ImageInfo ii = new ImageInfo(w,h,null);
	        BufferedImage bim = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

	    	System.out.println("Accessing page 0...");
	    	
	    	AllTests.waitSome();
	    	
	        PDFRenderer pre = new PDFRenderer(pdf_page, ii, bim);

	        pre.setup();
	        pre.run();
	        
	        while (! pre.isFinished()) {
	        	System.out.println("Drawing page 0 on image...");
	        	Thread.sleep(50);
	        }

	        fChan.close(); 
	        fIn.close();
	        
	        System.out.println("Testing green transp. point...");
	        int col = bim.getRGB(200,80); // verde transp 101 255 101
	        Color c = new Color(col);
	        assertEquals("Bad green transp. point", 102, c.getRed());
	        assertEquals("Bad green transp. point", 255, c.getGreen());
	        assertEquals("Bad green transp. point", 102, c.getBlue());
	        
	        System.out.println("Testing magenta point...");
	        col = bim.getRGB(213,64); // magenta 255 0 255
	        c = new Color(col);
	        assertEquals("Bad magenta point", 255, c.getRed());
	        assertEquals("Bad magenta point", 0, c.getGreen());
	        assertEquals("Bad magenta point", 255, c.getBlue());
	        
	        System.out.println("Done.");
	        
	      } catch (Exception exc) {
	    	  fail(exc.getMessage());
	      }

		
		
		
	}

	
}
