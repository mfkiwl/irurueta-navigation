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
package com.irurueta.navigation.inertial.calibration;

import com.irurueta.algebra.AlgebraException;
import com.irurueta.algebra.Matrix;
import com.irurueta.algebra.Utils;
import com.irurueta.algebra.WrongSizeException;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.navigation.frames.converters.ECEFtoNEDPositionVelocityConverter;
import com.irurueta.navigation.frames.converters.NEDtoECEFPositionVelocityConverter;
import com.irurueta.navigation.inertial.BodyKinematics;
import com.irurueta.navigation.inertial.ECEFGravity;
import com.irurueta.navigation.inertial.ECEFPosition;
import com.irurueta.navigation.inertial.ECEFVelocity;
import com.irurueta.navigation.inertial.NEDPosition;
import com.irurueta.navigation.inertial.NEDVelocity;
import com.irurueta.navigation.inertial.estimators.ECEFGravityEstimator;
import com.irurueta.numerical.EvaluationException;
import com.irurueta.numerical.GradientEstimator;
import com.irurueta.numerical.MultiDimensionFunctionEvaluatorListener;
import com.irurueta.numerical.fitting.FittingException;
import com.irurueta.numerical.fitting.LevenbergMarquardtMultiDimensionFitter;
import com.irurueta.numerical.fitting.LevenbergMarquardtMultiDimensionFunctionEvaluator;
import com.irurueta.units.Acceleration;
import com.irurueta.units.AccelerationConverter;
import com.irurueta.units.AccelerationUnit;

import java.util.Collection;

