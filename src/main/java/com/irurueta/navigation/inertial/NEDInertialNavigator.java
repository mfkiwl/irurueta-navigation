/*
 * Copyright (C) 2019 Alberto Irurueta Carro (alberto@irurueta.com)
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
package com.irurueta.navigation.inertial;

import com.irurueta.algebra.AlgebraException;
import com.irurueta.algebra.Matrix;
import com.irurueta.algebra.Utils;
import com.irurueta.geometry.InvalidRotationMatrixException;
import com.irurueta.navigation.frames.CoordinateTransformation;
import com.irurueta.navigation.frames.FrameType;
import com.irurueta.navigation.frames.InvalidSourceAndDestinationFrameTypeException;
import com.irurueta.navigation.frames.NEDFrame;
import com.irurueta.navigation.geodesic.Constants;
import com.irurueta.navigation.inertial.estimators.NEDGravityEstimator;
import com.irurueta.navigation.inertial.estimators.RadiiOfCurvatureEstimator;
import com.irurueta.units.*;

/**
 * Runs precision local-navigation-frame inertial navigation equations.
 * NOTE: only the attitude update and specific force frame transformation phases are precise).
 * This implementation is based on the equations defined in "Principles of GNSS, Inertial, and Multisensor
 * Integrated Navigation Systems, Second Edition" and on the companion software available at:
 * https://github.com/ymjdz/MATLAB-Codes
 */
@SuppressWarnings("WeakerAccess")
public class NEDInertialNavigator {

    /**
     * Earth rotation rate expressed in radians per second (rad/s).
     */
    public static final double EARTH_ROTATION_RATE = Constants.EARTH_ROTATION_RATE;

    /**
     * Alpha threshold.
     */
    private static final double ALPHA_THRESHOLD = 1e-8;

