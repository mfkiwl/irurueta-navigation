/*
 * Copyright (C) 2020 Alberto Irurueta Carro (alberto@irurueta.com)
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
package com.irurueta.navigation.inertial.calibration.accelerometer;

import com.irurueta.algebra.Matrix;
import com.irurueta.algebra.WrongSizeException;
import com.irurueta.navigation.inertial.calibration.StandardDeviationBodyKinematics;
import com.irurueta.numerical.robust.RobustEstimatorMethod;
import com.irurueta.statistics.UniformRandomizer;
import com.irurueta.units.Acceleration;
import com.irurueta.units.AccelerationUnit;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.junit.Assert.assertSame;

public class RobustKnownBiasAndGravityNormAccelerometerCalibratorTest implements
        RobustKnownBiasAndGravityNormAccelerometerCalibratorListener {

    @Test
    public void testCreate1() {
        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
    }

    @Test
    public void testCreate2() {
        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate3() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        measurements, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertSame(measurements, calibrator.getMeasurements());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                measurements, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertSame(measurements, calibrator.getMeasurements());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                measurements, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertSame(measurements, calibrator.getMeasurements());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                measurements, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(measurements, calibrator.getMeasurements());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                measurements, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreate4() {
        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        true, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertTrue(calibrator.isCommonAxisUsed());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                true, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertTrue(calibrator.isCommonAxisUsed());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                true, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                true, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                true, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreate5() {
        final double[] initialBias = new double[3];
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertArrayEquals(initialBias, calibrator.getBias(), 0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertArrayEquals(initialBias, calibrator.getBias(), 0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertArrayEquals(initialBias, calibrator.getBias(), 0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertArrayEquals(initialBias, calibrator.getBias(), 0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertArrayEquals(initialBias, calibrator.getBias(), 0.0);
    }

    @Test
    public void testCreate6() throws WrongSizeException {
        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate7() throws WrongSizeException {
        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithGaussianRandomValues(
                3, 3, -1.0, 1.0);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        initialBias, initialMa,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, initialMa, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, initialMa, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, initialMa, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                initialBias, initialMa, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate8() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
    }

    @Test
    public void testCreate9() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreate10() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate11() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreate12() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate13() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate14() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate15() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate16() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate17() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate18() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate19() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate20() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate21() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate22() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate23() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate24() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate25() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
    }

    @Test
    public void testCreate26() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreate27() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate28() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreate29() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate30() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate31() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate32() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate33() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate34() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate35() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate36() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate37() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);


        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true, initialBias,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate38() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate39() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, initialBias, initialMa,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate40() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate41() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                gravityNorm, measurements, true,
                initialBias, initialMa, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate42() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreate43() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate44() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreate45() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate46() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate47() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate48() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate49() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate50() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate51() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate52() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, true,
                initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate53() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate54() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, initialMa,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate55() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, initialMa, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate56() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, initialMa,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate57() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, initialMa,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate58() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
    }

    @Test
    public void testCreate59() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreate60() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate61() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreate62() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate63() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate64() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate65() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreate66() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate67() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate68() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate69() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreate70() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate71() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, initialMa,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate72() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        initialBias, initialMa, this,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements, initialBias,
                initialMa, this, RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreate73() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, initialMa,
                        RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreate74() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final double[] qualityScores = new double[13];

        // RANSAC
        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        qualityScores, gravityNorm, measurements,
                        true, initialBias, initialMa,
                        this, RobustEstimatorMethod.RANSAC);

        // check
        assertTrue(calibrator instanceof RANSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.RANSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // LMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa, this,
                RobustEstimatorMethod.LMedS);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // MSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa, this,
                RobustEstimatorMethod.MSAC);

        // check
        assertTrue(calibrator instanceof MSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.MSAC, calibrator.getMethod());
        assertNull(calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROSAC
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa, this,
                RobustEstimatorMethod.PROSAC);

        // check
        assertTrue(calibrator instanceof PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROSAC, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());

        // PROMedS
        calibrator = RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                qualityScores, gravityNorm, measurements,
                true, initialBias, initialMa, this,
                RobustEstimatorMethod.PROMedS);

        // check
        assertTrue(calibrator instanceof PROMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.PROMedS, calibrator.getMethod());
        assertSame(qualityScores, calibrator.getQualityScores());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault1() {
        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create();

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
    }

    @Test
    public void testCreateDefault2() {
        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        this);

        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault3() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        measurements);

        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreateDefault4() {
        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        true);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreateDefault5() {
        final double[] initialBias = new double[3];
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertArrayEquals(initialBias, calibrator.getBias(), 0.0);
    }

    @Test
    public void testCreateDefault6() throws WrongSizeException {
        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreateDefault7() throws WrongSizeException {
        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithGaussianRandomValues(
                3, 3, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        initialBias, initialMa);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreateDefault8() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
    }

    @Test
    public void testCreateDefault9() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreateDefault10() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault11() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreateDefault12() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault13() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreateDefault14() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault15() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreateDefault16() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault17() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreateDefault18() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault19() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreateDefault20() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault21() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreateDefault22() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);


        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault23() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreateDefault24() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double gravityNorm = randomizer.nextDouble();

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNorm(), 0.0);
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault25() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
    }

    @Test
    public void testCreateDefault26() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
    }

    @Test
    public void testCreateDefault27() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault28() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testCreateDefault29() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault30() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreateDefault31() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault32() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
    }

    @Test
    public void testCreateDefault33() {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
        final double[] initialBias = new double[3];
        randomizer.fill(initialBias);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertArrayEquals(initialBias, calibrator.getBias(),
                0.0);
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault34() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias);
        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreateDefault35() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault36() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
    }

    @Test
    public void testCreateDefault37() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);


        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault38() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreateDefault39() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, initialBias,
                        initialMa, this);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }

    @Test
    public void testCreateDefault40() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa);

        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
    }

    @Test
    public void testCreateDefault41() throws WrongSizeException {
        final List<StandardDeviationBodyKinematics> measurements =
                Collections.emptyList();

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final Acceleration gravityNorm = new Acceleration(
                randomizer.nextDouble(),
                AccelerationUnit.METERS_PER_SQUARED_SECOND);

        final Matrix initialBias = Matrix.createWithUniformRandomValues(
                3, 1, -1.0, 1.0);
        final Matrix initialMa = Matrix.createWithUniformRandomValues(
                3, 3, -1.0, 1.0);

        final RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator =
                RobustKnownBiasAndGravityNormAccelerometerCalibrator.create(
                        gravityNorm, measurements, true,
                        initialBias, initialMa, this);
        // check
        assertTrue(calibrator instanceof LMedSRobustKnownBiasAndGravityNormAccelerometerCalibrator);
        assertEquals(RobustEstimatorMethod.LMedS, calibrator.getMethod());
        assertEquals(gravityNorm, calibrator.getGroundTruthGravityNormAsAcceleration());
        assertSame(measurements, calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertEquals(initialBias, calibrator.getBiasAsMatrix());
        assertEquals(initialMa, calibrator.getInitialMa());
        assertSame(this, calibrator.getListener());
    }
    
    @Override
    public void onCalibrateStart(RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator) {

    }

    @Override
    public void onCalibrateEnd(RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator) {

    }

    @Override
    public void onCalibrateNextIteration(RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator, int iteration) {

    }

    @Override
    public void onCalibrateProgressChange(RobustKnownBiasAndGravityNormAccelerometerCalibrator calibrator, float progress) {

    }
}
