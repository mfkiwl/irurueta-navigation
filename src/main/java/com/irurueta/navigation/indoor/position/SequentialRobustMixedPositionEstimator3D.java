package com.irurueta.navigation.indoor.position;

import com.irurueta.geometry.Point3D;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.indoor.Fingerprint;
import com.irurueta.navigation.indoor.RadioSource;
import com.irurueta.navigation.indoor.RadioSourceLocated;
import com.irurueta.navigation.indoor.Reading;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class SequentialRobustMixedPositionEstimator3D extends
        SequentialRobustMixedPositionEstimator<Point3D> {

    /**
     * Constructor.
     */
    public SequentialRobustMixedPositionEstimator3D() {
        super();
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sources located radio sources used for lateration.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(
            List<? extends RadioSourceLocated<Point3D>> sources) {
        super(sources);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param fingerprint fingerprint containing RSSI readings at an unknown location
     *                    for provided located radio sources.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator3D(
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(fingerprint);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sources       located radio sources used for lateration.
     * @param fingerprint   fingerprint containing reagins at an unknown location
     *                      for provided located radio sources.
     * @throws IllegalArgumentException if either provided sources or fingerprint is
     *                                  null or the number of provided sources is less
     *                                  than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(
            List<? extends RadioSourceLocated<Point3D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(sources, fingerprint);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param listener listener in charge of handling events.
     */
    public SequentialRobustMixedPositionEstimator3D(
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sources   located radio sources used for lateration.
     * @param listener  listener in charge of handling events.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required
     *                                  minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(
            List<? extends RadioSourceLocated<Point3D>> sources,
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(sources, listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param fingerprint   fingerprint containing readings at an unknown location for
     *                      provided located radio sources.
     * @param listener      listener in charge of handling events.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator3D(
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(fingerprint, listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sources       located radio sources used for lateration.
     * @param fingerprint   fingerprint containing readings at an unknown location
     *                      for provided located radio sources.
     * @param listener      listener in charge of handling events.
     * @throws IllegalArgumentException if either provided sources or fingerprint is
     *                                  null or the number of provided sources is less
     *                                  than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(
            List<? extends RadioSourceLocated<Point3D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(sources, fingerprint, listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to
     *                                          readings within provided fingerprint.
     *                                          The larger the score the better the
     *                                          quality of the reading.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
                                                    double[] fingerprintReadingQualityScores) {
        super(sourceQualityScores, fingerprintReadingQualityScores);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
                                                    double[] fingerprintReadingQualityScores,
                                                    List<? extends RadioSourceLocated<Point3D>> sources) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located
     *                                          radio sources.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
                double[] fingerprintReadingQualityScores,
                Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(sourceQualityScores, fingerprintReadingQualityScores, fingerprint);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located
     *                                          radio sources.
     * @throws IllegalArgumentException if either provided sources or fingerprint is null
     * or the number of provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            List<? extends RadioSourceLocated<Point3D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources,
                fingerprint);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param listener                          listener in charge of handling events.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @param listener                          listener in charge of handling events.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            List<? extends RadioSourceLocated<Point3D>> sources,
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources,
                listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to
     *                                          readings within provided fingerprint.
     *                                          The larger the score the better the
     *                                          quality of the reading.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located
     *                                          radio sources.
     * @param listener                          listener in charge of handling events.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, fingerprint,
                listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located radio
     *                                          sources.
     * @param listener                          listener in charge of handling events.
     * @throws IllegalArgumentException if either provided sources or fingerprint is null
     * or the number of provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator3D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            List<? extends RadioSourceLocated<Point3D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point3D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources,
                fingerprint, listener);
        mRangingPreliminarySubsetSize = mRssiPreliminarySubsetSize = getMinRequiredSources();
    }

    /**
     * Gets number of dimesnions of provided and estimated points.
     *
     * @return number of dimensions of provided and estimated points.
     */
    @Override
    public int getNumberOfDimensions() {
        return Point3D.POINT3D_INHOMOGENEOUS_COORDINATES_LENGTH;
    }

    /**
     * Gets minimum required number of located radio sources to perform lateration.
     *
     * @return minimum required number of located radio sources to perform
     * lateration.
     */
    @Override
    public int getMinRequiredSources() {
        return Point3D.POINT3D_HOMOGENEOUS_COORDINATES_LENGTH;
    }

    /**
     * Builds ranging internal estimator.
     */
    @Override
    protected void buildRangingEstimator() {
        mRangingEstimator = RobustRangingPositionEstimator3D.create(
                mRangingRobustMethod);
    }

    /**
     * Builds RSSI internal estimator.
     */
    @Override
    protected void buildRssiEstimator() {
        mRssiEstimator = RobustRssiPositionEstimator3D.create(mRssiRobustMethod);
    }

    /**
     * Setup ranging internal estimator.
     *
     * @throws LockedException if estimator is locked.
     */
    @Override
    protected void setupRangingEstimator() throws LockedException {
        super.setupRangingEstimator();
        if (mRangingThreshold != null) {
            switch (mRangingRobustMethod) {
                case RANSAC:
                    ((RANSACRobustRangingPositionEstimator3D) mRangingEstimator).setThreshold(mRangingThreshold);
                    break;
                case LMedS:
                    ((LMedSRobustRangingPositionEstimator3D) mRangingEstimator).setStopThreshold(mRangingThreshold);
                    break;
                case MSAC:
                    ((MSACRobustRangingPositionEstimator3D) mRangingEstimator).setThreshold(mRangingThreshold);
                    break;
                case PROSAC:
                    ((PROSACRobustRangingPositionEstimator3D) mRangingEstimator).setThreshold(mRangingThreshold);
                    break;
                case PROMedS:
                    ((PROMedSRobustRangingPositionEstimator3D) mRangingEstimator).setStopThreshold(mRangingThreshold);
                    break;
            }
        }
    }

    /**
     * Setup RSSI internal estimator.
     *
     * @throws LockedException if estimator is locked.
     */
    @Override
    protected void setupRssiEstimator() throws LockedException {
        super.setupRssiEstimator();
        if (mRssiThreshold != null) {
            switch (mRssiRobustMethod) {
                case RANSAC:
                    ((RANSACRobustRssiPositionEstimator3D) mRssiEstimator).setThreshold(mRssiThreshold);
                    break;
                case LMedS:
                    ((LMedSRobustRssiPositionEstimator3D) mRssiEstimator).setStopThreshold(mRssiThreshold);
                    break;
                case MSAC:
                    ((MSACRobustRssiPositionEstimator3D) mRssiEstimator).setThreshold(mRssiThreshold);
                    break;
                case PROSAC:
                    ((PROSACRobustRssiPositionEstimator3D) mRssiEstimator).setThreshold(mRssiThreshold);
                    break;
                case PROMedS:
                    ((PROMedSRobustRssiPositionEstimator3D) mRssiEstimator).setStopThreshold(mRssiThreshold);
                    break;
            }
        }
    }
}