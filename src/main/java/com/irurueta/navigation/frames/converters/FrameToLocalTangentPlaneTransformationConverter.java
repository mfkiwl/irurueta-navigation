/*
 * Copyright (C) 2021 Alberto Irurueta Carro (alberto@irurueta.com)
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
package com.irurueta.navigation.frames.converters;

import com.irurueta.geometry.EuclideanTransformation3D;
import com.irurueta.geometry.InvalidRotationMatrixException;
import com.irurueta.geometry.Quaternion;
import com.irurueta.geometry.Rotation3D;
import com.irurueta.navigation.frames.CoordinateTransformation;
import com.irurueta.navigation.frames.ECEFFrame;
import com.irurueta.navigation.frames.FrameType;
import com.irurueta.navigation.frames.NEDFrame;

/**
 * Converts current frame into a 3D rotation and translation change respect
 * an initial frame.
 */
public class FrameToLocalTangentPlaneTransformationConverter {

    /**
     * Reference rotation to be reused for efficiency purposes.
     */
    private final Quaternion mRefQ = new Quaternion();

    /**
     * Coordinate transformation to be reused for efficiency purposes.
     */
    private final CoordinateTransformation mC = new CoordinateTransformation(
            FrameType.BODY_FRAME, FrameType.BODY_FRAME);

    /**
     * Current ECEF frame to be reused for efficiency purposes.
     */
    private final ECEFFrame mCurrentEcefFrame = new ECEFFrame();

    /**
     * Reference frame to be reused for efficiency purposes.
     */
    private final ECEFFrame mReferenceEcefFrame = new ECEFFrame();

