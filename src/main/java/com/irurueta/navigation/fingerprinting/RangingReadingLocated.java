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

import com.irurueta.algebra.Matrix;
import com.irurueta.geometry.Point;

/**
 * Contains a located ranging reading associated to a given radio source (e.g. WiFi access point or
 * bluetooth beacon), indicating the distance to such access point.
 * @param <S> a {@link RadioSource} type.
 * @param <P> a {@link Point} type.
 */
@SuppressWarnings("WeakerAccess")
public class RangingReadingLocated<S extends RadioSource, P extends Point> extends RangingReading<S>
        implements ReadingLocated<P> {

    /**
     * Position where WiFi reading was made.
     */
    private P mPosition;

    /**
     * Covariance of inhomogeneous coordinates of current position
     * (if available).
     */
    private Matrix mPositionCovariance;

    /**
     * Constructor.
     * @param source radio source associated to this reading.
     * @param distance distance in meters to the radio source.
     * @param position position where reading was made.
     * @throws IllegalArgumentException if radio source data is null, distance is negative
     * or position is null.
     */
    public RangingReadingLocated(S source, double distance,
            P position) throws IllegalArgumentException {
        super(source, distance);

        if (position == null) {
            throw new IllegalArgumentException();
        }

        mPosition = position;
    }

    /**
     * Constructor.
     * @param source radio source associated to this reading.
     * @param distance distance in meters to the radio source.
     * @param position position where reading was made.
     * @param distanceStandardDeviation standard deviation of distance, if available.
     * @throws IllegalArgumentException if radio source data is null, distance is negative,
     * position is null or standard deviation is zero or negative.
     */
    public RangingReadingLocated(S source, double distance,
            P position, Double distanceStandardDeviation)
            throws IllegalArgumentException {
        super(source, distance, distanceStandardDeviation);

        if (position == null) {
            throw new IllegalArgumentException();
        }

        mPosition = position;
    }

    /**
     * Constructor.
     * @param source radio source associated to this reading.
     * @param distance distance in meters to the radio source.
     * @param position position where reading was made.
     * @param positionCovariance covariance of inhomogeneous coordinates of
     *                           current position (if available).
     * @throws IllegalArgumentException if radio source data is null, distance is negative
     * or position is null.
     */
    public RangingReadingLocated(S source, double distance,
            P position, Matrix positionCovariance) throws IllegalArgumentException {
        this(source, distance, position);

        if (positionCovariance != null) {
            int dims = position.getDimensions();
            if (positionCovariance.getRows() != dims ||
                    positionCovariance.getColumns() != dims) {
                throw new IllegalArgumentException();
            }
        }
        mPositionCovariance = positionCovariance;
    }

    /**
     * Constructor.
     * @param source radio source associated to this reading.
     * @param distance distance in meters to the radio source.
     * @param position position where reading was made.
     * @param distanceStandardDeviation standard deviation of distance, if available.
     * @throws IllegalArgumentException if radio source data is null, distance is negative,
     * position is null or standard deviation is zero or negative.
     */
    public RangingReadingLocated(S source, double distance,
            P position, Double distanceStandardDeviation,
            Matrix positionCovariance) throws IllegalArgumentException {
        this(source, distance, position, distanceStandardDeviation);

        if (positionCovariance != null) {
            int dims = position.getDimensions();
            if (positionCovariance.getRows() != dims ||
                    positionCovariance.getColumns() != dims) {
                throw new IllegalArgumentException();
            }
        }
        mPositionCovariance = positionCovariance;
    }

    /**
     * Empty constructor.
     */
    protected RangingReadingLocated() {
        super();
    }

    /**
     * Gets position where reading was made.
     * @return position where reading was made.
     */
    public P getPosition() {
        return mPosition;
    }

    /**
     * Gets covariance of inhomogeneous coordinates of current position (if available).
     * @return covariance of position or null.
     */
    public Matrix getPositionCovariance() {
        return mPositionCovariance;
    }
}
