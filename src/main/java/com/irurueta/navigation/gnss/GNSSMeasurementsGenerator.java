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
package com.irurueta.navigation.gnss;

import com.irurueta.algebra.Matrix;
import com.irurueta.algebra.Utils;
import com.irurueta.algebra.WrongSizeException;
import com.irurueta.navigation.frames.CoordinateTransformation;
import com.irurueta.navigation.frames.converters.ECEFtoNEDPositionVelocityConverter;
import com.irurueta.navigation.geodesic.Constants;
import com.irurueta.navigation.inertial.ECEFPosition;
import com.irurueta.navigation.inertial.ECEFVelocity;
import com.irurueta.navigation.inertial.NEDPosition;
import com.irurueta.navigation.inertial.NEDVelocity;
import com.irurueta.units.Time;
import com.irurueta.units.TimeConverter;
import com.irurueta.units.TimeUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Generates satellite GNSS measurement data.
 * This implementation is based on the equations defined in "Principles of GNSS, Inertial, and Multisensor
 * Integrated Navigation Systems, Second Edition" and on the companion software available at:
 * https://github.com/ymjdz/MATLAB-Codes/blob/master/Generate_GNSS_measurements.m
 */
public class GNSSMeasurementsGenerator {

    /**
     * Speed of light in the vacuum expressed in meters per second (m/s).
     */
    public static final double SPEED_OF_LIGHT = Constants.SPEED_OF_LIGHT;

