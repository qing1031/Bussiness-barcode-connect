package com.foodlogiq.distributormobile.entityClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Photo stores the path to a photo, and allows generating a thumbnail to display.
 */
public class Photo {
    final int THUMBSIZE = 350;
    private final String path;
    private Bitmap thumbNail;

    public Photo(String path) {
        this.path = path;
    }

    /**
     * Calculates the scale at which to display the bitmap in order to meet the max required height,
     * or width, whichever is larger.
     *
     * @param reqWidth  Max width needed for Thumbnail
     * @param reqHeight Max height needed for Thumbnail
     * @return A thumbnail of the correct required height/width.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int
            reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public String getPath() {
        return path;
    }

    public Bitmap getThumbNail() {
        if (thumbNail != null) return thumbNail;
        // calculating image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(new FileInputStream(new File(getPath())), null, options);
            int scale = calculateInSampleSize(options, THUMBSIZE, THUMBSIZE);

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            thumbNail = BitmapFactory.decodeStream(new FileInputStream(new File(getPath())),
                    null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return thumbNail;
    }
}