    /**
     * Converts provided current frame respect to provided reference frame into the
     * amount of translation and rotation that relates both frames.
     *
     * @param currentFrame      current frame.
     * @param referenceFrame    reference frame.
     * @param translationResult instance where estimated translation change will be stored.
     * @param rotationResult    instance where estimated rotation change will be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final ECEFFrame currentFrame,
                        final ECEFFrame referenceFrame,
                        final double[] translationResult,
                        final Rotation3D rotationResult)
            throws InvalidRotationMatrixException {

        if (translationResult.length != EuclideanTransformation3D.NUM_TRANSLATION_COORDS) {
            throw new IllegalArgumentException();
        }

        final double refX = referenceFrame.getX();
        final double refY = referenceFrame.getY();
        final double refZ = referenceFrame.getZ();

        final double x = currentFrame.getX();
        final double y = currentFrame.getY();
        final double z = currentFrame.getZ();

        translationResult[0] = x - refX;
        translationResult[1] = y - refY;
        translationResult[2] = z - refZ;

        referenceFrame.getCoordinateTransformation(mC);
        mC.asRotation(mRefQ);
        mRefQ.inverse();

        currentFrame.getCoordinateTransformation(mC);
        mC.asRotation(rotationResult);

        rotationResult.combine(mRefQ);
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @param result         instance where estimated 3D euclidean transformation will
     *                       be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final ECEFFrame currentFrame,
                        final ECEFFrame referenceFrame,
                        final EuclideanTransformation3D result)
            throws InvalidRotationMatrixException {
        double[] translation = result.getTranslation();
        if (translation == null) {
            translation = new double[EuclideanTransformation3D.NUM_TRANSLATION_COORDS];
        }

        Rotation3D rotation = result.getRotation();
        if (rotation == null) {
            rotation = new Quaternion();
        }

        convert(currentFrame, referenceFrame, translation, rotation);
    }

    /**
     * Converts provided current frame respect to provided reference frame into the
     * amount of translation and rotation that relates both frames.
     *
     * @param currentFrame      current frame.
     * @param referenceFrame    reference frame.
     * @param translationResult instance where estimated translation change will be stored.
     * @param rotationResult    instance where estimated rotation change will be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final NEDFrame currentFrame,
                        final ECEFFrame referenceFrame,
                        final double[] translationResult,
                        final Rotation3D rotationResult)
            throws InvalidRotationMatrixException {
        NEDtoECEFFrameConverter.convertNEDtoECEF(currentFrame, mCurrentEcefFrame);
        convert(mCurrentEcefFrame, referenceFrame, translationResult, rotationResult);
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @param result         instance where estimated 3D euclidean transformation will
     *                       be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final NEDFrame currentFrame,
                        final ECEFFrame referenceFrame,
                        final EuclideanTransformation3D result)
            throws InvalidRotationMatrixException {
        NEDtoECEFFrameConverter.convertNEDtoECEF(currentFrame, mCurrentEcefFrame);
        convert(mCurrentEcefFrame, referenceFrame, result);
    }

    /**
     * Converts provided current frame respect to provided reference frame into the
     * amount of translation and rotation that relates both frames.
     *
     * @param currentFrame      current frame.
     * @param referenceFrame    reference frame.
     * @param translationResult instance where estimated translation change will be stored.
     * @param rotationResult    instance where estimated rotation change will be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final ECEFFrame currentFrame,
                        final NEDFrame referenceFrame,
                        final double[] translationResult,
                        final Rotation3D rotationResult)
            throws InvalidRotationMatrixException {
        NEDtoECEFFrameConverter.convertNEDtoECEF(referenceFrame, mReferenceEcefFrame);
        convert(currentFrame, mReferenceEcefFrame, translationResult, rotationResult);
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @param result         instance where estimated 3D euclidean transformation will
     *                       be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final ECEFFrame currentFrame,
                        final NEDFrame referenceFrame,
                        final EuclideanTransformation3D result)
            throws InvalidRotationMatrixException {
        NEDtoECEFFrameConverter.convertNEDtoECEF(referenceFrame, mReferenceEcefFrame);
        convert(currentFrame, mReferenceEcefFrame, result);
    }

    /**
     * Converts provided current frame respect to provided reference frame into the
     * amount of translation and rotation that relates both frames.
     *
     * @param currentFrame      current frame.
     * @param referenceFrame    reference frame.
     * @param translationResult instance where estimated translation change will be stored.
     * @param rotationResult    instance where estimated rotation change will be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final NEDFrame currentFrame,
                        final NEDFrame referenceFrame,
                        final double[] translationResult,
                        final Rotation3D rotationResult)
            throws InvalidRotationMatrixException {
        NEDtoECEFFrameConverter.convertNEDtoECEF(referenceFrame, mReferenceEcefFrame);
        convert(currentFrame, mReferenceEcefFrame, translationResult, rotationResult);
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @param result         instance where estimated 3D euclidean transformation will
     *                       be stored.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public void convert(final NEDFrame currentFrame,
                        final NEDFrame referenceFrame,
                        final EuclideanTransformation3D result)
            throws InvalidRotationMatrixException {
        NEDtoECEFFrameConverter.convertNEDtoECEF(referenceFrame, mReferenceEcefFrame);
        convert(currentFrame, mReferenceEcefFrame, result);
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @return estimated 3D euclidean transformation.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public EuclideanTransformation3D convertAndReturn(
            final ECEFFrame currentFrame, final ECEFFrame referenceFrame)
            throws InvalidRotationMatrixException {
        final EuclideanTransformation3D result = new EuclideanTransformation3D();
        convert(currentFrame, referenceFrame, result);
        return result;
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @return estimated 3D euclidean transformation.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public EuclideanTransformation3D convertAndReturn(
            final NEDFrame currentFrame, final ECEFFrame referenceFrame)
            throws InvalidRotationMatrixException {
        final EuclideanTransformation3D result = new EuclideanTransformation3D();
        convert(currentFrame, referenceFrame, result);
        return result;
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @return estimated 3D euclidean transformation.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public EuclideanTransformation3D convertAndReturn(
            final ECEFFrame currentFrame, final NEDFrame referenceFrame)
            throws InvalidRotationMatrixException {
        final EuclideanTransformation3D result = new EuclideanTransformation3D();
        convert(currentFrame, referenceFrame, result);
        return result;
    }

    /**
     * Converts provided current frame respect to provided reference frame into a
     * 3D euclidean transformation that relates both frames.
     *
     * @param currentFrame   current frame.
     * @param referenceFrame reference frame.
     * @return estimated 3D euclidean transformation.
     * @throws InvalidRotationMatrixException if either current or reference frame contains
     *                                        numerically unstable rotation values.
     */
    public EuclideanTransformation3D convertAndReturn(
            final NEDFrame currentFrame, final NEDFrame referenceFrame)
            throws InvalidRotationMatrixException {
        final EuclideanTransformation3D result = new EuclideanTransformation3D();
        convert(currentFrame, referenceFrame, result);
        return result;
    }
}
