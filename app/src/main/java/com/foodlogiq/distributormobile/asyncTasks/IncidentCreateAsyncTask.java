package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.entityClasses.Incident;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * POST ng/businesses/:businessId/incidents
 * with Incident value.
 * Returns incident id from server if successful.
 */
public class IncidentCreateAsyncTask extends AsyncTask<Void, Void, Object> {
    private final Activity activity;
    private final ConnectionChecker connectionChecker;
    private final String host;
    private final String path;
    private final int taskId;
    private final AsyncObjectResponse delegate;
    private final Incident incident;
    private String error = "";

    public IncidentCreateAsyncTask(Activity activity, String host, String businessId, Incident
            incident, int taskId, AsyncObjectResponse delegate) {
        this.activity = activity;
        this.host = host;
        this.path = activity.getString(R.string.api_create_incident)
                .replaceAll(":businessId", businessId);
        this.taskId = taskId;
        this.delegate = delegate;
        this.connectionChecker = new ConnectionChecker(activity);

        this.incident = incident;
    }

    @Override
    protected Object doInBackground(Void... params) {
        if (connectionChecker.isOnline()) {
            JSONObject incidentJson = incident.createJSON();
            AuthenticatedHttpServiceHandler apiRequestHandler = new
                    AuthenticatedHttpServiceHandler(activity);
            String incidentCreateResponse = apiRequestHandler.makeHttpServiceRequest(
                    host,
                    path,
                    "POST",
                    null,
                    incident.createJSON()
            );
            if (incidentCreateResponse == null) {
                this.error = activity.getString(R.string.error_server_connect);
                return null;
            } else {
                try {
                    JSONObject locationJSONObject = new JSONObject(incidentCreateResponse);
                    if (locationJSONObject.has("msg") && locationJSONObject.getString("msg")
                            .equals("api error")) {
                        this.error = "Error ecountered on API";
                        return null;
                    }
                    if (!locationJSONObject.has("_id")) {
                        this.error = "No id sent back. Suspected failure on server";
                        return null;
                    }
                    return locationJSONObject.getString("_id");
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
