package org.gvsig.mapsheets.print.series.layout;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameViewFactory;
import com.iver.cit.gvsig.project.documents.layout.fframes.FrameFactory;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;

/**
 * Every gvsig document needs to have an associated factory. This is the 
 * factory for {@link MapSheetFrameView}
 * 
 * @author jlopez
 *
 */
public class MapSheetFrameViewFactory extends FrameFactory {
    public static String registerName = "MapSheetFrameView";


    /**
     * Create a new IFFrame.
     *
     * @return IFFrame.
     */
    public IFFrame createFrame() {
        MapSheetFrameView view = new MapSheetFrameView();
        view.setFrameLayoutFactory(this);
        return view;
    }

    /**
     * Returns the name of registration in the point of extension.
     *
     * @return Name of registration
     */
    public String getRegisterName() {
        return registerName;
    }

    /**
     * Returns the name of IFFrame.
     *
     * @return Name of IFFrame.
     */
    public String getNameType() {
        return PluginServices.getText(this, "MapSheetFrameView");
    }

    /**
     * Registers in the points of extension the Factory with alias.
     *
     */
    public static void register() {
        register(registerName, new MapSheetFrameViewFactory(),
            "org.gvsig.mapsheets.print.series.layout.MapSheetFrameView");

    }
}
