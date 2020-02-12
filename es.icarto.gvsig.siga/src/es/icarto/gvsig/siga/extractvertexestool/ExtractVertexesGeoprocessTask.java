package es.icarto.gvsig.siga.extractvertexestool;

import com.iver.utiles.swing.threads.CancellableProgressTask;

public class ExtractVertexesGeoprocessTask extends CancellableProgressTask {

    private ExtractVertexesGeoprocess geoprocess;

    public ExtractVertexesGeoprocessTask(ExtractVertexesGeoprocess geoprocess) {
        this.geoprocess = geoprocess;
    }

    public void finished() {
        super.finished = true;
    }

    public void run() throws Exception {
        this.geoprocess.initialize(this);
        this.geoprocess.process(this);
    }
}
