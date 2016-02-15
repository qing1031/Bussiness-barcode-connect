package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.flqassets.ConnectionChecker;

import java.io.IOException;
import java.net.URL;

/**
 * Gets a business's icon image from s3 to show it in the business list.
 * Returns true if successful
 */
public class GetIconImageAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final Activity activity;
    private final ConnectionChecker connectionChecker;
    private final ImageView iconView;
    private String iconUrl;
    private String error = "";
    private Bitmap bmp;

    public GetIconImageAsyncTask(Activity activity, String iconUrl, ImageView iconView) {
        this.activity = activity;
        this.iconUrl = iconUrl;
        this.iconView = iconView;
        this.connectionChecker = new ConnectionChecker(activity);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (this.iconUrl.isEmpty()) {
            return false;
        }
        if (connectionChecker.isOnline()) {
            try {
                URL url = new URL(this.iconUrl);
                this.bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            this.error = activity.getString(R.string.error_offline);
            return false;
        }
    }

    protected void onPostExecute(Boolean result) {
        if (result)
            this.iconView.setImageBitmap(this.bmp);
    }
}
