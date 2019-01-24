package org.gvsig.mapsheets.print.series.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.gvsig.mapsheets.print.series.gui.utils.IProgressListener;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;
import org.gvsig.mapsheets.print.series.print.PdfPrintTask;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLayer;

/**
 * Print progress dialog used when printing to PDF files.
 * 
 * @author jldominguez
 *
 */
public class PrintTaskWindow extends JPanel implements IWindow, IProgressListener, ActionListener {

	private WindowInfo winfo = null;
	private JButton cancelButton = null;
	private JLabel titleLabel = null;
	private JLabel progLabel = null;
	private JProgressBar proBar = null;
	
	private int upmargin = 20;
	private int downmargin = 15;
	private int leftmargin = 30;
	private int rightmargin = 30;
	private int sep = 15;
	private int controlw = 200;
	private int buttonw = 100;
	private int controlh = 30;
	
	private int WIDTH = leftmargin + controlw + rightmargin;
	private int HEIGHT = upmargin + downmargin + 3 * controlh + 2 * sep;
	
	private static PdfPrintTask task = null;
	private static String outFolder = "";
	
	private PrintTaskWindow() {
		
		this.setSize(WIDTH, HEIGHT);
		this.setLayout(null);
		
		titleLabel = new JLabel(PluginServices.getText(this, "Print_in_progress"));
		titleLabel.setBounds(leftmargin, upmargin, controlw, controlh);
		
		proBar = new JProgressBar();
		proBar.setMinimum(0);
		
		proBar.setBounds(leftmargin, upmargin+controlh+sep, controlw, controlh);
		
		cancelButton = new JButton(PluginServices.getText(this,"Cancel"));
		cancelButton.setBounds(
				(leftmargin+rightmargin+controlw-buttonw)/2,
				upmargin+2*controlh+2*sep, buttonw, controlh);
		cancelButton.addActionListener(this);
		
		add(titleLabel);
		add(proBar);
		add(cancelButton);
	}
	
	public WindowInfo getWindowInfo() {
		
		if (winfo == null) {
			winfo = new WindowInfo(0 + // palette
	                16 + // no modal
	                0 + // modal 8
	                0 + // iconifiable 4
	                0 + // maximizable 2
	                0 // resizable 1
	                );
			winfo.setTitle(PluginServices.getText(this, "Job_progress"));
			winfo.setWidth(WIDTH);
			winfo.setHeight(HEIGHT);
		}
		return winfo;
		
		
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}
	
	public static void startPrintTask(
			MapSheetsLayoutTemplate lay_template,
			boolean all_sheets,
			boolean highlight,
			FLayer back_lyr,
			String out_folder,
			String bname,
			IWindow wtoclose,
			boolean is_test) {
		
		outFolder = out_folder;
		
		PrintTaskWindow ptw = null;
		
		if (!is_test) {
			ptw = new PrintTaskWindow();
			PluginServices.getMDIManager().addCentredWindow(ptw);
		}

		task = new PdfPrintTask(
				lay_template,
				all_sheets,
				highlight,
				back_lyr,
				out_folder,
				bname, ptw);
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		if (!is_test) {
			PluginServices.getMDIManager().closeWindow(lay_template);
		}
		
		
		if (wtoclose != null && (!is_test)) {
			PluginServices.getMDIManager().closeWindow(wtoclose);
		}
	}

	public void cancelled(String msg) {
		
		PluginServices.getMDIManager().closeWindow(this);
		
		JOptionPane.showMessageDialog(
				this,
				PluginServices.getText(this, "Job_cancelled") + ":\n" +
				msg,
				PluginServices.getText(this, "Job_cancelled"),
				JOptionPane.WARNING_MESSAGE);
	}

	public void finished() {
		
		AuxRunnable ar = new AuxRunnable(this);
		SwingUtilities.invokeLater(ar);

		JOptionPane.showMessageDialog(
				this,
				PluginServices.getText(this, "Maps_generated_in_folder") + ":\n" +
				outFolder,
				PluginServices.getText(this, "Job_done"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void progress(int done, int tot) {
		proBar.setMaximum(tot);
		proBar.setValue(done);
	}

	public void started() {
		// TODO Auto-generated method stub
		
	}

	public void actionPerformed(ActionEvent e) {
		
		Object src = e.getSource();
		if (src == cancelButton) {
			if (task != null) {
				task.setCanceled(true);
			}
		}
		
	}
	
	
	private class AuxRunnable implements  Runnable {

		IWindow wind = null;
		
		public AuxRunnable(IWindow w) {
			wind = w;
		}
		public void run() {
			PluginServices.getMDIManager().closeWindow(wind);
		}
		
	}

}
