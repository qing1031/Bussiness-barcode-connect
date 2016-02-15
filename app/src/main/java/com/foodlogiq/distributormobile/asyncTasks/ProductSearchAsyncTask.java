package com.foodlogiq.distributormobile.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.entityClasses.Product;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * GET ng/communities/:communityId/products/search/:productGlobalTradeItemNumber
 * with globalTradeItemNumber value.
 * Returns Product object if one is found.
 */
public class ProductSearchAsyncTask extends AsyncTask<Void, Void, Object> {
    private final Activity activity;
    private final ConnectionChecker connectionChecker;
    private final String host;
    private final Product defaultProduct;
    private final int taskId;
    private final AsyncObjectResponse delegate;
    private final String communityId;
    private String path;
    private String error = "";

    public ProductSearchAsyncTask(Activity activity, String host, String communityId, String
            globalTradeItemNumber, int taskId, AsyncObjectResponse delegate) {
        this.activity = activity;
        this.communityId = communityId;
        this.host = host;
        this.path = activity.getString(R.string.api_product_search);
        this.path = this.path.replaceAll(":communityId", communityId);
        this.path = this.path.replaceAll(":productGlobalTradeItemNumber", globalTradeItemNumber);
        this.taskId = taskId;
        this.delegate = delegate;
        this.connectionChecker = new ConnectionChecker(activity);
        this.defaultProduct = new Product(globalTradeItemNumber);
    }

    @Override
    protected Product doInBackground(Void... params) {
        if (connectionChecker.isOnline()) {
            AuthenticatedHttpServiceHandler apiRequestHandler = new
                    AuthenticatedHttpServiceHandler(activity);
            String productSearchResponse = apiRequestHandler.makeHttpServiceRequest(
                    host,
                    path,
                    "GET",
                    null,
                    null);
            if (productSearchResponse == null) {
                this.error = activity.getString(R.string.error_server_connect);
                return null;
            } else {
                try {
                    JSONObject productJSONObject = new JSONObject(productSearchResponse);
                    if (productJSONObject.has("msg") && productJSONObject.getString("msg").equals
                            ("not found")) {
                        return this.defaultProduct;
                    }
                    return new Product(productJSONObject, communityId);
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
