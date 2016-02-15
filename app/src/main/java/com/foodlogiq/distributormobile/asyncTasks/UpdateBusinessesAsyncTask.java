package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.databases.commonTasks.DropBusinesses;
import com.foodlogiq.distributormobile.entityClasses.Business;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.asyncHelpers.AsyncBooleanResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * GET ng/businesses
 * On success, drops old businesses from the database and inserts new businesses returned from the
 * server.
 * Returns true if successful.
 */
public class UpdateBusinessesAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final Activity activity;
    private final String mobileAccessToken;
    private final ConnectionChecker connectionChecker;
    private AsyncBooleanResponse delegate;
    private String host;
    private String path;
    private String error = "";
    private int taskId;

    public UpdateBusinessesAsyncTask(Activity activity, String host, String mobileAccessToken,
                                     int taskId, AsyncBooleanResponse delegate) {
        this.activity = activity;
        this.host = host;
        this.path = activity.getString(R.string.api_list_businesses);
        this.mobileAccessToken = mobileAccessToken;
        this.taskId = taskId;
        this.delegate = delegate;
        this.connectionChecker = new ConnectionChecker(activity);
    }

    protected Boolean doInBackground(Void... params) {
        if (connectionChecker.isOnline()) {
            AuthenticatedHttpServiceHandler apiRequestHandler = new
                    AuthenticatedHttpServiceHandler(activity);
            String businessListResponse = apiRequestHandler.makeHttpServiceRequest(
                    host,
                    path,
                    "GET",
                    null,
                    null);
            if (businessListResponse == null) {
                this.error = activity.getString(R.string.error_server_connect);
                return false;
            } else {
                try {
                    JSONArray businessesJSONArray = new JSONArray(businessListResponse);
                            /*
                            * Remove old businesses beforehand to ensure that if a business has been
                            * removed server side, we reflect that here as well
                            */
                    DropBusinesses dropBusinesses = new DropBusinesses(activity
                            .getContentResolver(), mobileAccessToken);
                            /* Done Dropping. */
                    int businessesInserted = Business.batchCreate(activity, mobileAccessToken,
                            businessesJSONArray);
                            /*
                            * Clean out all locations to avoid having orphan businesses
                            * with no parent business present.
                            */

                            /* Done Cleaning. */
                    return true;
                } catch (JSONException e) {
                    this.error = activity.getString(R.string.error_bad_data);
                    return false;
                }
            }
        } else {
            this.error = activity.getString(R.string.error_offline);
            return false;
        }
    }

    protected void onPostExecute(Boolean result) {
        delegate
                .processAsyncResponse(error, result, this.taskId);
    }
}
