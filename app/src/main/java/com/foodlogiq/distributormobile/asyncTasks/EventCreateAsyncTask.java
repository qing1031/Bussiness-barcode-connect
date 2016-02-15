package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.interfaces.JSONParceable;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * POST ng/businesses/:businessId/events/receiving
 * with Receiving Event value.
 * Returns receiving event id from server if successful.
 */
public class EventCreateAsyncTask extends AsyncTask<Void, Void, Object> {
    private final Activity activity;
    private final ConnectionChecker connectionChecker;
    private final String host;
    private final String path;
    private final int taskId;
    private final AsyncObjectResponse delegate;
    private final JSONParceable event;
    private String error = "";

    public EventCreateAsyncTask(Activity activity, String host, String businessId, String eventType,
                                JSONParceable event, int taskId, AsyncObjectResponse delegate) {
        this.activity = activity;
        this.host = host;
        this.path = activity.getString(R.string.api_create_event)
                .replaceAll(":businessId", businessId)
                .replaceAll(":eventType", eventType);
        this.taskId = taskId;
        this.delegate = delegate;
        this.connectionChecker = new ConnectionChecker(activity);
        this.event = event;
    }

    @Override
    protected Object doInBackground(Void... params) {
        if (connectionChecker.isOnline()) {
            AuthenticatedHttpServiceHandler apiRequestHandler = new
                    AuthenticatedHttpServiceHandler(activity);
            JSONObject jsonObj = event.createJSON();
            String eventCreateResponse = apiRequestHandler.makeHttpServiceRequest(
                    host,
                    path,
                    "POST",
                    null,
                    event.createJSON()
            );
            if (eventCreateResponse == null) {
                this.error = activity.getString(R.string.error_server_connect);
                return null;
            } else {
                try {
                    JSONObject responseJsonObject = new JSONObject(eventCreateResponse);
                    if (responseJsonObject.has("msg") && responseJsonObject.getString("msg")
                            .equals("api error")) {
                        this.error = "Error ecountered on API";
                        return null;
                    }
                    if (!responseJsonObject.has("_id")) {
                        this.error = "No id sent back. Suspected failure on server";
                        return null;
                    }
                    return responseJsonObject.getString("_id");
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
