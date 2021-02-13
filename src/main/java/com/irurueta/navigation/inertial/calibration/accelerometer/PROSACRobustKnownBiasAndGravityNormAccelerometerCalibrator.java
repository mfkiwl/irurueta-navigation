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
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.navigation.inertial.calibration.CalibrationException;
import com.irurueta.navigation.inertial.calibration.StandardDeviationBodyKinematics;
import com.irurueta.numerical.robust.PROSACRobustEstimator;
import com.irurueta.numerical.robust.PROSACRobustEstimatorListener;
import com.irurueta.numerical.robust.RobustEstimator;
import com.irurueta.numerical.robust.RobustEstimatorException;
import com.irurueta.numerical.robust.RobustEstimatorMethod;
import com.irurueta.units.Acceleration;

import java.util.List;

/**
 * Robustly estimates accelerometer cross couplings and scaling factors
 * using a PROSAC algorithm to discard outliers.
 * <p>
 * To use this calibrator at least 7 measurements taken at a single position
 * where gravity norm is known must be taken at 7 different unknown
 * orientations and zero velocity when common z-axis
 * is assumed, otherwise at least 10 measurements are required.
 * <p>
 * Measured specific force is assumed to follow the model shown below:
 * <pre>
 *     fmeas = ba + (I + Ma) * ftrue + w
 * </pre>
 * Where:
 * - fmeas is the measured specific force. This is a 3x1 vector.
 * - ba is accelerometer bias. Ideally, on a perfect accelerometer, this should be a
 * 3x1 zero vector.
 * - I is the 3x3 identity matrix.
 * - Ma is the 3x3 matrix containing cross-couplings and scaling factors. Ideally, on
 * a perfect accelerometer, this should be a 3x3 zero matrix.
 * - ftrue is ground-trush specific force.
 * - w is measurement noise.
 */
