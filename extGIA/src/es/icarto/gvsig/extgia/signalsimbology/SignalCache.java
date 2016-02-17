package es.icarto.gvsig.extgia.signalsimbology;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.gvsig.symbology.fmap.styles.ImageStyle;

public class SignalCache {

    private final transient HashMap<String, ImageStyle> cache;

    public SignalCache() {
	cache = new HashMap<String, ImageStyle>();
    }

    public ImageStyle getImage(String file) throws IOException {
	ImageStyle imageStyle = cache.get(file);
	if (imageStyle == null) {
	    URL imageURL = new URL("file:senhales/" + file);
	    imageStyle = new ImageStyle();
	    imageStyle.setSource(imageURL);
	    cache.put(file, imageStyle);
	}
	return imageStyle;
    }
}
