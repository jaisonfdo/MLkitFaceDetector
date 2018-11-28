package com.droidmentor.mlkitfacedetection.FaceCenterCircleView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

/**
 * Created by Jaison.
 */
public class FaceCenterCrop {
    String TAG = "FaceCenterCrop";

    public static final int PIXEL = 0;
    public static final int DP = 1;
    protected int width, height;

    FirebaseVisionImage firebaseVisionImage;
    FirebaseVisionFaceDetector detector;

    FaceCenterCropListener faceCenterCropListener;


    public FaceCenterCrop(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }

    public FaceCenterCrop(Context context, int width, int height, int unit) {
        init();
        if (unit == PIXEL) {
            this.width = width;
            this.height = height;
        } else if (unit == DP) {
            this.width = convertDpToPixel(width, context);
            this.height = convertDpToPixel(height, context);
        } else {
            throw new IllegalArgumentException("unit should either be FaceCenterCrop.PIXEL, FaceCenterCrop.DP");
        }
    }

    public void init() {
        // To initialise the detector

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    public void transform(Bitmap original, PointF focusPoint, FaceCenterCropListener faceCenterCropListener) {

        Log.d("Time log", "Image cropping begins");

        Log.d(TAG, "transform: ");

        this.faceCenterCropListener=faceCenterCropListener;

        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("width or height should not be zero!");
        }
        float scaleX = (float) width / original.getWidth();
        float scaleY = (float) height / original.getHeight();

        if (scaleX != scaleY) {

            Bitmap.Config config =
                    original.getConfig() != null ? original.getConfig() : Bitmap.Config.ARGB_8888;
            Bitmap result = Bitmap.createBitmap(width, height, config);

            float scale = Math.max(scaleX, scaleY);

            float left = 0f;
            float top = 0f;

            float scaledWidth = width, scaledHeight = height;

            if (scaleX < scaleY) {

                scaledWidth = scale * original.getWidth();

                float faceCenterX = scale * focusPoint.x;
                left = getLeftPoint(width, scaledWidth, faceCenterX);

            } else {

                scaledHeight = scale * original.getHeight();

                float faceCenterY = scale * focusPoint.y;
                top = getTopPoint(height, scaledHeight, faceCenterY);
            }

            RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(original, null, targetRect, null);

            Log.d("Time log", "Face cropping done");

            if (faceCenterCropListener != null)
                faceCenterCropListener.onTransform(result);

          //  original.recycle();

        } else {
            if (faceCenterCropListener != null)
                faceCenterCropListener.onTransform(original);
        }
    }


    /**
     * Calculates a point (focus point) in the bitmap, around which cropping needs to be performed.
     *
     * @param bitmap                 Bitmap in which faces are to be detected.
     * @param faceCenterCropListener To send the updated bitmap with center point.
     */
    public void detectFace(Bitmap bitmap, FaceCenterCropListener faceCenterCropListener) {

        Log.d("Time log", "Detect face starts");

        firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        Log.d("Time log", "FBVI convertion done");

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(firebaseVisionImage)
                        .addOnSuccessListener(
                                faces -> {
                                    // Task completed successfully

                                    Log.d("Time log", "Face detection done");

                                    Log.d(TAG, "detectFace: " + faces.size());

                                    final int totalFaces = faces.size();
                                    if (totalFaces > 0) {
                                        transform(bitmap, getCenterPoint(faces), faceCenterCropListener);
                                    } else
                                        faceCenterCropListener.onFailure();


                                })
                        .addOnFailureListener(
                                e -> {
                                    // Task failed with an exception
                                    // ...
                                    faceCenterCropListener.onFailure();
                                });


    }

    public void detectFace(Bitmap bitmap, List<FirebaseVisionFace> faces, FaceCenterCropListener faceCenterCropListener) {

        final int totalFaces = faces.size();
        if (totalFaces > 0) {
            transform(bitmap, getCenterPoint(faces), faceCenterCropListener);
        } else
            faceCenterCropListener.onFailure();

    }


    public PointF getCenterPoint(List<FirebaseVisionFace> faces) {
        PointF centerOfAllFaces = new PointF();

        final int totalFaces = faces.size();
        if (totalFaces > 0) {
            float sumX = 0f;
            float sumY = 0f;
            for (int i = 0; i < totalFaces; i++) {
                PointF faceCenter = new PointF();
                getFaceCenter(faces.get(i), faceCenter);
                sumX = sumX + faceCenter.x;
                sumY = sumY + faceCenter.y;
            }
            centerOfAllFaces.set(sumX / totalFaces, sumY / totalFaces);
        }

        return centerOfAllFaces;
    }

    /**
     * Calculates center of a given face
     *
     * @param face   Face
     * @param center Center of the face
     */
    private void getFaceCenter(FirebaseVisionFace face, PointF center) {
        float x = face.getBoundingBox().left;
        float y = face.getBoundingBox().top;
        float width = (face.getBoundingBox().right - face.getBoundingBox().left);
        float height = (face.getBoundingBox().bottom - face.getBoundingBox().top);
        center.set(x + (width / 2), y + (height / 2)); // face center in original bitmap
    }

    private float getTopPoint(int height, float scaledHeight, float faceCenterY) {
        if (faceCenterY <= height / 2) { // Face is near the top edge
            return 0f;
        } else if ((scaledHeight - faceCenterY) <= height / 2) { // face is near bottom edge
            return height - scaledHeight;
        } else {
            return (height / 2) - faceCenterY;
        }
    }

    private float getLeftPoint(int width, float scaledWidth, float faceCenterX) {
        if (faceCenterX <= width / 2) { // face is near the left edge.
            return 0f;
        } else if ((scaledWidth - faceCenterX) <= width / 2) {  // face is near right edge
            return (width - scaledWidth);
        } else {
            return (width / 2) - faceCenterX;
        }
    }

    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) px;
    }

    public static int convertPixelsToDp(float px, Context context) {
        return (int) (px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public FaceCenterCropListener getFaceCenterCropListener() {
        return faceCenterCropListener;
    }

    public void setFaceCenterCropListener(FaceCenterCropListener faceCenterCropListener) {
        this.faceCenterCropListener = faceCenterCropListener;
    }

    public interface FaceCenterCropListener {
        void onTransform(Bitmap updatedBitmap);

        void onFailure();
    }

}
