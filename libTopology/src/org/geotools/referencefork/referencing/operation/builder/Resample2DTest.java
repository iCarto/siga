/**
 *
 */
package org.geotools.referencefork.referencing.operation.builder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Random;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
//import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.grid.GridCoverageFactory;
////import org.geotools.coverage.processing.AbstractProcessor;
//import org.geotools.referencefork.geometry.Envelope2D;
//import org.geotools.referencefork.geometry.GeneralEnvelope;
//import org.geotools.referencefork.referencing.CRS;
//import org.geotools.referencefork.referencing.crs.DefaultDerivedCRS;
//import org.geotools.referencefork.referencing.crs.DefaultEngineeringCRS;
//import org.geotools.referencefork.referencing.cs.DefaultCartesianCS;
//import org.geotools.referencefork.referencing.operation.DefaultOperationMethod;
import org.geotools.referencefork.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencefork.referencing.operation.transform.ProjectiveTransform;


/**
 * @author jezekjan
 *
 */
public class Resample2DTest extends TestCase {
    /**The source grid coverage, to be initialized by {@link #setUp}.
     * Contains 8-bits indexed color model for a PNG image, with categories.
     */
//    private GridCoverage2D coverage;

    public Resample2DTest(String arg0) {
        super(arg0);
    }

//    private GridCoverage2D coverageGenerator(int width, int height) {
//        final Random random = new Random(8578348921369L);
//        float[][] cov = new float[height][width];
//
//        for (int i = 0; i < height; i++) {
//            for (int j = 0; j < width; j++) {
//                cov[i][j] = random.nextFloat();
//            }
//        }
//
//        DefaultEngineeringCRS b;
//        Envelope2D env = new Envelope2D(DefaultEngineeringCRS.CARTESIAN_2D, 1000, 5000, 300, 300);
//
//        return (new GridCoverageFactory()).create("Example", cov, env);
//     
//    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        //coverage = coverageGenerator(100, 100);
    }

    public void testEnvelope() throws TransformException, FactoryException {
       /* GeneralMatrix M = new GeneralMatrix(3, 3);
        double[] m0 = { 0.991, 0.01, 0.001 };
        double[] m1 = { -0.01, 0.991, 0.001 };
        double[] m2 = { 0, 0, 1 };
        M.setRow(0, m0);
        M.setRow(1, m1);
        M.setRow(2, m2);

        MathTransform transform = (MathTransform) ProjectiveTransform.create(M);

        CoordinateReferenceSystem targetCRS = new DefaultDerivedCRS("targetCRS",
                new DefaultOperationMethod(transform), coverage.getCoordinateReferenceSystem(),
                transform, DefaultCartesianCS.GENERIC_2D);

        AbstractProcessor processor = AbstractProcessor.getInstance();
        ParameterValueGroup param = processor.getOperation("Resample").getParameters();
        param.parameter("Source").setValue(coverage);
        param.parameter("CoordinateReferenceSystem").setValue(targetCRS);

        GridCoverage2D target = (GridCoverage2D) processor.doOperation(param);
      
        GeneralEnvelope targetEnv = CRS.transform(CRS.findMathTransform(
                    coverage.getCoordinateReferenceSystem(), targetCRS), coverage.getEnvelope());

      //  System.out.println("To run the test uncomment last line in Resample2D.testEnvelope method");

        /**
         * Test whether CRS.transform return smae envelope as processor.doOperation
         */

       //  assertEquals(targetEnv, (GeneralEnvelope)target.getEnvelope());*/
    }

    public static Test suite() {
        return new TestSuite(Resample2DTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
