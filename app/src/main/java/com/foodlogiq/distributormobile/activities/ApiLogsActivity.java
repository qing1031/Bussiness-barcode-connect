package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.customViews.DataWedgeClickListener;
import com.foodlogiq.distributormobile.databases.commonTasks.AllApiTransactions;
import com.foodlogiq.distributormobile.databases.contentProviders.ApiTransactionContentProvider;
import com.foodlogiq.distributormobile.entityClasses.ApiTransaction;
import com.foodlogiq.distributormobile.miscellaneousHelpers.StringUtils;
import com.foodlogiq.distributormobile.viewAdapters.ApiTransactionListAdapter;
import com.foodlogiq.flqassets.CustomActionBar;
import com.foodlogiq.flqassets.FLQActivity;

import java.util.ArrayList;

/**
 * ApiLogsActivity handles listing previously transactions to the API.
 * Currently, this includes receipt events, and quality reports.
 * ApiLogs have two states (success/failed). Failed transactions are clicked to reinitialize
 * the correct "Create" activity with the data populated from the previous attempt.
 */
public class ApiLogsActivity extends FLQActivity {
    private boolean datawedgeExists;
    private String currentBusinessFoodlogiqId;
    private String currentLocationFoodlogiqId;
    private String transactionType;
    private String transactionSubType;
    private ApiTransactionListAdapter apiTransactionListAdapter;
    private ArrayList<ApiTransaction> apiTransactions;

    /**
     * loads the apitransactions from the database and sets up the list adapter to display them.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        transactionType = b.getString("transactionType");
        transactionSubType = b.getString("transactionSubType");

        final SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        datawedgeExists = sharedPreferences.getBoolean(getString(R.string.datawedge_exists), false);
        currentBusinessFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_business_foodlogiq_id), "");
        currentLocationFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_location_foodlogiq_id), "");

        apiTransactions = getLogItems(transactionType, transactionSubType,
                currentBusinessFoodlogiqId, currentLocationFoodlogiqId);
        apiTransactionListAdapter = new ApiTransactionListAdapter(ApiLogsActivity.this,
                apiTransactions, (ListView) findViewById(R.id.api_transaction_list_view),
                getResyncActivityForTransactionType(transactionType, transactionSubType));
    }

    /**
     * @see FLQActivity#getLayoutId()
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_api_logs;
    }

    /**
     * @see FLQActivity#initializeActionBar()
     */
    @Override
    protected void initializeActionBar() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String currentLocationName = sharedPreferences.getString(getString(R.string
                .current_location_name), "");
        Bundle b = getIntent().getExtras();
        String transactionType = b.getString("transactionType");
        String transactionSubType = b.getString("transactionSubType");
        String titleString = transactionSubType == null ? transactionType : transactionSubType;
        CustomActionBar customActionBar = new CustomActionBar(this, StringUtils.toTitleCase
                (titleString + " logs for " + currentLocationName), true);
        setENTER_ANIMATION(R.anim.slide_in_from_right);
        setENTER_ANIMATION_PREVIOUS(R.anim.slide_in_from_left);
        setEXIT_ANIMATION(R.anim.slide_out_to_left);
        setEXIT_ANIMATION_PREVIOUS(R.anim.slide_out_to_right);
        customActionBar.setUpListeners();
    }

    /**
     * Returns the class for the corresponding "Create" activity associated with the apitransaction
     * type.
     *
     * @param transactionType the type of event that was sent/attempted against the api.
     * @return Create Activity for the input transactionType
     */
    private Class<?> getResyncActivityForTransactionType(String transactionType, String
            transactionSubType) {
        switch (transactionType) {
            case "events":
                switch (transactionSubType) {
                    case "receipts":
                        return CreateReceiptActivity.class;
                    case "shipments":
                        return CreateShipmentActivity.class;
                    case "transformations":
                        return CreateTransformationActivity.class;
                }
            case "incidents":
                return CreateIncidentActivity.class;
            default:
                return Activity.class;
        }
    }

    /**
     * Returns ArrayList of all apiTransactions that match the given selectors
     *
     * @param transactionType     Event type for apitransaction. Ex: "incidents"
     * @param businessFoodlogiqId id of the api business to whom the transaction belongs, or was
     *                            made by
     * @param locationFoodlogiqId id of the api location to whom the transaction belongs, or was
     *                            made by
     * @return all matching api transactions
     */
    private ArrayList<ApiTransaction> getLogItems(String transactionType, String
            transactionSubType, String businessFoodlogiqId, String locationFoodlogiqId) {
        AllApiTransactions allApiTransactions = new AllApiTransactions(getContentResolver(),
                transactionType, transactionSubType, businessFoodlogiqId, locationFoodlogiqId);
        return allApiTransactions.getApiTransactions();
    }

    private ArrayList<ApiTransaction> getLogItems(String transactionType, String
            businessFoodlogiqId, String locationFoodlogiqId) {
        return getLogItems(transactionType, null, businessFoodlogiqId, locationFoodlogiqId);
    }

    /**
     * Reloads all the current business/locations `transactionType` apiTransactions, and reinserts
     * them into the apiTransactionListAdapter. Essentially cleans and reloads.
     */
    public void updateTransactions() {
        apiTransactions = getLogItems(transactionType, transactionSubType,
                currentBusinessFoodlogiqId, currentLocationFoodlogiqId);
        apiTransactionListAdapter.clear();
        apiTransactionListAdapter.addAll(apiTransactions);
    }

    /**
     * Add the reload datawedge button if datawedge exists on the device.
     *
     * @see Activity#onCreateOptionsMenu(Menu)
     * @see DataWedgeClickListener#DataWedgeClickListener(Activity)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_api_logs, menu);
        if (datawedgeExists) {
            MenuItem dwReload = menu.add(getString(R.string.reload_datawedge));
            dwReload.setOnMenuItemClickListener(new DataWedgeClickListener(ApiLogsActivity.this));
        }
        return true;
    }

    /**
     * R.id.clear_completed clears the completed logs from the database and list.
     *
     * @see Activity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear_completed) {
            int rowsDeleted = getContentResolver().delete(
                    ApiTransactionContentProvider.CONTENT_URI,
                    "success=? and entity_type=? and sub_owner=? and business_owner=?",
                    new String[]{
                            "1",
                            transactionType,
                            currentLocationFoodlogiqId,
                            currentBusinessFoodlogiqId
                    }
            );
            Toast t = Toast.makeText(ApiLogsActivity.this, rowsDeleted + " completed actions " +
                    "removed", Toast.LENGTH_SHORT);
            t.show();
            updateTransactions();
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Fires off when a retried api transaction is completed. For instance, when the user clicks a
     * failed transaction and attempts to resubmit it, on returning to the api logs list, the list
     * is reloaded in order to show the updated status of the api transaction that was reattempted.
     *
     * @param requestCode for now, this should only ever be RESYNC_ACTIVITY
     * @param resultCode  this can be ignored, because the only downside of it not being success is
     *                    that we reload the apilogs, which is a trivial process
     * @param intent      ignored
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ApiTransactionListAdapter.RESYNC_ACTIVITY) {
            updateTransactions();
        }
    }
}