    /**
     * Number of rows.
     */
    private static final int ROWS = 3;

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final double fx,
                         final double fy,
                         final double fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final double oldLatitude,
                         final double oldLongitude,
                         final double oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final double fx,
                         final double fy,
                         final double fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final double oldVn,
                         final double oldVe,
                         final double oldVd,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final double timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public void navigate(final Time timeInterval,
                         final Angle oldLatitude,
                         final Angle oldLongitude,
                         final Distance oldHeight,
                         final CoordinateTransformation oldC,
                         final Speed oldSpeedN,
                         final Speed oldSpeedE,
                         final Speed oldSpeedD,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final double timeInterval,
                         final NEDFrame oldFrame,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final Time timeInterval,
                         final NEDFrame oldFrame,
                         final double fx,
                         final double fy,
                         final double fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final double timeInterval,
                         final NEDFrame oldFrame,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final Time timeInterval,
                         final NEDFrame oldFrame,
                         final BodyKinematics kinematics,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final double timeInterval,
                         final NEDFrame oldFrame,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final Time timeInterval,
                         final NEDFrame oldFrame,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final double angularRateX,
                         final double angularRateY,
                         final double angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final double timeInterval,
                         final NEDFrame oldFrame,
                         final double fx,
                         final double fy,
                         final double fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final Time timeInterval,
                         final NEDFrame oldFrame,
                         final double fx,
                         final double fy,
                         final double fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final double timeInterval,
                         final NEDFrame oldFrame,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public void navigate(final Time timeInterval,
                         final NEDFrame oldFrame,
                         final Acceleration fx,
                         final Acceleration fy,
                         final Acceleration fz,
                         final AngularSpeed angularRateX,
                         final AngularSpeed angularRateY,
                         final AngularSpeed angularRateZ,
                         final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final double oldLatitude,
                                         final double oldLongitude,
                                         final double oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final double oldVn,
                                         final double oldVe,
                                         final double oldVd,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC, oldVn, oldVe, oldVd,
                fx, fy, fz, angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final Angle oldLatitude,
                                         final Angle oldLongitude,
                                         final Distance oldHeight,
                                         final CoordinateTransformation oldC,
                                         final Speed oldSpeedN,
                                         final Speed oldSpeedE,
                                         final Speed oldSpeedD,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        return navigateNEDAndReturnNew(timeInterval,
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final NEDFrame oldFrame,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final NEDFrame oldFrame,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final NEDFrame oldFrame,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final NEDFrame oldFrame,
                                         final BodyKinematics kinematics)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, kinematics);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final NEDFrame oldFrame,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final NEDFrame oldFrame,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final double angularRateX,
                                         final double angularRateY,
                                         final double angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final NEDFrame oldFrame,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final NEDFrame oldFrame,
                                         final double fx,
                                         final double fy,
                                         final double fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final double timeInterval,
                                         final NEDFrame oldFrame,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public NEDFrame navigateAndReturnNew(final Time timeInterval,
                                         final NEDFrame oldFrame,
                                         final Acceleration fx,
                                         final Acceleration fy,
                                         final Acceleration fz,
                                         final AngularSpeed angularRateX,
                                         final AngularSpeed angularRateY,
                                         final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        return navigateNEDAndReturnNew(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {

        if (!isValidBodyToNEDCoordinateTransformationMatrix(oldC)) {
            throw new InvalidSourceAndDestinationFrameTypeException();
        }

        try {
            // Calculate attitude increment, magnitude, and skew-symmetric matrix
            final Matrix alphaIbb = new Matrix(ROWS, 1);
            alphaIbb.setElementAtIndex(0, angularRateX * timeInterval);
            alphaIbb.setElementAtIndex(1, angularRateY * timeInterval);
            alphaIbb.setElementAtIndex(2, angularRateZ * timeInterval);

            final double magAlpha = Utils.normF(alphaIbb);
            final Matrix skewAlpha = Utils.skewMatrix(alphaIbb);

            // From (2.123), determine the angular rate of the ECEF frame with respect
            // the ECI frame, resolved about NED
            final Matrix omegaIen = new Matrix(ROWS, 1);
            omegaIen.setElementAtIndex(0,
                    Math.cos(oldLatitude) * EARTH_ROTATION_RATE);
            omegaIen.setElementAtIndex(2,
                    -Math.sin(oldLatitude) * EARTH_ROTATION_RATE);

            // From (5.44), determine the angular rate of the NED frame with respect
            // the ECEF frame, resolved about NED
            final RadiiOfCurvature oldRadiiOfCurvature = RadiiOfCurvatureEstimator
                    .estimateRadiiOfCurvatureAndReturnNew(oldLatitude);
            final double oldRe = oldRadiiOfCurvature.getRe();
            final double oldRn = oldRadiiOfCurvature.getRn();

            final double oldRePlusHeight = oldRe + oldHeight;
            final Matrix oldOmegaEnN = new Matrix(ROWS, 1);
            oldOmegaEnN.setElementAtIndex(0, oldVe / oldRePlusHeight);
            oldOmegaEnN.setElementAtIndex(1, -oldVn / (oldRn + oldHeight));
            oldOmegaEnN.setElementAtIndex(2,
                    -oldVe * Math.tan(oldLatitude) / oldRePlusHeight);

            final Matrix oldCbn = oldC.getMatrix();

            final Matrix skewOmega = Utils.skewMatrix(
                    oldOmegaEnN.addAndReturnNew(omegaIen));
            skewOmega.multiplyByScalar(0.5);
            skewOmega.multiply(oldCbn);

            // Calculate the average body-to-ECEF-frame coordinate transformation
            // matrix over the update interval using (5.84) and (5.86)
            final Matrix aveCbn;
            if (magAlpha > ALPHA_THRESHOLD) {
                final double magAlpha2 = magAlpha * magAlpha;
                final double value1 = (1.0 - Math.cos(magAlpha)) / magAlpha2;
                final double value2 = (1.0 - Math.sin(magAlpha) / magAlpha) / magAlpha2;

                final Matrix tmp1 = Matrix.identity(ROWS, ROWS);
                final Matrix tmp2 = skewAlpha.multiplyByScalarAndReturnNew(value1);
                final Matrix tmp3 = skewAlpha.multiplyByScalarAndReturnNew(value2);
                tmp3.multiply(skewAlpha);

                tmp1.add(tmp2);
                tmp1.add(tmp3);

                aveCbn = oldCbn.multiplyAndReturnNew(tmp1);
                aveCbn.subtract(skewOmega);
            } else {
                aveCbn = oldCbn.subtractAndReturnNew(skewOmega);
            }

            // Transform specific force to ECEF-frame resolving axes using (5.86)
            final Matrix fIbb = new Matrix(ROWS, 1);
            fIbb.setElementAtIndex(0, fx);
            fIbb.setElementAtIndex(1, fy);
            fIbb.setElementAtIndex(2, fz);

            // aveCbn now contains specific force fIbn = aveCbn * fIbb
            aveCbn.multiply(fIbb);

            // Update velocity
            // From (5.54),
            final NEDGravity gravity = NEDGravityEstimator
                    .estimateGravityAndReturnNew(oldLatitude, oldHeight);
            final Matrix g = gravity.asMatrix();
            aveCbn.add(g);
            aveCbn.multiplyByScalar(timeInterval);

            final Matrix oldVebn = new Matrix(ROWS, 1);
            oldVebn.setElementAtIndex(0, oldVn);
            oldVebn.setElementAtIndex(1, oldVe);
            oldVebn.setElementAtIndex(2, oldVd);

            final Matrix skewOmega2 = Utils.skewMatrix(
                    oldOmegaEnN.addAndReturnNew(
                            omegaIen.multiplyByScalarAndReturnNew(2.0)));
            skewOmega2.multiply(oldVebn);

            final Matrix vEbn = oldVebn.addAndReturnNew(aveCbn);
            vEbn.subtract(skewOmega2);

            final double vn = vEbn.getElementAtIndex(0);
            final double ve = vEbn.getElementAtIndex(1);
            final double vd = vEbn.getElementAtIndex(2);

            // Update curvilinear position
            // Update height using (5.56)
            final double height = oldHeight - 0.5 * timeInterval * (oldVd + vd);

            // Update latitude using (5.56)
            final double latitude = oldLatitude
                    + 0.5 * timeInterval * (oldVn / (oldRn + oldHeight) + vn / (oldRn + height));

            // Calculate meridian and transverse radii of curvature
            RadiiOfCurvature radiiOfCurvature = RadiiOfCurvatureEstimator
                    .estimateRadiiOfCurvatureAndReturnNew(latitude);
            final double rn = radiiOfCurvature.getRn();
            final double re = radiiOfCurvature.getRe();

            // Update longitude using (5.56)
            final double longitude = oldLongitude
                    + 0.5 * timeInterval * (oldVe / ((oldRe + oldHeight) * Math.cos(oldLatitude))
                    + ve / ((re + height) * Math.cos(latitude)));

            // Attitude update
            // From (5.44), determine the angular rate of the NED frame with respect the
            // ECEF frame, resolved about NED
            final double rePlusHeight = re + height;
            final Matrix omegaEnN = new Matrix(ROWS, 1);
            omegaEnN.setElementAtIndex(0, ve / rePlusHeight);
            omegaEnN.setElementAtIndex(1, -vn / (rn + height));
            omegaEnN.setElementAtIndex(2, -ve * Math.tan(latitude) / rePlusHeight);

            // Obtain coordinate transformation matrix from the new attitude with respect
            // an intertial frame to the old using Rodrigues' formula, (5.73)
            final Matrix cNewOld = Matrix.identity(ROWS, ROWS);
            if (magAlpha > ALPHA_THRESHOLD) {
                final double magAlpha2 = magAlpha * magAlpha;
                final double value1 = Math.sin(magAlpha) / magAlpha;
                final double value2 = (1.0 - Math.cos(magAlpha)) / magAlpha2;

                final Matrix tmp1 = skewAlpha.multiplyByScalarAndReturnNew(value1);
                final Matrix tmp2 = skewAlpha.multiplyByScalarAndReturnNew(value2);
                tmp2.multiply(skewAlpha);

                cNewOld.add(tmp1);
                cNewOld.add(tmp2);
            } else {
                cNewOld.add(skewAlpha);
            }

            // Update attitude using (5.77)
            omegaEnN.multiplyByScalar(0.5);
            oldOmegaEnN.multiplyByScalar(0.5);
            omegaIen.add(omegaEnN);
            omegaIen.add(oldOmegaEnN);

            final Matrix skewOmega3 = Utils.skewMatrix(omegaIen);
            skewOmega3.multiplyByScalar(timeInterval);

            final Matrix cbn = Matrix.identity(ROWS, ROWS);
            cbn.subtract(skewOmega3);
            cbn.multiply(oldCbn);
            cbn.multiply(cNewOld);

            result.setPosition(latitude, longitude, height);
            result.setVelocityCoordinates(vn, ve, vd);

            // normalize cbn to ensure it remains valid
            final double detCbn = Utils.det(cbn);
            cbn.multiplyByScalar(Math.pow(1.0 / detCbn, 1.0 / ROWS));

            final CoordinateTransformation c = new CoordinateTransformation(cbn,
                    FrameType.BODY_FRAME, FrameType.LOCAL_NAVIGATION_FRAME);
            result.setCoordinateTransformation(c);

        } catch (final AlgebraException | InvalidRotationMatrixException e) {
            throw new InertialNavigatorException(e);
        }
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY, angularRateZ,
                result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {

        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics.getFx(), kinematics.getFy(),
                kinematics.getFz(), kinematics.getAngularRateX(),
                kinematics.getAngularRateY(), kinematics.getAngularRateZ(), result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval),
                oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, convertAngleToDouble(oldLatitude),
                convertAngleToDouble(oldLongitude), convertDistanceToDouble(oldHeight), oldC,
                oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY, angularRateZ,
                result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY,
                angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, convertAngleToDouble(oldLatitude),
                convertAngleToDouble(oldLongitude), convertDistanceToDouble(oldHeight), oldC,
                oldVn, oldVe, oldVd, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude,
                oldHeight, oldC, oldVn, oldVe, oldVd, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                convertSpeedToDouble(oldSpeedN), convertSpeedToDouble(oldSpeedE),
                convertSpeedToDouble(oldSpeedD), fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics.getFx(), kinematics.getFy(),
                kinematics.getFz(), kinematics.getAngularRateX(), kinematics.getAngularRateY(),
                kinematics.getAngularRateZ(), result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, convertAccelerationToDouble(fx),
                convertAccelerationToDouble(fy),
                convertAccelerationToDouble(fz),
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude,
                oldHeight, oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                convertAngularSpeedToDouble(angularRateX),
                convertAngularSpeedToDouble(angularRateY),
                convertAngularSpeedToDouble(angularRateZ), result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final double oldLatitude,
                                   final double oldLongitude,
                                   final double oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude,
                oldHeight, oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, convertAngleToDouble(oldLatitude),
                convertAngleToDouble(oldLongitude), convertDistanceToDouble(oldHeight),
                oldC, convertSpeedToDouble(oldSpeedN), convertSpeedToDouble(oldSpeedE),
                convertSpeedToDouble(oldSpeedD), fx, fy, fz, angularRateX, angularRateY,
                angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, convertAngleToDouble(oldLatitude),
                convertAngleToDouble(oldLongitude), convertDistanceToDouble(oldHeight),
                oldC, convertSpeedToDouble(oldSpeedN), convertSpeedToDouble(oldSpeedE),
                convertSpeedToDouble(oldSpeedD), convertAccelerationToDouble(fx),
                convertAccelerationToDouble(fy), convertAccelerationToDouble(fz),
                convertAngularSpeedToDouble(angularRateX),
                convertAngularSpeedToDouble(angularRateY),
                convertAngularSpeedToDouble(angularRateZ), result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, convertAngleToDouble(oldLatitude),
                convertAngleToDouble(oldLongitude), convertDistanceToDouble(oldHeight),
                oldC, oldVn, oldVe, oldVd, convertAccelerationToDouble(fx),
                convertAccelerationToDouble(fy), convertAccelerationToDouble(fz),
                convertAngularSpeedToDouble(angularRateX),
                convertAngularSpeedToDouble(angularRateY),
                convertAngularSpeedToDouble(angularRateZ), result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final double oldVn,
                                   final double oldVe,
                                   final double oldVd,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(convertTimeToDouble(timeInterval), oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY,
                angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final double timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                convertSpeedToDouble(oldSpeedN),
                convertSpeedToDouble(oldSpeedE),
                convertSpeedToDouble(oldSpeedD), kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static void navigateNED(final Time timeInterval,
                                   final Angle oldLatitude,
                                   final Angle oldLongitude,
                                   final Distance oldHeight,
                                   final CoordinateTransformation oldC,
                                   final Speed oldSpeedN,
                                   final Speed oldSpeedE,
                                   final Speed oldSpeedD,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                convertSpeedToDouble(oldSpeedN),
                convertSpeedToDouble(oldSpeedE),
                convertSpeedToDouble(oldSpeedD), kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final double timeInterval,
                                   final NEDFrame oldFrame,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        try {
            navigateNED(timeInterval, oldFrame.getLatitude(), oldFrame.getLongitude(),
                    oldFrame.getHeight(), oldFrame.getCoordinateTransformation(),
                    oldFrame.getVn(), oldFrame.getVe(), oldFrame.getVd(),
                    fx, fy, fz, angularRateX, angularRateY, angularRateZ, result);
        } catch (final InvalidSourceAndDestinationFrameTypeException ignore) {
            // never happens
        }
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final Time timeInterval,
                                   final NEDFrame oldFrame,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(convertTimeToDouble(timeInterval), oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final double timeInterval,
                                   final NEDFrame oldFrame,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        try {
            navigateNED(timeInterval, oldFrame.getLatitude(), oldFrame.getLongitude(),
                    oldFrame.getHeight(), oldFrame.getCoordinateTransformation(),
                    oldFrame.getVn(), oldFrame.getVe(), oldFrame.getVd(), kinematics,
                    result);
        } catch (final InvalidSourceAndDestinationFrameTypeException ignore) {
            // never happens
        }
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final Time timeInterval,
                                   final NEDFrame oldFrame,
                                   final BodyKinematics kinematics,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(convertTimeToDouble(timeInterval), oldFrame, kinematics, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final double timeInterval,
                                   final NEDFrame oldFrame,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        try {
            navigateNED(timeInterval, oldFrame.getLatitude(), oldFrame.getLongitude(),
                    oldFrame.getHeight(), oldFrame.getCoordinateTransformation(),
                    oldFrame.getVn(), oldFrame.getVe(), oldFrame.getVd(),
                    fx, fy, fz, angularRateX, angularRateY, angularRateZ, result);
        } catch (final InvalidSourceAndDestinationFrameTypeException ignore) {
            // never happens
        }
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final Time timeInterval,
                                   final NEDFrame oldFrame,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final double angularRateX,
                                   final double angularRateY,
                                   final double angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(convertTimeToDouble(timeInterval), oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final double timeInterval,
                                   final NEDFrame oldFrame,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        try {
            navigateNED(timeInterval, oldFrame.getLatitude(), oldFrame.getLongitude(),
                    oldFrame.getHeight(), oldFrame.getCoordinateTransformation(),
                    oldFrame.getVn(), oldFrame.getVe(), oldFrame.getVd(),
                    fx, fy, fz, angularRateX, angularRateY, angularRateZ, result);
        } catch (final InvalidSourceAndDestinationFrameTypeException ignore) {
            // never happens
        }
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final Time timeInterval,
                                   final NEDFrame oldFrame,
                                   final double fx,
                                   final double fy,
                                   final double fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(convertTimeToDouble(timeInterval), oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final double timeInterval,
                                   final NEDFrame oldFrame,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        try {
            navigateNED(timeInterval, oldFrame.getLatitude(), oldFrame.getLongitude(),
                    oldFrame.getHeight(), oldFrame.getCoordinateTransformation(),
                    oldFrame.getVn(), oldFrame.getVe(), oldFrame.getVd(),
                    convertAccelerationToDouble(fx),
                    convertAccelerationToDouble(fy),
                    convertAccelerationToDouble(fz),
                    angularRateX, angularRateY, angularRateZ, result);
        } catch (final InvalidSourceAndDestinationFrameTypeException ignore) {
            // never happens
        }
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param result       instance where new estimated NED frame containing new body position,
     *                     velocity and coordinate transformation matrix will be stored.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static void navigateNED(final Time timeInterval,
                                   final NEDFrame oldFrame,
                                   final Acceleration fx,
                                   final Acceleration fy,
                                   final Acceleration fz,
                                   final AngularSpeed angularRateX,
                                   final AngularSpeed angularRateY,
                                   final AngularSpeed angularRateZ,
                                   final NEDFrame result)
            throws InertialNavigatorException {
        navigateNED(convertTimeToDouble(timeInterval), oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY,
                angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY, angularRateZ,
                result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY, angularRateZ,
                result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY,
                angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude,
                oldHeight, oldC, oldVn, oldVe, oldVd, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude,
                oldHeight, oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude expressed in radians (rad).
     * @param oldLongitude previous longitude expressed in radians (rad).
     * @param oldHeight    previous height expressed in meters (m).
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final double oldLatitude,
                                                   final double oldLongitude,
                                                   final double oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude,
                oldHeight, oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldSpeedN, oldSpeedE, oldSpeedD, fx, fy, fz, angularRateX,
                angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldVn        previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVe        previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param oldVd        previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes and expressed in meters per second (m/s).
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final double oldVn,
                                                   final double oldVe,
                                                   final double oldVd,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight,
                oldC, oldVn, oldVe, oldVd, fx, fy, fz, angularRateX, angularRateY,
                angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldLatitude  previous latitude angle.
     * @param oldLongitude previous longitude angle.
     * @param oldHeight    previous height.
     * @param oldC         previous body-to-NED coordinate transformation.
     * @param oldSpeedN    previous velocity north-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedE    previous velocity east-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param oldSpeedD    previous velocity down-coordinate of body frame with respect ECEF frame,
     *                     resolved along NED-frame axes.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException                    if navigation fails due to numerical instabilities.
     * @throws InvalidSourceAndDestinationFrameTypeException if source or destination frame types of previous
     *                                                       body-to-NED-frame coordinate transformation matrix are
     *                                                       invalid.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final Angle oldLatitude,
                                                   final Angle oldLongitude,
                                                   final Distance oldHeight,
                                                   final CoordinateTransformation oldC,
                                                   final Speed oldSpeedN,
                                                   final Speed oldSpeedE,
                                                   final Speed oldSpeedD,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException, InvalidSourceAndDestinationFrameTypeException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldLatitude, oldLongitude, oldHeight, oldC,
                oldSpeedN, oldSpeedE, oldSpeedD, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param kinematics   body kinematics containing specific forces and angular rates applied to
     *                     the body.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final BodyKinematics kinematics)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, kinematics, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in radians per second (rad/s).
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final double angularRateX,
                                                   final double angularRateY,
                                                   final double angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz, angularRateX, angularRateY,
                angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs.
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final double fx,
                                                   final double fy,
                                                   final double fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final double timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Runs precision local-navigation-frame inertial navigation equations.
     * NOTE: only the attitude update and specific force frame transformation
     * phases are precise.
     *
     * @param timeInterval time interval between epochs expressed in seconds (s).
     * @param oldFrame     previous NED frame containing body position, velocity and
     *                     coordinate transformation matrix.
     * @param fx           specific force x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fy           specific force y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param fz           specific force z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval and
     *                     expressed in meters per squared second (m/s^2).
     * @param angularRateX angular rate x-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateY angular rate y-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @param angularRateZ angular rate z-coordinate of body frame with respect ECEF frame,
     *                     resolved along body-frame axes, averaged over time interval.
     * @return estimated NED frame containing new body position, velocity and coordinate
     * transformation matrix.
     * @throws InertialNavigatorException if navigation fails due to numerical instabilities.
     */
    public static NEDFrame navigateNEDAndReturnNew(final Time timeInterval,
                                                   final NEDFrame oldFrame,
                                                   final Acceleration fx,
                                                   final Acceleration fy,
                                                   final Acceleration fz,
                                                   final AngularSpeed angularRateX,
                                                   final AngularSpeed angularRateY,
                                                   final AngularSpeed angularRateZ)
            throws InertialNavigatorException {
        final NEDFrame result = new NEDFrame();
        navigateNED(timeInterval, oldFrame, fx, fy, fz,
                angularRateX, angularRateY, angularRateZ, result);
        return result;
    }

    /**
     * Checks whether provided coordinate transformation matrix is valid or not.
     * Only body to NED transformation matrices are considered to be valid.
     *
     * @param c coordinate transformation matrix to be checked.
     * @return true if provided value is valid, false otherwise.
     */
    public static boolean isValidBodyToNEDCoordinateTransformationMatrix(final CoordinateTransformation c) {
        return NEDFrame.isValidCoordinateTransformation(c);
    }

    /**
     * Converts provided time instance into its corresponding value expressed in
     * seconds.
     *
     * @param time time instance to be converted.
     * @return converted value expressed in seconds.
     */
    private static double convertTimeToDouble(final com.irurueta.units.Time time) {
        return TimeConverter.convert(time.getValue().doubleValue(), time.getUnit(),
                TimeUnit.SECOND);
    }

    /**
     * Converts provided angle instance into its corresponding value expressed in
     * radians.
     *
     * @param angle angle instance to be converted.
     * @return converted value expressed in meters.
     */
    private static double convertAngleToDouble(final Angle angle) {
        return AngleConverter.convert(angle.getValue().doubleValue(),
                angle.getUnit(), AngleUnit.RADIANS);
    }

    /**
     * Converts provided distance instance into its corresponding value expressed in
     * meters.
     *
     * @param distance distance instance to be converted.
     * @return converted value expressed in meters.
     */
    private static double convertDistanceToDouble(final Distance distance) {
        return DistanceConverter.convert(distance.getValue().doubleValue(),
                distance.getUnit(), DistanceUnit.METER);
    }

    /**
     * Converts provided speed instance into its corresponding value expressed in
     * meters per second.
     *
     * @param speed speed instance to be converted.
     * @return converted value expressed in meters per second.
     */
    private static double convertSpeedToDouble(final Speed speed) {
        return SpeedConverter.convert(speed.getValue().doubleValue(),
                speed.getUnit(), SpeedUnit.METERS_PER_SECOND);
    }

    /**
     * Converts provided acceleration instance into its corresponding value expressed
     * in meters per squared second.
     *
     * @param acceleration acceleration instance to be converted.
     * @return converted value expressed in meters per squared second.
     */
    private static double convertAccelerationToDouble(final Acceleration acceleration) {
        return AccelerationConverter.convert(acceleration.getValue().doubleValue(),
                acceleration.getUnit(), AccelerationUnit.METERS_PER_SQUARED_SECOND);
    }

    /**
     * Converts provided angular speed into its corresponding value expressed in
     * radians per second.
     *
     * @param angularSpeed angular speed instance to be converted.
     * @return converted value expressed in radians per second.
     */
    private static double convertAngularSpeedToDouble(final AngularSpeed angularSpeed) {
        return AngularSpeedConverter.convert(angularSpeed.getValue().doubleValue(),
                angularSpeed.getUnit(), AngularSpeedUnit.RADIANS_PER_SECOND);
    }
}
