package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.entityClasses.Location;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * GET ng/businesses/:businessId/locations/search/:locationInternalId
 * with internalId value.
 * Returns Location object if one is found.
 */
public class LocationSearchAsyncTask extends AsyncTask<Void, Void, Object> {
    private final Activity activity;
    private final ConnectionChecker connectionChecker;
    private final String host;
    private final String path;
    private final int taskId;
    private final AsyncObjectResponse delegate;
    private String error = "";

    public LocationSearchAsyncTask(Activity activity, String host, String businessId, String
            locationInternalId, int taskId, AsyncObjectResponse delegate) {
        this.activity = activity;
        this.host = host;
        this.path = activity.getString(R.string.api_location_search)
                .replaceAll(":businessId", businessId)
                .replaceAll(":locationInternalId", locationInternalId);
        this.taskId = taskId;
        this.delegate = delegate;
        this.connectionChecker = new ConnectionChecker(activity);
    }

    @Override
    protected Object doInBackground(Void... params) {
        if (connectionChecker.isOnline()) {
            AuthenticatedHttpServiceHandler apiRequestHandler = new
                    AuthenticatedHttpServiceHandler(activity);
            String locationSearchResponse = apiRequestHandler.makeHttpServiceRequest(
                    host,
                    path,
                    "GET",
                    null,
                    null);
            if (locationSearchResponse == null) {
                this.error = activity.getString(R.string.error_server_connect);
                return null;
            } else {
                try {
                    JSONObject locationJSONObject = new JSONObject(locationSearchResponse);
                    if (locationJSONObject.has("msg") && locationJSONObject.getString("msg")
                            .equals("not found")) {
                        this.error = "No location found with that Internal Id";
                        return null;
                    }
                    Location location = new Location();
                    location.parseJSON(locationJSONObject);
                    return location;
                } catch (JSONException e) {
                    this.error = activity.getString(R.string.error_bad_data);
                    return null;
                }
            }
        } else {
            this.error = activity.getString(R.string.error_offline);
            return null;
        }
    }

    protected void onPostExecute(Object result) {
        delegate
                .processAsyncResponse(error, result, this.taskId);
    }
}