/**
 * Estimates accelerometer biases, cross couplings and scaling factors.
 * This calibrator uses Levenberg-Marquardt to find a minimum least squared error
 * solution.
 * <p>
 * To use this calibrator at least 10 measurements taken at a single known position must
 * be taken at 10 different unknown orientations and zero velocity when common z-axis
 * is assumed, otherwise at least 13 measurements are required.
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
public class KnownPositionAccelerometerCalibrator {

    /**
     * Indicates whether by default a common z-axis is assumed for both the accelerometer
     * and gyroscope.
     */
    public static final boolean DEFAULT_USE_COMMON_Z_AXIS = false;

    /**
     * Number of unknowns when common z-axis is assumed for both the accelerometer
     * and gyroscope.
     */
    public static final int COMMON_Z_AXIS_UNKNOWNS = 9;

    /**
     * Number of unknowns for the general case.
     */
    public static final int GENERAL_UNKNOWNS = 12;

    /**
     * Required minimum number of measurements when common z-axis is assumed.
     */
    public static final int MINIMUM_MEASUREMENTS_COMON_Z_AXIS = COMMON_Z_AXIS_UNKNOWNS + 1;

    /**
     * Required minimum number of measurements for the general case.
     */
    public static final int MINIMUM_MEASUREMENTS_GENERAL = GENERAL_UNKNOWNS + 1;

    /**
     * Levenberg-Marquardt fitter to find a non-linear solution.
     */
    private LevenbergMarquardtMultiDimensionFitter mFitter =
            new LevenbergMarquardtMultiDimensionFitter();

    /**
     * Initial x-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     */
    private double mInitialBiasX;

    /**
     * Initial y-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     */
    private double mInitialBiasY;

    /**
     * Initial z-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     */
    private double mInitialBiasZ;

    /**
     * Initial x scaling factor.
     */
    private double mInitialSx;

    /**
     * Initial y scaling factor.
     */
    private double mInitialSy;

    /**
     * Initial z scaling factor.
     */
    private double mInitialSz;

    /**
     * Initial x-y cross coupling error.
     */
    private double mInitialMxy;

    /**
     * Initial x-z cross coupling error.
     */
    private double mInitialMxz;

    /**
     * Initial y-x cross coupling error.
     */
    private double mInitialMyx;

    /**
     * Initial y-z cross coupling error.
     */
    private double mInitialMyz;

    /**
     * Initial z-x cross coupling error.
     */
    private double mInitialMzx;

    /**
     * Initial z-y cross coupling error.
     */
    private double mInitialMzy;

    /**
     * Position where body kinematics measures have been taken.
     */
    private ECEFPosition mPosition;

    /**
     * Contains a collection of body kinematics measurements taken at
     * a given position with different unknown orientations and containing
     * the standard deviations of accelerometer and gyroscope measurements.
     */
    private Collection<StandardDeviationBodyKinematics> mMeasurements;

    /**
     * This flag indicates whether z-axis is assumed to be common for accelerometer
     * and gyroscope.
     * When enabled, this eliminates 3 variables from Ma matrix.
     */
    private boolean mCommonAxisUsed = DEFAULT_USE_COMMON_Z_AXIS;

    /**
     * Listener to handle events raised by this calibrator.
     */
    private KnownPositionAccelerometerCalibratorListener mListener;

    /**
     * Estimated accelerometer biases for each IMU axis expressed in meter per squared
     * second (m/s^2).
     */
    private double[] mEstimatedBiases;

    /**
     * Estimated accelerometer scale factors and cross coupling errors.
     * This is the product of matrix Ta containing cross coupling errors and Ka
     * containing scaling factors.
     * So tat:
     * <pre>
     *     Ma = [sx    mxy  mxz] = Ta*Ka
     *          [myx   sy   myz]
     *          [mzx   mzy  sz ]
     * </pre>
     * Where:
     * <pre>
     *     Ka = [sx 0   0 ]
     *          [0  sy  0 ]
     *          [0  0   sz]
     * </pre>
     * and
     * <pre>
     *     Ta = [1          -alphaXy    alphaXz ]
     *          [alphaYx    1           -alphaYz]
     *          [-alphaZx   alphaZy     1       ]
     * </pre>
     * Hence:
     * <pre>
     *     Ma = [sx    mxy  mxz] = Ta*Ka =  [sx             -sy * alphaXy   sz * alphaXz ]
     *          [myx   sy   myz]            [sx * alphaYx   sy              -sz * alphaYz]
     *          [mzx   mzy  sz ]            [-sx * alphaZx  sy * alphaZy    sz           ]
     * </pre>
     * This instance allows any 3x3 matrix however, typically alphaYx, alphaZx and alphaZy
     * are considered to be zero if the accelerometer z-axis is assumed to be the same
     * as the body z-axis. When this is assumed, myx = mzx = mzy = 0 and the Ma matrix
     * becomes upper diagonal:
     * <pre>
     *     Ma = [sx    mxy  mxz]
     *          [0     sy   myz]
     *          [0     0    sz ]
     * </pre>
     * Values of this matrix are unitless.
     */
    private Matrix mEstimatedMa;

    /**
     * Estimated covariance matrix for estimated position.
     */
    private Matrix mEstimatedCovariance;

    /**
     * Estimated chi square value.
     */
    private double mEstimatedChiSq;

    /**
     * Indicates whether estimator is running.
     */
    private boolean mRunning;

    /**
     * Internally holds x-coordinate of measured specific force during calibration.
     */
    private double mFmeasX;

    /**
     * Internally holds y-coordinate of measured specific force during calibration.
     */
    private double mFmeasY;

    /**
     * Internally holds z-coordinate of measured specific force during calibration.
     */
    private double mFmeasZ;

    /**
     * Internaly holds measured specific force during calibration expressed as
     * a column matrix.
     */
    private Matrix mFmeas;

    /**
     * Internally holds cross-coupling errors during calibration.
     */
    private Matrix mM;

    /**
     * Internally holds inverse of cross-coupling errors during calibration.
     */
    private Matrix mInvM;

    /**
     * Internally holds biases during calibration.
     */
    private Matrix mB;

    /**
     * Internally holds computed true specific force during calibration.
     */
    private Matrix mFtrue;


    /**
     * Constructor.
     */
    public KnownPositionAccelerometerCalibrator() {
    }

    /**
     * Constructor.
     *
     * @param listener listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final KnownPositionAccelerometerCalibratorListener listener) {
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements) {
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed) {
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed) {
        this(measurements);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        try {
            setInitialBias(initialBiasX, initialBiasY, initialBiasZ);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ) {
        try {
            setInitialBias(initialBiasX, initialBiasY, initialBiasZ);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        try {
            setInitialScalingFactors(initialSx, initialSy, initialSz);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy,
                initialSz);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double initialBiasX,
            final double initialBiasY, final double initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double initialBiasX,
            final double initialBiasY, final double initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        try {
            setInitialScalingFactors(initialSx, initialSy, initialSz);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        try {
            setInitialScalingFactorsAndCrossCouplingErrors(initialSx, initialSy, initialSz,
                    initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        try {
            setInitialScalingFactorsAndCrossCouplingErrors(initialSx, initialSy, initialSz,
                    initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBias initial accelerometer bias to be used to find a solution.
     *                    This must have length 3 and is expressed in meters per
     *                    squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final double[] initialBias) {
        try {
            setInitialBias(initialBias);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param initialBias initial accelerometer bias to be used to find a solution.
     *                    This must have length 3 and is expressed in meters per
     *                    squared second (m/s^2).
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial accelerometer bias to be used to find a solution.
     *                     This must have length 3 and is expressed in meters per
     *                     squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double[] initialBias) {
        this(initialBias);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial accelerometer bias to be used to find a solution.
     *                     This must have length 3 and is expressed in meters per
     *                     squared second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final double[] initialBias) {
        this(initialBias);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] initialBias) {
        this(commonAxisUsed, initialBias);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBias initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(final Matrix initialBias) {
        try {
            setInitialBias(initialBias);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param initialBias initial bias to find a solution.
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias) {
        this(initialBias);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Matrix initialBias) {
        this(initialBias);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias) {
        this(commonAxisUsed, initialBias);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBias);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param initialBias initial bias to find a solution.
     * @param initialMa   initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Matrix initialBias, final Matrix initialMa) {
        this(initialBias);
        try {
            setInitialMa(initialMa);
        } catch (final LockedException ignore) {
            // never happens
        }
    }

    /**
     * Constructor.
     *
     * @param initialBias initial bias to find a solution.
     * @param initialMa   initial scale factors and cross coupling errors matrix.
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Matrix initialBias, final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBias, initialMa);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param initialMa    initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias, final Matrix initialMa) {
        this(initialBias, initialMa);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param initialMa    initial scale factors and cross coupling errors matrix.
     * @param listener     listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias, final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBias, initialMa);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa) {
        this(initialBias, initialMa);
        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Constructor.
     *
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBias, initialMa);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa) {
        this(commonAxisUsed, initialBias, initialMa);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBias, initialMa);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param position position where body kinematics measures have been taken.
     */
    public KnownPositionAccelerometerCalibrator(final ECEFPosition position) {
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position position where body kinematics measures have been taken.
     * @param listener listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(position);
        mListener = listener;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements) {
        this(position);
        mMeasurements = measurements;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final boolean commonAxisUsed) {
        this(commonAxisUsed);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final boolean commonAxisUsed,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed) {
        this(measurements, commonAxisUsed);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY,
                initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(initialBiasX, initialBiasY, initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy,
                initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double initialBiasX,
            final double initialBiasY, final double initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double initialBiasX,
            final double initialBiasY, final double initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ, initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx, initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial accelerometer bias to be used to find a solution.
     *                    This must have length 3 and is expressed in meters per
     *                    squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final double[] initialBias) {
        this(initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial accelerometer bias to be used to find a solution.
     *                    This must have length 3 and is expressed in meters per
     *                    squared second (m/s^2).
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial accelerometer bias to be used to find a solution.
     *                     This must have length 3 and is expressed in meters per
     *                     squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double[] initialBias) {
        this(measurements, initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial accelerometer bias to be used to find a solution.
     *                     This must have length 3 and is expressed in meters per
     *                     squared second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final boolean commonAxisUsed,
            final double[] initialBias) {
        this(commonAxisUsed, initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position, final boolean commonAxisUsed,
            final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] initialBias) {
        this(measurements, commonAxisUsed, initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Matrix initialBias) {
        this(initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias) {
        this(measurements, initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Matrix initialBias) {
        this(commonAxisUsed, initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias) {
        this(measurements, commonAxisUsed, initialBias);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBias, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @param initialMa   initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Matrix initialBias, final Matrix initialMa) {
        this(initialBias, initialMa);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @param initialMa   initial scale factors and cross coupling errors matrix.
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Matrix initialBias, final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(initialBias, initialMa, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param initialMa    initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias, final Matrix initialMa) {
        this(measurements, initialBias, initialMa);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param initialMa    initial scale factors and cross coupling errors matrix.
     * @param listener     listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias, final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, initialBias, initialMa, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa) {
        this(commonAxisUsed, initialBias, initialMa);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(commonAxisUsed, initialBias, initialMa, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa) {
        this(measurements, commonAxisUsed, initialBias, initialMa);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final ECEFPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(measurements, commonAxisUsed, initialBias, initialMa, listener);
        mPosition = position;
    }

    /**
     * Constructor.
     *
     * @param position position where body kinematics measures have been taken.
     */
    public KnownPositionAccelerometerCalibrator(final NEDPosition position) {
        this(convertPosition(position));
    }

    /**
     * Constructor.
     *
     * @param position position where body kinematics measures have been taken.
     * @param listener listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements) {
        this(convertPosition(position), measurements);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final boolean commonAxisUsed) {
        this(convertPosition(position), commonAxisUsed);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final boolean commonAxisUsed,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed) {
        this(convertPosition(position), measurements, commonAxisUsed);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(convertPosition(position), initialBiasX, initialBiasY,
                initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), initialBiasX, initialBiasY,
                initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(convertPosition(position), measurements, initialBiasX,
                initialBiasY, initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, initialBiasX,
                initialBiasY, initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(convertPosition(position), commonAxisUsed, initialBiasX,
                initialBiasY, initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed, initialBiasX,
                initialBiasY, initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(convertPosition(position), initialBiasX, initialBiasY,
                initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), initialBiasX, initialBiasY,
                initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ) {
        this(convertPosition(position), measurements, initialBiasX,
                initialBiasY, initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, initialBiasX,
                initialBiasY, initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(convertPosition(position), commonAxisUsed, initialBiasX,
                initialBiasY, initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed, initialBiasX,
                initialBiasY, initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(convertPosition(position), initialBiasX, initialBiasY,
                initialBiasZ, initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double initialBiasX,
            final double initialBiasY, final double initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double initialBiasX,
            final double initialBiasY, final double initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(convertPosition(position), initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(convertPosition(position), initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution. This is expressed in meters per squared
     *                     second (m/s^2).
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution. This is expressed in meters per squared
     *                       second (m/s^2).
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed,
            final double initialBiasX, final double initialBiasY,
            final double initialBiasZ, final double initialSx, final double initialSy,
            final double initialSz, final double initialMxy, final double initialMxz,
            final double initialMyx, final double initialMyz, final double initialMzx,
            final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz,
                initialMyx, initialMyz, initialMzx, initialMzy, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy) {
        this(convertPosition(position), initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBiasX initial x-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasY initial y-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialBiasZ initial z-coordinate of accelerometer bias to be used
     *                     to find a solution.
     * @param initialSx    initial x scaling factor.
     * @param initialSy    initial y scaling factor.
     * @param initialSz    initial z scaling factor.
     * @param initialMxy   initial x-y cross coupling error.
     * @param initialMxz   initial x-z cross coupling error.
     * @param initialMyx   initial y-x cross coupling error.
     * @param initialMyz   initial y-z cross coupling error.
     * @param initialMzx   initial z-x cross coupling error.
     * @param initialMzy   initial z-y cross coupling error.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Acceleration initialBiasX, final Acceleration initialBiasY,
            final Acceleration initialBiasZ, final double initialSx,
            final double initialSy, final double initialSz, final double initialMxy,
            final double initialMxz, final double initialMyx, final double initialMyz,
            final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz,
                initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBiasX   initial x-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasY   initial y-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialBiasZ   initial z-coordinate of accelerometer bias to be used
     *                       to find a solution.
     * @param initialSx      initial x scaling factor.
     * @param initialSy      initial y scaling factor.
     * @param initialSz      initial z scaling factor.
     * @param initialMxy     initial x-y cross coupling error.
     * @param initialMxz     initial x-z cross coupling error.
     * @param initialMyx     initial y-x cross coupling error.
     * @param initialMyz     initial y-z cross coupling error.
     * @param initialMzx     initial z-x cross coupling error.
     * @param initialMzy     initial z-y cross coupling error.
     * @param listener       listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Acceleration initialBiasX,
            final Acceleration initialBiasY, final Acceleration initialBiasZ,
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBiasX, initialBiasY, initialBiasZ,
                initialSx, initialSy, initialSz, initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy, listener);
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial accelerometer bias to be used to find a solution.
     *                    This must have length 3 and is expressed in meters per
     *                    squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final double[] initialBias) {
        this(convertPosition(position), initialBias);
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial accelerometer bias to be used to find a solution.
     *                    This must have length 3 and is expressed in meters per
     *                    squared second (m/s^2).
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), initialBias, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial accelerometer bias to be used to find a solution.
     *                     This must have length 3 and is expressed in meters per
     *                     squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double[] initialBias) {
        this(convertPosition(position), measurements, initialBias);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial accelerometer bias to be used to find a solution.
     *                     This must have length 3 and is expressed in meters per
     *                     squared second (m/s^2).
     * @param listener     listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, initialBias, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final boolean commonAxisUsed,
            final double[] initialBias) {
        this(convertPosition(position), commonAxisUsed, initialBias);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position, final boolean commonAxisUsed,
            final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed, initialBias,
                listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] initialBias) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBias);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial accelerometer bias to be used to find a solution.
     *                       This must have length 3 and is expressed in meters per
     *                       squared second (m/s^2).
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias array does not have length 3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final double[] initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed, initialBias,
                listener);
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Matrix initialBias) {
        this(convertPosition(position), initialBias);
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), initialBias, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias) {
        this(convertPosition(position), measurements, initialBias);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param listener     listener to handle events raised by this calibrator.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, initialBias,
                listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Matrix initialBias) {
        this(convertPosition(position), commonAxisUsed, initialBias);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed, initialBias, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBias);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if provided bias matrix is not 3x1.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBias, listener);
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @param initialMa   initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Matrix initialBias, final Matrix initialMa) {
        this(convertPosition(position), initialBias, initialMa);
    }

    /**
     * Constructor.
     *
     * @param position    position where body kinematics measures have been taken.
     * @param initialBias initial bias to find a solution.
     * @param initialMa   initial scale factors and cross coupling errors matrix.
     * @param listener    listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Matrix initialBias, final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), initialBias, initialMa, listener);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param initialMa    initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias, final Matrix initialMa) {
        this(convertPosition(position), measurements, initialBias,
                initialMa);
    }

    /**
     * Constructor.
     *
     * @param position     position where body kinematics measures have been taken.
     * @param measurements collection of body kinematics measurements with standard
     *                     deviations taken at the same position with zero velocity
     *                     and unknown different orientations.
     * @param initialBias  initial bias to find a solution.
     * @param initialMa    initial scale factors and cross coupling errors matrix.
     * @param listener     listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final Matrix initialBias, final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, initialBias,
                initialMa, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa) {
        this(convertPosition(position), commonAxisUsed, initialBias,
                initialMa);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), commonAxisUsed, initialBias,
                initialMa, listener);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBias, initialMa);
    }

    /**
     * Constructor.
     *
     * @param position       position where body kinematics measures have been taken.
     * @param measurements   collection of body kinematics measurements with standard
     *                       deviations taken at the same position with zero velocity
     *                       and unknown different orientations.
     * @param commonAxisUsed indicates whether z-axis is assumed to be common for
     *                       accelerometer and gyroscope.
     * @param initialBias    initial bias to find a solution.
     * @param initialMa      initial scale factors and cross coupling errors matrix.
     * @param listener       listener to handle events raised by this calibrator.
     * @throws IllegalArgumentException if either provided bias matrix is not 3x1 or
     *                                  scaling and coupling error matrix is not 3x3.
     */
    public KnownPositionAccelerometerCalibrator(
            final NEDPosition position,
            final Collection<StandardDeviationBodyKinematics> measurements,
            final boolean commonAxisUsed, final Matrix initialBias,
            final Matrix initialMa,
            final KnownPositionAccelerometerCalibratorListener listener) {
        this(convertPosition(position), measurements, commonAxisUsed,
                initialBias, initialMa, listener);
    }

    /**
     * Gets initial x-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     *
     * @return initial x-coordinate of accelerometer bias.
     */
    public double getInitialBiasX() {
        return mInitialBiasX;
    }

    /**
     * Sets initial x-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBiasX(final double initialBiasX) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasX = initialBiasX;
    }

    /**
     * Gets initial y-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     *
     * @return initial y-coordinate of accelerometer bias.
     */
    public double getInitialBiasY() {
        return mInitialBiasY;
    }

    /**
     * Sets initial y-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     *
     * @param initialBiasY initial y-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBiasY(final double initialBiasY) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasY = initialBiasY;
    }

    /**
     * Gets initial z-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     *
     * @return initial z-coordinate of accelerometer bias.
     */
    public double getInitialBiasZ() {
        return mInitialBiasZ;
    }

    /**
     * Sets initial z-coordinate of accelerometer bias to be used to find a solution.
     * This is expressed in meters per squared second (m/s^2).
     *
     * @param initialBiasZ initial z-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBiasZ(final double initialBiasZ) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasZ = initialBiasZ;
    }

    /**
     * Gets initial x-coordinate of accelerometer bias to be used to find a solution.
     *
     * @return initial x-coordinate of accelerometer bias.
     */
    public Acceleration getInitialBiasXAsAcceleration() {
        return new Acceleration(mInitialBiasX,
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Gets initial x-coordinate of accelerometer bias to be used to find a solution.
     *
     * @param result instance where result data will be stored.
     */
    public void getInitialBiasXAsAcceleration(final Acceleration result) {
        result.setValue(mInitialBiasX);
        result.setUnit(AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Sets initial x-coordinate of accelerometer bias to be used to find a solution.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBiasX(final Acceleration initialBiasX)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasX = convertAcceleration(initialBiasX);
    }

    /**
     * Gets initial y-coordinate of accelerometer bias to be used to find a solution.
     *
     * @return initial y-coordinate of accelerometer bias.
     */
    public Acceleration getInitialBiasYAsAcceleration() {
        return new Acceleration(mInitialBiasY,
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Gets initial y-coordinate of accelerometer bias to be used to find a solution.
     *
     * @param result instance where result data will be stored.
     */
    public void getInitialBiasYAsAcceleration(final Acceleration result) {
        result.setValue(mInitialBiasY);
        result.setUnit(AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Sets initial y-coordinate of accelerometer bias to be used to find a solution.
     *
     * @param initialBiasY initial y-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBiasY(final Acceleration initialBiasY)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasY = convertAcceleration(initialBiasY);
    }

    /**
     * Gets initial z-coordinate of accelerometer bias to be used to find a solution.
     *
     * @return initial z-coordinate of accelerometer bias.
     */
    public Acceleration getInitialBiasZAsAcceleration() {
        return new Acceleration(mInitialBiasZ,
                AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Gets initial z-coordinate of accelerometer bias to be used to find a solution.
     *
     * @param result instance where result data will be stored.
     */
    public void getInitialBiasZAsAcceleration(final Acceleration result) {
        result.setValue(mInitialBiasZ);
        result.setUnit(AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Sets initial z-coordinate of accelerometer bias to be used to find a solution.
     *
     * @param initialBiasZ initial z-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBiasZ(final Acceleration initialBiasZ)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasZ = convertAcceleration(initialBiasZ);
    }

    /**
     * Sets initial bias coordinates of accelerometer used to find a solution
     * expressed in meters per squared second (m/s^2).
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias.
     * @param initialBiasY initial y-coordinate of accelerometer bias.
     * @param initialBiasZ initial z-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBias(final double initialBiasX, final double initialBiasY,
                               final double initialBiasZ) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasX = initialBiasX;
        mInitialBiasY = initialBiasY;
        mInitialBiasZ = initialBiasZ;
    }

    /**
     * Sets initial bias coordinates of accelerometer used to find a solution.
     *
     * @param initialBiasX initial x-coordinate of accelerometer bias.
     * @param initialBiasY initial y-coordinate of accelerometer bias.
     * @param initialBiasZ initial z-coordinate of accelerometer bias.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialBias(final Acceleration initialBiasX,
                               final Acceleration initialBiasY,
                               final Acceleration initialBiasZ) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialBiasX = convertAcceleration(initialBiasX);
        mInitialBiasY = convertAcceleration(initialBiasY);
        mInitialBiasZ = convertAcceleration(initialBiasZ);
    }

    /**
     * Gets initial x scaling factor.
     *
     * @return initial x scaling factor.
     */
    public double getInitialSx() {
        return mInitialSx;
    }

    /**
     * Sets initial x scaling factor.
     *
     * @param initialSx initial x scaling factor.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialSx(final double initialSx) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialSx = initialSx;
    }

    /**
     * Gets initial y scaling factor.
     *
     * @return initial y scaling factor.
     */
    public double getInitialSy() {
        return mInitialSy;
    }

    /**
     * Sets initial y scaling factor.
     *
     * @param initialSy initial y scaling factor.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialSy(final double initialSy) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialSy = initialSy;
    }

    /**
     * Gets initial z scaling factor.
     *
     * @return initial z scaling factor.
     */
    public double getInitialSz() {
        return mInitialSz;
    }

    /**
     * Sets initial z scaling factor.
     *
     * @param initialSz initial z scaling factor.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialSz(final double initialSz) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialSz = initialSz;
    }

    /**
     * Gets initial x-y cross coupling error.
     *
     * @return initial x-y cross coupling error.
     */
    public double getInitialMxy() {
        return mInitialMxy;
    }

    /**
     * Sets initial x-y cross coupling error.
     *
     * @param initialMxy initial x-y cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialMxy(final double initialMxy) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialMxy = initialMxy;
    }

    /**
     * Gets initial x-z cross coupling error.
     *
     * @return initial x-z cross coupling error.
     */
    public double getInitialMxz() {
        return mInitialMxz;
    }

    /**
     * Sets initial x-z cross coupling error.
     *
     * @param initialMxz initial x-z cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialMxz(final double initialMxz) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialMxz = initialMxz;
    }

    /**
     * Gets initial y-x cross coupling error.
     *
     * @return initial y-x cross coupling error.
     */
    public double getInitialMyx() {
        return mInitialMyx;
    }

    /**
     * Sets initial y-x cross coupling error.
     *
     * @param initialMyx initial y-x cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialMyx(final double initialMyx) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialMyx = initialMyx;
    }

    /**
     * Gets initial y-z cross coupling error.
     *
     * @return initial y-z cross coupling error.
     */
    public double getInitialMyz() {
        return mInitialMyz;
    }

    /**
     * Sets initial y-z cross coupling error.
     *
     * @param initialMyz initial y-z cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialMyz(final double initialMyz) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialMyz = initialMyz;
    }

    /**
     * Gets initial z-x cross coupling error.
     *
     * @return initial z-x cross coupling error.
     */
    public double getInitialMzx() {
        return mInitialMzx;
    }

    /**
     * Sets initial z-x cross coupling error.
     *
     * @param initialMzx initial z-x cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialMzx(final double initialMzx) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialMzx = initialMzx;
    }

    /**
     * Gets initial z-y cross coupling error.
     *
     * @return initial z-y cross coupling error.
     */
    public double getInitialMzy() {
        return mInitialMzy;
    }

    /**
     * Sets initial z-y cross coupling error.
     *
     * @param initialMzy initial z-y cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialMzy(final double initialMzy) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialMzy = initialMzy;
    }

    /**
     * Sets initial scaling factors.
     *
     * @param initialSx initial x scaling factor.
     * @param initialSy initial y scaling factor.
     * @param initialSz initial z scaling factor.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialScalingFactors(
            final double initialSx, final double initialSy, final double initialSz)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialSx = initialSx;
        mInitialSy = initialSy;
        mInitialSz = initialSz;
    }

    /**
     * Sets initial cross coupling errors.
     *
     * @param initialMxy initial x-y cross coupling error.
     * @param initialMxz initial x-z cross coupling error.
     * @param initialMyx initial y-x cross coupling error.
     * @param initialMyz initial y-z cross coupling error.
     * @param initialMzx initial z-x cross coupling error.
     * @param initialMzy initial z-y cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialCrossCouplingErrors(
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mInitialMxy = initialMxy;
        mInitialMxz = initialMxz;
        mInitialMyx = initialMyx;
        mInitialMyz = initialMyz;
        mInitialMzx = initialMzx;
        mInitialMzy = initialMzy;
    }

    /**
     * Sets initial scaling factors and cross coupling errors.
     *
     * @param initialSx  initial x scaling factor.
     * @param initialSy  initial y scaling factor.
     * @param initialSz  initial z scaling factor.
     * @param initialMxy initial x-y cross coupling error.
     * @param initialMxz initial x-z cross coupling error.
     * @param initialMyx initial y-x cross coupling error.
     * @param initialMyz initial y-z cross coupling error.
     * @param initialMzx initial z-x cross coupling error.
     * @param initialMzy initial z-y cross coupling error.
     * @throws LockedException if calibrator is currently running.
     */
    public void setInitialScalingFactorsAndCrossCouplingErrors(
            final double initialSx, final double initialSy, final double initialSz,
            final double initialMxy, final double initialMxz, final double initialMyx,
            final double initialMyz, final double initialMzx, final double initialMzy)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        setInitialScalingFactors(initialSx, initialSy, initialSz);
        setInitialCrossCouplingErrors(initialMxy, initialMxz, initialMyx,
                initialMyz, initialMzx, initialMzy);
    }

    /**
     * Gets initial bias to be used to find a solution as an array.
     * Array values are expressed in meters per squared second (m/s^2).
     *
     * @return array containing coordinates of initial bias.
     */
    public double[] getInitialBias() {
        final double[] result = new double[BodyKinematics.COMPONENTS];
        getInitialBias(result);
        return result;
    }

    /**
     * Gets initial bias to be used to find a solution as an array.
     * Array values are expressed in meters per squared second (m/s^2).
     *
     * @param result instance where result data will be copied to.
     * @throws IllegalArgumentException if provided array does not have length 3.
     */
    public void getInitialBias(final double[] result) {
        if (result.length != BodyKinematics.COMPONENTS) {
            throw new IllegalArgumentException();
        }
        result[0] = mInitialBiasX;
        result[1] = mInitialBiasY;
        result[2] = mInitialBiasZ;
    }

    /**
     * Sets initial bias to be used to find a solution as an array.
     * Array values are expressed in meters per squared second (m/s^2).
     *
     * @param initialBias initial bias to find a solution.
     * @throws LockedException          if calibrator is currently running.
     * @throws IllegalArgumentException if provided array does not have length 3.
     */
    public void setInitialBias(final double[] initialBias) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }

        if (initialBias.length != BodyKinematics.COMPONENTS) {
            throw new IllegalArgumentException();
        }
        mInitialBiasX = initialBias[0];
        mInitialBiasY = initialBias[1];
        mInitialBiasZ = initialBias[2];
    }

    /**
     * Gets initial bias to be used to find a solution as a column matrix.
     *
     * @return initial bias to be used to find a solution as a column matrix.
     */
    public Matrix getInitialBiasAsMatrix() {
        Matrix result;
        try {
            result = new Matrix(BodyKinematics.COMPONENTS, 1);
            getInitialBiasAsMatrix(result);
        } catch (final WrongSizeException ignore) {
            // never happens
            result = null;
        }
        return result;
    }

    /**
     * Gets initial bias to be used to find a solution as a column matrix.
     *
     * @param result instance where result data will be copied to.
     * @throws IllegalArgumentException if provided matrix is not 3x1.
     */
    public void getInitialBiasAsMatrix(final Matrix result) {
        if (result.getRows() != BodyKinematics.COMPONENTS
                || result.getColumns() != 1) {
            throw new IllegalArgumentException();
        }
        result.setElementAtIndex(0, mInitialBiasX);
        result.setElementAtIndex(1, mInitialBiasY);
        result.setElementAtIndex(2, mInitialBiasZ);
    }

    /**
     * Sets initial bias to be used to find a solution as an array.
     *
     * @param initialBias initial bias to find a solution.
     * @throws LockedException          if calibrator is currently running.
     * @throws IllegalArgumentException if provided matrix is not 3x1.
     */
    public void setInitialBias(final Matrix initialBias) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        if (initialBias.getRows() != BodyKinematics.COMPONENTS
                || initialBias.getColumns() != 1) {
            throw new IllegalArgumentException();
        }

        mInitialBiasX = initialBias.getElementAtIndex(0);
        mInitialBiasY = initialBias.getElementAtIndex(1);
        mInitialBiasZ = initialBias.getElementAtIndex(2);
    }

    /**
     * Gets initial scale factors and cross coupling errors matrix.
     *
     * @return initial scale factors and cross coupling errors matrix.
     */
    public Matrix getInitialMa() {
        Matrix result;
        try {
            result = new Matrix(BodyKinematics.COMPONENTS,
                    BodyKinematics.COMPONENTS);
            getInitialMa(result);
        } catch (final WrongSizeException ignore) {
            // never happens
            result = null;
        }
        return result;
    }

    /**
     * Gets initial scale factors and cross coupling errors matrix.
     *
     * @param result instance where data will be stored.
     * @throws IllegalArgumentException if provided matrix is not 3x3.
     */
    public void getInitialMa(final Matrix result) {
        if (result.getRows() != BodyKinematics.COMPONENTS ||
                result.getColumns() != BodyKinematics.COMPONENTS) {
            throw new IllegalArgumentException();
        }
        result.setElementAtIndex(0, mInitialSx);
        result.setElementAtIndex(1, mInitialMyx);
        result.setElementAtIndex(2, mInitialMzx);

        result.setElementAtIndex(3, mInitialMxy);
        result.setElementAtIndex(4, mInitialSy);
        result.setElementAtIndex(5, mInitialMzy);

        result.setElementAtIndex(6, mInitialMxz);
        result.setElementAtIndex(7, mInitialMyz);
        result.setElementAtIndex(8, mInitialSz);
    }

    /**
     * Sets initial scale factors and cross coupling errors matrix.
     *
     * @param initialMa initial scale factors and cross coupling errors matrix.
     * @throws IllegalArgumentException if provided matrix is not 3x3.
     * @throws LockedException          if calibrator is currently running.
     */
    public void setInitialMa(final Matrix initialMa) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        if (initialMa.getRows() != BodyKinematics.COMPONENTS ||
                initialMa.getColumns() != BodyKinematics.COMPONENTS) {
            throw new IllegalArgumentException();
        }

        mInitialSx = initialMa.getElementAtIndex(0);
        mInitialMyx = initialMa.getElementAtIndex(1);
        mInitialMzx = initialMa.getElementAtIndex(2);

        mInitialMxy = initialMa.getElementAtIndex(3);
        mInitialSy = initialMa.getElementAtIndex(4);
        mInitialMzy = initialMa.getElementAtIndex(5);

        mInitialMxz = initialMa.getElementAtIndex(6);
        mInitialMyz = initialMa.getElementAtIndex(7);
        mInitialSz = initialMa.getElementAtIndex(8);
    }

    /**
     * Gets position where body kinematics measures have been taken expressed in
     * ECEF coordinates.
     *
     * @return position where body kinematics measures have been taken.
     */
    public ECEFPosition getEcefPosition() {
        return mPosition;
    }

    /**
     * Gets position where body kinematics measures have been taken expressed in
     * ECEF coordinates.
     *
     * @param position position where body kinematics measures have been taken.
     * @throws LockedException if calibrator is currently running.
     */
    public void setPosition(final ECEFPosition position) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }

        mPosition = position;
    }

    /**
     * Gets position where body kinematics measures have been taken expressed in
     * NED coordinates.
     *
     * @return position where body kinematics measures have been taken or null if
     * not available.
     */
    public NEDPosition getNedPosition() {
        final NEDPosition result = new NEDPosition();
        return getNedPosition(result) ? result : null;
    }

    /**
     * Gets position where body kinematics measures have been taken expressed in
     * NED coordinates.
     *
     * @param result instance where result will be stored.
     * @return true if NED position could be computed, false otherwise.
     */
    public boolean getNedPosition(final NEDPosition result) {

        if (mPosition != null) {
            final NEDVelocity velocity = new NEDVelocity();
            ECEFtoNEDPositionVelocityConverter.convertECEFtoNED(
                    mPosition.getX(), mPosition.getY(), mPosition.getZ(),
                    0.0, 0.0, 0.0, result, velocity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets position where body kinematics measures have been taken expressed in
     * NED coordinates.
     *
     * @param position position where body kinematics measures have been taken.
     * @throws LockedException if calibrator is currently running.
     */
    public void setPosition(final NEDPosition position) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }

        mPosition = convertPosition(position);
    }

    /**
     * Gets a collection of body kinematics measurements taken at
     * a given position with different unknown orientations and containing
     * the standard deviations of accelerometer and gyroscope measurements.
     *
     * @return collection of body kinematics measurements at a known position
     * with unknown orientations.
     */
    public Collection<StandardDeviationBodyKinematics> getMeasurements() {
        return mMeasurements;
    }

    /**
     * Sets a collection of body kinematics measurements taken at
     * a given position with different unknown orientations and containing
     * the standard deviations of accelerometer and gyroscope measurements.
     *
     * @param measurements collection of body kinematics measurements at a
     *                     known position witn unknown orientations.
     * @throws LockedException if calibrator is currently running.
     */
    public void setMeasurements(final Collection<StandardDeviationBodyKinematics> measurements)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }
        mMeasurements = measurements;
    }

    /**
     * Indicates whether z-axis is assumed to be common for accelerometer and
     * gyroscope.
     * When enabled, this eliminates 3 variables from Ma matrix.
     *
     * @return true if z-axis is assumed to be common for accelerometer and gyroscope,
     * false otherwise.
     */
    public boolean isCommonAxisUsed() {
        return mCommonAxisUsed;
    }

    /**
     * Specifies whether z-axis is assumed to be common for accelerometer and
     * gyroscope.
     * When enabled, this eliminates 3 variables from Ma matrix.
     *
     * @param commonAxisUsed true if z-axis is assumed to be common for accelerometer
     *                       and gyroscope, false otherwise.
     * @throws LockedException if calibrator is currently running.
     */
    public void setCommonAxisUsed(final boolean commonAxisUsed) throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }

        mCommonAxisUsed = commonAxisUsed;
    }

    /**
     * Gets listener to handle events raised by this estimator.
     *
     * @return listener to handle events raised by this estimator.
     */
    public KnownPositionAccelerometerCalibratorListener getListener() {
        return mListener;
    }

    /**
     * Sets listener to handle events raised by this estimator.
     *
     * @param listener listener to handle events raised by this estimator.
     * @throws LockedException if calibrator is currently running.
     */
    public void setListener(
            final KnownPositionAccelerometerCalibratorListener listener)
            throws LockedException {
        if (mRunning) {
            throw new LockedException();
        }

        mListener = listener;
    }

    /**
     * Gets minimum number of required measurements.
     *
     * @return minimum number of required measurements.
     */
    public int getMinimumRequiredMeasurements() {
        return mCommonAxisUsed ? MINIMUM_MEASUREMENTS_COMON_Z_AXIS :
                MINIMUM_MEASUREMENTS_GENERAL;
    }

    /**
     * Indicates whether calibrator is ready to start.
     *
     * @return true if calibrator is ready, false otherwise.
     */
    public boolean isReady() {
        return mMeasurements != null && mMeasurements.size() >= getMinimumRequiredMeasurements()
                && mPosition != null;
    }

    /**
     * Indicates whether calibrator is currently running or not.
     *
     * @return true if calibrator is running, false otherwise.
     */
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Estimates accelerometer calibration parameters containing bias, scale factors
     * and cross-coupling errors.
     *
     * @throws LockedException      if calibrator is currently running.
     * @throws NotReadyException    if calibrator is not ready.
     * @throws CalibrationException if estimation fails for numerical reasons.
     */
    public void calibrate() throws LockedException, NotReadyException, CalibrationException {
        if (mRunning) {
            throw new LockedException();
        }

        if (!isReady()) {
            throw new NotReadyException();
        }

        try {
            mRunning = true;

            if (mListener != null) {
                mListener.onCalibrateStart(this);
            }

            if (mCommonAxisUsed) {
                calibrateCommonAxis();
            } else {
                calibrateGeneral();
            }

            if (mListener != null) {
                mListener.onCalibrateEnd(this);
            }

        } catch (final AlgebraException | FittingException
                | com.irurueta.numerical.NotReadyException e) {
            throw new CalibrationException(e);
        } finally {
            mRunning = false;
        }
    }

    /**
     * Gets array containing x,y,z components of estimated accelerometer biases
     * expressed in meters per squared second (m/s^2).
     *
     * @return array containing x,y,z components of estimated accelerometer biases.
     */
    public double[] getEstimatedBiases() {
        return mEstimatedBiases;
    }

    /**
     * Gets array containing x,y,z components of estimated accelerometer biases
     * expressed in meters per squared second (m/s^2).
     *
     * @param result instance where estimated accelerometer biases will be stored.
     * @return true if result instance was updated, false otherwise (when estimation
     * is not yet available).
     */
    public boolean getEstimatedBiases(final double[] result) {
        if (mEstimatedBiases != null) {
            System.arraycopy(mEstimatedBiases, 0, result,
                    0, mEstimatedBiases.length);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets column matrix containing x,y,z components of estimated accelerometer biases
     * expressed in meters per squared second (m/s^2).
     *
     * @return column matrix containing x,y,z components of estimated accelerometer
     * biases.
     */
    public Matrix getEstimatedBiasesAsMatrix() {
        return mEstimatedBiases != null ? Matrix.newFromArray(mEstimatedBiases) : null;
    }

    /**
     * Gets column matrix containing x,y,z components of estimated accelerometer biases
     * expressed in meters per squared second (m/s^2).
     *
     * @param result instance where result data will be stored.
     * @return true if result was updated, false otherwise.
     * @throws WrongSizeException if provided result instance has invalid size.
     */
    public boolean getEstimatedBiasesAsMatrix(final Matrix result)
            throws WrongSizeException {
        if (mEstimatedBiases != null) {
            result.fromArray(mEstimatedBiases);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets x coordinate of estimated accelerometer bias expressed in meters per
     * squared second (m/s^2).
     *
     * @return x coordinate of estimated accelerometer bias or null if not available.
     */
    public Double getEstimatedBiasFx() {
        return mEstimatedBiases != null ? mEstimatedBiases[0] : null;
    }

    /**
     * Gets y coordinate of estimated accelerometer bias expressed in meters per
     * squared second (m/s^2).
     *
     * @return y coordinate of estimated accelerometer bias or null if not available.
     */
    public Double getEstimatedBiasFy() {
        return mEstimatedBiases != null ? mEstimatedBiases[1] : null;
    }

    /**
     * Gets z coordinate of estimated accelerometer bias expressed in meters per
     * squared second (m/s^2).
     *
     * @return z coordinate of estimated accelerometer bias or null if not available.
     */
    public Double getEstimatedBiasFz() {
        return mEstimatedBiases != null ? mEstimatedBiases[2] : null;
    }

    /**
     * Gets x coordinate of estimated accelerometer bias.
     *
     * @return x coordinate of estimated accelerometer bias or null if not available.
     */
    public Acceleration getEstimatedBiasFxAsAcceleration() {
        return mEstimatedBiases != null ?
                new Acceleration(mEstimatedBiases[0],
                        AccelerationUnit.METERS_PER_SQUARED_SECOND) : null;
    }

    /**
     * Gets x coordinate of estimated accelerometer bias.
     *
     * @param result instance where result will be stored.
     * @return true if result was updated, false if estimation is not available.
     */
    public boolean getEstimatedBiasFxAsAcceleration(final Acceleration result) {
        if (mEstimatedBiases != null) {
            result.setValue(mEstimatedBiases[0]);
            result.setUnit(AccelerationUnit.METERS_PER_SQUARED_SECOND);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets y coordinate of estimated accelerometer bias.
     *
     * @return y coordinate of estimated accelerometer bias or null if not available.
     */
    public Acceleration getEstimatedBiasFyAsAcceleration() {
        return mEstimatedBiases != null ?
                new Acceleration(mEstimatedBiases[1],
                        AccelerationUnit.METERS_PER_SQUARED_SECOND) : null;
    }

    /**
     * Gets y coordinate of estimated accelerometer bias.
     *
     * @param result instance where result will be stored.
     * @return true if result was updated, false if estimation is not available.
     */
    public boolean getEstimatedBiasFyAsAcceleration(final Acceleration result) {
        if (mEstimatedBiases != null) {
            result.setValue(mEstimatedBiases[1]);
            result.setUnit(AccelerationUnit.METERS_PER_SQUARED_SECOND);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets z coordinate of estimated accelerometer bias.
     *
     * @return z coordinate of estimated accelerometer bias or null if not available.
     */
    public Acceleration getEstimatedBiasFzAsAcceleration() {
        return mEstimatedBiases != null ?
                new Acceleration(mEstimatedBiases[2],
                        AccelerationUnit.METERS_PER_SQUARED_SECOND) : null;
    }

    /**
     * Gets z coordinate of estimated accelerometer bias.
     *
     * @param result instance where result will be stored.
     * @return true if result was updated, false if estimation is not available.
     */
    public boolean getEstimatedBiasFzAsAcceleration(final Acceleration result) {
        if (mEstimatedBiases != null) {
            result.setValue(mEstimatedBiases[2]);
            result.setUnit(AccelerationUnit.METERS_PER_SQUARED_SECOND);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets estimated accelerometer scale factors and ross coupling errors.
     * This is the product of matrix Ta containing cross coupling errors and Ka
     * containing scaling factors.
     * So tat:
     * <pre>
     *     Ma = [sx    mxy  mxz] = Ta*Ka
     *          [myx   sy   myz]
     *          [mzx   mzy  sz ]
     * </pre>
     * Where:
     * <pre>
     *     Ka = [sx 0   0 ]
     *          [0  sy  0 ]
     *          [0  0   sz]
     * </pre>
     * and
     * <pre>
     *     Ta = [1          -alphaXy    alphaXz ]
     *          [alphaYx    1           -alphaYz]
     *          [-alphaZx   alphaZy     1       ]
     * </pre>
     * Hence:
     * <pre>
     *     Ma = [sx    mxy  mxz] = Ta*Ka =  [sx             -sy * alphaXy   sz * alphaXz ]
     *          [myx   sy   myz]            [sx * alphaYx   sy              -sz * alphaYz]
     *          [mzx   mzy  sz ]            [-sx * alphaZx  sy * alphaZy    sz           ]
     * </pre>
     * This instance allows any 3x3 matrix however, typically alphaYx, alphaZx and alphaZy
     * are considered to be zero if the accelerometer z-axis is assumed to be the same
     * as the body z-axis. When this is assumed, myx = mzx = mzy = 0 and the Ma matrix
     * becomes upper diagonal:
     * <pre>
     *     Ma = [sx    mxy  mxz]
     *          [0     sy   myz]
     *          [0     0    sz ]
     * </pre>
     * Values of this matrix are unitless.
     *
     * @return estimated accelerometer scale factors and cross coupling errors, or null
     * if not available.
     */
    public Matrix getEstimatedMa() {
        return mEstimatedMa;
    }

    /**
     * Gets estimated x-axis scale factor.
     *
     * @return estimated x-axis scale factor or null if not available.
     */
    public Double getEstimatedSx() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(0, 0) : null;
    }

    /**
     * Gets estimated y-axis scale factor.
     *
     * @return estimated y-axis scale factor or null if not available.
     */
    public Double getEstimatedSy() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(1, 1) : null;
    }

    /**
     * Gets estimated z-axis scale factor.
     *
     * @return estimated z-axis scale factor or null if not available.
     */
    public Double getEstimatedSz() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(2, 2) : null;
    }

    /**
     * Gets estimated x-y cross-coupling error.
     *
     * @return estimated x-y cross-coupling error or null if not available.
     */
    public Double getEstimatedMxy() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(0, 1) : null;
    }

    /**
     * Gets estimated x-z cross-coupling error.
     *
     * @return estimated x-z cross-coupling error or null if not available.
     */
    public Double getEstimatedMxz() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(0, 2) : null;
    }

    /**
     * Gets estimated y-x cross-coupling error.
     *
     * @return estimated y-x cross-coupling error or null if not available.
     */
    public Double getEstimatedMyx() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(1, 0) : null;
    }

    /**
     * Gets estimated y-z cross-coupling error.
     *
     * @return estimated y-z cross-coupling error or null if not available.
     */
    public Double getEstimatedMyz() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(1, 2) : null;
    }

    /**
     * Gets estimated z-x cross-coupling error.
     *
     * @return estimated z-x cross-coupling error or null if not available.
     */
    public Double getEstimatedMzx() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(2, 0) : null;
    }

    /**
     * Gets estimated z-y cross-coupling error.
     *
     * @return estimated z-y cross-coupling error or null if not available.
     */
    public Double getEstimatedMzy() {
        return mEstimatedMa != null ?
                mEstimatedMa.getElementAt(2, 1) : null;
    }

    /**
     * Gets estimated covariance matrix for estimated position.
     *
     * @return estimated covariance matrix for estimated position.
     */
    public Matrix getEstimatedCovariance() {
        return mEstimatedCovariance;
    }

    /**
     * Gets estimated chi square value.
     *
     * @return estimated chi square value.
     */
    public double getEstimatedChiSq() {
        return mEstimatedChiSq;
    }

    /**
     * Sets input data into Levenberg-Marquardt fitter.
     *
     * @throws WrongSizeException never happens.
     */
    private void setInputData() throws WrongSizeException {

        final ECEFGravity gravity = ECEFGravityEstimator.estimateGravityAndReturnNew(
                mPosition.getX(), mPosition.getY(), mPosition.getZ());
        final double g = gravity.getNorm();
        final double g2 = g * g;

        final int numMeasurements = mMeasurements.size();
        final Matrix x = new Matrix(numMeasurements, BodyKinematics.COMPONENTS);
        final double[] y = new double[numMeasurements];
        final double[] specificForceStandardDeviations = new double[numMeasurements];
        int i = 0;
        for (final StandardDeviationBodyKinematics measurement : mMeasurements) {
            final BodyKinematics measuredKinematics = measurement.getKinematics();

            final double fmeasX = measuredKinematics.getFx();
            final double fmeasY = measuredKinematics.getFy();
            final double fmeasZ = measuredKinematics.getFz();

            x.setElementAt(i, 0, fmeasX);
            x.setElementAt(i, 1, fmeasY);
            x.setElementAt(i, 2, fmeasZ);

            y[i] = g2;

            specificForceStandardDeviations[i] =
                    measurement.getSpecificForceStandardDeviation();

            i++;
        }

        mFitter.setInputData(x, y, specificForceStandardDeviations);
    }

    /**
     * Converts provided NED position expressed in terms of latitude, longitude and height respect
     * mean Earth surface, to position expressed in ECEF coordinates.
     *
     * @param position NED position to be converted.
     * @return converted position expressed in ECEF coordinates.
     */
    private static ECEFPosition convertPosition(final NEDPosition position) {
        final ECEFVelocity velocity = new ECEFVelocity();
        final ECEFPosition result = new ECEFPosition();
        NEDtoECEFPositionVelocityConverter.convertNEDtoECEF(
                position.getLatitude(), position.getLongitude(), position.getHeight(),
                0.0, 0.0, 0.0, result, velocity);
        return result;
    }

    /**
     * Converts acceleration instance to meters per squared second.
     *
     * @param acceleration acceleration instance to be converted.
     * @return converted value.
     */
    private static double convertAcceleration(final Acceleration acceleration) {
        return AccelerationConverter.convert(acceleration.getValue().doubleValue(),
                acceleration.getUnit(), AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Internal method to perform general calibration.
     *
     * @throws FittingException                         if Levenberg-Marquardt fails for numerical reasons.
     * @throws AlgebraException                         if there are numerical instabilities that prevent
     *                                                  matrix inversion.
     * @throws com.irurueta.numerical.NotReadyException never happens.
     */
    private void calibrateGeneral() throws AlgebraException, FittingException,
            com.irurueta.numerical.NotReadyException {
        // The accelerometer model is:
        // fmeas = ba + (I + Ma) * ftrue + w

        // Ideally a least squares solution tries to minimize noise component, so:
        // fmeas = ba + (I + Ma) * ftrue

        // For convergence purposes of the Levenberg-Marquardt algorithm, the
        // accelerometer model can be better expressed as:
        // fmeas = T*K*(ftrue + b)
        // fmeas = M*(ftrue + b)
        // fmeas = M*ftrue + M*b

        //where:
        // M = I + Ma
        // ba = M*b = (I + Ma)*b --> b = M^-1*ba

        // We know that the norm of the true specific force is equal to the amount
        // of gravity at a certain Earth position
        // ||ftrue|| = ||g|| ~ 9.81 m/s^2

        // Hence:
        // fmeas - M*b = M*ftrue

        // M^-1 * (fmeas - M*b) = ftrue

        // ||g||^2 = ||ftrue||^2 = (M^-1 * (fmeas - M*b))^T * (M^-1 * (fmeas - M*b))
        // ||g||^2 = (fmeas - M*b)^T*(M^-1)^T * M^-1 * (fmeas - M*b)
        // ||g||^2 = (fmeas - M * b)^T * ||M^-1||^2 * (fmeas - M * b)
        // ||g||^2 = ||fmeas - M * b||^2 * ||M^-1||^2

        // Where:

        // b = [bx]
        //     [by]
        //     [bz]

        // M = [m11 	m12 	m13]
        //     [m21 	m22 	m23]
        //     [m31 	m32 	m33]

        final GradientEstimator gradientEstimator = new GradientEstimator(
                new MultiDimensionFunctionEvaluatorListener() {
            @Override
            public double evaluate(double[] point) throws EvaluationException {
                return evaluateGeneral(point);
            }
        });

        final Matrix initialM = Matrix.identity(BodyKinematics.COMPONENTS, BodyKinematics.COMPONENTS);
        initialM.add(getInitialMa());

        final Matrix invInitialM = Utils.inverse(initialM);
        final Matrix initialBa = getInitialBiasAsMatrix();
        final Matrix initialB = invInitialM.multiplyAndReturnNew(initialBa);

        mFitter.setFunctionEvaluator(
                new LevenbergMarquardtMultiDimensionFunctionEvaluator() {
            @Override
            public int getNumberOfDimensions() {
                // Input points are measured specific force coordinates
                return BodyKinematics.COMPONENTS;
            }

            @Override
            public double[] createInitialParametersArray() {
                final double[] initial = new double[GENERAL_UNKNOWNS];

                // biases b
                for (int i = 0; i < BodyKinematics.COMPONENTS; i++) {
                    initial[i] = initialB.getElementAtIndex(i);
                }

                // cross coupling errors M
                final int num = BodyKinematics.COMPONENTS * BodyKinematics.COMPONENTS;
                for (int i = 0, j = BodyKinematics.COMPONENTS; i < num; i++, j++) {
                    initial[j] = initialM.getElementAtIndex(i);
                }

                return initial;
            }

            @Override
            public double evaluate(final int i, final double[] point,
                                   final double[] params, final double[] derivatives)
                    throws EvaluationException {

                mFmeasX = point[0];
                mFmeasY = point[1];
                mFmeasZ = point[2];

                gradientEstimator.gradient(params, derivatives);

                return evaluateGeneral(params);
            }
        });

        setInputData();

        mFitter.fit();

        final double[] result = mFitter.getA();

        final double bx = result[0];
        final double by = result[1];
        final double bz = result[2];

        final double m11 = result[3];
        final double m21 = result[4];
        final double m31 = result[5];

        final double m12 = result[6];
        final double m22 = result[7];
        final double m32 = result[8];

        final double m13 = result[9];
        final double m23 = result[10];
        final double m33 = result[11];

        final Matrix b = new Matrix(BodyKinematics.COMPONENTS, 1);
        b.setElementAtIndex(0, bx);
        b.setElementAtIndex(1, by);
        b.setElementAtIndex(2, bz);

        final Matrix m = new Matrix(BodyKinematics.COMPONENTS,
                BodyKinematics.COMPONENTS);
        m.setElementAtIndex(0, m11);
        m.setElementAtIndex(1, m21);
        m.setElementAtIndex(2, m31);

        m.setElementAtIndex(3, m12);
        m.setElementAtIndex(4, m22);
        m.setElementAtIndex(5, m32);

        m.setElementAtIndex(6, m13);
        m.setElementAtIndex(7, m23);
        m.setElementAtIndex(8, m33);

        setResult(m, b);
    }

    /**
     * Internal method to perform calibration when common z-axis is assumed for both
     * the accelerometer and gyroscope.
     *
     * @throws FittingException                         if Levenberg-Marquardt fails for numerical reasons.
     * @throws AlgebraException                         if there are numerical instabilities that prevent
     *                                                  matrix inversion.
     * @throws com.irurueta.numerical.NotReadyException never happens.
     */
    private void calibrateCommonAxis() throws AlgebraException, FittingException,
            com.irurueta.numerical.NotReadyException {
        // The accelerometer model is:
        // fmeas = ba + (I + Ma) * ftrue + w

        // Ideally a least squares solution tries to minimize noise component, so:
        // fmeas = ba + (I + Ma) * ftrue

        // For convergence purposes of the Levenberg-Marquardt algorithm, the
        // accelerometer model can be better expressed as:
        // fmeas = T*K*(ftrue + b)
        // fmeas = M*(ftrue + b)
        // fmeas = M*ftrue + M*b

        //where:
        // M = I + Ma
        // ba = M*b = (I + Ma)*b --> b = M^-1*ba

        // We know that the norm of the true specific force is equal to the amount
        // of gravity at a certain Earth position
        // ||ftrue|| = ||g|| ~ 9.81 m/s^2

        // Hence:
        // fmeas - M*b = M*ftrue

        // M^-1 * (fmeas - M*b) = ftrue

        // ||g||^2 = ||ftrue||^2 = (M^-1 * (fmeas - M*b))^T * (M^-1 * (fmeas - M*b))
        // ||g||^2 = (fmeas - M*b)^T*(M^-1)^T * M^-1 * (fmeas - M*b)
        // ||g||^2 = (fmeas - M * b)^T * ||M^-1||^2 * (fmeas - M * b)
        // ||g||^2 = ||fmeas - M * b||^2 * ||M^-1||^2

        // Where:

        // b = [bx]
        //     [by]
        //     [bz]

        // M = [m11 	m12 	m13]
        //     [0 		m22 	m23]
        //     [0 	 	0 		m33]


        final GradientEstimator gradientEstimator = new GradientEstimator(
                new MultiDimensionFunctionEvaluatorListener() {
                    @Override
                    public double evaluate(double[] point) throws EvaluationException {
                        return evaluateCommonAxis(point);
                    }
                });

        final Matrix initialM = Matrix.identity(BodyKinematics.COMPONENTS, BodyKinematics.COMPONENTS);
        initialM.add(getInitialMa());

        // Force initial M to be upper diagonal
        initialM.setElementAt(1, 0, 0.0);
        initialM.setElementAt(2, 0, 0.0);
        initialM.setElementAt(2, 1, 0.0);

        final Matrix invInitialM = Utils.inverse(initialM);
        final Matrix initialBa = getInitialBiasAsMatrix();
        final Matrix initialB = invInitialM.multiplyAndReturnNew(initialBa);

        mFitter.setFunctionEvaluator(
                new LevenbergMarquardtMultiDimensionFunctionEvaluator() {
                    @Override
                    public int getNumberOfDimensions() {
                        // Input points are measured specific force coordinates
                        return BodyKinematics.COMPONENTS;
                    }

                    @Override
                    public double[] createInitialParametersArray() {
                        final double[] initial = new double[COMMON_Z_AXIS_UNKNOWNS];

                        // biases b
                        for (int i = 0; i < BodyKinematics.COMPONENTS; i++) {
                            initial[i] = initialB.getElementAtIndex(i);
                        }

                        // upper diagonal cross coupling errors M
                        int k = BodyKinematics.COMPONENTS;
                        for (int j = 0; j < BodyKinematics.COMPONENTS; j++) {
                            for (int i = 0; i < BodyKinematics.COMPONENTS; i++) {
                                if (i <= j) {
                                    initial[k] = initialM.getElementAt(i, j);
                                    k++;
                                }
                            }
                        }

                        return initial;
                    }

                    @Override
                    public double evaluate(final int i, final double[] point,
                                           final double[] params, final double[] derivatives)
                            throws EvaluationException {

                        mFmeasX = point[0];
                        mFmeasY = point[1];
                        mFmeasZ = point[2];

                        gradientEstimator.gradient(params, derivatives);

                        return evaluateCommonAxis(params);
                    }
                });

        setInputData();

        mFitter.fit();

        final double[] result = mFitter.getA();

        final double bx = result[0];
        final double by = result[1];
        final double bz = result[2];

        final double m11 = result[3];

        final double m12 = result[4];
        final double m22 = result[5];

        final double m13 = result[6];
        final double m23 = result[7];
        final double m33 = result[8];

        final Matrix b = new Matrix(BodyKinematics.COMPONENTS, 1);
        b.setElementAtIndex(0, bx);
        b.setElementAtIndex(1, by);
        b.setElementAtIndex(2, bz);

        final Matrix m = new Matrix(BodyKinematics.COMPONENTS,
                BodyKinematics.COMPONENTS);
        m.setElementAtIndex(0, m11);
        m.setElementAtIndex(1, 0.0);
        m.setElementAtIndex(2, 0.0);

        m.setElementAtIndex(3, m12);
        m.setElementAtIndex(4, m22);
        m.setElementAtIndex(5, 0.0);

        m.setElementAtIndex(6, m13);
        m.setElementAtIndex(7, m23);
        m.setElementAtIndex(8, m33);

        setResult(m, b);
    }

    /**
     * Makes proper conversion of internal cross-coupling and bias matrices.
     *
     * @param m internal cross-coupling matrix.
     * @param b internal bias matrix.
     * @throws AlgebraException if a numerical instability occurs.
     */
    private void setResult(final Matrix m, final Matrix b) throws AlgebraException {
        // Because:
        // M = I + Ma
        // b = M^-1*ba

        // Then:
        // Ma = M - I
        // ba = M*b

        if (mEstimatedBiases == null) {
            mEstimatedBiases = new double[BodyKinematics.COMPONENTS];
        }

        final Matrix ba = m.multiplyAndReturnNew(b);
        ba.toArray(mEstimatedBiases);

        if (mEstimatedMa == null) {
            mEstimatedMa = m;
        } else {
            mEstimatedMa.copyFrom(m);
        }

        for (int i = 0; i < BodyKinematics.COMPONENTS; i++) {
            mEstimatedMa.setElementAt(i, i,
                    mEstimatedMa.getElementAt(i, i) - 1.0);
        }

        mEstimatedCovariance = mFitter.getCovar();
        mEstimatedChiSq = mFitter.getChisq();
    }

    /**
     * Computes estimated true specific force squared norm using current measured
     * specific force and provided parameters for the general case.
     * This method is internally executed during gradient estimation and
     * Levenberg-Marquardt fitting needed for calibration computation.
     *
     * @param params array containing current parameters for the general purpose case.
     *               Must have length 12.
     * @return estimated true specific force squared norm.
     * @throws EvaluationException if there are numerical instabilities.
     */
    private double evaluateGeneral(final double[] params) throws EvaluationException {
        final double bx = params[0];
        final double by = params[1];
        final double bz = params[2];

        final double m11 = params[3];
        final double m21 = params[4];
        final double m31 = params[5];

        final double m12 = params[6];
        final double m22 = params[7];
        final double m32 = params[8];

        final double m13 = params[9];
        final double m23 = params[10];
        final double m33 = params[11];

        return evaluate(bx, by, bz, m11, m21, m31, m12, m22, m32,
                m13, m23, m33);
    }

    /**
     * Computes estimated true specific force squared norm using current measured
     * specific force and provided parameters when common z-axis is assumed.
     * This method is internally executed during gradient estimation and
     * Levenberg-Marquardt fitting needed for calibration computation.
     *
     * @param params array containing current parameters for the common z-axis case.
     *               Must have length 9.
     * @return estimated true specific force squared norm.
     * @throws EvaluationException if there are numerical instabilities.
     */
    private double evaluateCommonAxis(final double[] params) throws EvaluationException {
        final double bx = params[0];
        final double by = params[1];
        final double bz = params[2];

        final double m11 = params[3];

        final double m12 = params[4];
        final double m22 = params[5];

        final double m13 = params[6];
        final double m23 = params[7];
        final double m33 = params[8];

        return evaluate(bx, by, bz, m11, 0.0, 0.0, m12, m22, 0.0,
                m13, m23, m33);
    }

    /**
     * Computes estimated true specific force squared norm using current measured
     * specific force and provided parameters.
     * This method is internally executed during gradient estimation and
     * Levenberg-Marquardt fitting needed for calibration computation.
     *
     * @param bx x-coordinate of bias.
     * @param by y-coordinate of bias.
     * @param bz z-coordinate of bias.
     * @param m11 element 1,1 of cross-coupling error matrix.
     * @param m21 element 2,1 of cross-coupling error matrix.
     * @param m31 element 3,1 of cross-coupling error matrix.
     * @param m12 element 1,2 of cross-coupling error matrix.
     * @param m22 element 2,2 of cross-coupling error matrix.
     * @param m32 element 3,2 of cross-coupling error matrix.
     * @param m13 element 1,3 of cross-coupling error matrix.
     * @param m23 element 2,3 of cross-coupling error matrix.
     * @param m33 element 3,3 of cross-coupling error matrix.
     * @return estimated true specific force squared norm.
     * @throws EvaluationException if there are numerical instabilities.
     */
    private double evaluate(final double bx, final double by, final double bz,
                            final double m11, final double m21, final double m31,
                            final double m12, final double m22, final double m32,
                            final double m13, final double m23, final double m33)
            throws EvaluationException {

        // fmeas = M*(ftrue + b)

        // ftrue = M^-1*fmeas - b

        try {
            if (mFmeas == null) {
                mFmeas = new Matrix(BodyKinematics.COMPONENTS, 1);
            }
            if (mM == null) {
                mM = new Matrix(BodyKinematics.COMPONENTS, BodyKinematics.COMPONENTS);
            }
            if (mInvM == null) {
                mInvM = new Matrix(BodyKinematics.COMPONENTS,
                        BodyKinematics.COMPONENTS);
            }
            if (mB == null) {
                mB = new Matrix(BodyKinematics.COMPONENTS, 1);
            }
            if (mFtrue == null) {
                mFtrue = new Matrix(BodyKinematics.COMPONENTS, 1);
            }

            mFmeas.setElementAtIndex(0, mFmeasX);
            mFmeas.setElementAtIndex(1, mFmeasY);
            mFmeas.setElementAtIndex(2, mFmeasZ);

            mM.setElementAt(0, 0, m11);
            mM.setElementAt(1, 0, m21);
            mM.setElementAt(2, 0, m31);

            mM.setElementAt(0, 1, m12);
            mM.setElementAt(1, 1, m22);
            mM.setElementAt(2, 1, m32);

            mM.setElementAt(0, 2, m13);
            mM.setElementAt(1, 2, m23);
            mM.setElementAt(2, 2, m33);

            Utils.inverse(mM, mInvM);

            mB.setElementAtIndex(0, bx);
            mB.setElementAtIndex(1, by);
            mB.setElementAtIndex(2, bz);

            mInvM.multiply(mFmeas, mFtrue);
            mFtrue.subtract(mB);

            final double norm = Utils.normF(mFtrue);
            return norm * norm;

        } catch (final AlgebraException e) {
            throw new EvaluationException(e);
        }
    }
}