    /**
     * Earth rotation rate expressed in radians per second (rad/s).
     */
    public static final double EARTH_ROTATION_RATE = Constants.EARTH_ROTATION_RATE;

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time.
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPositionAndVelocity         user position and velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each
     *                                        satellite position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @return collection of GNSS measurements.
     */
    public static Collection<GNSSMeasurement> generate(
            final Time time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random) {
        return generate(convertTime(time), satellitePositionsAndVelocities,
                userPositionAndVelocity, gnssRangeErrorBiases, config, random);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time.
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPositionAndVelocity         user position and velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each
     *                                        satellite position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @param result                          instance where resulting collection of
     *                                        GNSS measurements are stored.
     */
    public static void generate(
            final Time time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random, final Collection<GNSSMeasurement> result) {
        generate(convertTime(time), satellitePositionsAndVelocities,
                userPositionAndVelocity, gnssRangeErrorBiases, config, random,
                result);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time expressed in
     *                                        seconds (s).
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPositionAndVelocity         user position and velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each
     *                                        satellite position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @return collection of GNSS measurements.
     */
    public static Collection<GNSSMeasurement> generate(
            final double time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random) {
        return generate(time, satellitePositionsAndVelocities, userPositionAndVelocity.getX(),
                userPositionAndVelocity.getY(), userPositionAndVelocity.getZ(),
                userPositionAndVelocity.getVx(), userPositionAndVelocity.getVy(),
                userPositionAndVelocity.getVz(), gnssRangeErrorBiases, config, random);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time expressed in
     *                                        seconds (s).
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPositionAndVelocity         user position and velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each
     *                                        satellite position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @param result                          instance where resulting collection of
     *                                        GNSS measurements are stored.
     */
    public static void generate(
            final double time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random, final Collection<GNSSMeasurement> result) {
        generate(time, satellitePositionsAndVelocities, userPositionAndVelocity.getX(),
                userPositionAndVelocity.getY(), userPositionAndVelocity.getZ(),
                userPositionAndVelocity.getVx(), userPositionAndVelocity.getVy(),
                userPositionAndVelocity.getVz(), gnssRangeErrorBiases, config, random,
                result);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time.
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPosition                    user position.
     * @param userVelocity                    user velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each satellite
     *                                        position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @return collection of GNSS measurements.
     */
    public static Collection<GNSSMeasurement> generate(
            final Time time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPosition userPosition, final ECEFVelocity userVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random) {
        return generate(convertTime(time), satellitePositionsAndVelocities,
                userPosition, userVelocity, gnssRangeErrorBiases, config,
                random);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time.
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPosition                    user position.
     * @param userVelocity                    user velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each satellite
     *                                        position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @param result                          instance where resulting collection of
     *                                        GNSS measurements are stored.
     */
    public static void generate(
            final Time time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPosition userPosition, final ECEFVelocity userVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random, final Collection<GNSSMeasurement> result) {
        generate(convertTime(time), satellitePositionsAndVelocities,
                userPosition, userVelocity, gnssRangeErrorBiases, config,
                random, result);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time expressed in
     *                                        seconds (s).
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPosition                    user position.
     * @param userVelocity                    user velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each satellite
     *                                        position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @return collection of GNSS measurements.
     */
    public static Collection<GNSSMeasurement> generate(
            final double time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPosition userPosition, final ECEFVelocity userVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random) {
        return generate(time, satellitePositionsAndVelocities,
                userPosition.getX(), userPosition.getY(), userPosition.getZ(),
                userVelocity.getVx(), userVelocity.getVy(), userVelocity.getVz(),
                gnssRangeErrorBiases, config, random);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time expressed in
     *                                        seconds (s).
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userPosition                    user position.
     * @param userVelocity                    user velocity.
     * @param gnssRangeErrorBiases            GNSS range error biases for each
     *                                        satellite position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @param result                          instance where resulting collection of
     *                                        GNSS measurements are stored.
     */
    public static void generate(
            final double time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final ECEFPosition userPosition, final ECEFVelocity userVelocity,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random, final Collection<GNSSMeasurement> result) {
        generate(time, satellitePositionsAndVelocities,
                userPosition.getX(), userPosition.getY(), userPosition.getZ(),
                userVelocity.getVx(), userVelocity.getVy(), userVelocity.getVz(),
                gnssRangeErrorBiases, config, random, result);
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simulation time expressed in
     *                                        seconds (s).
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userX                           x ECEF coordinate of user position
     *                                        expressed in meters (m).
     * @param userY                           y ECEF coordinate of user position
     *                                        expressed in meters (m).
     * @param userZ                           z ECEF coordinate of user position
     *                                        expressed in meters (m).
     * @param userVx                          x ECEF coordinate of user velocity
     *                                        expressed in meters per second (m/s).
     * @param userVy                          y ECEF coordinate of user velocity
     *                                        expressed in meters per second (m/s).
     * @param userVz                          z ECEF coordinate of user velocity
     *                                        expressed in meters per second (m/s).
     * @param gnssRangeErrorBiases            GNSS range error biases for each
     *                                        satellite position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @return collection of GNSS measurements.
     */
    public static Collection<GNSSMeasurement> generate(
            final double time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final double userX, final double userY, final double userZ,
            final double userVx, final double userVy, final double userVz,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random) {
        final List<GNSSMeasurement> result = new ArrayList<>();
        generate(time, satellitePositionsAndVelocities, userX, userY, userZ,
                userVx, userVy, userVz, gnssRangeErrorBiases, config, random, result);
        return result;
    }

    /**
     * Generates satellite GNSS measurements.
     *
     * @param time                            current simultion time expressed in
     *                                        seconds (s).
     * @param satellitePositionsAndVelocities satellite positions and velocities.
     * @param userX                           x ECEF coordinate of user position
     *                                        expressed in meters (m).
     * @param userY                           y ECEF coordinate of user position
     *                                        expressed in meters (m).
     * @param userZ                           z ECEF coordinate of user position
     *                                        expressed in meters (m).
     * @param userVx                          x ECEF coordinate of user velocity
     *                                        expressed in meters per second (m/s).
     * @param userVy                          y ECEF coordinate of user velocity
     *                                        expressed in meters per second (m/s).
     * @param userVz                          z ECEF coordinate of user velocity
     *                                        expressed in meters per second (m/s).
     * @param gnssRangeErrorBiases            GNSS range error biases for each
     *                                        satellite position and velocity.
     * @param config                          GNSS configuration parameters.
     * @param random                          random number generator.
     * @param result                          instance where resulting collection of
     *                                        GNSS measurements are stored.
     */
    public static void generate(
            final double time, final List<ECEFPositionAndVelocity> satellitePositionsAndVelocities,
            final double userX, final double userY, final double userZ,
            final double userVx, final double userVy, final double userVz,
            final List<Double> gnssRangeErrorBiases, final GNSSConfig config,
            final Random random, final Collection<GNSSMeasurement> result) {

        if (satellitePositionsAndVelocities.size() != gnssRangeErrorBiases.size()) {
            throw new IllegalArgumentException();
        }

        result.clear();

        int pos = 0;
        for (ECEFPositionAndVelocity satellitePositionAndVelocity : satellitePositionsAndVelocities) {
            final Double gnssRangeErrorBias = gnssRangeErrorBiases.get(pos);
            pos++;

            if (gnssRangeErrorBias == null) {
                continue;
            }

            final GNSSMeasurement measurement = generate(time,
                    satellitePositionAndVelocity.getX(),
                    satellitePositionAndVelocity.getY(),
                    satellitePositionAndVelocity.getZ(),
                    satellitePositionAndVelocity.getVx(),
                    satellitePositionAndVelocity.getVy(),
                    satellitePositionAndVelocity.getVz(),
                    userX, userY, userZ,
                    userVx, userVy, userVz, gnssRangeErrorBias, config,
                    random);
            if (measurement != null) {
                result.add(measurement);
            }
        }
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time                         current simulation time.
     * @param satellitePositionAndVelocity satellite position and velocity.
     * @param userPositionAndVelocity      user position and velocity.
     * @param gnssRangeErrorBias           GNSS range error bias.
     * @param config                       GNSS configuration parameters.
     * @param random                       random number generator.
     * @return a new GNSS measurement.
     */
    public static GNSSMeasurement generate(
            final Time time, final ECEFPositionAndVelocity satellitePositionAndVelocity,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final double gnssRangeErrorBias, final GNSSConfig config,
            final Random random) {
        return generate(convertTime(time), satellitePositionAndVelocity,
                userPositionAndVelocity, gnssRangeErrorBias, config, random);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time                         current simulation time.
     * @param satellitePositionAndVelocity satellite position and velocity.
     * @param userPositionAndVelocity      user position and velocity.
     * @param gnssRangeErrorBias           GNSS range error bias.
     * @param config                       GNSS configuration parameters.
     * @param random                       random number generator.
     * @param result                       instance where resulting GNSS measurement
     *                                     is stored.
     * @return true if result has been obtained, failed if satellite is below elevation
     * mask angle and result is not updated.
     */
    public static boolean generate(
            final Time time, final ECEFPositionAndVelocity satellitePositionAndVelocity,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final double gnssRangeErrorBias, final GNSSConfig config,
            final Random random, final GNSSMeasurement result) {
        return generate(convertTime(time), satellitePositionAndVelocity,
                userPositionAndVelocity, gnssRangeErrorBias, config, random, result);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time                         current simulation time expressed in
     *                                     seconds (s).
     * @param satellitePositionAndVelocity satellite position and velocity.
     * @param userPositionAndVelocity      user position and velocity.
     * @param gnssRangeErrorBias           GNSS range error bias.
     * @param config                       GNSS configuration parameters.
     * @param random                       random number generator.
     * @return a new GNSS measurement.
     */
    public static GNSSMeasurement generate(
            final double time, final ECEFPositionAndVelocity satellitePositionAndVelocity,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final double gnssRangeErrorBias, final GNSSConfig config,
            final Random random) {
        return generate(time, satellitePositionAndVelocity.getX(),
                satellitePositionAndVelocity.getY(), satellitePositionAndVelocity.getZ(),
                satellitePositionAndVelocity.getVx(), satellitePositionAndVelocity.getVy(),
                satellitePositionAndVelocity.getVz(), userPositionAndVelocity.getX(),
                userPositionAndVelocity.getY(), userPositionAndVelocity.getZ(),
                userPositionAndVelocity.getVx(), userPositionAndVelocity.getVy(),
                userPositionAndVelocity.getVz(), gnssRangeErrorBias, config, random);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time                         current simulation time expressed in
     *                                     seconds (s).
     * @param satellitePositionAndVelocity satellite position and velocity.
     * @param userPositionAndVelocity      user position and velocity.
     * @param gnssRangeErrorBias           GNSS range error bias.
     * @param config                       GNSS configuration parameters.
     * @param random                       random number generator.
     * @param result                       instance where resulting GNSS measurement
     *                                     is stored.
     * @return true if result has been obtained, failed if satellite is below elevation
     * mask angle and result is not updated.
     */
    public static boolean generate(
            final double time,
            final ECEFPositionAndVelocity satellitePositionAndVelocity,
            final ECEFPositionAndVelocity userPositionAndVelocity,
            final double gnssRangeErrorBias,
            final GNSSConfig config, final Random random, final GNSSMeasurement result) {
        return generate(time, satellitePositionAndVelocity.getX(),
                satellitePositionAndVelocity.getY(), satellitePositionAndVelocity.getZ(),
                satellitePositionAndVelocity.getVx(), satellitePositionAndVelocity.getVy(),
                satellitePositionAndVelocity.getVz(), userPositionAndVelocity.getX(),
                userPositionAndVelocity.getY(), userPositionAndVelocity.getZ(),
                userPositionAndVelocity.getVx(), userPositionAndVelocity.getVy(),
                userPositionAndVelocity.getVz(), gnssRangeErrorBias, config, random, result);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time               current simulation time.
     * @param satellitePosition  satellite position.
     * @param satelliteVelocity  satellite velocity.
     * @param userPosition       user position.
     * @param userVelocity       user velocity.
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param random             random number generator.
     * @return a new GNSS measurement.
     */
    public static GNSSMeasurement generate(
            final Time time, final ECEFPosition satellitePosition,
            final ECEFVelocity satelliteVelocity, final ECEFPosition userPosition,
            final ECEFVelocity userVelocity, final double gnssRangeErrorBias,
            final GNSSConfig config, final Random random) {
        return generate(convertTime(time), satellitePosition, satelliteVelocity,
                userPosition, userVelocity, gnssRangeErrorBias, config, random);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time               current simulation time.
     * @param satellitePosition  satellite position.
     * @param satelliteVelocity  satellite velocity.
     * @param userPosition       user position.
     * @param userVelocity       user velocity.
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param random             random number generator.
     * @param result             instance where resulting GNSS measurement is stored.
     * @return true if result has been obtained, failed if satellite is below elevation
     * mask angle and result is not updated.
     */
    public static boolean generate(
            final Time time, final ECEFPosition satellitePosition,
            final ECEFVelocity satelliteVelocity, final ECEFPosition userPosition,
            final ECEFVelocity userVelocity, final double gnssRangeErrorBias,
            final GNSSConfig config, final Random random, final GNSSMeasurement result) {
        return generate(convertTime(time), satellitePosition,
                satelliteVelocity, userPosition, userVelocity,
                gnssRangeErrorBias, config, random, result);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time               current simulation time expressed in seconds (s).
     * @param satellitePosition  satellite position.
     * @param satelliteVelocity  satellite velocity.
     * @param userPosition       user position.
     * @param userVelocity       user velocity.
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param random             random number generator.
     * @return a new GNSS measurement.
     */
    public static GNSSMeasurement generate(
            final double time, final ECEFPosition satellitePosition,
            final ECEFVelocity satelliteVelocity, final ECEFPosition userPosition,
            final ECEFVelocity userVelocity, final double gnssRangeErrorBias,
            final GNSSConfig config, final Random random) {
        return generate(time,
                satellitePosition.getX(), satellitePosition.getY(), satellitePosition.getZ(),
                satelliteVelocity.getVx(), satelliteVelocity.getVy(), satelliteVelocity.getVz(),
                userPosition.getX(), userPosition.getY(), userPosition.getZ(),
                userVelocity.getVx(), userVelocity.getVy(), userVelocity.getVz(),
                gnssRangeErrorBias, config, random);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time               current simulation time expressed in seconds (s).
     * @param satellitePosition  satellite position.
     * @param satelliteVelocity  satellite velocity.
     * @param userPosition       user position.
     * @param userVelocity       user velocity.
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param random             random number generator.
     * @param result             instance where resulting GNSS measurement
     *                           is stored.
     * @return true if result has been obtained, failed if satellite is below elevation
     * mask angle and result is not updated.
     */
    public static boolean generate(
            final double time, final ECEFPosition satellitePosition,
            final ECEFVelocity satelliteVelocity, final ECEFPosition userPosition,
            final ECEFVelocity userVelocity, final double gnssRangeErrorBias,
            final GNSSConfig config, final Random random, final GNSSMeasurement result) {
        return generate(time,
                satellitePosition.getX(), satellitePosition.getY(), satellitePosition.getZ(),
                satelliteVelocity.getVx(), satelliteVelocity.getVy(), satelliteVelocity.getVz(),
                userPosition.getX(), userPosition.getY(), userPosition.getZ(),
                userVelocity.getVx(), userVelocity.getVy(), userVelocity.getVz(),
                gnssRangeErrorBias, config, random, result);
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time               current simulation time expressed in seconds (s).
     * @param satelliteX         x ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteY         y ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteZ         z ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteVx        x ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVy        y ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVz        z ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param userX              x ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userY              y ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userZ              z ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userVx             x ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVy             y ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVz             z ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param random             random number generator.
     * @return a new GNSS measurement.
     */
    public static GNSSMeasurement generate(
            final double time, final double satelliteX, final double satelliteY,
            final double satelliteZ, final double satelliteVx, final double satelliteVy,
            final double satelliteVz, final double userX, final double userY,
            final double userZ, final double userVx, final double userVy,
            final double userVz, final double gnssRangeErrorBias,
            final GNSSConfig config, final Random random) {
        final GNSSMeasurement result = new GNSSMeasurement();
        if (generate(time, satelliteX, satelliteY, satelliteZ,
                satelliteVx, satelliteVy, satelliteVz,
                userX, userY, userZ, userVx, userVy, userVz,
                gnssRangeErrorBias, config, random, result)) {
            return result;
        } else {
            return null;
        }
    }

    /**
     * Generates a single satellite GNSS measurement.
     *
     * @param time               current simulation time expressed in seconds (s).
     * @param satelliteX         x ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteY         y ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteZ         z ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteVx        x ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVy        y ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVz        z ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param userX              x ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userY              y ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userZ              z ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userVx             x ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVy             y ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVz             z ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param random             random number generator.
     * @param result             instance where resulting GNSS measurement
     *                           is stored.
     * @return true if result has been obtained, failed if satellite is below elevation
     * mask angle and result is not updated.
     */
    public static boolean generate(
            final double time, final double satelliteX, final double satelliteY,
            final double satelliteZ, final double satelliteVx, final double satelliteVy,
            final double satelliteVz, final double userX, final double userY,
            final double userZ, final double userVx, final double userVy,
            final double userVz, final double gnssRangeErrorBias,
            final GNSSConfig config, final Random random, final GNSSMeasurement result) {
        final NEDPosition userNedPosition = new NEDPosition();
        final NEDVelocity userNedVelocity = new NEDVelocity();
        ECEFtoNEDPositionVelocityConverter.convertECEFtoNED(
                userX, userY, userZ,
                userVx, userVy, userVz, userNedPosition, userNedVelocity);
        final double userLatitude = userNedPosition.getLatitude();
        final double userLongitude = userNedPosition.getLongitude();

        return generate(time, satelliteX, satelliteY, satelliteZ,
                satelliteVx, satelliteVy, satelliteVz,
                userX, userY, userZ, userLatitude, userLongitude,
                userVx, userVy, userVz, gnssRangeErrorBias,
                config, random, result);
    }

    /**
     * Internal method to generate a single satellite GNSS measurement.
     *
     * @param time               current simulation time expressed in seconds (s).
     * @param satelliteX         x ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteY         y ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteZ         z ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteVx        x ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVy        y ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVz        z ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param userX              x ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userY              y ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userZ              z ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userLatitude       latitude of user NED position expressed in
     *                           radians (rad).
     * @param userLongitude      longitude of user NED position expressed in
     *                           radians (rad).
     * @param userVx             x ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVy             y ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVz             z ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param random             random number generator.
     * @param result             instance where resulting GNSS measurement
     *                           is stored.
     * @return true if result has been obtained, failed if satellite is below elevation
     * mask angle and result is not updated.
     */
    private static boolean generate(
            final double time, final double satelliteX, final double satelliteY,
            final double satelliteZ, final double satelliteVx, final double satelliteVy,
            final double satelliteVz, final double userX, final double userY,
            final double userZ, final double userLatitude, final double userLongitude,
            final double userVx, final double userVy, final double userVz,
            final double gnssRangeErrorBias, final GNSSConfig config,
            final Random random, final GNSSMeasurement result) {

        // Calculate ECEF to NED coordinate transformation matrix using (2.150)
        final Matrix cen = CoordinateTransformation.ecefToNedMatrix(userLatitude, userLongitude);

        // Skew symmetric matrix of Earth rate
        try {
            final Matrix omegaIe = Utils.skewMatrix(new double[]{0.0, 0.0, EARTH_ROTATION_RATE});
            final Matrix cei = Matrix.identity(CoordinateTransformation.ROWS,
                    CoordinateTransformation.COLS);
            final Matrix satellitePosition = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix deltaR = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix satelliteVelocity = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix userPosition = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix userVelocity = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix tmp1 = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix tmp2 = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix tmp3 = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix tmp4 = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix tmp5 = new Matrix(CoordinateTransformation.ROWS, 1);
            final Matrix tmp6 = new Matrix(CoordinateTransformation.ROWS, 1);

            return generate(time, satelliteX, satelliteY, satelliteZ, satelliteVx, satelliteVy, satelliteVz,
                    userX, userY, userZ, userVx, userVy, userVz, gnssRangeErrorBias, config, cen,
                    omegaIe, cei, satellitePosition, deltaR, satelliteVelocity, userPosition,
                    userVelocity, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, random, result);
        } catch (final WrongSizeException ignore) {
            return false;
        }
    }

    /**
     * Internal method to generate a single satellite GNSS measurement.
     *
     * @param time               current simulation time expressed in seconds (s).
     * @param satelliteX         x ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteY         y ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteZ         z ECEF coordinate of satellite position
     *                           expressed in meters (m).
     * @param satelliteVx        x ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVy        y ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param satelliteVz        z ECEF coordinate of satellite velocity
     *                           expressed in meters per second (m/s).
     * @param userX              x ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userY              y ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userZ              z ECEF coordinate of user position
     *                           expressed in meters (m).
     * @param userVx             x ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVy             y ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param userVz             z ECEF coordinate of user velocity
     *                           expressed in meters per second (m/s).
     * @param gnssRangeErrorBias GNSS range error bias.
     * @param config             GNSS configuration parameters.
     * @param cen                ECEF to NED coordinate transformation matrix.
     * @param omegaIe            skew symmetric matrix of Earth rotation rate.
     * @param cei                ECEF to ECI conversion matrix.
     * @param satellitePosition  column matrix containing satellite position.
     * @param deltaR             vector containing satellite to user posittion
     *                           difference.
     * @param satelliteVelocity  column matrix containing satellite velocity.
     * @param userPosition       column matrix containing user position.
     * @param userVelocity       column matrix containing user velocity.
     * @param tmp1               column matrix containing temporal values.
     * @param tmp2               column matrix containing temporal values.
     * @param tmp3               column matrix containing temporal values.
     * @param tmp4               column matrix containing temporal values.
     * @param tmp5               column matrix containing temporal values.
     * @param tmp6               column matrix containing temporal values.
     * @param random             random number generator.
     * @param result             instance where resulting GNSS measurement
     *                           is stored.
     * @return true if result has been obtained, failed if satellite is below elevation
     * mask angle and result is not updated.
     * @throws WrongSizeException if an error occurs.
     */
    private static boolean generate(
            final double time,
            final double satelliteX, final double satelliteY, final double satelliteZ,
            final double satelliteVx, final double satelliteVy, final double satelliteVz,
            final double userX, final double userY, final double userZ,
            final double userVx, final double userVy, final double userVz,
            final double gnssRangeErrorBias, final GNSSConfig config,
            final Matrix cen, final Matrix omegaIe, final Matrix cei,
            final Matrix satellitePosition, final Matrix deltaR,
            final Matrix satelliteVelocity, final Matrix userPosition,
            final Matrix userVelocity, final Matrix tmp1, final Matrix tmp2,
            final Matrix tmp3, final Matrix tmp4, final Matrix tmp5, final Matrix tmp6,
            final Random random, final GNSSMeasurement result) throws WrongSizeException {

        // Determine ECEF line-of-sight vector using (8.41)
        final double deltaRx = satelliteX - userX;
        final double deltaRy = satelliteY - userY;
        final double deltaRz = satelliteZ - userZ;

        final double approxRange = Math.sqrt(deltaRx * deltaRx + deltaRy * deltaRy + deltaRz * deltaRz);

        final double uaseX = deltaRx / approxRange;
        final double uaseY = deltaRy / approxRange;
        final double uaseZ = deltaRz / approxRange;

        // Convert line-of-sight vector to NED using (8.39) and determine
        // elevation using (8.57)
        final double cen1 = cen.getElementAt(2, 0);
        final double cen2 = cen.getElementAt(2, 1);
        final double cen3 = cen.getElementAt(2, 2);

        final double elevation = -Math.asin(cen1 * uaseX + cen2 * uaseY + cen3 * uaseZ);

        // Determine if satellite is above the masking angle
        if (elevation >= Math.toRadians(config.getMaskAngleDegrees())) {

            // Calculate frame rotation during signal transit time using (8.36)
            final double ceiValue = EARTH_ROTATION_RATE * approxRange / SPEED_OF_LIGHT;
            cei.setElementAt(0, 1, ceiValue);
            cei.setElementAt(1, 0, -ceiValue);

            // Calculate range using (8.35)
            satellitePosition.setElementAtIndex(0, satelliteX);
            satellitePosition.setElementAtIndex(1, satelliteY);
            satellitePosition.setElementAtIndex(2, satelliteZ);

            cei.multiply(satellitePosition, deltaR);

            deltaR.setElementAtIndex(0, deltaR.getElementAtIndex(0) - userX);
            deltaR.setElementAtIndex(1, deltaR.getElementAtIndex(1) - userY);
            deltaR.setElementAtIndex(2, deltaR.getElementAtIndex(2) - userZ);

            final double range = Utils.normF(deltaR);

            // Calculate range rate using (8.44)

            satelliteVelocity.setElementAtIndex(0, satelliteVx);
            satelliteVelocity.setElementAtIndex(1, satelliteVy);
            satelliteVelocity.setElementAtIndex(2, satelliteVz);

            omegaIe.multiply(satellitePosition, tmp1);

            satelliteVelocity.add(tmp1, tmp2);

            cei.multiply(tmp2, tmp3);

            userPosition.setElementAtIndex(0, userX);
            userPosition.setElementAtIndex(1, userY);
            userPosition.setElementAtIndex(2, userZ);

            omegaIe.multiply(userPosition, tmp4);

            userVelocity.setElementAtIndex(0, userVx);
            userVelocity.setElementAtIndex(1, userVy);
            userVelocity.setElementAtIndex(2, userVz);

            userVelocity.add(tmp4, tmp5);

            tmp3.subtract(tmp5, tmp6);

            final double rangeRate = uaseX * tmp6.getElementAtIndex(0)
                    + uaseY * tmp6.getElementAtIndex(1)
                    + uaseZ * tmp6.getElementAtIndex(2);

            // Calculate pseudo-range measurement
            final double pseudoRange = range + gnssRangeErrorBias
                    + config.getInitialReceiverClockOffset()
                    + config.getInitialReceiverClockDrift() * time
                    + config.getCodeTrackingErrorSD() * random.nextGaussian();

            // Calculate pseudo-range rate measurement
            final double pseudoRate = rangeRate
                    + config.getInitialReceiverClockDrift()
                    + config.getRangeRateTrackingErrorSD() * random.nextGaussian();

            // Set result values
            result.setPseudoRange(pseudoRange);
            result.setPseudoRate(pseudoRate);
            result.setPositionCoordinates(satelliteX, satelliteY, satelliteZ);
            result.setVelocityCoordinates(satelliteVx, satelliteVy, satelliteVz);

            // Indicate that result has been updated
            return true;
        } else {
            // Indicate that result is not updated
            return false;
        }
    }

    /**
     * Converts time instance into seconds.
     *
     * @param time instance to be converted.
     * @return time converted to seconds.
     */
    private static double convertTime(final Time time) {
        return TimeConverter.convert(time.getValue().doubleValue(),
                time.getUnit(), TimeUnit.SECOND);
    }
}
