package com.droidmentor.mlkitfacedetection.FaceDetectionUtil;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.droidmentor.mlkitfacedetection.FaceDetectionUtil.common.FrameMetadata;
import com.droidmentor.mlkitfacedetection.FaceDetectionUtil.common.GraphicOverlay;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.util.List;

/**
 * Created by Jaison.
 */
public interface FaceDetectionResultListener {
    void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    void onFailure(@NonNull Exception e);
}
