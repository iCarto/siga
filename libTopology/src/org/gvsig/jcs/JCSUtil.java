/*
 * Created on 10-abr-2006
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
/* CVS MESSAGES:
 *
 * $Id: 
 * $Log: 
 */
package org.gvsig.jcs;

import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jump.adapter.FeatureAdapter;
import org.gvsig.jump.adapter.FeatureCollectionAdapter;
import org.gvsig.jump.adapter.FeatureCollectionDataSourceAdapter;
import org.gvsig.jump.adapter.FeatureSchemaLayerDefinitionAdapter;
import org.gvsig.jump.adapter.IFeatureAdapter;
import org.gvsig.jump.adapter.TaskMonitorAdapter;
import org.gvsig.topology.Messages;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLayerGenericVectorial;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jcs.conflate.boundarymatch.BoundaryMatcher;
import com.vividsolutions.jcs.conflate.boundarymatch.BoundaryMatcherParameters;
import com.vividsolutions.jcs.conflate.boundarymatch.SegmentMatcher;
import com.vividsolutions.jcs.conflate.coverage.CoverageAligner;
import com.vividsolutions.jcs.conflate.coverage.CoverageGapRemover;
import com.vividsolutions.jcs.conflate.polygonmatch.AngleHistogramMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.CentroidAligner;
import com.vividsolutions.jcs.conflate.polygonmatch.CentroidDistanceMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.CompactnessMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.FeatureMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.HausdorffDistanceMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.IndependentCandidateMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.Matches;
import com.vividsolutions.jcs.conflate.polygonmatch.SymDiffMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.WeightedMatcher;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.match.EdgeMatchOptions;
import com.vividsolutions.jcs.conflate.roads.match.OverlapMatchEvaluator;
import com.vividsolutions.jcs.conflate.roads.match.RoadMatchOptions;
import com.vividsolutions.jcs.conflate.roads.match.RoadMatcherProcess;
import com.vividsolutions.jcs.conflate.roads.model.ReferenceDatasetPrecedenceRuleEngine;
import com.vividsolutions.jcs.conflate.roads.model.Statistics;
import com.vividsolutions.jcs.qa.InternalMatchedSegmentFinder;
import com.vividsolutions.jcs.qa.OverlapFinder;
import com.vividsolutions.jcs.qa.diff.BufferGeometryMatcher;
import com.vividsolutions.jcs.qa.diff.DiffGeometryComponents;
import com.vividsolutions.jcs.qa.diff.DiffSegments;
import com.vividsolutions.jcs.qa.diff.DiffSegmentsWithTolerance;
import com.vividsolutions.jcs.qa.diff.ExactGeometryMatcher;
import com.vividsolutions.jcs.qa.offsetcorner.OffsetBoundaryCornerFinder;
import com.vividsolutions.jcs.simplify.FeatureShortSegmentRemover;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JCSUtil {
	
	
	public static IFeature match(FLyrVect originalLyr, IFeature originalFeature, FLyrVect candidateLyr){
		
		FeatureMatcher matcher = createFeatureMatcher(1, 1, 1, 1, 1, 1);
		return match(originalLyr, originalFeature, candidateLyr, matcher);
	}
	
	
	
	public static IFeature match(FLyrVect originalLyr, IFeature originalFeature, 
			FLyrVect candidateLyr, FeatureMatcher featureMatcher){
		
		 int idx = Integer.parseInt(originalFeature.getID());
		
		 FeatureAdapter feature = new FeatureAdapter(originalLyr.getSource(), idx);
		 FeatureCollectionAdapter fc = new FeatureCollectionAdapter(candidateLyr.getSource());
         Matches matches = featureMatcher.match(feature, fc);
         
         Feature topMatch = matches.getTopMatch();
         IFeature bestMatch = new IFeatureAdapter(topMatch);
		
         return bestMatch;
	}
	
	
	public static List<IFeature> findDuplicates(FLyrVect lyr,
												IFeature originalFeature){
		FeatureMatcher matcher = JCSUtil.createFeatureMatcher(1, 1, 1, 1, 1, 1);
		return findDuplicates(lyr, originalFeature, matcher);
	}
	
	static CentroidDistanceMatcher CENTROID_DISTANCE_MATCHER = 
		new CentroidDistanceMatcher();
	static CentroidAligner CENTROID_ALIGNER = 
		new CentroidAligner(new HausdorffDistanceMatcher());
	static SymDiffMatcher SYM_DIFF_MATCHER = new SymDiffMatcher();
	static CentroidAligner CENTROID_SYM_DIFF = new CentroidAligner(new SymDiffMatcher());
	static CompactnessMatcher COMPACTNESS = new CompactnessMatcher();
	static AngleHistogramMatcher ANGLE_HISTROGRAM = new AngleHistogramMatcher();
	
	static List<IndependentCandidateMatcher> matchers = new ArrayList<IndependentCandidateMatcher>();
	static{
		matchers.add(CENTROID_DISTANCE_MATCHER);
		matchers.add(CENTROID_ALIGNER);
		matchers.add(SYM_DIFF_MATCHER);
		matchers.add(CENTROID_SYM_DIFF);
		matchers.add(COMPACTNESS);
		matchers.add(ANGLE_HISTROGRAM);
	}
	
	public static boolean checkMatchingPolygons(Polygon a, Polygon b){
		double WEIGHT_UMBRAL = 0.7d;
		double weightSum = 0d;
		int numMatchers = 0;
		for(int i = 0; i < matchers.size(); i++){
			IndependentCandidateMatcher matcher = matchers.get(i);
			double weight = matcher.match(a, b);
			weightSum += weight;
			numMatchers ++;
		}
		double average = weightSum / (double)numMatchers;
		return average >= WEIGHT_UMBRAL;
		
	}
	
	public static boolean checkMatchingGeometryCollections(GeometryCollection a, 
														  GeometryCollection b,
														  double clusterTolerance){
		int aSize = a.getNumGeometries();
		int bSize = b.getNumGeometries();
		
		if(aSize != bSize)
			return false;
		for(int i = 0; i < aSize; i++){
			Geometry childA = a.getGeometryN(i);
			Geometry childB = b.getGeometryN(i);
			if(! checkMatchingCriteria(childA, childB, clusterTolerance))
				return false;
		}
		return true;
	}
	
	public static boolean checkMatchingLines(LineString a, LineString b, double clusterTolerance){
		//we apply an epsilon-bande algorithm
		
		//geometries only match is first point is within cluster tolerance distance
		Coordinate firstFirst = a.getCoordinates()[0];
		Coordinate firstSecond = b.getCoordinates()[0];
		if(! (firstFirst.distance(firstSecond) < clusterTolerance))
			return false;
		
		Geometry firstEpsilonBand = a.buffer(clusterTolerance);
		Geometry secondEpsilonBand = b.buffer(clusterTolerance);
		
		if(firstEpsilonBand.contains(b) || secondEpsilonBand.contains(a))
			return true;
		
		OverlapMatchEvaluator matchEvaluator = new OverlapMatchEvaluator();
		matchEvaluator.setDistanceTolerance(clusterTolerance);
		
		double matchMeasure = matchEvaluator.match(a, b);
		double MATCHING_UMBRAL = 0.85d;
		if(matchMeasure > MATCHING_UMBRAL)
			return true;
		
		return false;
	}
	
	
	public static boolean checkMatchingPoints(Point a, Point b, double clusterTolerance){
		return a.distance(b) <= clusterTolerance;
	}
	
	
	public static boolean checkMatchingCriteria(Geometry first, Geometry second, double clusterTolerance){
		if(first.equals((Geometry)second))
			return true;
		if(first.equalsExact(second, clusterTolerance))
			return true;
		
		//Geometry must be assignable 
		if(! first.getClass().isAssignableFrom(second.getClass()))
			return false;
		
		if(first instanceof Polygon && second instanceof Polygon)
			return checkMatchingPolygons((Polygon)first, (Polygon)second);
		else if(first instanceof LineString && second instanceof LineString)
			return checkMatchingLines((LineString)first, (LineString)second, clusterTolerance);
		else if(first instanceof Point && second instanceof Point)
			return checkMatchingPoints((Point)first,(Point) second, clusterTolerance);
		else if(first instanceof GeometryCollection && second instanceof GeometryCollection)
			return checkMatchingGeometryCollections((GeometryCollection) first, (GeometryCollection) second, clusterTolerance);
		else
			return false;
	}

	
	
	public static List<IFeature> findDuplicates(FLyrVect lyr, 
											IFeature originalFeature, 
											FeatureMatcher matcher){
		List<IFeature> solution = new ArrayList<IFeature>(); 
		int idx = Integer.parseInt(originalFeature.getID());
		FeatureAdapter feature = new FeatureAdapter(lyr.getSource(), idx);
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		Matches matches = matcher.match(feature, fc);
		for (int i = 0; i < matches.size(); i++) {
			Feature f = matches.getFeature(i);
			double score = matches.getScore(i);
			if(f.getID() == feature.getID())
				continue;
			if(score < 0.53d)//0.53 is the frontier of matching
				continue;
			IFeatureAdapter ifeature = new IFeatureAdapter(f);
			solution.add(ifeature);
		}
		return solution;
	}
	
	
	public static void getCoincidentSegments(FLyrVect lyr, FLyrVect lyr2){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		ConflationSession conflationSession = new ConflationSession(fc, fc2);
		FeatureCollection[] coincidenSegments = conflationSession.getCoincidentSegments();
	}
	
	
	public static void linearMatch(FLyrVect lyr, FLyrVect lyr2){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		ConflationSession cs = new ConflationSession(fc, fc2);
	    cs.getSourceNetwork(0).setName("A");
	    cs.getSourceNetwork(1).setName("B");

	    // set automatching options
	    RoadMatchOptions matchOptions = cs.getMatchOptions();
	    EdgeMatchOptions emOpt = matchOptions.getEdgeMatchOptions();
	    emOpt.setDistanceTolerance(20.0);
	    matchOptions.setStandaloneEnabled(true);
	    cs.setPrecedenceRuleEngine(new ReferenceDatasetPrecedenceRuleEngine().setReferenceDatasetName("A"));

	    TaskMonitorAdapter monitor = new TaskMonitorAdapter();
	    RoadMatcherProcess rm = cs.autoMatch(monitor);
	    cs.updateResultStates(monitor);
	    Statistics stat = cs.getStatistics();
	    FeatureCollection edgeIndFC = rm.getRoadSegmentMatchInd();
	    FeatureCollection nodeMatchIndFC = rm.getNodeMatchVectors();
	}
	
	
	public static final int NORMALIZE_DIFF_OP_MODE = 0;
	public static final int BUFFER_DIFF_OP_MODE = 1;
	public static final int EXACT_DIFF_OP_MODE = 2;
	
	
	public static void diffOperation(FLyrVect lyr, 
									 FLyrVect lyr2, 
									 int diffOperationMode,
									 boolean splitMultiGeomsInComponents){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		DiffGeometryComponents diff = new DiffGeometryComponents(fc, fc2, new TaskMonitorAdapter());
	   
		switch(diffOperationMode){
		case BUFFER_DIFF_OP_MODE:
			diff.setMatcher(new BufferGeometryMatcher(0.2));
		break;
		
		case EXACT_DIFF_OP_MODE:
			diff.setMatcher(new ExactGeometryMatcher());
		break;
		
		case NORMALIZE_DIFF_OP_MODE:
		default:
			diff.setNormalize(true);
		break;
		
		}
		
	    diff.setSplitIntoComponents(splitMultiGeomsInComponents);
	    FeatureCollection[] diffFC = diff.diff();
	    
	    //la primera tiene las geometrias que casan, la segunda las que no casan

	   
	}
	
	////Igual que el DIFF GEOMETRY, pero a nivel de segmentos en vez de a nivel de geometrias
	
	public static void diffSegmentsOperation(FLyrVect lyr, FLyrVect lyr2, double tolerance){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		DiffSegmentsWithTolerance diff = new DiffSegmentsWithTolerance(fc, fc2, tolerance);
		//el primero tiene los segmentos de A que no están en B, y el segundo viceversa
		FeatureCollection[] diffFC  = diff.diff();
	}
	
	public static void diffSegments(FLyrVect lyr, FLyrVect lyr2){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		DiffSegments diff = new DiffSegments(new TaskMonitorAdapter());
		diff.setSegments(0, fc);
		diff.setSegments(1, fc2);
		//el primero tiene los segmentos de A que no están en B, y el segundo viceversa
		FeatureCollection a = diff.computeDiffEdges(0);
		FeatureCollection b = diff.computeDiffEdges(1);
	}
	
	
	public static void findBoundaryCornerOffsets(FLyrVect lyr, FLyrVect lyr2, double clusterTolerance, double offsetTolerance, int maxOffsetAngle){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		OffsetBoundaryCornerFinder.Parameters param = new OffsetBoundaryCornerFinder.Parameters();
	    param.boundaryDistanceTolerance = clusterTolerance;
	    param.offsetTolerance = offsetTolerance;
	    param.maxCornerAngle = maxOffsetAngle;
	    OffsetBoundaryCornerFinder finder = new OffsetBoundaryCornerFinder(fc, fc2, param);
	    finder.compute(new TaskMonitorAdapter());
	    FeatureCollection solution = finder.getOffsetIndicators();
	}
	
	
	public static void findOverlaps(FLyrVect lyr, FLyrVect lyr2){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		OverlapFinder ovFinder = new OverlapFinder(fc, fc2);

	    FeatureCollection ovRef = ovFinder.getOverlappingFeatures(0);
	    FeatureCollection ovSub = ovFinder.getOverlappingFeatures(1);
	    FeatureCollection indicators = ovFinder.getOverlapIndicators();
	    FeatureCollection indicatorsSize = ovFinder.getOverlapSizeIndicators();
	}
	
	
	public static void findInternalMatchesSegments(FLyrVect lyr, double clusterTolerance){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		InternalMatchedSegmentFinder.Parameters param = 
			new InternalMatchedSegmentFinder.Parameters();
	    param.distanceTolerance = clusterTolerance;
	    InternalMatchedSegmentFinder finder = new InternalMatchedSegmentFinder(fc, param);
	    FeatureCollection matchFeat = finder.getMatchedFeatures();
	    List triangleMatches = finder.findTriangleMatches();
	    FeatureCollection matchedSegments = finder.getMatchedSegments();
	    FeatureCollection sizeIndicators = finder.getSizeIndicators();
	}
	
	
	public static void removeGapsFromPolygonCoverage(FLyrVect lyr, 
											double clusterTolerance, 
											double angleTolerance){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		CoverageGapRemover.Parameters param = new CoverageGapRemover.Parameters();
		param.distanceTolerance = clusterTolerance;
		param.angleTolerance = angleTolerance;
		CoverageGapRemover covGap = new CoverageGapRemover(fc, new TaskMonitorAdapter());
		covGap.process(param);

		FeatureCollection adjSrc = covGap.getAdjustedFeatures();
		FeatureCollection indicators = covGap.getAdjustmentIndicators();
		FeatureCollection matched = covGap.getMatchedFeatures();
		FeatureCollection updated = covGap.getUpdatedFeatures();
	}
	
	
	public static FLyrVect getGaps(FLyrVect lyr, double clusterTolerance){
		 InternalMatchedSegmentFinder.Parameters msfParam
	        = new InternalMatchedSegmentFinder.Parameters();
		 msfParam.distanceTolerance = clusterTolerance;
		 double angleTolerance = 22.5d;
		 msfParam.angleTolerance = angleTolerance;
	    InternalMatchedSegmentFinder msf = 
	    	new InternalMatchedSegmentFinder(new FeatureCollectionAdapter(lyr.getSource()),
	    									msfParam);
	    FeatureCollection gapsCandidates = msf.getMatchedFeatures();
	    FeatureCollectionDataSourceAdapter driver =
	    	new FeatureCollectionDataSourceAdapter(Messages.getText("GAPS"), 
	    											gapsCandidates, 
	    											new FeatureSchemaLayerDefinitionAdapter(gapsCandidates.getFeatureSchema()));
	    FLayerGenericVectorial solution = new FLayerGenericVectorial();
		solution.setName(driver.getName());
		solution.setProjection(lyr.getProjection());
		solution.setDriver(driver);
		try {
			solution.load();
	
		} catch (LoadLayerException e) {
			e.printStackTrace();
		}
		return solution;
	}
	
	
	public static void alignCoverage(FLyrVect lyr, FLyrVect lyr2, double clusterTolerance, double angleTolerance){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		CoverageAligner.Parameters param = new CoverageAligner.Parameters();
		param.distanceTolerance = clusterTolerance;
		param.angleTolerance = angleTolerance;
		CoverageAligner cvgAligner = new CoverageAligner(fc, fc2, new TaskMonitorAdapter());
		cvgAligner.process(param);

		FeatureCollection adjSub = cvgAligner.getAdjustedFeatures();   
		FeatureCollection updatedSub = cvgAligner.getUpdatedFeatures();
		FeatureCollection indicators = cvgAligner.getAdjustmentIndicators();
	}
	
	public static void removeShortSegments(FLyrVect lyr, double minLength, double displacementTolerance){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
	    FeatureShortSegmentRemover remover = new FeatureShortSegmentRemover(fc, minLength, displacementTolerance);
	    FeatureCollection newFC = remover.process(new TaskMonitorAdapter());
	}
	
	
	
	public static void boundaryMatch(FLyrVect lyr, FLyrVect lyr2, double clusterTol, boolean insertRefVertices){
		FeatureCollectionAdapter fc = new FeatureCollectionAdapter(lyr.getSource());
		FeatureCollectionAdapter fc2 = new FeatureCollectionAdapter(lyr2.getSource());
		BoundaryMatcher bm = new BoundaryMatcher(fc, fc2);
	    BoundaryMatcherParameters bmParam = new BoundaryMatcherParameters();
	    bmParam.distanceTolerance = clusterTol;
	    bmParam.insertRefVertices  = insertRefVertices;
	    bm.match(bmParam);

	    FeatureCollection adjSrc = bm.getAdjustedFeatures(0);
	    FeatureCollection adjTgt = bm.getAdjustedFeatures(1);
	    FeatureCollection adjRefEdge = bm.getAdjustedEdgeIndicators(0);
	    FeatureCollection adjSubEdge = bm.getAdjustedEdgeIndicators(1);
	}
	
	
	public static void matchLinearFeatures(IFeature linearFeature, FLyrVect lyr, double clusterTolerance, double angleTolerance){
		try {
			IFeatureIterator iterator = lyr.getSource().getFeatureIterator();
			SegmentMatcher segmentMatcher = new SegmentMatcher(clusterTolerance, angleTolerance);
			while(iterator.hasNext()){
				IFeature feature = iterator.next();
				if(feature.getID().equals(linearFeature.getID()))
					continue;
				Geometry geom = NewFConverter.toJtsGeometry(feature.getGeometry());
				LineString[] lineStrings = JtsUtil.extractLineStrings(geom);
				for (int i = 0; i < lineStrings.length; i++) {
					LineString lineStr = lineStrings[i];
					Coordinate[] pts = lineStr.getCoordinates();
					for (int j = 0; j < pts.length - 1; j++) {
						Coordinate a = pts[j];
						Coordinate b = pts[j + 1];
						LineSegment segment = new LineSegment(a, b);
						
						//TODO Recorrer aqui los segmentos de linearFeature, y comparar
						
						
						segmentMatcher.isMatch(segment, null);
						                   
					}
						
				}
			}
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	

//	public static void match(FLyrVect targetLyr, FLyrVect candidateLyr,
//			FeatureMatcher featureMatcher, boolean filteringByWindow,
//			double windowBuffer, boolean filteringByArea, double minArea,
//			double maxArea, boolean unioningCandidates, int maxUnionMembers) {
//
//		FeatureMatcher actualFeatureMatcher = featureMatcher;
//		if (filteringByWindow) {
//			actualFeatureMatcher = new ChainMatcher(new FeatureMatcher[] {
//					new WindowFilter(50), actualFeatureMatcher });
//		}
//
//		FCMatchFinder matchFinder = new BasicFCMatchFinder(actualFeatureMatcher);
//
//		// We definitely want to one-to-one before union (combinatorial) -- if
//		// after, we'll
//		// wipe out some union members! [Jon Aquino]
//		matchFinder = new DisambiguatingFCMatchFinder(matchFinder);
//
//		TargetUnioningFCMatchFinder targetUnioningFCMatchFinder = null;
//		if (unioningCandidates) {
//			targetUnioningFCMatchFinder = new TargetUnioningFCMatchFinder(
//					maxUnionMembers, matchFinder);
//			matchFinder = targetUnioningFCMatchFinder;
//		}
//		if (filteringByArea) {
//			matchFinder = new AreaFilterFCMatchFinder(minArea, maxArea,
//					matchFinder);
//		}
//
//		FeatureCollectionAdapter targetFcAdapter = new FeatureCollectionAdapter(
//				targetLyr.getSource());
//
//		FeatureCollectionAdapter candidatesFcAdapter = new FeatureCollectionAdapter(
//				candidateLyr.getSource());
//
//		TaskMonitorAdapter taskMonitor = new TaskMonitorAdapter();
//
//		Map<Feature, Matches> originalFeatureToFeatureCandidatesMap = matchFinder
//				.match(targetFcAdapter, candidatesFcAdapter, taskMonitor);
//
//		matchPairFeatureCollection = createMatchPairFeatureCollection(targetUnioningFCMatchFinder);
//
//		matchedTargetsFeatureCollection = createMatchedFeatureCollection(true,
//				targetUnioningFCMatchFinder);
//
//		unmatchedTargetsFeatureCollection = createUnmatchedFeatureCollection(true);
//
//		matchedCandidatesFeatureCollection = createMatchedFeatureCollection(
//				false, targetUnioningFCMatchFinder);
//
//		unmatchedCandidatesFeatureCollection = createUnmatchedFeatureCollection(false);
//
//	}
//	
//	private FeatureCollection createUnmatchedFeatureCollection(boolean fromTarget) {
//        FeatureCollection originalFC =
//            fromTarget ? targetFeatureCollection : candidateFeatureCollection;
//        FeatureCollection newFC = new FeatureDataset(originalFC.getFeatureSchema());
//        newFC.addAll(originalFC.getFeatures());
//        newFC.removeAll(
//            fromTarget
//                ? matchedTargets(targetFeatureToMatchesMap)
//                : topCandidates(targetFeatureToMatchesMap));
//        return clone(newFC);
//    }
//	
//	private FeatureCollection createMatchedFeatureCollection(boolean fromTarget, TargetUnioningFCMatchFinder targetUnioningFCMatchFinder) {
//        AttributeMapping mapping =
//            new AttributeMapping(
//                (fromTarget ? targetFeatureCollection : candidateFeatureCollection)
//                    .getFeatureSchema(),
//                new FeatureSchema());
//        //Feature may appear more than once because of unioning. So remove duplicates. [Jon Aquino]
//        TreeSet featureSet = new TreeSet();
//        FeatureCollection newFC = new FeatureDataset(createSchema(mapping, targetUnioningFCMatchFinder != null));
//        for (Iterator i = targetFeatureToMatchesMap.keySet().iterator(); i.hasNext();) {
//            Feature target = (Feature) i.next();
//            Matches matches = (Matches) targetFeatureToMatchesMap.get(target);
//            Feature topCandidate = matches.getTopMatch();
//            if (topCandidate == null) {
//                continue;
//            }
//            Feature originalFeature = fromTarget ? target : topCandidate;
//            Feature newFeature = new BasicFeature(newFC.getFeatureSchema());
//            //2nd arg doesn't matter. [Jon Aquino]
//            mapping.transferAttributes(originalFeature, originalFeature, newFeature);
//            newFeature.setAttribute(SCORE_ATTRIBUTE, new Double(matches.getTopScore()));
//            if (targetUnioningFCMatchFinder != null) {
//                newFeature.setAttribute(UNION_ID_ATTRIBUTE, targetUnioningFCMatchFinder.getUnionID(target));
//            }
//            newFeature.setGeometry((Geometry) originalFeature.getGeometry().clone());
//            featureSet.add(newFeature);
//        }
//        newFC.addAll(featureSet);
//        return newFC;
//    }
//	
//	private FeatureCollection createMatchPairFeatureCollection(TargetUnioningFCMatchFinder targetUnioningFCMatchFinder) {
//        GeometryFactory factory = new GeometryFactory();
//        AttributeMapping mapping =
//            new AttributeMapping(
//                targetFeatureCollection.getFeatureSchema(),
//                candidateFeatureCollection.getFeatureSchema());
//        FeatureCollection newFC = new FeatureDataset(createSchema(mapping, targetUnioningFCMatchFinder != null));
//        for (Iterator i = targetFeatureToMatchesMap.keySet().iterator(); i.hasNext();) {
//            Feature target = (Feature) i.next();
//            Matches matches = (Matches) targetFeatureToMatchesMap.get(target);
//            Feature topCandidate = matches.getTopMatch();
//            if (topCandidate == null) {
//                continue;
//            }
//            Feature newFeature = new BasicFeature(newFC.getFeatureSchema());
//            mapping.transferAttributes(target, topCandidate, newFeature);
//            newFeature.setAttribute(SCORE_ATTRIBUTE, new Double(matches.getTopScore()));
//            if (targetUnioningFCMatchFinder != null) {
//                newFeature.setAttribute(UNION_ID_ATTRIBUTE, targetUnioningFCMatchFinder.getUnionID(target));
//            }
//            newFeature.setGeometry(
//                factory.createGeometryCollection(
//                    new Geometry[] {
//                        (Geometry) target.getGeometry().clone(),
//                        (Geometry) topCandidate.getGeometry().clone()}));
//            newFC.add(newFeature);
//        }
//        return newFC;
//    }

	public static FeatureMatcher createFeatureMatcher(){
		return createFeatureMatcher(1d, 1d, 1d, 1d, 1d, 1d);
	}
	
	
	
	public static FeatureMatcher createFeatureMatcher(double centroidDistanceWeight,
			double hasdorffDistanceWeight, double symDiffWeight,
			double symDiffCentroidsWeight, double compactnessWeight,
			double angleWeight) {

		return new WeightedMatcher(new Object[] {
				new Double(centroidDistanceWeight),
				new CentroidDistanceMatcher(),

				new Double(hasdorffDistanceWeight),
				new CentroidAligner(new HausdorffDistanceMatcher()),

				new Double(symDiffWeight), new SymDiffMatcher(),

				new Double(symDiffCentroidsWeight),
				new CentroidAligner(new SymDiffMatcher()),

				new Double(compactnessWeight), new CompactnessMatcher(),

				new Double(angleWeight), new AngleHistogramMatcher() });
	}
}
