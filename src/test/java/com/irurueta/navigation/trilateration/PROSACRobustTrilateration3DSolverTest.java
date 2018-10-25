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
package com.irurueta.navigation.trilateration;

import com.irurueta.geometry.InhomogeneousPoint3D;
import com.irurueta.geometry.Point3D;
import com.irurueta.geometry.Sphere;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.numerical.robust.RobustEstimatorMethod;
import com.irurueta.statistics.GaussianRandomizer;
import com.irurueta.statistics.UniformRandomizer;
import org.junit.*;

import java.util.Random;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
public class PROSACRobustTrilateration3DSolverTest implements
        RobustTrilaterationSolverListener<Point3D> {

    private static final int MIN_SPHERES = 100;
    private static final int MAX_SPHERES = 500;

    private static final double MIN_RANDOM_VALUE = -50.0;
    private static final double MAX_RANDOM_VALUE = 50.0;

    private static final double MIN_DISTANCE_ERROR = -1e-2;
    private static final double MAX_DISTANCE_ERROR = 1e-2;

    private static final double ABSOLUTE_ERROR = 1e-6;
    private static final double LARGE_ABSOLUTE_ERROR = 1e-2;

    private static final int TIMES = 50;

    private static final int PERCENTAGE_OUTLIERS = 20;

    private static final double STD_OUTLIER_ERROR = 10.0;

    private int solveStart;
    private int solveEnd;
    private int solveNextIteration;
    private int solveProgressChange;

    public PROSACRobustTrilateration3DSolverTest() { }

    @BeforeClass
    public static void setUpClass() { }

    @AfterClass
    public static void tearDownClass() { }

    @Before
    public void setUp() { }

    @After
    public void tearDown() { }

    @Test
    @SuppressWarnings("all")
    public void testConstructor() {
        //empty constructor
        PROSACRobustTrilateration3DSolver solver = new PROSACRobustTrilateration3DSolver();

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(solver.getPositions());
        assertNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());


        //constructor with listener
        solver = new PROSACRobustTrilateration3DSolver(this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(solver.getPositions());
        assertNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());


        //constructor with positions and distances
        Point3D[] positions = new Point3D[4];
        positions[0] = new InhomogeneousPoint3D();
        positions[1] = new InhomogeneousPoint3D();
        positions[2] = new InhomogeneousPoint3D();
        positions[3] = new InhomogeneousPoint3D();
        double[] distances = new double[4];
        solver = new PROSACRobustTrilateration3DSolver(positions, distances);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        double[] wrong = new double[5];
        Point3D[] shortPositions = new Point3D[1];
        double[] shortDistances = new double[1];
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((Point3D[])null, distances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortPositions, shortDistances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with positions, distances and standard deviations
        double[] standardDeviations = new double[4];
        solver = new PROSACRobustTrilateration3DSolver(positions, distances,
                standardDeviations);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertSame(solver.getDistanceStandardDeviations(), standardDeviations);
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null, distances,
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, null,
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, distances,
                    (double[]) null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, wrong,
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, distances,
                    wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortPositions,
                    shortDistances, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with positions, distances, standard deviations and listener
        solver = new PROSACRobustTrilateration3DSolver(positions, distances,
                standardDeviations, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertSame(solver.getDistanceStandardDeviations(), standardDeviations);
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null, distances,
                    standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, null,
                    standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, distances,
                    null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, wrong,
                    standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, distances,
                    wrong, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortPositions,
                    shortDistances, standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with positions, distances and listener
        solver = new PROSACRobustTrilateration3DSolver(positions, distances,
                this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((Point3D[])null, distances,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, null,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(positions, wrong,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortPositions,
                    shortDistances, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with spheres
        Sphere[] spheres = new Sphere[4];
        spheres[0] = new Sphere(positions[0], distances[0]);
        spheres[1] = new Sphere(positions[1], distances[1]);
        spheres[2] = new Sphere(positions[2], distances[2]);
        spheres[3] = new Sphere(positions[3], distances[3]);
        solver = new PROSACRobustTrilateration3DSolver(spheres);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        Sphere[] shortSpheres = new Sphere[1];

        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((Sphere[])null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortSpheres);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with spheres and standard deviations
        solver = new PROSACRobustTrilateration3DSolver(spheres,
                standardDeviations);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((Sphere[])null,
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(spheres,
                    (double[]) null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortSpheres,
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(spheres, wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with spheres and listener
        solver = new PROSACRobustTrilateration3DSolver(spheres, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((Sphere[])null,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortSpheres,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with spheres, standard deviation and listener
        solver = new PROSACRobustTrilateration3DSolver(spheres,
                standardDeviations, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration2DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration2DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration2DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);
        assertFalse(solver.isReady());
        assertNull(solver.getQualityScores());
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((Sphere[])null,
                    standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(spheres,
                    null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortSpheres,
                    standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(spheres, wrong,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores
        double[] qualityscores = new double[4];
        solver = new PROSACRobustTrilateration3DSolver(qualityscores);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(solver.getPositions());
        assertNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((double[])null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(new double[2]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores and listener
        solver = new PROSACRobustTrilateration3DSolver(qualityscores, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNull(solver.getPositions());
        assertNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertFalse(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver((double[])null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(new double[2], this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores, positions and distances
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                positions, distances);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertNull(solver.getDistanceStandardDeviations());
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        double[] shortQualityScores = new double[1];

        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null, positions,
                    distances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    (Point3D[])null, distances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(
                    shortQualityScores, positions, distances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortPositions, shortDistances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores, positions and distances
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                positions, distances, standardDeviations);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null, positions,
                    distances, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    null, distances, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, null, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, distances, (double[])null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, wrong, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, distances, wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(
                    shortQualityScores, positions, distances,
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortPositions, shortDistances, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores, positions, distances,
        // standard deviations and listener
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                positions, distances, standardDeviations, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null, positions,
                    distances, standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    null, distances, standardDeviations,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, null, standardDeviations,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, distances, null,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, wrong, standardDeviations,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, distances, wrong, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(
                    shortQualityScores, positions, distances,
                    standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortPositions, shortDistances, standardDeviations,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores, positions, distances
        //and listener
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                positions, distances, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertNull(solver.getDistanceStandardDeviations());
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null, positions,
                    distances, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    (Point3D[])null, distances, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    positions, wrong, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(
                    shortQualityScores, positions, distances,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortPositions, shortDistances, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores and spheres
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                spheres);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null,
                    spheres);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    (Sphere[])null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortQualityScores,
                    spheres);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortSpheres);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores, circles and standard deviations
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                spheres, standardDeviations);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertNull(solver.getListener());
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null,
                    spheres, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    (Sphere[])null, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    spheres, (double[])null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortQualityScores,
                    spheres, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortSpheres, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    spheres, wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores, circles and listener
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                spheres, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertNull(solver.getDistanceStandardDeviations());
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null,
                    spheres, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortQualityScores,
                    spheres, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortSpheres, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);


        //constructor with quality scores, spheres, standard deviations
        //and listener
        solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                spheres, standardDeviations, this);

        //check correctness
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(solver.getMethod(), RobustEstimatorMethod.PROSAC);
        assertEquals(solver.getNumberOfDimensions(), 3);
        assertEquals(solver.getMinRequiredPositionsAndDistances(), 4);
        assertNotNull(solver.getSpheres());
        assertSame(solver.getListener(), this);
        assertFalse(solver.isLocked());
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);
        assertNull(solver.getInliersData());
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);
        assertNotNull(solver.getPositions());
        assertNotNull(solver.getDistances());
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);
        assertTrue(solver.isReady());
        assertSame(solver.getQualityScores(), qualityscores);
        assertNull(solver.getCovariance());
        assertNull(solver.getEstimatedPosition());

        //force IllegalArgumentException
        solver = null;
        try {
            solver = new PROSACRobustTrilateration3DSolver(null,
                    spheres, standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    (Sphere[])null, standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    spheres, null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(shortQualityScores,
                    spheres, standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    shortSpheres, standardDeviations, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver = new PROSACRobustTrilateration3DSolver(qualityscores,
                    spheres, wrong, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(solver);
    }

    @Test
    public void testGetSetThreshold() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check initial value
        assertEquals(solver.getThreshold(),
                PROSACRobustTrilateration3DSolver.DEFAULT_THRESHOLD, 0.0);

        //set new value
        solver.setThreshold(1.0);

        //check
        assertEquals(solver.getThreshold(), 1.0, 0.0);

        //force IllegalArgumentException
        try {
            solver.setThreshold(0.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testIsSetComputeAndKeepInliersEnabled() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check initial value
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);

        //set new value
        solver.setComputeAndKeepInliersEnabled(
                !PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);

        //check
        assertEquals(solver.isComputeAndKeepInliersEnabled(),
                !PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
    }

    @Test
    public void testIsSetComputeAndKeepResidualsEnabled() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check initial value
        assertEquals(solver.isComputeAndKeepResiduals(),
                PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);

        //set new value
        solver.setComputeAndKeepResidualsEnabled(
                !PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);

        //check
        assertEquals(solver.isComputeAndKeepResiduals(),
                !PROSACRobustTrilateration3DSolver.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
    }

    @Test
    public void testGetSetSpheres() throws LockedException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check initial value
        assertNull(solver.getSpheres());

        //set new value
        Point3D[] positions = new Point3D[4];
        positions[0] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[1] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[2] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[3] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        double[] distances = new double[4];
        distances[0] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[1] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[2] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[3] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);

        Sphere[] spheres = new Sphere[4];
        spheres[0] = new Sphere(positions[0], distances[0]);
        spheres[1] = new Sphere(positions[1], distances[1]);
        spheres[2] = new Sphere(positions[2], distances[2]);
        spheres[3] = new Sphere(positions[3], distances[3]);
        solver.setSpheres(spheres);

        //check
        Sphere[] spheres2 = solver.getSpheres();
        for (int i = 0; i < 4; i++) {
            assertSame(spheres[i].getCenter(), spheres2[i].getCenter());
            assertEquals(spheres[i].getRadius(), spheres2[i].getRadius(), 0.0);
        }

        //force IllegalArgumentException
        try {
            solver.setSpheres(null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setSpheres(new Sphere[1]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetSpheresAndStandardDeviations() throws LockedException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check initial value
        assertNull(solver.getSpheres());

        //set new value
        Point3D[] positions = new Point3D[4];
        positions[0] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[1] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[2] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[3] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        double[] distances = new double[4];
        distances[0] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[1] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[2] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[3] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        double[] standardDeviations = new double[4];
        standardDeviations[0] = randomizer.nextDouble();
        standardDeviations[1] = randomizer.nextDouble();
        standardDeviations[2] = randomizer.nextDouble();
        standardDeviations[3] = randomizer.nextDouble();

        Sphere[] spheres = new Sphere[4];
        spheres[0] = new Sphere(positions[0], distances[0]);
        spheres[1] = new Sphere(positions[1], distances[1]);
        spheres[2] = new Sphere(positions[2], distances[2]);
        spheres[3] = new Sphere(positions[3], distances[3]);
        solver.setSpheresAndStandardDeviations(spheres, standardDeviations);

        //check
        Sphere[] spheres2 = solver.getSpheres();
        for (int i = 0; i < 4; i++) {
            assertSame(spheres[i].getCenter(), spheres2[i].getCenter());
            assertEquals(spheres[i].getRadius(), spheres2[i].getRadius(), 0.0);
        }
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);

        //force IllegalArgumentException
        try {
            solver.setSpheresAndStandardDeviations(null,
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setSpheresAndStandardDeviations(spheres,
                    null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setSpheresAndStandardDeviations(new Sphere[1],
                    standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setSpheresAndStandardDeviations(spheres,
                    new double[1]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetListener() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertNull(solver.getListener());

        //set new value
        solver.setListener(this);

        //check
        assertSame(solver.getListener(), this);
    }

    @Test
    public void testGetSetProgressDelta() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertEquals(solver.getProgressDelta(),
                RobustTrilaterationSolver.DEFAULT_PROGRESS_DELTA, 0.0);

        //set new value
        solver.setProgressDelta(0.5f);

        //check
        assertEquals(solver.getProgressDelta(), 0.5f, 0.0);

        //force IllegalArgumentException
        try {
            solver.setProgressDelta(-1.0f);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setProgressDelta(2.0f);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetConfidence() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertEquals(solver.getConfidence(),
                RobustTrilaterationSolver.DEFAULT_CONFIDENCE, 0.0);

        //set new value
        solver.setConfidence(0.8);

        //check
        assertEquals(solver.getConfidence(), 0.8, 0.0);

        //force IllegalArgumentException
        try {
            solver.setConfidence(-1.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setConfidence(2.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetMaxIterations() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertEquals(solver.getMaxIterations(),
                RobustTrilaterationSolver.DEFAULT_MAX_ITERATIONS);

        //set new value
        solver.setMaxIterations(10);

        //check
        assertEquals(solver.getMaxIterations(), 10);

        //force IllegalArgumentException
        try {
            solver.setMaxIterations(0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testIsSetResultRefined() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertEquals(solver.isResultRefined(),
                RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);

        //set new value
        solver.setResultRefined(
                !RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);

        //check
        assertEquals(solver.isResultRefined(),
                !RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
    }

    @Test
    public void testIsSetCovarianceKept() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertEquals(solver.isCovarianceKept(),
                RobustTrilaterationSolver.DEFAULT_KEEP_COVARIANCE);

        //set new value
        solver.setCovarianceKept(
                !RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);

        //check
        assertEquals(solver.isCovarianceKept(),
                !RobustTrilaterationSolver.DEFAULT_REFINE_RESULT);
    }

    @Test
    public void testGetSetQualityScores() throws LockedException {
        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertNull(solver.getQualityScores());

        //set new value
        double[] qualityScores = new double[4];
        solver.setQualityScores(qualityScores);

        //check
        assertSame(solver.getQualityScores(), qualityScores);
    }

    @Test
    public void testGetSetPositionsAndDistances() throws LockedException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertNull(solver.getPositions());
        assertNull(solver.getDistances());

        //set new values
        Point3D[] positions = new Point3D[4];
        positions[0] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[1] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[2] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[3] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        double[] distances = new double[4];
        distances[0] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[1] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[2] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[3] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);

        solver.setPositionsAndDistances(positions, distances);

        //check
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);

        //force IllegalArgumentException
        double[] wrong = new double[5];
        Point3D[] shortPositions = new Point3D[1];
        double[] shortDistances = new double[1];
        try {
            solver.setPositionsAndDistances(null, distances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsAndDistances(positions, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsAndDistances(positions, wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsAndDistances(shortPositions, shortDistances);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetPositionsDistancesAndStandardDeviations() throws LockedException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        PROSACRobustTrilateration3DSolver solver =
                new PROSACRobustTrilateration3DSolver();

        //check default value
        assertNull(solver.getPositions());
        assertNull(solver.getDistances());

        //set new values
        Point3D[] positions = new Point3D[4];
        positions[0] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[1] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[2] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        positions[3] = new InhomogeneousPoint3D(randomizer.nextDouble(),
                randomizer.nextDouble(), randomizer.nextDouble());
        double[] distances = new double[4];
        distances[0] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[1] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[2] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        distances[3] = randomizer.nextDouble(1.0, MAX_RANDOM_VALUE);
        double[] standardDeviations = new double[4];
        standardDeviations[0] = randomizer.nextDouble();
        standardDeviations[1] = randomizer.nextDouble();
        standardDeviations[2] = randomizer.nextDouble();
        standardDeviations[3] = randomizer.nextDouble();

        solver.setPositionsDistancesAndStandardDeviations(
                positions, distances, standardDeviations);

        //check
        assertSame(solver.getPositions(), positions);
        assertSame(solver.getDistances(), distances);
        assertSame(solver.getDistanceStandardDeviations(),
                standardDeviations);

        //force IllegalArgumentException
        double[] wrong = new double[5];
        Point3D[] shortPositions = new Point3D[1];
        double[] shortDistances = new double[1];
        double[] shortStandardDeviations = new double[1];
        try {
            solver.setPositionsDistancesAndStandardDeviations(
                    null, distances, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsDistancesAndStandardDeviations(
                    positions, null, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsDistancesAndStandardDeviations(
                    positions, distances, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsDistancesAndStandardDeviations(
                    positions, wrong, standardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsDistancesAndStandardDeviations(
                    positions, distances, wrong);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            solver.setPositionsDistancesAndStandardDeviations(
                    shortPositions, shortDistances, shortStandardDeviations);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testSolveNoInlierErrorNoRefinementNoInlierDataAndNoResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores, spheres, this);
            solver.setResultRefined(false);
            solver.setComputeAndKeepInliersEnabled(false);
            solver.setComputeAndKeepResidualsEnabled(false);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
            assertNull(solver.getCovariance());
            assertNull(solver.getInliersData());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveNoInlierErrorNoRefinementNoInlierDataAndWithResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, this);
            solver.setResultRefined(false);
            solver.setComputeAndKeepInliersEnabled(false);
            solver.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
            assertNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNull(solver.getInliersData().getInliers());
            assertNotNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveNoInlierErrorNoRefinementWithInlierDataAndNoResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, this);
            solver.setResultRefined(false);
            solver.setComputeAndKeepInliersEnabled(true);
            solver.setComputeAndKeepResidualsEnabled(false);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
            assertNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveNoInlierErrorNoRefinementWithInlierDataAndWithResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, this);
            solver.setResultRefined(false);
            solver.setComputeAndKeepInliersEnabled(true);
            solver.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
            assertNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNotNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveNoInlierErrorWithRefinementNoInlierDataAndNoResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, this);
            solver.setResultRefined(true);
            solver.setComputeAndKeepInliersEnabled(false);
            solver.setComputeAndKeepResidualsEnabled(false);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
            assertNotNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNotNull(solver.getInliersData().getInliers());
            assertNotNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveNoInlierErrorWithRefinementNoInlierDataAndWithResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, this);
            solver.setResultRefined(true);
            solver.setComputeAndKeepInliersEnabled(false);
            solver.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
            assertNotNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNotNull(solver.getInliersData().getInliers());
            assertNotNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveNoInlierErrorWithRefinementWithInlierDataAndWithResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, this);
            solver.setResultRefined(true);
            solver.setComputeAndKeepInliersEnabled(true);
            solver.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, ABSOLUTE_ERROR));
            assertNotNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNotNull(solver.getInliersData().getInliers());
            assertNotNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveWithInlierErrorWithRefinementWithInlierDataAndWithResiduals() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }
                qualityScores[i] = 1.0 / (1.0 + error);
                error += randomizer.nextDouble(MIN_DISTANCE_ERROR, MAX_DISTANCE_ERROR);
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }

            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, this);
            solver.setResultRefined(true);
            solver.setComputeAndKeepInliersEnabled(true);
            solver.setComputeAndKeepResidualsEnabled(true);
            solver.setCovarianceKept(true);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR));
            assertNotNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNotNull(solver.getInliersData().getInliers());
            assertNotNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testSolveWithInlierErrorWithRefinementWithInlierDataWithResidualsAndStandardDeviations() throws Exception {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            int numSpheres = randomizer.nextInt(MIN_SPHERES, MAX_SPHERES);

            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                    randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
            InhomogeneousPoint3D center;
            double radius, error;
            Sphere[] spheres = new Sphere[numSpheres];
            double[] standardDeviations = new double[numSpheres];
            double[] qualityScores = new double[numSpheres];
            for (int i = 0; i < numSpheres; i++) {
                center = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE),
                        randomizer.nextDouble(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE));
                radius = center.distanceTo(position);

                if(randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                    standardDeviations[i] = STD_OUTLIER_ERROR;
                } else {
                    //inlier
                    error = 0.0;
                    standardDeviations[i] = 0.0;
                }
                //add variance of uniform distribution containing inlier error
                standardDeviations[i] += Math.pow(MAX_DISTANCE_ERROR - MIN_DISTANCE_ERROR, 2.0) / 12.0;
                standardDeviations[i] = Math.sqrt(standardDeviations[i]);
                //add inlier error
                error += randomizer.nextDouble(MIN_DISTANCE_ERROR, MAX_DISTANCE_ERROR);
                qualityScores[i] = 1.0 / (1.0 + Math.abs(error));
                radius = Math.max(RobustTrilaterationSolver.EPSILON,
                        radius + error);
                spheres[i] = new Sphere(center, radius);
            }


            PROSACRobustTrilateration3DSolver solver =
                    new PROSACRobustTrilateration3DSolver(qualityScores,
                            spheres, standardDeviations, this);
            solver.setResultRefined(true);
            solver.setComputeAndKeepInliersEnabled(true);
            solver.setComputeAndKeepResidualsEnabled(true);
            solver.setCovarianceKept(true);

            reset();
            assertEquals(solveStart, 0);
            assertEquals(solveEnd, 0);
            assertEquals(solveNextIteration, 0);
            assertEquals(solveProgressChange, 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());
            assertNull(solver.getEstimatedPosition());

            Point3D estimatedPosition = solver.solve();

            //check
            if (!position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(position.equals(estimatedPosition, LARGE_ABSOLUTE_ERROR));
            assertNotNull(solver.getCovariance());
            assertNotNull(solver.getInliersData());
            assertNotNull(solver.getInliersData().getInliers());
            assertNotNull(solver.getInliersData().getResiduals());

            assertEquals(solveStart, 1);
            assertEquals(solveEnd, 1);
            assertTrue(solveNextIteration > 0);
            assertTrue(solveProgressChange >= 0);
            assertTrue(solver.isReady());
            assertFalse(solver.isLocked());

            //force NotReadyException
            solver = new PROSACRobustTrilateration3DSolver();

            try {
                solver.solve();
                fail("LockedException expected but not thrown");
            } catch (NotReadyException ignore) { }

            numValid++;

            break;
        }

        assertTrue(numValid > 0);
    }

    @Override
    public void onSolveStart(RobustTrilaterationSolver<Point3D> solver) {
        solveStart++;
        checkLocked((PROSACRobustTrilateration3DSolver)solver);
    }

    @Override
    public void onSolveEnd(RobustTrilaterationSolver<Point3D> solver) {
        solveEnd++;
        checkLocked((PROSACRobustTrilateration3DSolver)solver);
    }

    @Override
    public void onSolveNextIteration(RobustTrilaterationSolver<Point3D> solver, int iteration) {
        solveNextIteration++;
        checkLocked((PROSACRobustTrilateration3DSolver)solver);
    }

    @Override
    public void onSolveProgressChange(RobustTrilaterationSolver<Point3D> solver, float progress) {
        solveProgressChange++;
        checkLocked((PROSACRobustTrilateration3DSolver)solver);
    }

    private void reset() {
        solveStart = solveEnd = solveNextIteration =
                solveProgressChange = 0;
    }

    private void checkLocked(PROSACRobustTrilateration3DSolver solver) {
        try {
            solver.setListener(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setProgressDelta(0.5f);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setConfidence(0.5);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setMaxIterations(5);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setResultRefined(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setCovarianceKept(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setPositionsAndDistances(null, null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setPositionsDistancesAndStandardDeviations(
                    null, null, null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }

        try {
            solver.setSpheres(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setSpheresAndStandardDeviations(null, null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setThreshold(0.5);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setQualityScores(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setComputeAndKeepInliersEnabled(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.setComputeAndKeepResidualsEnabled(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        }
        try {
            solver.solve();
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        } catch (Exception ignore) {
            fail("LockedException expected but not thrown");
        }
    }
}
