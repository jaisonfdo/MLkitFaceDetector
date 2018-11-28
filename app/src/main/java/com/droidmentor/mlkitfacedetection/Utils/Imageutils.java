package com.droidmentor.mlkitfacedetection.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.droidmentor.mlkitfacedetection.FaceDetectionUtil.ScannerActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

@SuppressLint("SdCardPath")
public class Imageutils {


    public static final int SCANNER_REQUEST_CODE = 1001;
    public static final int GALEERY_REQUEST_CODE = 2001;

    Context context;
    private Activity current_activity;
    private Fragment current_fragment;

    private ImageAttachmentListener imageAttachment_callBack;

    private String selected_path = "";
    private Uri imageUri;
    private File path = null;

    private int from = 0;
    private boolean isFragment = false;

    public Imageutils(Activity act) {

        this.context = act;
        this.current_activity = act;
    }

    public Imageutils(Activity act, ImageAttachmentListener imageAttachmentListener) {

        this.context = act;
        this.current_activity = act;
        imageAttachment_callBack = imageAttachmentListener;
    }


    public Imageutils(Activity act, Fragment fragment, boolean isFragment) {

        this.context = act;
        this.current_activity = act;
        imageAttachment_callBack = (ImageAttachmentListener) fragment;
        if (isFragment) {
            this.isFragment = true;
            current_fragment = fragment;
        }

    }

    public ImageAttachmentListener getImageAttachment_callBack() {
        return imageAttachment_callBack;
    }

    public void setImageAttachment_callBack(ImageAttachmentListener imageAttachment_callBack) {
        this.imageAttachment_callBack = imageAttachment_callBack;
    }

    /**
     * Get file name from path
     *
     * @param path
     * @return
     */

