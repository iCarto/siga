package es.icarto.gvsig.andami.ui.skin;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;

import com.iver.andami.ui.theme.Theme;

@SuppressWarnings("serial")
class MyDesktopPane extends JDesktopPane {

    private ImageIcon image;
    private String typeDesktop;

    public MyDesktopPane() {
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;
        if (image != null) {
            if (typeDesktop.equals(Theme.CENTERED)) {
                w = image.getIconWidth();
                h = image.getIconHeight();
                x = (getWidth() - w) / 2;
                y = (getHeight() - h) / 2;
                g.drawImage(image.getImage(), x, y, w, h, this);
            } else if (typeDesktop.equals(Theme.EXPAND)) {
                w = getWidth();
                h = getHeight();
                g.drawImage(image.getImage(), x, y, w, h, this);
            } else if (typeDesktop.equals(Theme.MOSAIC)) {
                int wAux = image.getIconWidth();
                int hAux = image.getIconHeight();
                int i = 0;
                int j = 0;
                w = wAux;
                h = hAux;
                while (x < getWidth()) {
                    x = wAux * i;
                    while (y < getHeight()) {
                        y = hAux * j;
                        j++;
                        g.drawImage(image.getImage(), x, y, w, h, this);
                    }
                    y = 0;
                    j = 0;
                    i++;
                }
            }
        }
    }

    public void setBackgroundImage(ImageIcon image, String typeDesktop) {
        this.image = image;
        this.typeDesktop = typeDesktop;
    }
}