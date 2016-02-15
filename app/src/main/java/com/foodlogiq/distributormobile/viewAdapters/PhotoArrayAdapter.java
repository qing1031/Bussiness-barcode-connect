package com.foodlogiq.distributormobile.viewAdapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;

import com.foodlogiq.distributormobile.entityClasses.Photo;

import java.util.ArrayList;

/**
 * Handles displaying thumbnails of each photo in the Array. Allows removal of photo from array by
 * clicking on it's thumbnail.
 */
public class PhotoArrayAdapter extends ArrayAdapter<Photo> {
    private final Activity activity;
    private final ArrayList<Photo> values;
    private final ArrayList<View> childViews = new ArrayList<>();

    public PhotoArrayAdapter(final Activity activity, final ArrayList<Photo> values, GridView
            listView) {
        super(activity, 0, values);
        this.activity = activity;
        this.values = values;
        listView.setAdapter(this);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Photo photo = values.get(position);
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageButton rowView = new ImageButton(activity.getApplicationContext());
        rowView.setPadding(0, 0, 0, 0);
        rowView.setAdjustViewBounds(true);


        rowView.setImageBitmap(photo.getThumbNail());

        //Update views

        //Attach Row
        setClickListener(rowView, values.get(position));
        rowView.setId(position);
        childViews.add(rowView);
        return rowView;
    }

    /**
     * Show a confirm alert on click, and if user selects yes, remove photo from parent Array.
     *
     * @param photo The photo to remove if the users desires.
     */
    private void setClickListener(final View rowView, final Photo photo) {
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                alertBuilder.setTitle("Do you want to remove this photo");
                alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PhotoArrayAdapter.this.remove(photo);
                        photo.getThumbNail().recycle();
                        PhotoArrayAdapter.this.notifyDataSetChanged();
                    }
                });
                alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
        });
    }
}
