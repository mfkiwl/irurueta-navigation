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
package com.irurueta.navigation.fingerprinting;

import com.irurueta.algebra.AlgebraException;
import com.irurueta.algebra.Matrix;
import com.irurueta.geometry.Point;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.numerical.NumericalException;
import com.irurueta.numerical.fitting.FittingException;
import com.irurueta.numerical.fitting.LevenbergMarquardtMultiDimensionFitter;
import com.irurueta.numerical.fitting.LevenbergMarquardtMultiDimensionFunctionEvaluator;

import java.util.List;

/**
 * Estimates position and transmitted power of a WiFi access point assuming that the
 * access point emits isotropically following the expression below:
 * Pr = Pt*Gt*Gr*lambda^2 / (4*pi*d)^2,
 * where Pr is the received power (expressed in mW),
 * Gt is the Gain of the transmission antena
 * Gr is the Gain of the receiver antena
 * d is the distance between emitter and receiver
 * and lambda is the wavelength and is equal to: lambda = c / f,
 * where c is the speed of light
 * and f is the carrier frequency of the WiFi signal.
 * Because usually information about the antena of the Wifi Access Point cannot be
 * retrieved (because many measurements are made on unkown access points where
 * physical access is not possible), this implementation will estimate the
 * equivalent transmitted power as: Pte = Pt * Gt * Gr.
 * If WifiReadings contain RSSI standard deviations, those values will be used,
 * otherwise it will be asumed an RSSI standard deviation of 1 dB.
 * @param <P> a {@link Point} type.
 */
@SuppressWarnings("WeakerAccess")
public abstract class WifiAccessPointPowerAndPositionEstimator<P extends Point> {

    /**
     * Speed of light expressed in meters per second (m/s).
     */
    public static final double SPEED_OF_LIGHT = 299792458.0;

    /**
     * Default standard deviations assumed for RSSI readings being fitted.
     */
    public static final double DEFAULT_POWER_STANDARD_DEVIATION = 1.0;

    /**
     * Default exponent typically used on free space for path loss propagation in
     * terms of distance.
     */
    public static final double DEFAULT_PATH_LOSS_EXPONENT = 2.0;

    /**
     * Estimated position.
     */
    protected double[] mEstimatedPositionCoordinates;

    /**
     * Estimated transmitted power expressed in dBm's.
     */
    private double mEstimatedTransmittedPowerdBm;

    /**
     * Estimated exponent typically used on free space for path loss propagation in
     * terms of distance.
     * On different environments path loss exponent might have different values:
     * - Free space: 2.0
     * - Urban Area: 2.7 to 3.5
     * - Suburban Area: 3 to 5
     * - Indoor (line-of-sight): 1.6 to 1.8
     * If path loss exponent estimation is not enabled, this value will always be equal to
     * {@link #DEFAULT_PATH_LOSS_EXPONENT}
     */
    private double mEstimatedPathLossExponent;

    /**
     * Indicates whether path loss estimation is enabled or not.
     */
    private boolean mPathLossEstimationEnabled;

    /**
     * Covariance of estimated position and power (and path loss exponent if its estimation is enabled).
     */
    private Matrix mEstimatedCovariance;

    /**
     * Estimated chi square value.
     */
    private double mChiSq;

    /**
     * Initial transmitted power to start the estimation of access point
     * transmitted power.
     * If not defined, average value of received power readings will be used.
     */
    private Double mInitialTransmittedPowerdBm;

    /**
     * Initial position to start the estimation of access point position.
     * If not defined, centroid of provided fingerprints will be used.
     */
    private P mInitialPosition;

    /**
     * Initial exponent typically used on free space for path loss propagation in
     * terms of distance.
     * On different environments path loss exponent might have different values:
     * - Free space: 2.0
     * - Urban Area: 2.7 to 3.5
     * - Suburban Area: 3 to 5
     * - Indoor (line-of-sight): 1.6 to 1.8
     *
     * Initial path loss exponent is only taken into account if path loss exponent
     * estimation is enabled. Otherwise it is assumed a free space environment
     */
    private double mInitialPathLossExponent = DEFAULT_PATH_LOSS_EXPONENT;

    /**
     * WiFi signal readings belonging to the same access point to be estimated.
     */
    private List<? extends WifiRssiReadingLocated<P>> mReadings;

    /**
     * Indicates whether estimator is locked during estimation.
     */
    private boolean mLocked;

    /**
     * Listener in charge of attending events raised by this instance.
     */
    private WifiAccessPointPowerAndPositionEstimatorListener<P> mListener;