public class PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator extends
        RobustKnownBiasAndGravityNormAccelerometerCalibrator {

    /**
     * Constant defining default threshold to determine whether samples are inliers or not.
     */
    public static final double DEFAULT_THRESHOLD = 1e-2;

    /**
     * Minimum value that can be set as threshold.
     * Threshold must be strictly greater than 0.0.
     */
    public static final double MIN_THRESHOLD = 0.0;

    /**
     * Indicates that by default inliers will only be computed but not kept.
     */
    public static final boolean DEFAULT_COMPUTE_AND_KEEP_INLIERS = false;

    /**
     * Indicates that by default residuals will only be computed but not kept.
     */
    public static final boolean DEFAULT_COMPUTE_AND_KEEP_RESIDUALS = false;

    /**
     * Threshold to determine whether samples are inliers or not when testing possible solutions.
     * The threshold refers to the amount of error on distance between estimated position and
     * distances provided for each sample.
     */
    private double mThreshold = DEFAULT_THRESHOLD;

    /**
     * Indicates whether inliers must be computed and kept.
     */
    private boolean mComputeAndKeepInliers = DEFAULT_COMPUTE_AND_KEEP_INLIERS;

    /**
     * Indicates whether residuals must be computed and kept.
     */
    private boolean mComputeAndKeepResiduals = DEFAULT_COMPUTE_AND_KEEP_RESIDUALS;

    /**
     * Quality scores corresponding to each provided sample.
     * The larger the score value the better the quality of the sample.
     */
    private double[] mQualityScores;

    /**
     * Constructor.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator() {
        super();
    }

    /**
     * Constructor.
     *
     * @param listener listener to be notified of events such as when estimation
     *                 starts, ends or its progress significantly changes.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(listener);
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final List<StandardDeviationBodyKinematics> measurements) {
        super(measurements);
    }


    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final boolean commonAxisUsed) {
        super(commonAxisUsed);
    }

    /**
     * Constructor.
     *
     * @param bias known accelerometer bias. This must have length 3 and is expressed
     *             in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] bias) {
        super(bias);
    }

    /**
     * Constructor.
     *
     * @param bias known accelerometer bias.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Matrix bias) {
        super(bias);
    }

    /**
     * Constructor.
     *
     * @param bias      known accelerometer bias.
     * @param initialMa initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Matrix bias, final Matrix initialMa) {
        super(bias, initialMa);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm) {
        super(groundTruthGravityNorm);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements) {
        super(groundTruthGravityNorm, measurements);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias) {
        super(groundTruthGravityNorm, measurements, bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is
     *                               expressed in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias) {
        super(groundTruthGravityNorm, measurements, bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements, bias, initialMa);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, initialMa,
                listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, initialMa);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, initialMa, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm) {
        super(groundTruthGravityNorm);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements) {
        super(groundTruthGravityNorm, measurements);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias) {
        super(groundTruthGravityNorm, measurements,
                bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias) {
        super(groundTruthGravityNorm, measurements, bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed, bias);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias, listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements, bias, initialMa);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, initialMa,
                listener);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, initialMa);
    }

    /**
     * Constructor.
     *
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, initialMa, listener);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements) {
        super(groundTruthGravityNorm, measurements);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias) {
        super(groundTruthGravityNorm, measurements, bias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is
     *                               expressed in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias) {
        super(groundTruthGravityNorm, measurements, bias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements, bias, initialMa);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, bias, initialMa,
                listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, initialMa);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm expressed in meters per
     *                               squared second (m/s^2).
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is
     *                                  smaller than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Double groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements, commonAxisUsed,
                bias, initialMa, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm) {
        super(groundTruthGravityNorm);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements) {
        super(groundTruthGravityNorm, measurements);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           list of body kinematics measurements taken at a given position with
     *                               different unknown orientations and containing the standard deviations
     *                               of accelerometer and gyroscope measurements.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param listener               listener to be notified of events such as when estimation
     *                               starts, ends or its progress significantly changes.
     * @throws IllegalArgumentException if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias) {
        super(groundTruthGravityNorm, measurements, bias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                bias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias. This must have length 3 and is expressed
     *                               in meters per squared second (m/s^2).
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias) {
        super(groundTruthGravityNorm, measurements, bias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param initialBias            initial bias to find a solution.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                initialBias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param initialBias            initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, initialBias);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements, bias, initialMa);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final Matrix bias, final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                bias, initialMa, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias, initialMa);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Constructor.
     *
     * @param qualityScores          quality scores corresponding to each provided
     *                               measurement. The larger the score value the better
     *                               the quality of the sample.
     * @param groundTruthGravityNorm ground truth gravity norm.
     * @param measurements           collection of body kinematics measurements with standard
     *                               deviations taken at the same position with zero velocity
     *                               and unknown different orientations.
     * @param commonAxisUsed         indicates whether z-axis is assumed to be common for
     *                               accelerometer and gyroscope.
     * @param bias                   known accelerometer bias.
     * @param initialMa              initial scale factors and cross coupling errors matrix.
     * @param listener               listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3 or
     *                                  if provided gravity norm value is negative or
     *                                  if provided quality scores length is smaller
     *                                  than 10 or 13 samples.
     */
    public PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator(
            final double[] qualityScores,
            final Acceleration groundTruthGravityNorm,
            final List<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix bias,
            final Matrix initialMa,
            final RobustKnownBiasAndGravityNormAccelerometerCalibratorListener listener) {
        super(groundTruthGravityNorm, measurements,
                commonAxisUsed, bias, initialMa, listener);
        internalSetQualityScores(qualityScores);
    }

    /**
     * Gets threshold to determine whether samples are inliers or not when testing possible solutions.
     * The threshold refers to the amount of error on norm between measured specific forces and the
     * ones generated with estimated calibration parameters provided for each sample.
     *
     * @return threshold to determine whether samples are inliers or not.
     */
    public double getThreshold() {
        return mThreshold;
    }

    /**
     * Sets threshold to determine whether samples are inliers or not when testing possible solutions.
     * The threshold refers to the amount of error on norm between measured specific forces and the
     * ones generated with estimated calibration parameters provided for each sample.
     *
     * @param threshold threshold to determine whether samples are inliers or not.
     * @throws IllegalArgumentException if provided value is equal or less than zero.
     * @throws LockedException          if calibrator is currently running.
     */
    public void setThreshold(final double threshold) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        if (threshold <= MIN_THRESHOLD) {
            throw new IllegalArgumentException();
        }
        mThreshold = threshold;
    }

    /**
     * Returns quality scores corresponding to each provided sample.
     * The larger the score value the better the quality of the sample.
     *
     * @return quality scores corresponding to each sample.
     */
    @Override
    public double[] getQualityScores() {
        return mQualityScores;
    }

    /**
     * Sets quality scores corresponding to each provided sample.
     * The larger the score value the better the quality of the sample.
     *
     * @param qualityScores quality scores corresponding to each sample.
     * @throws IllegalArgumentException if provided quality scores length
     *                                  is smaller than minimum required samples
     *                                  (10 or 13).
     * @throws LockedException          if calibrator is currently running.
     */
    @Override
    public void setQualityScores(final double[] qualityScores)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        internalSetQualityScores(qualityScores);
    }

    /**
     * Indicates whether calibrator is ready to find a solution.
     *
     * @return true if calibrator is ready, false otherwise.
     */
    @Override
    public boolean isReady() {
        return super.isReady() && mQualityScores != null &&
                mQualityScores.length == mMeasurements.size();
    }

    /**
     * Indicates whether inliers must be computed and kept.
     *
     * @return true if inliers must be computed and kept, false if inliers
     * only need to be computed but not kept.
     */
    public boolean isComputeAndKeepInliersEnabled() {
        return mComputeAndKeepInliers;
    }

    /**
     * Specifies whether inliers must be computed and kept.
     *
     * @param computeAndKeepInliers true if inliers must be computed and kept,
     *                              false if inliers only need to be computed but not kept.
     * @throws LockedException if calibrator is currently running.
     */
    public void setComputeAndKeepInliersEnabled(final boolean computeAndKeepInliers)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mComputeAndKeepInliers = computeAndKeepInliers;
    }

    /**
     * Indicates whether residuals must be computed and kept.
     *
     * @return true if residuals must be computed and kept, false if residuals
     * only need to be computed but not kept.
     */
    public boolean isComputeAndKeepResiduals() {
        return mComputeAndKeepResiduals;
    }

    /**
     * Specifies whether residuals must be computed and kept.
     *
     * @param computeAndKeepResiduals true if residuals must be computed and kept,
     *                                false if residuals only need to be computed but not kept.
     * @throws LockedException if calibrator is currently running.
     */
    public void setComputeAndKeepResidualsEnabled(
            final boolean computeAndKeepResiduals)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mComputeAndKeepResiduals = computeAndKeepResiduals;
    }

    /**
     * Estimates accelerometer calibration parameters scale factors
     * and cross-coupling errors.
     *
     * @throws LockedException      if calibrator is currently running.
     * @throws NotReadyException    if calibrator is not ready.
     * @throws CalibrationException if estimation fails for numerical reasons.
     */
    @Override
    public void calibrate() throws LockedException, NotReadyException, CalibrationException {
        if (mRunning) {
            throw new LockedException();
        }
        if (!isReady()) {
            throw new NotReadyException();
        }

        final PROSACRobustEstimator<PreliminaryResult> innerEstimator =
                new PROSACRobustEstimator<>(
                        new PROSACRobustEstimatorListener<PreliminaryResult>() {
                            @Override
                            public double[] getQualityScores() {
                                return mQualityScores;
                            }

                            @Override
                            public double getThreshold() {
                                return mThreshold;
                            }

                            @Override
                            public int getTotalSamples() {
                                return mMeasurements.size();
                            }

                            @Override
                            public int getSubsetSize() {
                                return mPreliminarySubsetSize;
                            }

                            @Override
                            public void estimatePreliminarSolutions(
                                    final int[] samplesIndices,
                                    final List<PreliminaryResult> solutions) {
                                computePreliminarySolutions(samplesIndices, solutions);
                            }

                            @Override
                            public double computeResidual(
                                    final PreliminaryResult currentEstimation,
                                    final int i) {
                                return computeError(mMeasurements.get(i), currentEstimation);
                            }

                            @Override
                            public boolean isReady() {
                                return PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator.this.isReady();
                            }

                            @Override
                            public void onEstimateStart(
                                    final RobustEstimator<PreliminaryResult> estimator) {
                            }

                            @Override
                            public void onEstimateEnd(
                                    final RobustEstimator<PreliminaryResult> estimator) {
                            }

                            @Override
                            public void onEstimateNextIteration(
                                    final RobustEstimator<PreliminaryResult> estimator,
                                    final int iteration) {
                                if (mListener != null) {
                                    mListener.onCalibrateNextIteration(
                                            PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator.this,
                                            iteration);
                                }
                            }

                            @Override
                            public void onEstimateProgressChange(
                                    final RobustEstimator<PreliminaryResult> estimator,
                                    final float progress) {
                                if (mListener != null) {
                                    mListener.onCalibrateProgressChange(
                                            PROSACRobustKnownBiasAndGravityNormAccelerometerCalibrator.this,
                                            progress);
                                }
                            }
                        });

        try {
            mRunning = true;

            if (mListener != null) {
                mListener.onCalibrateStart(this);
            }

            mInliersData = null;
            innerEstimator.setComputeAndKeepInliersEnabled(
                    mComputeAndKeepInliers || mRefineResult);
            innerEstimator.setComputeAndKeepResidualsEnabled(
                    mComputeAndKeepResiduals || mRefineResult);
            innerEstimator.setConfidence(mConfidence);
            innerEstimator.setMaxIterations(mMaxIterations);
            innerEstimator.setProgressDelta(mProgressDelta);
            final PreliminaryResult preliminaryResult = innerEstimator.estimate();
            mInliersData = innerEstimator.getInliersData();

            attemptRefine(preliminaryResult);

            if (mListener != null) {
                mListener.onCalibrateEnd(this);
            }

        } catch (final com.irurueta.numerical.LockedException e) {
            throw new LockedException(e);
        } catch (final com.irurueta.numerical.NotReadyException e) {
            throw new NotReadyException(e);
        } catch (final RobustEstimatorException e) {
            throw new CalibrationException(e);
        } finally {
            mRunning = false;
        }
    }

    /**
     * Returns method being used for robust estimation.
     *
     * @return method being used for robust estimation.
     */
    @Override
    public RobustEstimatorMethod getMethod() {
        return RobustEstimatorMethod.PROSAC;
    }

    /**
     * Indicates whether this calibrator requires quality scores for each
     * measurement or not.
     *
     * @return true if quality scores are required, false otherwise.
     */
    @Override
    public boolean isQualityScoresRequired() {
        return true;
    }

    /**
     * Sets quality scores corresponding to each provided sample.
     * This method is used internally and does not check whether instance is
     * locked or not.
     *
     * @param qualityScores quality scores to be set.
     * @throws IllegalArgumentException if provided quality scores length
     *                                  is smaller than the minimum required
     *                                  number of samples (10 or 13).
     */
    private void internalSetQualityScores(final double[] qualityScores) {
        if (qualityScores == null ||
                qualityScores.length < getMinimumRequiredMeasurements()) {
            throw new IllegalArgumentException();
        }

        mQualityScores = qualityScores;
    }
}
