package es.icarto.gvsig.extgex.navtable.decorators.printreports;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JOptionPane;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class PrintReportsAction {

    public void print(String outputFile, String reportFilePathName, PrintReportsData data) {
        try {

            HashMap<String, Object> parameters = new HashMap<String, Object>();

            Calendar calendar = Calendar.getInstance();
            String today = String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1, // january is month 0
                    calendar.get(Calendar.YEAR));
            parameters.put("FECHA_CONSULTA_EXPEDIENTE", today);

            parameters.put("ID_FINCA", data.getIDFinca());
            parameters.put("LOGO_URL", data.getLogoUrl());
            parameters.put("REPORT_NAME", data.getReportName());
            parameters.put("MAP_URL", data.getValue("image_from_view"));
            parameters.put("COORDENADA_X", data.getValue("coordenada_utm_x"));
            parameters.put("COORDENADA_Y", data.getValue("coordenada_utm_y"));

            JasperPrint print = JasperFillManager.fillReport(reportFilePathName, parameters, DBSession
                    .getCurrentSession().getJavaConnection());

            // Create a PDF exporter
            JRExporter exporter = new JRPdfExporter();

            // Configure the exporter (set output file name and print object)
            exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFile);
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);

            // Export the PDF file
            exporter.exportReport();

            Object[] reportGeneratedOptions = { "Ver informe", "Cerrar" };
            int m = JOptionPane.showOptionDialog(null, "Informe generado con éxito en: \n" + "\"" + outputFile + "\"",
                    null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    reportGeneratedOptions, reportGeneratedOptions[1]);

            if (m == JOptionPane.OK_OPTION) {
                Desktop d = Desktop.getDesktop();
                try {
                    d.open(new File(outputFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("File PDF created in " + outputFile + " path.");

        } catch (JRException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