    public String getfilename_from_path(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());

    }

    /**
     * Get Image URI from Bitmap
     *
     * @param context
     * @param photo
     * @return
     */

    public Uri getImageUri(Context context, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 80, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Get Path from Image URI
     *
     * @param uri
     * @return
     */

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.context.getContentResolver().query(uri, projection, null, null, null);
        int column_index = 0;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } else
            return uri.getPath();
    }

    /**
     * Bitmap from String
     *
     * @param encodedString
     * @return
     */
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


    /**
     * Get String from Bitmap
     *
     * @param bitmap
     * @return
     */

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }


    /**
     * Check Camera Availability
     *
     * @return
     */

    public boolean isDeviceSupportCamera() {
        if (this.context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    /**
     * Compress Imgae
     *
     * @param imageUri
     * @param height
     * @param width
     * @return
     */


    public Bitmap compressImage(String imageUri, float height, float width) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.

        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // max Height and width values of the compressed image is taken as 816x612

        float maxHeight = height;
        float maxWidth = width;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        //  setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        //  inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        // this options allow android to claim the bitmap memory if it runs low on memory

        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            //  load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        // check the rotation of the image and display it properly

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);

            return scaledBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get RealPath from Content URI
     *
     * @param contentURI
     * @return
     */
    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }


    /**
     * ImageSize Calculation
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * Launch Camera
     *
     * @param from
     */

    public void launchCamera(int from) {
        this.from = from;

        if (Build.VERSION.SDK_INT >= 23) {
            if (isFragment)
                permission_check_fragment(1);
            else
                permission_check(1);
        } else {
            launchScanner();
        }
    }

    /**
     * Launch Gallery
     *
     * @param from
     */

    public void launchGallery(int from) {

        this.from = from;

        if (Build.VERSION.SDK_INT >= 23) {
            if (isFragment)
                permission_check_fragment(2);
            else
                permission_check(2);
        } else {
            launchGallery();
        }
    }

    /**
     * Show AlertDialog with the following options
     * <p>
     * Camera
     * Gallery
     *
     * @param from
     */

    public void imagepicker(final int from) {
        this.from = from;

        final CharSequence[] items;

        if (isDeviceSupportCamera()) {
            items = new CharSequence[2];
            items[0] = "Camera";
            items[1] = "Gallery";
        } else {
            items = new CharSequence[1];
            items[0] = "Gallery";
        }

        android.app.AlertDialog.Builder alertdialog = new android.app.AlertDialog.Builder(current_activity);
        alertdialog.setTitle("Add Image");
        alertdialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Camera")) {
                    //launchCamera(from);
                    launchScanner();
                } else if (items[item].equals("Gallery")) {
                    launchGallery(from);
                }
            }
        });
        alertdialog.show();
    }

    /**
     * Check permission
     *
     * @param code
     */

    public void permission_check(final int code) {
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(current_activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(current_activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                showMessageOKCancel("For adding images , You need to provide permission to access your files",
                        (dialog, which) -> ActivityCompat.requestPermissions(current_activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                code));
                return;
            }

            ActivityCompat.requestPermissions(current_activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    code);
            return;
        }

        if (code == 1)
            launchScanner();
        else if (code == 2)
            launchGallery();
    }


    /**
     * Check permission
     *
     * @param code
     */

    public void permission_check_fragment(final int code) {
        Log.d(TAG, "permission_check_fragment: " + code);
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(current_activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED)

        {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(current_activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                showMessageOKCancel("For adding images , You need to provide permission to access your files",
                        (dialog, which) -> current_fragment.requestPermissions(
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                code));
                return;
            }

            current_fragment.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    code);
            return;
        }

        if (code == 1)
            launchScanner();
        else if (code == 2)
            launchGallery();
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(current_activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    /**
     * Capture image from camera
     */

    public void launchScanner() {
        Intent scannerIntent = new Intent(current_activity, ScannerActivity.class);

        if (isFragment)
            current_fragment.startActivityForResult(scannerIntent, SCANNER_REQUEST_CODE);
        else
            current_activity.startActivityForResult(scannerIntent, SCANNER_REQUEST_CODE);
    }

    /**
     * pick image from Gallery
     */

    public void launchGallery() {

        Log.d("Time log", "launchGallery: ");
        Log.d(TAG, "galley_call: ");

        Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent2.setType("image/*");

        if (isFragment)
            current_fragment.startActivityForResult(intent2, GALEERY_REQUEST_CODE);
        else
            current_activity.startActivityForResult(intent2, GALEERY_REQUEST_CODE);

    }


    /**
     * Activity PermissionResult
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchScanner();
                } else {
                    Toast.makeText(current_activity, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            case 2:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchGallery();
                } else {

                    Toast.makeText(current_activity, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    /**
     * Intent ActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String file_name;
        Bitmap bitmap;

        Log.d(TAG, "onActivityResult: ");

        switch (requestCode) {
            case SCANNER_REQUEST_CODE:

                if (resultCode == Activity.RESULT_OK && !TextUtils.isEmpty(data.getStringExtra("image"))) {

                    Log.i("Camera Selected", "Photo");

                    try {

                        String imagePath = data.getStringExtra("image");
                        File imgFile = new File(imagePath);

                        Bitmap bmp = getImage(imgFile);

                        if (bmp != null)
                            imageAttachment_callBack.image_attachment(SCANNER_REQUEST_CODE, "captured_image", bmp, imageUri);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                break;
            case GALEERY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("Time log", "Image selected: ");

                    Log.i("Gallery", "Photo");
                    Uri selectedImage = data.getData();

                    try {
                        selected_path = null;
                        selected_path = getPath(selectedImage);

                        Log.d(TAG, "onActivityResult: " + selected_path);
                        file_name = selected_path.substring(selected_path.lastIndexOf("/") + 1);
                        bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImage);
                        Log.d("Time log", "got bitmap: ");
                        imageAttachment_callBack.image_attachment(GALEERY_REQUEST_CODE, file_name, bitmap, selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
        }


    }

    /**
     * Get image from Uri
     *
     * @param uri
     * @param height
     * @param width
     * @return
     */
    public Bitmap getImage_FromUri(Uri uri, float height, float width) {
        Bitmap bitmap = null;

        try {
            bitmap = compressImage(uri.toString(), height, width);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Get filename from URI
     *
     * @param uri
     * @return
     */
    public String getFileName_from_Uri(Uri uri) {
        String path = null, file_name = null;

        try {

            path = getRealPathFromURI(uri.getPath());
            file_name = path.substring(path.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return file_name;

    }


    /**
     * Check Image Exist (or) Not
     *
     * @param file_name
     * @param file_path
     * @return
     */

    public boolean checkimage(String file_name, String file_path) {
        boolean flag;
        path = new File(file_path);

        File file = new File(path, file_name);
        if (file.exists()) {
            Log.i("file", "exists");
            flag = true;
        } else {
            Log.i("file", "not exist");
            flag = false;
        }

        return flag;
    }


    /**
     * Get Image from the given path
     *
     * @param file_name
     * @param file_path
     * @return
     */

    public Bitmap getImage(String file_name, String file_path) {

        path = new File(file_path);
        File file = new File(path, file_name);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 2;
        options.inTempStorage = new byte[16 * 1024];

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        if (bitmap != null)
            return bitmap;
        else
            return null;
    }

    public Bitmap getImage(File selectedFile) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 2;
        options.inTempStorage = new byte[16 * 1024];

        if (!selectedFile.exists())
            return null;

        Bitmap bitmap = BitmapFactory.decodeFile(selectedFile.getAbsolutePath(), options);

        if (bitmap != null)
            return bitmap;
        else
            return null;
    }


    /**
     * Create an image
     *
     * @param bitmap
     * @param file_name
     * @param filepath
     * @param file_replace
     */


    public void createImage(Bitmap bitmap, String file_name, String filepath, boolean file_replace) {

        path = new File(filepath);

        if (!path.exists()) {
            path.mkdirs();
        }

        File file = new File(path, file_name);

        if (file.exists()) {
            if (file_replace) {
                file.delete();
                file = new File(path, file_name);
                store_image(file, bitmap);
                Log.i("file", "replaced");
            }
        } else {
            store_image(file, bitmap);
        }

    }


    /**
     * @param file
     * @param bmp
     */
    public void store_image(File file, Bitmap bmp) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Image Attachment Callback

    public interface ImageAttachmentListener {
        public void image_attachment(int from, String filename, Bitmap file, Uri uri);
    }
}

