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
package com.irurueta.navigation.indoor.fingerprint;

import com.irurueta.algebra.Matrix;
import com.irurueta.geometry.Point3D;
import com.irurueta.navigation.indoor.*;
import com.irurueta.statistics.MultivariateNormalDist;

import java.util.List;

/**
 * 3D position estimator based on located fingerprints containing only RSSI readings and
 * having as well prior knowledge of the location of radio sources associated to those
 * readings.
 * This implementation uses a first-order Taylor approximation over provided located
 * fingerprints to determine an approximate position for a non-located fingerprint using
 * a non-linear solving algorithm.
 * An initial position can be provided as a starting point to solve the position,
 * otherwise the average point of selected nearest fingerprints is used as a starting
 * point.
 */
@SuppressWarnings("WeakerAccess")
public class FirstOrderNonLinearFingerprintPositionEstimator3D extends
        NonLinearFingerprintPositionEstimator3D {

    /**
     * Constructor.
     */
    public FirstOrderNonLinearFingerprintPositionEstimator3D() {
    }

    /**
     * Constructor.
     *
     * @param listener listener in charge of handling events.
     */
    public FirstOrderNonLinearFingerprintPositionEstimator3D(
            final FingerprintPositionEstimatorListener<Point3D> listener) {
        super(listener);
    }

    /**
     * Constructor.
     *
     * @param locatedFingerprints located fingerprints containing RSSI readings.
     * @param fingerprint         fingerprint containing readings at an unknown location
     *                            for provided located fingerprints.
     * @param sources             located radio sources.
     * @throws IllegalArgumentException if provided non located fingerprint is null,
     *                                  located fingerprints value is null or there are not enough fingerprints or
     *                                  readings within provided fingerprints (for 3D position estimation 3 located
     *                                  total readings are required among all fingerprints).
     */
    public FirstOrderNonLinearFingerprintPositionEstimator3D(
            final List<? extends RssiFingerprintLocated<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>, Point3D>> locatedFingerprints,
            final RssiFingerprint<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>> fingerprint,
            final List<? extends RadioSourceLocated<Point3D>> sources) {
        super(locatedFingerprints, fingerprint, sources);
    }

    /**
     * Constructor.
     *
     * @param locatedFingerprints located fingerprints containing RSSI readings.
     * @param fingerprint         fingerprint containing readings at an unknown location
     *                            for provided located fingerprints.
     * @param sources             located radio sources.
     * @param listener            listener in charge of handling events.
     * @throws IllegalArgumentException if provided non located fingerprint is null,
     *                                  located fingerprints value is null or there are not enough fingerprints or
     *                                  readings within provided fingerprints (for 3D position estimation 3 located
     *                                  total readings are required among all fingerprints).
     */
    public FirstOrderNonLinearFingerprintPositionEstimator3D(
            final List<? extends RssiFingerprintLocated<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>, Point3D>> locatedFingerprints,
            final RssiFingerprint<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>> fingerprint,
            final List<? extends RadioSourceLocated<Point3D>> sources,
            final FingerprintPositionEstimatorListener<Point3D> listener) {
        super(locatedFingerprints, fingerprint, sources, listener);
    }

    /**
     * Constructor.
     *
     * @param locatedFingerprints located fingerprints containing RSSI readings.
     * @param fingerprint         fingerprint containing readings at an unknown location
     *                            for provided located fingerprints.
     * @param sources             located radio sources.
     * @param initialPosition     initial position to start the solving algorithm or null.
     * @throws IllegalArgumentException if provided non located fingerprint is null,
     *                                  located fingerprints value is null or there are not enough fingerprints or
     *                                  readings within provided fingerprints (for 3D position estimation 3 located
     *                                  total readings are required among all fingerprints).
     */
    public FirstOrderNonLinearFingerprintPositionEstimator3D(
            final List<? extends RssiFingerprintLocated<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>, Point3D>> locatedFingerprints,
            final RssiFingerprint<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>> fingerprint,
            final List<? extends RadioSourceLocated<Point3D>> sources, Point3D initialPosition) {
        super(locatedFingerprints, fingerprint, sources, initialPosition);
    }

    /**
     * Constructor.
     *
     * @param locatedFingerprints located fingerprints containing RSSI readings.
     * @param fingerprint         fingerprint containing readings at an unknown location
     *                            for provided located fingerprints.
     * @param sources             located radio sources.
     * @param initialPosition     initial position to start the solving algorithm or null.
     * @param listener            listener in charge of handling events.
     * @throws IllegalArgumentException if provided non located fingerprint is null,
     *                                  located fingerprints value is null or there are not enough fingerprints or
     *                                  readings within provided fingerprints (for 2D position estimation at least 2
     *                                  located total readings are required among all fingerprints, for example 2
     *                                  readings are required in a single fingerprint, or at least 2 fingerprints at
     *                                  different locations containing a single reading are required. For 3D position
     *                                  estimation 3 located total readings are required among all fingerprints).
     */
    public FirstOrderNonLinearFingerprintPositionEstimator3D(
            final List<? extends RssiFingerprintLocated<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>, Point3D>> locatedFingerprints,
            final RssiFingerprint<? extends RadioSource,
                    ? extends RssiReading<? extends RadioSource>> fingerprint,
            final List<? extends RadioSourceLocated<Point3D>> sources, Point3D initialPosition,
            final FingerprintPositionEstimatorListener<Point3D> listener) {
        super(locatedFingerprints, fingerprint, sources, initialPosition, listener);
    }

    /**
     * Gets type of position estimator.
     *
     * @return type of position estimator.
     */
    @Override
    public NonLinearFingerprintPositionEstimatorType getType() {
        return NonLinearFingerprintPositionEstimatorType.FIRST_ORDER;
    }

    /**
     * Evaluates a non-linear multi dimension function at provided point using
     * provided parameters and returns its evaluation and derivatives of the
     * function respect the function parameters.
     *
     * @param i           number of sample being evaluated.
     * @param point       point where function will be evaluated.
     * @param params      initial parameters estimation to be tried. These will
     *                    change as the Levenberg-Marquard algorithm iterates to the best solution.
     *                    These are used as input parameters along with point to evaluate function.
     * @param derivatives partial derivatives of the function respect to each
     *                    provided parameter.
     * @return function evaluation at provided point.
     */
    @Override
    @SuppressWarnings("Duplicates")
    protected double evaluate(
            final int i, final double[] point, final double[] params,
            final double[] derivatives) {
        //This method implements received power at point pi = (xi, yi, zi) and its derivatives

        //Pr(pi) = Pr(p1)
        //  - 10*n*(x1 - xa)/(ln(10)*d1a^2)*(xi - x1)
        //  - 10*n*(y1 - ya)/(ln(10)*d1a^2)*(yi - y1)
        //  - 10*n*(z1 - za)/(ln(10)*d1a^2)*(zi - z1)

        final double xi = params[0];
        final double yi = params[1];
        final double zi = params[2];

        //received power
        final double pr = point[0];

        //fingerprint coordinates
        final double x1 = point[1];
        final double y1 = point[2];
        final double z1 = point[3];

        //radio source coordinates
        final double xa = point[4];
        final double ya = point[5];
        final double za = point[6];

        //path loss exponent
        final double n = point[7];

        final double ln10 = Math.log(10.0);

        final double diffXi1 = xi - x1;
        final double diffYi1 = yi - y1;
        final double diffZi1 = zi - z1;

        final double diffX1a = x1 - xa;
        final double diffY1a = y1 - ya;
        final double diffZ1a = z1 - za;

        final double diffX1a2 = diffX1a * diffX1a;
        final double diffY1a2 = diffY1a * diffY1a;
        final double diffZ1a2 = diffZ1a * diffZ1a;

        final double d1a2 = diffX1a2 + diffY1a2 + diffZ1a2;

        final double value1 = -10.0 * n * diffX1a / (ln10 * d1a2);
        final double value2 = -10.0 * n * diffY1a / (ln10 * d1a2);
        final double value3 = -10.0 * n * diffZ1a / (ln10 * d1a2);

        final double result = pr
                + value1 * diffXi1
                + value2 * diffYi1
                + value3 * diffZi1;

        //derivative respect xi
        //diff(Pr(pi))/diff(xi) = - 10*n*(x1 - xa)/(ln(10)*d1a^2)
        derivatives[0] = value1;

        //derivative respect yi
        //diff(Pr(pi))/diff(yi) = - 10*n*(y1 - ya)/(ln(10)*d1a^2)
        derivatives[1] = value2;

        //derivative respect zi
        //diff(Pr(pi))/diff(zi) = - 10*n*(z1 - za)/(ln(10)*d1a^2)
        derivatives[2] = value3;

        return result;
    }

    /**
     * Propagates provided variances into RSSI variance of non-located fingerprint
     * reading.
     *
     * @param fingerprintRssi               closest located fingerprint reading RSSI expressed in dBm's.
     * @param pathlossExponent              path-loss exponent.
     * @param fingerprintPosition           position of closest fingerprint.
     * @param radioSourcePosition           radio source position associated to fingerprint reading.
     * @param estimatedPosition             position to be estimated. Usually this is equal to the
     *                                      initial position used by a non linear algorithm.
     * @param fingerprintRssiVariance       variance of fingerprint RSSI or null if unknown.
     * @param pathlossExponentVariance      variance of path-loss exponent or null if unknown.
     * @param fingerprintPositionCovariance covariance of fingerprint position or null if
     *                                      unknown.
     * @param radioSourcePositionCovariance covariance of radio source position or null if
     *                                      unknown.
     * @return variance of RSSI measured at non located fingerprint reading.
     */
    @Override
    @SuppressWarnings("Duplicates")
    protected Double propagateVariances(
            final double fingerprintRssi, final double pathlossExponent,
            final Point3D fingerprintPosition, final Point3D radioSourcePosition,
            final Point3D estimatedPosition, final Double fingerprintRssiVariance,
            final Double pathlossExponentVariance,
            final Matrix fingerprintPositionCovariance,
            final Matrix radioSourcePositionCovariance) {
        try {
            final MultivariateNormalDist dist =
                    Utils.propagateVariancesToRssiVarianceFirstOrderNonLinear3D(
                            fingerprintRssi, pathlossExponent, fingerprintPosition,
                            radioSourcePosition, estimatedPosition, fingerprintRssiVariance,
                            pathlossExponentVariance, fingerprintPositionCovariance,
                            radioSourcePositionCovariance, null);
            if (dist == null) {
                return null;
            }

            final Matrix covariance = dist.getCovariance();
            if (covariance == null) {
                return null;
            }

            return covariance.getElementAt(0, 0);

        } catch (IndoorException e) {
            return null;
        }
    }
}
