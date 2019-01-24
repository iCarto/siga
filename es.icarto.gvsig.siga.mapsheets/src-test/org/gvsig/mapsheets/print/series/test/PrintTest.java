package org.gvsig.mapsheets.print.series.test;

import java.io.File;

import junit.framework.TestCase;

import org.gvsig.mapsheets.print.series.gui.PrintTaskWindow;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;

import com.iver.andami.PluginServices;

public class PrintTest extends TestCase {
	
	public static MapSheetsLayoutTemplate template = null;
	
	public void test() {
		
		AllTests.waitSome();
		AllTests.waitSome();
		
		File dummyf = new File(
				System.getProperty("user.home") + File.separator
				+ "mapsheets_test");
		if (dummyf.exists()) {
			dummyf.delete();
		}
		dummyf.mkdirs();
		dummyf.deleteOnExit();
		
		
		PrintTaskWindow.startPrintTask(
				template,
				false,
				false,
				null,
				System.getProperty("user.home") + File.separator + "mapsheets_test",
				PluginServices.getText(this, "sheet"), null, true);
		
		// wait 
		AllTests.waitSome();
		
		PdfTest.thepdf = new File(
				System.getProperty("user.home") +
				File.separator +
				"mapsheets_test" + 
				File.separator +
				"sheet_E_6.pdf");
	}

}
