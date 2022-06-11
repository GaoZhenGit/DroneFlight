package hk.hku.flight.util;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import hk.hku.flight.DroneApplication;

public class FetchImageUtil {
    private static final String TAG = "FetchImageUtil";
    private final int SELECT_IMAGE = 32132;
    private static final int REQUIRED_SIZE = 280;
    public void fetchImage(Activity activity) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        activity.startActivityForResult(i, SELECT_IMAGE);
    }

    public Bitmap getBitmapFromResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == RESULT_OK && null != data) {
                Uri uri = data.getData();
                try {
                    return decodeUri(uri);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        ContentResolver contentResolver = DroneApplication.getInstance().getContentResolver();
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o);
        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o2);

        // Rotate according to EXIF
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(contentResolver.openInputStream(selectedImage));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public File saveImageToDiskCache(Bitmap bitmap) {
        try {
            File cacheImage = new File(DroneApplication.getInstance().getCacheDir() + File.separator + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(cacheImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Log.i(TAG, "saveImageToDiskCache success");
            return cacheImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
