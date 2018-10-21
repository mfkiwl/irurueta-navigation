/*
 * Copyright (C) 2018 Alberto Irurueta Carro (alberto@irurueta.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.irurueta.navigation.indoor;

import com.irurueta.algebra.AlgebraException;
import com.irurueta.geometry.Accuracy3D;
import com.irurueta.geometry.InhomogeneousPoint3D;
import com.irurueta.geometry.Point3D;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.navigation.trilateration.PROMedSRobustTrilateration3DSolver;
import com.irurueta.navigation.trilateration.RobustTrilaterationSolver;
import com.irurueta.numerical.robust.RobustEstimatorException;
import com.irurueta.numerical.robust.RobustEstimatorMethod;
import com.irurueta.statistics.GaussianRandomizer;
import com.irurueta.statistics.UniformRandomizer;
import org.junit.Test;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("Duplicates")
public class PROMedSRobustPositionEstimator3DTest implements
        RobustPositionEstimatorListener<Point3D> {

    private static final Logger LOGGER = Logger.getLogger(
            PROMedSRobustPositionEstimator3DTest.class.getName());

    private static final double FREQUENCY = 2.4e9; //(Hz)

    private static final int MIN_SOURCES = 100;
    private static final int MAX_SOURCES = 500;

    private static final double MIN_POS = -50.0;
    private static final double MAX_POS = 50.0;

    private static final double MIN_RSSI = -100;
    private static final double MAX_RSSI = -50;

    private static final double MIN_PATH_LOSS_EXPONENT = 1.6;
    private static final double MAX_PATH_LOSS_EXPONENT = 2.0;

    private static final double SPEED_OF_LIGHT = 299792458.0;

    private static final double ABSOLUTE_ERROR = 1e-6;
    private static final double LARGE_ABSOLUTE_ERROR = 0.5;

    private static final int TIMES = 50;

    private static final int PERCENTAGE_OUTLIERS = 20;

    private static final double STD_OUTLIER_ERROR = 10.0;

    private static final double INLIER_ERROR_STD = 0.1;

    private static final double TX_POWER_VARIANCE = 0.1;
    private static final double RX_POWER_VARIANCE = 0.5;
    private static final double PATHLOSS_EXPONENT_VARIANCE = 0.001;
    private static final double RANGING_STD = 1.0;

    private int estimateStart;
    private int estimateEnd;
    private int estimateNextIteration;
    private int estimateProgressChange;

    @Test
    public void testConstructor() {
        //empty constructor
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertNull(estimator.getFingerprint());
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);


        //constructor with sources
        List<WifiAccessPointLocated3D> sources = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            sources.add(new WifiAccessPointLocated3D("id1", FREQUENCY,
                    new InhomogeneousPoint3D()));
        }
        estimator = new PROMedSRobustPositionEstimator3D(sources);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertNull(estimator.getFingerprint());
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    (List<WifiAccessPointLocated3D>)null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    new ArrayList<WifiAccessPointLocated3D>());
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with fingerprints
        RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>> fingerprint =
                new RssiFingerprint<>();
        estimator = new PROMedSRobustPositionEstimator3D(fingerprint);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertSame(estimator.getFingerprint(), fingerprint);
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    (RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>>)null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with sources and fingerprint
        estimator = new PROMedSRobustPositionEstimator3D(sources, fingerprint);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertSame(estimator.getFingerprint(), fingerprint);
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    (List<WifiAccessPointLocated3D>)null, fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    new ArrayList<WifiAccessPointLocated3D>(), fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(sources,
                    (RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>>)null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with listener
        estimator = new PROMedSRobustPositionEstimator3D(this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertNull(estimator.getFingerprint());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);


        //constructor with sources and listener
        estimator = new PROMedSRobustPositionEstimator3D(sources, this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertNull(estimator.getFingerprint());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    (List<WifiAccessPointLocated3D>)null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    new ArrayList<WifiAccessPointLocated3D>(), this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with fingerprint and listener
        estimator = new PROMedSRobustPositionEstimator3D(fingerprint, this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertSame(estimator.getFingerprint(), fingerprint);
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    (RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>>)null,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with sources, fingerprint and listener
        estimator = new PROMedSRobustPositionEstimator3D(sources, fingerprint,
                this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertSame(estimator.getFingerprint(), fingerprint);
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    (List<WifiAccessPointLocated3D>)null, fingerprint,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(
                    new ArrayList<WifiAccessPointLocated3D>(), fingerprint,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(sources,
                    null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores
        double[] qualityScores = new double[4];
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertNull(estimator.getFingerprint());
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D((double[])null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores and sources
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores, sources);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertNull(estimator.getFingerprint());
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(null,
                    sources);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1],
                    sources);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    (List<WifiAccessPointLocated3D>)null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    new ArrayList<WifiAccessPointLocated3D>());
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores and fingerprint
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores, fingerprint);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertSame(estimator.getFingerprint(), fingerprint);
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D((double[])null,
                    fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1],
                    fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    (RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>>)null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores, sources and fingerprint
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                fingerprint);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertSame(estimator.getFingerprint(), fingerprint);
        assertNull(estimator.getListener());
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(null,
                    sources, fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1],
                    sources, fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    null, fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    new ArrayList<WifiAccessPointLocated3D>(), fingerprint);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                    (RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>>)null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores and listener
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertNull(estimator.getFingerprint());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D((double[])null,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1],
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores, sources and listener
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertNull(estimator.getFingerprint());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(null,
                    sources, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1],
                    sources, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    (List<WifiAccessPointLocated3D>)null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    new ArrayList<WifiAccessPointLocated3D>(), this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores, fingerprint and listener
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                fingerprint, this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertNull(estimator.getSources());
        assertSame(estimator.getFingerprint(), fingerprint);
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D((double[])null,
                    fingerprint, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1],
                    fingerprint, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    (RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>>)null,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //constructor with quality scores, sources, fingerprint listener
        estimator = new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                fingerprint, this);

        //check default values
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);
        assertEquals(estimator.getMinRequiredSources(), 4);
        assertSame(estimator.getSources(), sources);
        assertSame(estimator.getFingerprint(), fingerprint);
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertEquals(estimator.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getInliersData());
        assertNull(estimator.getPositions());
        assertNull(estimator.getDistances());
        assertNull(estimator.getDistanceStandardDeviations());
        assertFalse(estimator.isReady());
        assertSame(estimator.getQualityScores(), qualityScores);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getCovariance());
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.PROMedS);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new PROMedSRobustPositionEstimator3D(null,
                    sources, fingerprint, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(new double[1],
                    sources, fingerprint, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    null, fingerprint, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores,
                    new ArrayList<WifiAccessPointLocated3D>(), fingerprint,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                    null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);
    }

    @Test
    public void testGetSetStopThreshold() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertEquals(estimator.getStopThreshold(),
                PROMedSRobustTrilateration3DSolver.DEFAULT_STOP_THRESHOLD, 0.0);

        //set new value
        estimator.setStopThreshold(1.0);

        //check
        assertEquals(estimator.getStopThreshold(), 1.0, 0.0);

        //force IllegalArgumentException
        try {
            estimator.setStopThreshold(0.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetSources() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertNull(estimator.getSources());

        //set new value
        List<WifiAccessPointLocated3D> sources = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            sources.add(new WifiAccessPointLocated3D("id1", FREQUENCY,
                    new InhomogeneousPoint3D()));
        }

        estimator.setSources(sources);

        //check
        assertSame(estimator.getSources(), sources);

        //force IllegalArgumentException
        try {
            estimator.setSources(null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator.setSources(new ArrayList<WifiAccessPointLocated3D>());
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetFingerprint() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertNull(estimator.getFingerprint());

        //set new value
        RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>> fingerprint =
                new RssiFingerprint<>();
        estimator.setFingerprint(fingerprint);

        //check
        assertSame(estimator.getFingerprint(), fingerprint);

        //force IllegalArgumentException
        try {
            estimator.setFingerprint(null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetListener() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertNull(estimator.getListener());

        //set new value
        estimator.setListener(this);

        //check
        assertSame(estimator.getListener(), this);
    }

    @Test
    public void testIsSetRadioSourcePositionCovarianceUsed() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertFalse(estimator.isRadioSourcePositionCovarianceUsed());

        //set new value
        estimator.setRadioSourcePositionCovarianceUsed(true);

        //chekc
        assertTrue(estimator.isRadioSourcePositionCovarianceUsed());
    }

    @Test
    public void testGetSetFallbackDistanceStandardDeviation() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                RobustPositionEstimator.FALLBACK_DISTANCE_STANDARD_DEVIATION,
                0.0);

        //set new value
        estimator.setFallbackDistanceStandardDeviation(1.0);

        //check
        assertEquals(estimator.getFallbackDistanceStandardDeviation(),
                1.0, 0.0);
    }

    @Test
    public void testGetSetProgressDelta() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertEquals(estimator.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);

        //set new value
        estimator.setProgressDelta(0.5f);

        //check
        assertEquals(estimator.getProgressDelta(), 0.5f, 0.0);

        //force IllegalArgumentException
        try {
            estimator.setProgressDelta(-1.0f);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetConfidence() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertEquals(estimator.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);

        //set new value
        try {
            estimator.setConfidence(-1.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator.setConfidence(2.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetMaxIterations() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertEquals(estimator.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);

        //set new value
        estimator.setMaxIterations(100);

        //check
        assertEquals(estimator.getMaxIterations(), 100);

        //force IllegalArgumentException
        try {
            estimator.setMaxIterations(0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testIsSetResultRefined() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertTrue(estimator.isResultRefined());

        //set new value
        estimator.setResultRefined(false);

        //check
        assertFalse(estimator.isResultRefined());
    }

    @Test
    public void testIsSetCovarianceKept() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertTrue(estimator.isCovarianceKept());

        //set new value
        estimator.setCovarianceKept(false);

        //check
        assertFalse(estimator.isCovarianceKept());
    }

    @Test
    public void testGetSetQualityScores() throws LockedException {
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();

        //check default value
        assertNull(estimator.getQualityScores());

        //set new value
        double[] qualityScores = new double[4];
        estimator.setQualityScores(qualityScores);

        //check
        assertSame(estimator.getQualityScores(), qualityScores);

        //force IllegalArgumentException
        try {
            estimator.setQualityScores(new double[1]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testEstimateRssi() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);


            List<WifiAccessPointWithPowerAndLocated3D> sources = new ArrayList<>();
            List<RssiReading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
                double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
                String bssid = String.valueOf(i);

                WifiAccessPointWithPowerAndLocated3D locatedAccessPoint =
                        new WifiAccessPointWithPowerAndLocated3D(bssid,
                                FREQUENCY, transmittedPowerdBm,
                                Math.sqrt(TX_POWER_VARIANCE),
                                pathLossExponent,
                                Math.sqrt(PATHLOSS_EXPONENT_VARIANCE),
                                accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(transmittedPower,
                        distance, FREQUENCY, pathLossExponent));

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RssiReading<>(accessPoint, rssi + error,
                        Math.sqrt(RX_POWER_VARIANCE)));
            }

            RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>> fingerprint =
                    new RssiFingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);
            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());

            Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
            accuracyStd.setStandardDeviationFactor(1.0);

            Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
            accuracy.setConfidence(0.99);

            double positionStd = accuracyStd.getAverageAccuracy();
            double positionStdConfidence = accuracyStd.getConfidence();
            double positionAccuracy = accuracy.getAverageAccuracy();
            double positionAccuracyConfidence = accuracy.getConfidence();

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
                avgValidPositionAccuracy += positionAccuracy;
            } else {
                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
                avgInvalidPositionAccuracy += positionAccuracy;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;
            avgPositionStdConfidence += positionStdConfidence;
            avgPositionAccuracy += positionAccuracy;
            avgPositionAccuracyConfidence += positionAccuracyConfidence;
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (TIMES - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateRssiWithInlierError() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidCovariance = 0, num = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);


            List<WifiAccessPointWithPowerAndLocated3D> sources = new ArrayList<>();
            List<RssiReading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
                double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
                String bssid = String.valueOf(i);

                WifiAccessPointWithPowerAndLocated3D locatedAccessPoint =
                        new WifiAccessPointWithPowerAndLocated3D(bssid,
                                FREQUENCY, transmittedPowerdBm,
                                Math.sqrt(TX_POWER_VARIANCE),
                                pathLossExponent,
                                Math.sqrt(PATHLOSS_EXPONENT_VARIANCE),
                                accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(transmittedPower,
                        distance, FREQUENCY, pathLossExponent));

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                error += inlierErrorRandomizer.nextDouble();

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RssiReading<>(accessPoint, rssi + error,
                        Math.sqrt(RX_POWER_VARIANCE)));
            }

            RssiFingerprint<WifiAccessPoint, RssiReading<WifiAccessPoint>> fingerprint =
                    new RssiFingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);

            double positionStd = 0.0, positionStdConfidence = 0.0,
                    positionAccuracy = 0.0, positionAccuracyConfidence = 0.0;
            boolean hasCovariance = false;
            if (estimator.getInliersData() != null && estimator.getCovariance() != null) {
                assertNotNull(estimator.getInliersData());
                assertNotNull(estimator.getCovariance());

                Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
                accuracyStd.setStandardDeviationFactor(1.0);

                Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
                accuracy.setConfidence(0.99);

                positionStd = accuracyStd.getAverageAccuracy();
                positionStdConfidence = accuracyStd.getConfidence();
                positionAccuracy = accuracy.getAverageAccuracy();
                positionAccuracyConfidence = accuracy.getConfidence();

                num++;
                hasCovariance = true;
            }

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= LARGE_ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;

                if (hasCovariance) {
                    avgValidPositionStd += positionStd;
                    avgValidPositionAccuracy += positionAccuracy;
                    numValidCovariance++;
                }
            } else {
                avgInvalidPositionError += positionDistance;

                if (hasCovariance) {
                    avgInvalidPositionStd += positionStd;
                    avgInvalidPositionAccuracy += positionAccuracy;
                }
            }

            avgPositionError += positionDistance;

            if (hasCovariance) {
                avgPositionStd += positionStd;
                avgPositionStdConfidence += positionStdConfidence;
                avgPositionAccuracy += positionAccuracy;
                avgPositionAccuracyConfidence += positionAccuracyConfidence;
            }
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidCovariance;
        avgInvalidPositionStd /= (num - numValidPosition);
        avgPositionStd /= num;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (num - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;


        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateRanging() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));

            List<WifiAccessPointLocated3D> sources = new ArrayList<>();
            List<RangingReading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                String bssid = String.valueOf(i);

                WifiAccessPointLocated3D locatedAccessPoint =
                        new WifiAccessPointLocated3D(bssid,
                                FREQUENCY, accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RangingReading<>(accessPoint,
                        Math.max(0.0, distance + error),
                        RANGING_STD));
            }

            RangingFingerprint<WifiAccessPoint, RangingReading<WifiAccessPoint>> fingerprint =
                    new RangingFingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);
            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());

            Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
            accuracyStd.setStandardDeviationFactor(1.0);

            Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
            accuracy.setConfidence(0.99);

            double positionStd = accuracyStd.getAverageAccuracy();
            double positionStdConfidence = accuracyStd.getConfidence();
            double positionAccuracy = accuracy.getAverageAccuracy();
            double positionAccuracyConfidence = accuracy.getConfidence();

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
                avgValidPositionAccuracy += positionAccuracy;
            } else {
                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
                avgInvalidPositionAccuracy += positionAccuracy;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;
            avgPositionStdConfidence += positionStdConfidence;
            avgPositionAccuracy += positionAccuracy;
            avgPositionAccuracyConfidence += positionAccuracyConfidence;
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (TIMES - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateRangingWithInlierError() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidCovariance = 0, num = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));

            List<WifiAccessPointLocated3D> sources = new ArrayList<>();
            List<RangingReading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                String bssid = String.valueOf(i);

                WifiAccessPointLocated3D locatedAccessPoint =
                        new WifiAccessPointLocated3D(bssid,
                                FREQUENCY, accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                error += inlierErrorRandomizer.nextDouble();

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RangingReading<>(accessPoint,
                        Math.max(0.0, distance + error),
                        RANGING_STD));
            }

            RangingFingerprint<WifiAccessPoint, RangingReading<WifiAccessPoint>> fingerprint =
                    new RangingFingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);

            double positionStd = 0.0, positionStdConfidence = 0.0,
                    positionAccuracy = 0.0, positionAccuracyConfidence = 0.0;
            boolean hasCovariance = false;
            if (estimator.getInliersData() != null && estimator.getCovariance() != null) {
                assertNotNull(estimator.getInliersData());
                assertNotNull(estimator.getCovariance());

                Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
                accuracyStd.setStandardDeviationFactor(1.0);

                Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
                accuracy.setConfidence(0.99);

                positionStd = accuracyStd.getAverageAccuracy();
                positionStdConfidence = accuracyStd.getConfidence();
                positionAccuracy = accuracy.getAverageAccuracy();
                positionAccuracyConfidence = accuracy.getConfidence();

                num++;
                hasCovariance = true;
            }

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= LARGE_ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;

                if (hasCovariance) {
                    avgValidPositionStd += positionStd;
                    avgValidPositionAccuracy += positionAccuracy;
                    numValidCovariance++;
                }
            } else {
                avgInvalidPositionError += positionDistance;

                if (hasCovariance) {
                    avgInvalidPositionStd += positionStd;
                    avgInvalidPositionAccuracy += positionAccuracy;
                }
            }

            avgPositionError += positionDistance;

            if (hasCovariance) {
                avgPositionStd += positionStd;
                avgPositionStdConfidence += positionStdConfidence;
                avgPositionAccuracy += positionAccuracy;
                avgPositionAccuracyConfidence += positionAccuracyConfidence;
            }
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidCovariance;
        avgInvalidPositionStd /= (num - numValidPosition);
        avgPositionStd /= num;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (num - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;


        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateRangingAndRssi() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);


            List<WifiAccessPointWithPowerAndLocated3D> sources = new ArrayList<>();
            List<RangingAndRssiReading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
                double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
                String bssid = String.valueOf(i);

                WifiAccessPointWithPowerAndLocated3D locatedAccessPoint =
                        new WifiAccessPointWithPowerAndLocated3D(bssid,
                                FREQUENCY, transmittedPowerdBm,
                                Math.sqrt(TX_POWER_VARIANCE),
                                pathLossExponent,
                                Math.sqrt(PATHLOSS_EXPONENT_VARIANCE),
                                accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(transmittedPower,
                        distance, FREQUENCY, pathLossExponent));

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RangingAndRssiReading<>(accessPoint,
                        Math.max(0.0, distance + error), rssi + error,
                        RANGING_STD, Math.sqrt(RX_POWER_VARIANCE)));
            }

            RangingAndRssiFingerprint<WifiAccessPoint, RangingAndRssiReading<WifiAccessPoint>> fingerprint =
                    new RangingAndRssiFingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);
            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());

            Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
            accuracyStd.setStandardDeviationFactor(1.0);

            Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
            accuracy.setConfidence(0.99);

            double positionStd = accuracyStd.getAverageAccuracy();
            double positionStdConfidence = accuracyStd.getConfidence();
            double positionAccuracy = accuracy.getAverageAccuracy();
            double positionAccuracyConfidence = accuracy.getConfidence();

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
                avgValidPositionAccuracy += positionAccuracy;
            } else {
                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
                avgInvalidPositionAccuracy += positionAccuracy;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;
            avgPositionStdConfidence += positionStdConfidence;
            avgPositionAccuracy += positionAccuracy;
            avgPositionAccuracyConfidence += positionAccuracyConfidence;
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (TIMES - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateRangingAndRssiWithInlierError() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidCovariance = 0, num = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);


            List<WifiAccessPointWithPowerAndLocated3D> sources = new ArrayList<>();
            List<RangingAndRssiReading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
                double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
                String bssid = String.valueOf(i);

                WifiAccessPointWithPowerAndLocated3D locatedAccessPoint =
                        new WifiAccessPointWithPowerAndLocated3D(bssid,
                                FREQUENCY, transmittedPowerdBm,
                                Math.sqrt(TX_POWER_VARIANCE),
                                pathLossExponent,
                                Math.sqrt(PATHLOSS_EXPONENT_VARIANCE),
                                accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(transmittedPower,
                        distance, FREQUENCY, pathLossExponent));

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                error += inlierErrorRandomizer.nextDouble();

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RangingAndRssiReading<>(accessPoint,
                        Math.max(0.0, distance + error), rssi + error,
                        RANGING_STD, Math.sqrt(RX_POWER_VARIANCE)));
            }

            RangingAndRssiFingerprint<WifiAccessPoint, RangingAndRssiReading<WifiAccessPoint>> fingerprint =
                    new RangingAndRssiFingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);

            double positionStd = 0.0, positionStdConfidence = 0.0,
                    positionAccuracy = 0.0, positionAccuracyConfidence = 0.0;
            boolean hasCovariance = false;
            if (estimator.getInliersData() != null && estimator.getCovariance() != null) {
                assertNotNull(estimator.getInliersData());
                assertNotNull(estimator.getCovariance());

                Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
                accuracyStd.setStandardDeviationFactor(1.0);

                Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
                accuracy.setConfidence(0.99);

                positionStd = accuracyStd.getAverageAccuracy();
                positionStdConfidence = accuracyStd.getConfidence();
                positionAccuracy = accuracy.getAverageAccuracy();
                positionAccuracyConfidence = accuracy.getConfidence();

                num++;
                hasCovariance = true;
            }

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= LARGE_ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;

                if (hasCovariance) {
                    avgValidPositionStd += positionStd;
                    avgValidPositionAccuracy += positionAccuracy;
                    numValidCovariance++;
                }
            } else {
                avgInvalidPositionError += positionDistance;

                if (hasCovariance) {
                    avgInvalidPositionStd += positionStd;
                    avgInvalidPositionAccuracy += positionAccuracy;
                }
            }

            avgPositionError += positionDistance;

            if (hasCovariance) {
                avgPositionStd += positionStd;
                avgPositionStdConfidence += positionStdConfidence;
                avgPositionAccuracy += positionAccuracy;
                avgPositionAccuracyConfidence += positionAccuracyConfidence;
            }
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidCovariance;
        avgInvalidPositionStd /= (num - numValidPosition);
        avgPositionStd /= num;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (num - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;


        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateMixed() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);


            List<WifiAccessPointWithPowerAndLocated3D> sources = new ArrayList<>();
            List<Reading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
                double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
                String bssid = String.valueOf(i);

                WifiAccessPointWithPowerAndLocated3D locatedAccessPoint =
                        new WifiAccessPointWithPowerAndLocated3D(bssid,
                                FREQUENCY, transmittedPowerdBm,
                                Math.sqrt(TX_POWER_VARIANCE),
                                pathLossExponent,
                                Math.sqrt(PATHLOSS_EXPONENT_VARIANCE),
                                accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(transmittedPower,
                        distance, FREQUENCY, pathLossExponent));

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RssiReading<>(accessPoint, rssi + error,
                        Math.sqrt(RX_POWER_VARIANCE)));
                readings.add(new RangingReading<>(accessPoint,
                        Math.max(0.0, distance + error),
                        RANGING_STD));
                readings.add(new RangingAndRssiReading<>(accessPoint,
                        Math.max(0.0, distance + error), rssi + error,
                        RANGING_STD, Math.sqrt(RX_POWER_VARIANCE)));
            }

            Fingerprint<WifiAccessPoint, Reading<WifiAccessPoint>> fingerprint =
                    new Fingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);
            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());

            Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
            accuracyStd.setStandardDeviationFactor(1.0);

            Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
            accuracy.setConfidence(0.99);

            double positionStd = accuracyStd.getAverageAccuracy();
            double positionStdConfidence = accuracyStd.getConfidence();
            double positionAccuracy = accuracy.getAverageAccuracy();
            double positionAccuracyConfidence = accuracy.getConfidence();

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
                avgValidPositionAccuracy += positionAccuracy;
            } else {
                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
                avgInvalidPositionAccuracy += positionAccuracy;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;
            avgPositionStdConfidence += positionStdConfidence;
            avgPositionAccuracy += positionAccuracy;
            avgPositionAccuracyConfidence += positionAccuracyConfidence;
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (TIMES - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateMixedWithInlierError() throws LockedException, NotReadyException,
            RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidCovariance = 0, num = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0, avgPositionStdConfidence = 0.0;
        double avgPositionAccuracy = 0.0, avgValidPositionAccuracy = 0.0,
                avgInvalidPositionAccuracy = 0.0, avgPositionAccuracyConfidence = 0.0;
        for (int t = 0; t < TIMES; t++) {
            int numSources = randomizer.nextInt(MIN_SOURCES, MAX_SOURCES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);


            List<WifiAccessPointWithPowerAndLocated3D> sources = new ArrayList<>();
            List<Reading<WifiAccessPoint>> readings = new ArrayList<>();
            double[] qualityScores = new double[numSources];
            double error;
            for (int i = 0; i < numSources; i++) {
                InhomogeneousPoint3D accessPointPosition = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
                double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
                String bssid = String.valueOf(i);

                WifiAccessPointWithPowerAndLocated3D locatedAccessPoint =
                        new WifiAccessPointWithPowerAndLocated3D(bssid,
                                FREQUENCY, transmittedPowerdBm,
                                Math.sqrt(TX_POWER_VARIANCE),
                                pathLossExponent,
                                Math.sqrt(PATHLOSS_EXPONENT_VARIANCE),
                                accessPointPosition);
                sources.add(locatedAccessPoint);

                WifiAccessPoint accessPoint = new WifiAccessPoint(bssid, FREQUENCY);

                double distance = position.distanceTo(accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(transmittedPower,
                        distance, FREQUENCY, pathLossExponent));

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                error += inlierErrorRandomizer.nextDouble();

                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));

                readings.add(new RssiReading<>(accessPoint, rssi + error,
                        Math.sqrt(RX_POWER_VARIANCE)));
                readings.add(new RangingReading<>(accessPoint,
                        Math.max(0.0, distance + error),
                        RANGING_STD));
                readings.add(new RangingAndRssiReading<>(accessPoint,
                        Math.max(0.0, distance + error), rssi + error,
                        RANGING_STD, Math.sqrt(RX_POWER_VARIANCE)));
            }

            Fingerprint<WifiAccessPoint, Reading<WifiAccessPoint>> fingerprint =
                    new Fingerprint<>(readings);


            PROMedSRobustPositionEstimator3D estimator =
                    new PROMedSRobustPositionEstimator3D(qualityScores, sources,
                            fingerprint, this);
            estimator.setResultRefined(true);

            reset();

            //check initial state
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertNull(estimator.getCovariance());
            assertNotNull(estimator.getPositions());
            assertNotNull(estimator.getDistances());
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);

            Point3D p = estimator.estimate();

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            Point3D estimatedPosition = estimator.getEstimatedPosition();
            assertSame(estimatedPosition, p);

            double positionStd = 0.0, positionStdConfidence = 0.0,
                    positionAccuracy = 0.0, positionAccuracyConfidence = 0.0;
            boolean hasCovariance = false;
            if (estimator.getInliersData() != null && estimator.getCovariance() != null) {
                assertNotNull(estimator.getInliersData());
                assertNotNull(estimator.getCovariance());

                Accuracy3D accuracyStd = new Accuracy3D(estimator.getCovariance());
                accuracyStd.setStandardDeviationFactor(1.0);

                Accuracy3D accuracy = new Accuracy3D(estimator.getCovariance());
                accuracy.setConfidence(0.99);

                positionStd = accuracyStd.getAverageAccuracy();
                positionStdConfidence = accuracyStd.getConfidence();
                positionAccuracy = accuracy.getAverageAccuracy();
                positionAccuracyConfidence = accuracy.getConfidence();

                num++;
                hasCovariance = true;
            }

            double positionDistance = position.distanceTo(estimatedPosition);
            if (positionDistance <= LARGE_ABSOLUTE_ERROR) {
                assertTrue(position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR));
                numValidPosition++;

                avgValidPositionError += positionDistance;

                if (hasCovariance) {
                    avgValidPositionStd += positionStd;
                    avgValidPositionAccuracy += positionAccuracy;
                    numValidCovariance++;
                }
            } else {
                avgInvalidPositionError += positionDistance;

                if (hasCovariance) {
                    avgInvalidPositionStd += positionStd;
                    avgInvalidPositionAccuracy += positionAccuracy;
                }
            }

            avgPositionError += positionDistance;

            if (hasCovariance) {
                avgPositionStd += positionStd;
                avgPositionStdConfidence += positionStdConfidence;
                avgPositionAccuracy += positionAccuracy;
                avgPositionAccuracyConfidence += positionAccuracyConfidence;
            }
        }

        assertTrue(numValidPosition > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPositionStd /= numValidCovariance;
        avgInvalidPositionStd /= (num - numValidPosition);
        avgPositionStd /= num;
        avgPositionStdConfidence /= TIMES;

        avgValidPositionAccuracy /= numValidPosition;
        avgInvalidPositionAccuracy /= (num - numValidPosition);
        avgPositionAccuracy /= TIMES;
        avgPositionAccuracyConfidence /= TIMES;


        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        NumberFormat format = NumberFormat.getPercentInstance();
        String formattedConfidence = format.format(avgPositionStdConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position standard deviation {0} meters ({1} confidence)",
                avgValidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position standard deviation {0} meters ({1} confidence)",
                avgInvalidPositionStd, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position standard deviation {0} meters ({1} confidence)",
                avgPositionStd, formattedConfidence));

        formattedConfidence = format.format(avgPositionAccuracyConfidence);
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Valid position accuracy {0} meters ({1} confidence)",
                avgValidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Invalid position accuracy {0} meters ({1} confidence)",
                avgInvalidPositionAccuracy, formattedConfidence));
        LOGGER.log(Level.INFO, MessageFormat.format(
                "Position accuracy {0} meters ({1} confidence)",
                avgPositionAccuracy, formattedConfidence));

        //force NotReadyException
        PROMedSRobustPositionEstimator3D estimator =
                new PROMedSRobustPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Override
    public void onEstimateStart(RobustPositionEstimator<Point3D> estimator) {
        estimateStart++;
        checkLocked((PROMedSRobustPositionEstimator3D)estimator);
    }

    @Override
    public void onEstimateEnd(RobustPositionEstimator<Point3D> estimator) {
        estimateEnd++;
        checkLocked((PROMedSRobustPositionEstimator3D)estimator);
    }

    @Override
    public void onEstimateNextIteration(RobustPositionEstimator<Point3D> estimator,
                                        int iteration) {
        estimateNextIteration++;
        checkLocked((PROMedSRobustPositionEstimator3D)estimator);
    }

    @Override
    public void onEstimateProgressChange(RobustPositionEstimator<Point3D> estimator,
                                         float progress) {
        estimateProgressChange++;
        checkLocked((PROMedSRobustPositionEstimator3D)estimator);
    }

    private void reset() {
        estimateStart = estimateEnd = estimateNextIteration =
                estimateProgressChange = 0;
    }

    @SuppressWarnings("all")
    private double receivedPower(double equivalentTransmittedPower,
                                 double distance, double frequency, double pathLossExponent) {
        //Pr = Pt*Gt*Gr*lambda^2/(4*pi*d)^2,    where Pr is the received power
        // lambda = c/f, where lambda is wavelength,
        // Pte = Pt*Gt*Gr, is the equivalent transmitted power, Gt is the transmitted Gain and Gr is the received Gain
        //Pr = Pte*c^2/((4*pi*f)^2 * d^2)
        double k = Math.pow(SPEED_OF_LIGHT / (4.0 * Math.PI * frequency), pathLossExponent);
        return equivalentTransmittedPower * k /
                Math.pow(distance, pathLossExponent);
    }

    private void checkLocked(PROMedSRobustPositionEstimator3D estimator) {
        try {
            estimator.setQualityScores(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setRadioSourcePositionCovarianceUsed(true);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setFallbackDistanceStandardDeviation(1.0);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setProgressDelta(0.5f);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setConfidence(0.8);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setMaxIterations(100);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setResultRefined(true);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setCovarianceKept(true);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setStopThreshold(1.0);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setSources(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setFingerprint(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setListener(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.estimate();
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        } catch (Exception e) {
            fail("LockedException expected but not thrown");
        }
    }
}
