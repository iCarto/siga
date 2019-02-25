package org.gvsig.mapsheets.print.series.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.cresques.cts.IProjection;
import org.gvsig.mapsheets.print.series.gui.utils.IProgressListener;
import org.gvsig.mapsheets.print.series.utils.GenerateGridTask;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

/**
 * Print progress dialog used when printing to PDF files.
 *
 * @author jldominguez
 *
 */
public class GridTaskWindow extends JPanel implements IWindow, IProgressListener, ActionListener {

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

    private static GenerateGridTask task = null;

	private GridTaskWindow() {

		this.setSize(WIDTH, HEIGHT);
		this.setLayout(null);

        titleLabel = new JLabel(PluginServices.getText(this, "Generating_grid"));
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
            winfo = new WindowInfo(WindowInfo.MODALDIALOG);
			winfo.setTitle(PluginServices.getText(this, "Job_progress"));
			winfo.setWidth(WIDTH);
			winfo.setHeight(HEIGHT);
		}
		return winfo;


	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}

    public static void startGridTask(boolean cover_view_selected,
            boolean selected_only, Rectangle2D useful_map_cm, ViewPort vp,
            long scale, int overlap_pc, IProjection iproj, FLyrVect ler,
            double w, double h, View v, IWindow wtoclose) {

        GridTaskWindow gtw = new GridTaskWindow();

        task = new GenerateGridTask(cover_view_selected, selected_only,
                useful_map_cm, vp, scale, overlap_pc, iproj, ler, w, h, v,
                wtoclose, gtw);
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();

        PluginServices.getMDIManager().addCentredWindow(gtw);

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
