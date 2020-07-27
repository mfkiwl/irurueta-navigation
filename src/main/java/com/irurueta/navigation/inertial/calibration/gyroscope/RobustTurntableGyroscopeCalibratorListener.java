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
package com.irurueta.navigation.inertial.calibration.gyroscope;

/**
 * Contains listener for robust gyroscope calibrators when
 * a turntable is used to obtain all measurements.
 */
public interface RobustTurntableGyroscopeCalibratorListener {

    /**
     * Called when calibration starts.
     *
     * @param calibrator calibrator that raised the event.
     */
    void onCalibrateStart(final RobustTurntableGyroscopeCalibrator calibrator);

    /**
     * Called when calibration ends.
     *
     * @param calibrator calibrator that raised the event.
     */
    void onCalibrateEnd(final RobustTurntableGyroscopeCalibrator calibrator);

    /**
     * Called when calibrator iterates to refine a possible solution.
     *
     * @param calibrator calibrator raising the event.
     * @param iteration  current iteration.
     */
    void onCalibrateNextIteration(final RobustTurntableGyroscopeCalibrator calibrator,
                                  final int iteration);

    /**
     * Called when calibration progress changes significantly.
     *
     * @param calibrator calibrator raising the event.
     * @param progress   progress of calibration expressed as a value between 0.0 and 1.0.
     */
    void onCalibrateProgressChange(final RobustTurntableGyroscopeCalibrator calibrator,
                                   final float progress);
}
