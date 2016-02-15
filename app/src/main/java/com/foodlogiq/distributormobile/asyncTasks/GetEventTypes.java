package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.databases.commonTasks.DropEventTypes;
import com.foodlogiq.distributormobile.entityClasses.EventType;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.asyncHelpers.AsyncBooleanResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * GET ng/businesses/:businessId/events/search/:eventInternalId
 * with internalId value.
 * Returns event object if one is found.
 */
public class GetEventTypes extends AsyncTask<Void, Void, Boolean> {
    private final Activity activity;
    private final ConnectionChecker connectionChecker;
    private final String host;
    private final String path;
    private final int taskId;
    private final AsyncBooleanResponse delegate;
    private final String businessId;
    private final String eventType;
    private String error = "";

    public GetEventTypes(Activity activity, String host, String businessId, String
            eventType, int taskId, AsyncBooleanResponse delegate) {
        this.activity = activity;
        this.host = host;
        this.businessId = businessId;
        this.eventType = eventType;
        this.path = activity.getString(R.string.api_get_event_types)
                .replaceAll(":businessId", businessId)
                .replaceAll(":eventType", eventType);
        this.taskId = taskId;
        this.delegate = delegate;
        this.connectionChecker = new ConnectionChecker(activity);
    }

    protected Boolean doInBackground(Void... params) {
        if (connectionChecker.isOnline()) {
            AuthenticatedHttpServiceHandler apiRequestHandler = new
                    AuthenticatedHttpServiceHandler(activity);
            String getEventTypesResponse = apiRequestHandler.makeHttpServiceRequest(
                    host,
                    path,
                    "GET",
                    null,
                    null);
            if (getEventTypesResponse == null) {
                this.error = activity.getString(R.string.error_server_connect);
                return false;
            } else {
                try {
                    new DropEventTypes(activity.getContentResolver(), this.businessId, this
                            .eventType);
                    JSONArray eventTypesJSONArray = new JSONArray(getEventTypesResponse);
                    EventType.batchCreate(activity, businessId, eventTypesJSONArray);
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