    /**
     * Levenberg-Marquardt fitter to find a solution.
     */
    private LevenbergMarquardtMultiDimensionFitter mFitter =
            new LevenbergMarquardtMultiDimensionFitter();

    /**
     * Constructor.
     */
    public WifiAccessPointPowerAndPositionEstimator() { }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings)
            throws IllegalArgumentException {
        internalSetReadings(readings);
    }

    /**
     * Constructor.
     * @param listener listener in charge of attending events raised by this instance.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener) {
        mListener = listener;
    }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if fingerprints are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings,
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener)
            throws IllegalArgumentException {
        this(readings);
        mListener = listener;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     */
    public WifiAccessPointPowerAndPositionEstimator(P initialPosition) {
        mInitialPosition = initialPosition;
    }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @throws IllegalArgumentException if fingerprints are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings,
            P initialPosition)
            throws IllegalArgumentException {
        internalSetReadings(readings);
        mInitialPosition = initialPosition;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @param listener listener in charge of attending events raised by this instance.
     */
    public WifiAccessPointPowerAndPositionEstimator(P initialPosition,
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener) {
        mListener = listener;
        mInitialPosition = initialPosition;
    }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if fingerprints are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings,
            P initialPosition,
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener)
            throws IllegalArgumentException {
        this(readings, initialPosition);
        mListener = listener;
    }

    /**
     * Constructor.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     */
    public WifiAccessPointPowerAndPositionEstimator(
            Double initialTransmittedPowerdBm) {
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     * @throws IllegalArgumentException if readings are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings,
            Double initialTransmittedPowerdBm)
            throws IllegalArgumentException {
        internalSetReadings(readings);
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     * @param listener listener in charge of attending events raised by this instance.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            Double initialTransmittedPowerdBm,
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener) {
        mListener = listener;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings,
            Double initialTransmittedPowerdBm,
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener)
            throws IllegalArgumentException {
        this(readings, initialTransmittedPowerdBm);
        mListener = listener;
    }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     * @throws IllegalArgumentException if readings are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings,
            P initialPosition, Double initialTransmittedPowerdBm)
            throws IllegalArgumentException {
        internalSetReadings(readings);
        mInitialPosition = initialPosition;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     */
    public WifiAccessPointPowerAndPositionEstimator(P initialPosition,
            Double initialTransmittedPowerdBm) {
        mInitialPosition = initialPosition;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     * @param listener listener in charge of attending events raised by this instance.
     */
    public WifiAccessPointPowerAndPositionEstimator(P initialPosition,
            Double initialTransmittedPowerdBm,
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener) {
        mListener = listener;
        mInitialPosition = initialPosition;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings containing belonging to
     *                 the same access point.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                estimation of access point transmitted power
     *                                (expressed in dBm's)
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public WifiAccessPointPowerAndPositionEstimator(
            List<? extends WifiRssiReadingLocated<P>> readings,
            P initialPosition, Double initialTransmittedPowerdBm,
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener)
            throws IllegalArgumentException {
        this(readings, initialPosition, initialTransmittedPowerdBm);
        mListener = listener;
    }

    /**
     * Gets initial transmitted power to start the estimation of access point
     * transmitted power (expressed in dBm's).
     * If not defined, average value of received power readings will be used.
     * @return initial transmitted power to start the estimation of access point
     * transmitted power.
     */
    public Double getInitialTransmittedPowerdBm() {
        return mInitialTransmittedPowerdBm;
    }

    /**
     * Sets initial transmitted power to start the estimation of access point
     * transmitted power (expressed in dBm's).
     * If not defined, average value of received power readings will be used.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of access point transmitted
     *                                   power.
     * @throws LockedException if estimator is locked.
     */
    public void setInitialTransmittedPowerdBm(Double initialTransmittedPowerdBm)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Gets initial transmitted power to start the estimation of access point
     * transmitted power (expressed in mW).
     * If not defined, average value of received power readings will be used.
     * @return initial transmitted power to start the estimation of access point
     * transmitted power.
     */
    public Double getInitialTransmittedPower() {
        return mInitialTransmittedPowerdBm != null ?
                Utils.dBmToPower(mInitialTransmittedPowerdBm) : null;
    }

    /**
     * Sets initial transmitted power to start the estimation of access point
     * transmitted power (expressed in mW).
     * If not defined, average value of received power readings will be used.
     * @param initialTransmittedPower initial transmitted power to start the
     *                                estimation of access point transmitted power.
     * @throws LockedException if estimator is locked.
     * @throws IllegalArgumentException if provided value is negative.
     */
    public void setInitialTransmittedPower(Double initialTransmittedPower)
            throws LockedException, IllegalArgumentException {
        if (isLocked()) {
            throw new LockedException();
        }
        if (initialTransmittedPower != null) {
            if (initialTransmittedPower < 0.0) {
                throw new IllegalArgumentException();
            }
            mInitialTransmittedPowerdBm = Utils.powerTodBm(
                    initialTransmittedPower);
        } else {
            mInitialTransmittedPowerdBm = null;
        }
    }

    /**
     * Gets initial position to start the estimation of access point position.
     * If not defined, centroid of provided fingerprints will be used.
     * @return initial position to start the estimation of access point position.
     */
    public P getInitialPosition() {
        return mInitialPosition;
    }

    /**
     * Sets initial position to start the estimation of access point position.
     * If not defined, centroid of provided fingerprints will be used.
     * @param initialPosition initial position to start the estimation of access
     *                        point position.
     * @throws LockedException if estimator is locked.
     */
    public void setInitialPosition(P initialPosition) throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mInitialPosition = initialPosition;
    }

    /**
     * Indicates whether estimator is locked during estimation.
     * @return true if estimator is locked, false otherwise.
     */
    public boolean isLocked() {
        return mLocked;
    }

    /**
     * Indicates whether readings are valid or not.
     * Readings are considered valid when there are enough readings.
     * @param readings readings to be validated.
     * @return true if readings are valid, false otherwise.
     */
    public boolean areValidReadings(
            List<? extends WifiRssiReadingLocated<P>> readings) {

        return readings != null && readings.size() >= getMinReadings();
    }

    /**
     * Gets WiFi signal readings belonging to the same access point to be estimated.
     * @return WiFi signal readings belonging to the same access point.
     */
    public List<? extends WifiRssiReadingLocated<P>> getReadings() {
        return mReadings;
    }

    /**
     * Sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings belonging to the same access point.
     * @throws LockedException if estimator is locked.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public void setReadings(List<? extends WifiRssiReadingLocated<P>> readings)
            throws LockedException, IllegalArgumentException {
        if (isLocked()) {
            throw new LockedException();
        }

        internalSetReadings(readings);
    }

    /**
     * Gets listener in charge of attending events raised by this instance.
     * @return listener in charge of attending events raised by this instance.
     */
    public WifiAccessPointPowerAndPositionEstimatorListener<P> getListener() {
        return mListener;
    }

    /**
     * Sets listener in charge of attending events raised by this instance.
     * @param listener listener in charge of attending events raised by this
     *                 instance.
     * @throws LockedException if estimator is locked.
     */
    public void setListener(
            WifiAccessPointPowerAndPositionEstimatorListener<P> listener)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }

        mListener = listener;
    }

    /**
     * Indicates whether this instance is ready to start the estimation.
     * @return true if this instance is ready, false otherwise.
     */
    public boolean isReady() {
        return areValidReadings(mReadings);
    }

    /**
     * Estimate position and transmitted power.
     * @throws FingerprintingException if estimation fails.
     * @throws NotReadyException if estimator is not ready.
     * @throws LockedException if estimator is locked.
     */
    public void estimate() throws FingerprintingException, NotReadyException,
            LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        if (!isReady()) {
            throw new NotReadyException();
        }

        try {
            mLocked = true;

            if (mListener != null) {
                mListener.onEstimateStart(this);
            }

            setupFitter();

            mFitter.fit();

            //estimated position and transmitted power
            double[] a = mFitter.getA();
            int dims = getNumberOfDimensions();

            mEstimatedPositionCoordinates = new double[dims];
            System.arraycopy(a, 0, mEstimatedPositionCoordinates, 0, dims);
            mEstimatedTransmittedPowerdBm = a[dims];
            mEstimatedCovariance = mFitter.getCovar();
            mChiSq = mFitter.getChisq();

            if (mListener != null) {
                mListener.onEstimateEnd(this);
            }
        } catch (NumericalException e) {
            throw new FingerprintingException(e);
        } finally {
            mLocked = false;
        }
    }

    /**
     * Gets estimated transmitted power expressed in milli watts (mW).
     * @return estimated transmitted power expressed in milli watts.
     */
    public double getEstimatedTransmittedPower() {
        return Utils.dBmToPower(mEstimatedTransmittedPowerdBm);
    }

    /**
     * Gets estimated transmitted power expressed in dBm's.
     * @return estimated transmitted power expressed in dBm's.
     */
    public double getEstimatedTransmittedPowerdBm() {
        return mEstimatedTransmittedPowerdBm;
    }

    /**
     * Gets estimated inhomogeneous position coordinates.
     * @return estimated inhomogeneous position coordinates.
     */
    public double[] getEstimatedPositionCoordinates() {
        return mEstimatedPositionCoordinates;
    }

    /**
     * Gets estimated estimated position and stores result into provided instance.
     * @param estimatedPosition instance where estimated estimated position will be stored.
     */
    public void getEstimatedPosition(P estimatedPosition) {
        if (mEstimatedPositionCoordinates != null) {
            for (int i = 0; i < mEstimatedPositionCoordinates.length; i++) {
                estimatedPosition.setInhomogeneousCoordinate(i,
                        mEstimatedPositionCoordinates[i]);
            }
        }
    }

    /**
     * Gets covariance for estimated position and power.
     * Top-left submatrix contains covariance of position, and last diagonal element
     * contains variance of estimated transmitted power.
     * @return covariance for estimated position and power.
     */
    public Matrix getEstimatedCovariance() {
        return mEstimatedCovariance;
    }

    /**
     * Gets estimated position covariance.
     * @return estimated position covariance.
     */
    public Matrix getEstimatedPositionCovariance() {
        if (mEstimatedCovariance == null) {
            return null;
        }

        int d = getNumberOfDimensions() - 1;
        return mEstimatedCovariance.getSubmatrix(
                0, 0, d, d);
    }

    /**
     * Gets estimated transmitted power variance.
     * @return estimated transmitted power variance.
     */
    public double getEstimatedTransmittedPowerVariance() {
        if (mEstimatedCovariance == null) {
            return 0.0;
        }

        int d = getNumberOfDimensions();
        return mEstimatedCovariance.getElementAt(d, d);
    }

    /**
     * Gets estimated chi square value.
     * @return estimated chi square value.
     */
    public double getChiSq() {
        return mChiSq;
    }

    /**
     * Gets minimum required number of readings to estimate
     * power and position.
     * This is 3 readings for 2D, and 4 readings for 3D.
     * @return minimum required number of readings.
     */
    public abstract int getMinReadings();

    /**
     * Gets number of dimensions of position points.
     * @return number of dimensions of position points.
     */
    public abstract int getNumberOfDimensions();

    /**
     * Gets estimated access point position.
     * @return estimated access point position.
     */
    public abstract P getEstimatedPosition();

    /**
     * Gets estimated located access point with estimated transmitted power.
     * @param <AP> type of located access point with transmitted power.
     * @return estimasted located access point with estimated transmitted power.
     */
    public abstract <AP extends WifiAccessPointWithPowerAndLocated<P>> AP getEstimatedAccessPoint();

    /**
     * Internally sets WiFi signal readings belonging to the same access point.
     * @param readings WiFi signal readings belonging to the same access point.
     * @throws IllegalArgumentException if readings are null, not enough readings
     * are available, or readings do not belong to the same access point.
     */
    protected void internalSetReadings(
            List<? extends WifiRssiReadingLocated<P>> readings)
            throws IllegalArgumentException {
        if (!areValidReadings(readings)) {
            throw new IllegalArgumentException();
        }

        mReadings = readings;
    }

    /**
     * Setups fitter to estimate transmitted power and position.
     * @throws FittingException if Levenberg-Marquardt fitting fails.
     */
    private void setupFitter() throws FittingException {
        //because all fingerprints must belong to the same access point, we
        //obtain the frequency of the first access point on the first fingerprint
        //reading
        WifiRssiReadingLocated<P> reading = mReadings.get(0);
        double frequency = reading.getAccessPoint().getFrequency();

        //Pr = Pt*Gt*Gr*lambda^2/(4*pi*d)^2,    where Pr is the received power
        // lambda = c/f, where lambda is wavelength,
        // Pte = Pt*Gt*Gr, is the equivalent transmitted power, Gt is the transmitted Gain and Gr is the received Gain
        //Pr = Pte*c^2/((4*pi*f)^2 * d^2)


        //compute k as the constant part of the isotropic received power formula
        //so that: Pr = Pte*k/d^2
        double k = Math.pow(SPEED_OF_LIGHT / (4.0 * Math.PI * frequency), 2.0);
        final double kdB = 10.0 * Math.log10(k);

        //for numerical accuracy reasons, a logarithmic version of the previous
        //formula will be used instead
        //Pr (dBm) = 10 * log(Pte * k / d^2) = 10*log(k) + 10*log(Pte) - 20*log(d)

        mFitter.setFunctionEvaluator(new LevenbergMarquardtMultiDimensionFunctionEvaluator() {

            @Override
            public int getNumberOfDimensions() {
                return WifiAccessPointPowerAndPositionEstimator.this.
                        getNumberOfDimensions() + 1;
            }

            @Override
            public double[] createInitialParametersArray() {
                int dims = WifiAccessPointPowerAndPositionEstimator.this.
                        getNumberOfDimensions();
                double[] initial = new double[dims + 1];
                int num = mReadings.size();

                if (mInitialPosition == null) {
                    //initialize
                    for(int i = 0; i < dims; i++) {
                        initial[i] = 0.0;
                    }

                    //compute average centroid of fingerprint positions
                    for (WifiRssiReadingLocated<P> reading : mReadings) {
                        P position = reading.getPosition();
                        for (int i = 0; i < dims; i++) {
                            initial[i] += position.getInhomogeneousCoordinate(i) /
                                    (double) num;
                        }
                    }
                } else {
                    //copy initial position
                    for(int i = 0; i < dims; i++) {
                        initial[i] = mInitialPosition.getInhomogeneousCoordinate(i);
                    }
                }

                initial[dims] = computeInitialTransmittedPowerdBm();

                return initial;
            }

            @Override
            public double evaluate(int i, double[] point, double[] params,
                                   double[] derivatives) {
                int dims = WifiAccessPointPowerAndPositionEstimator.this.
                        getNumberOfDimensions();
                double sqrDistance = 0.0, diff;
                for (int j = 0; j < dims; j++) {
                    diff = params[j] - point[j];
                    sqrDistance += diff * diff;

                    derivatives[j] = -20.0 * diff;
                }

                double transmittedPowerdBm = params[dims];

                //derivatives respect position coordinates are (2D case):
                //f(x,y) = -10*log((x - xap)^2 + (y - yap)^2)
                //df/dx = -10*2*(x - xap)/(ln(10)*((x - xap)^2 + (y - yap)^2)) = -20*diffX/(ln(10)*sqrDistance)
                //df/dy = -10*2*(y - yap)/((x - xap)^2 + (y - yap)^2) = -20*diffY/(ln(10)*sqrDistance)
                double ln10PerSqrDistance = Math.log(10.0) * sqrDistance;
                for (int j = 0; j < dims; j++) {
                    derivatives[j] /= ln10PerSqrDistance;
                }

                //derivative respect transmitted power
                derivatives[dims] = 1.0;

                //Pr = Pt*Gt*Gr*lambda^2/(4*pi*d)^2,    where Pr is the received power
                // lambda = c/f, where lambda is wavelength,
                // Pte = Pt*Gt*Gr, is the equivalent transmitted power, Gt is the transmitted Gain and Gr is the received Gain
                //Pr = Pte*c^2/((4*pi*f)^2 * d^2)
                //Pr (dBm) = 10*log(k) + 10*log(Pte) - 20*log(d) =
                //  10*log(k) + 10*log(Pte) - 10*log(d^2)
                return kdB + transmittedPowerdBm
                        - 10.0 * Math.log10(sqrDistance);
            }
        });

        int dims = getNumberOfDimensions();
        int numParams = dims + 1;
        int numFingerprints = mReadings.size();
        double initialTransmittedPowerdBm = computeInitialTransmittedPowerdBm();
        try {
            Matrix x = new Matrix(numFingerprints, numParams);
            double[] y = new double[numFingerprints];
            double[] standardDeviations = new double[numFingerprints];
            for (int i = 0; i < numFingerprints; i++) {
                reading = mReadings.get(i);
                P position = reading.getPosition();

                for (int j = 0; j < dims; j++) {
                    x.setElementAt(i, j, position.getInhomogeneousCoordinate(j));
                }
                x.setElementAt(i, dims, initialTransmittedPowerdBm);

                standardDeviations[i] = reading.getRssiStandardDeviation() != null ?
                        reading.getRssiStandardDeviation() :
                        DEFAULT_POWER_STANDARD_DEVIATION;
                y[i] = reading.getRssi();
            }

            mFitter.setInputData(x, y, standardDeviations);
        } catch (AlgebraException ignore) { }
    }

    /**
     * Computes initial transmitted power expressed in dBm's.
     * If no initial transmitted power is provided, the average of all measures
     * is used, otherwise provided value is used.
     * @return initial transmitted power.
     */
    private double computeInitialTransmittedPowerdBm() {
        if (mInitialTransmittedPowerdBm == null) {
            //compute average transmitted power (in mW)
            int num = mReadings.size();
            double result = 0.0;
            for (WifiRssiReadingLocated<P> reading : mReadings) {
                double rssi = reading.getRssi();
                result += rssi / (double)num;
            }
            return result;
        } else {
            //convert initial value
            return mInitialTransmittedPowerdBm;
        }

    }
}