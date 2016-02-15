package com.foodlogiq.distributormobile.asyncTasks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.activities.MainActivity;
import com.foodlogiq.distributormobile.databases.commonTasks.DropBusinesses;
import com.foodlogiq.distributormobile.databases.commonTasks.DropLocations;
import com.foodlogiq.flqassets.FLQActivity;

/**
 * Removes current business and location. Also drops businesses/locations from the database.
 */
public class LogOutAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final ContentResolver contentResolver;
    private final FLQActivity activity;

    public LogOutAsyncTask(FLQActivity activity, ContentResolver contentResolver) {
        this.activity = activity;
        this.contentResolver = contentResolver;
    }

    protected Boolean doInBackground(Void... params) {
        SharedPreferences sharedPrefs = activity
                .getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);
        String mobileAccessToken = sharedPrefs.getString(activity.getString(R.string
                .mobile_access_token), null);
        DropBusinesses dropBusinesses = new DropBusinesses(contentResolver, mobileAccessToken);
        DropLocations dropLocations = new DropLocations(contentResolver, mobileAccessToken);

        SharedPreferences.Editor editor = sharedPrefs.edit();
        //Essentially logging them out.
        editor.remove(activity.getString(R.string.mobile_access_token));
        editor.remove(activity.getString(R.string.singleBusinessOverride));
        editor.apply();

        return true;
    }

    protected void onPostExecute(Boolean result) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        int enterAnimation = activity.getENTER_ANIMATION_PREVIOUS();
        int exitAnimation = activity.getEXIT_ANIMATION_PREVIOUS();
        if (enterAnimation != 0 && exitAnimation != 0)
            activity.overridePendingTransition(enterAnimation, exitAnimation);
        activity.finish();
    }
}
