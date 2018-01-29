package es.icarto.gvsig.siga.printselected;

import com.iver.cit.gvsig.fmap.layers.FLayer;

public class Visibility {
    public final FLayer layer;
    public final boolean visible;

    public Visibility(FLayer layer, boolean visible) {
        this.layer = layer;
        this.visible = visible;
    }
}