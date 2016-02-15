package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.entityClasses.Photo;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;
import com.foodlogiq.flqassets.httpHandlers.MimeType;
import com.foodlogiq.flqassets.httpHandlers.S3FileUploadHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;

/**
 * PUT ng/businesses/:businessId/incidents/:incidentId/updateimages
 * with Incident value. This returns a link reserved on S3 for uploading the photo.
 * Then the photo is uploaded to S3
 */
public class UploadIncidentPhotoAsyncTask extends AsyncTask<Void, Void, String> {
    private final ConnectionChecker connectionChecker;
    private final String path;
    private final String incidentId;
    private Activity activity;
    private String host;
    private String businessId;
    private Photo photo;
    private int taskId;
    private AsyncObjectResponse delegate;
    private String error = "";

    public UploadIncidentPhotoAsyncTask(Activity activity, String host, String businessId, String
            incidentId, Photo photo, int taskId, AsyncObjectResponse delegate) {
        this.activity = activity;
        this.host = host;
        this.businessId = businessId;
        this.incidentId = incidentId;
        this.path = activity.getString(R.string.api_update_incident_images)
                .replaceAll(":businessId", businessId)
                .replaceAll(":incidentId", incidentId);
        this.photo = photo;
        this.taskId = taskId;
        this.delegate = delegate;
        this.connectionChecker = new ConnectionChecker(activity);
    }

    @Override
    protected String doInBackground(Void... params) {
        if (connectionChecker.isOnline()) {
            File photoFile = new File(photo.getPath());
            if (!photoFile.isFile()) {
                this.error = "File at path " + photo.getPath() + " does not exist";
                return incidentId;
            }

            JSONObject uploadInfo = new JSONObject();
            try {
                uploadInfo.put("ContentType", (new MimeType(photoFile.getAbsolutePath()))
                        .toString());
                uploadInfo.put("FileName", photoFile.getName());
            } catch (JSONException e) {
                e.printStackTrace();
                this.error = "Invalid Path MimeType";
                return incidentId;
            }

            AuthenticatedHttpServiceHandler getS3UploadLink = new AuthenticatedHttpServiceHandler
                    (activity);
            String getS3UploadLinkResponse = getS3UploadLink.makeHttpServiceRequest(
                    host,
                    path,
                    "PUT",
                    null,
                    uploadInfo
            );
            if (getS3UploadLinkResponse == null) {
                this.error = activity.getString(R.string.error_server_connect);
                return null;
            } else {
                try {
                    JSONObject getS3UploadLinkResponseJSON = new JSONObject
                            (getS3UploadLinkResponse);
                    if (getS3UploadLinkResponseJSON.has("msg") && getS3UploadLinkResponseJSON
                            .getString("msg").equals("api error")) {
                        this.error = "Error ecountered on API";
                        return incidentId;
                    }
                    if (!getS3UploadLinkResponseJSON.has("updateURL")) {
                        this.error = "No S3 link sent back. Suspected failure on server";
                        return incidentId;
                    }
                    Boolean uploaded = uploadToS3(getS3UploadLinkResponseJSON.getString
                            ("updateURL"), photoFile);
                    if (!uploaded) {
                        this.error = "S3 upload failed";
                        return incidentId;
                    }
                } catch (JSONException e) {
                    this.error = activity.getString(R.string.error_bad_data);
                    return incidentId;
                }
                return incidentId;
            }
        } else {
            this.error = activity.getString(R.string.error_offline);
            return incidentId;
        }
    }

    @Override
    protected void onPostExecute(final String result) {
        delegate
                .processAsyncResponse(error, result, this.taskId);
    }

    private Boolean uploadToS3(String uploadURL, File photoFile) {
        S3FileUploadHandler uploadToS3;
        try {
            uploadToS3 = new S3FileUploadHandler(uploadURL, photoFile);
            return uploadToS3.upload();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
