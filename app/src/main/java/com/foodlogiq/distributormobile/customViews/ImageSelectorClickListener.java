package com.foodlogiq.distributormobile.customViews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Allows user to select a picture from storage, or take one from the camera.
 */
public class ImageSelectorClickListener implements View.OnClickListener {
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_SELECT_PICTURE = 2;
    private final Activity activity;
    private String currentPhotoPath = "";

    /**
     * @param activity used for dialog builder.
     */
    public ImageSelectorClickListener(Activity activity) {
        this.activity = activity;
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    /**
     * Offers choice of storage and camera for how to get the picture.
     * Camera starts a camera intent.
     * Storage opens the photo selector.
     */
    @Override
    public void onClick(View v) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setTitle("Choose Picture From...");
        alertBuilder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentPhotoPath = dispatchTakePictureIntent();
            }
        });
        alertBuilder.setNeutralButton("Storage", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                        REQUEST_SELECT_PICTURE);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    /**
     * Creates image file, and then opens camera to capture photo at the path.
     *
     * @return the absolute path of the new photo.
     */
    private String dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                this.activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                return photoFile.getAbsolutePath();
            }
        }
        return "";
    }

    /**
     * Creates image file with timestamp to be populated from the camera.
     *
     * @return absolute path of temporary photo file.
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.activity.getExternalFilesDir(null);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }
}
