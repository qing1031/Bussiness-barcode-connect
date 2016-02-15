package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.asyncTasks.LogOutAsyncTask;
import com.foodlogiq.distributormobile.asyncTasks.UpdateBusinessesAsyncTask;
import com.foodlogiq.distributormobile.commands.LogOutAction;
import com.foodlogiq.distributormobile.customViews.DataWedgeClickListener;
import com.foodlogiq.distributormobile.databases.commonTasks.AllBusinesses;
import com.foodlogiq.distributormobile.entityClasses.Business;
import com.foodlogiq.distributormobile.viewAdapters.BusinessesArrayAdapter;
import com.foodlogiq.flqassets.CustomActionBar;
import com.foodlogiq.flqassets.FLQActivity;
import com.foodlogiq.flqassets.asyncHelpers.AsyncBooleanResponse;

import java.util.ArrayList;


/**
 * ListBusinessesActivity displays all of the businesses linked to the current users account.
 * The user can then select which one they would like to act as.
 * Implements AsyncObjectResponse to responses from server for business list request
 */
public class ListBusinessesActivity extends FLQActivity implements AsyncBooleanResponse {
    private static final int UPDATE_BUSINESSES_TASK_ID = 0;
    private boolean datawedgeExists;
    private String mobileAccessToken;
    private boolean singleBusinessOverride;
    private BusinessesArrayAdapter businessesArrayAdapter;

    /**
     * Only requests business updates from the server if there are currently none in the database.
     * If only one is returned on a request, or only one exists in the database, then the business
     * is automatically set as the default business, and the user it redirected to the location
     * selection activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        datawedgeExists = sharedPreferences.getBoolean(getString(R.string.datawedge_exists), false);
        mobileAccessToken = sharedPreferences.getString(getString(R.string.mobile_access_token),
                null);
        singleBusinessOverride = sharedPreferences.getBoolean(getString(R.string
                .singleBusinessOverride), false);
        String customHelperHost = sharedPreferences.getString(getString(R.string
                .custom_helper_site), getString(R.string.default_helper_site_host));


        /* The only way to get to this page is if these items shouldn't be present.*/
        final SharedPreferences.Editor editor = this
                .getSharedPreferences(getPackageName(), MODE_PRIVATE)
                .edit();
        /* Done removing relevant business data */

        /*Attempt to Update Stored Businesses On First Run*/

        final AllBusinesses businessesFromDB = new AllBusinesses(getContentResolver(),
                mobileAccessToken);

        if (businessesFromDB.getBusinessesCount() == 0) {
            this.getAsyncProgressSpinner().showIndeterminateProgress(true);
            UpdateBusinessesAsyncTask updateBusinessesAsyncTaskTask = new
                    UpdateBusinessesAsyncTask(this, customHelperHost, mobileAccessToken,
                    UPDATE_BUSINESSES_TASK_ID, this);
            updateBusinessesAsyncTaskTask.execute();
        } else {
            if (businessesFromDB.getBusinessesCount() == 1 && !singleBusinessOverride) {
                businessesFromDB
                        .getBusinesses()
                        .get(0)
                        .setAsCurrent(ListBusinessesActivity.this);
                launchLocationActivity();
            } else {
            /* Populate businesses from database into the listview. No need to wait for AsyncTask */

                ArrayList<Business> allBusinesses = businessesFromDB.getBusinesses();
                businessesArrayAdapter = new BusinessesArrayAdapter(this, allBusinesses,
                        (ListView) findViewById(R.id.businesses_list));
                /**************************************/
            }
        }

        /**************************************************/

    }

    /**
     * @see FLQActivity#getLayoutId()
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_list_business;
    }

    /**
     * @see FLQActivity#initializeActionBar() ()
     */
    @Override
    protected void initializeActionBar() {
        CustomActionBar customActionBar = new CustomActionBar(this, getString(R.string
                .select_business), /* Settings enabled */true, false, true);
        customActionBar.setCustomSuperBackAction(new LogOutAction(ListBusinessesActivity.this));
        setENTER_ANIMATION(R.anim.slide_in_from_right);
        setENTER_ANIMATION_PREVIOUS(R.anim.slide_in_from_left);
        setEXIT_ANIMATION(R.anim.slide_out_to_left);
        setEXIT_ANIMATION_PREVIOUS(R.anim.slide_out_to_right);
        customActionBar.setUpListeners();
    }

    /**
     * starts a {@link LocationSearchActivity} intent
     */
    public void launchLocationActivity() {
        Intent searchLocationActivity = new Intent(ListBusinessesActivity.this,
                LocationSearchActivity.class);
        startActivity(searchLocationActivity);
        overridePendingTransition(getENTER_ANIMATION(), getEXIT_ANIMATION());
    }

    /**
     * Add the reload datawedge button if datawedge exists on the device.
     *
     * @see Activity#onCreateOptionsMenu(Menu)
     * @see DataWedgeClickListener#DataWedgeClickListener(Activity)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_business, menu);
        if (datawedgeExists) {
            MenuItem dwReload = menu.add(getString(R.string.reload_datawedge));
            dwReload.setOnMenuItemClickListener(new DataWedgeClickListener(ListBusinessesActivity
                    .this));
        }
        return true;
    }

    /**
     * @see Activity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh_businesses) {
            SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                    MODE_PRIVATE);
            String customHelperHost = sharedPreferences.getString(getString(R.string
                    .custom_helper_site), getString(R.string.default_helper_site_host));

            this.getAsyncProgressSpinner().showIndeterminateProgress(true);
            UpdateBusinessesAsyncTask updateBusinessesAsyncTaskTask = new
                    UpdateBusinessesAsyncTask(this, customHelperHost, mobileAccessToken,
                    UPDATE_BUSINESSES_TASK_ID, this);
            updateBusinessesAsyncTaskTask.execute();

            return super.onOptionsItemSelected(item);
        } else if (id == R.id.logout) {
            LogOutAsyncTask logout = new LogOutAsyncTask(this, getContentResolver());
            logout.execute();
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * The async response is assumed to be a business lookup. If it's successful, then the listed
     * businesses should now exist in the database, so fire off a database lookup to populate
     * business list
     *
     * @param error  error response from server.
     * @param result success/failure of asyncTask
     * @param taskId Indicates which task is being processed.
     * @see AsyncBooleanResponse#processAsyncResponse(String, boolean, int)
     */
    @Override
    public void processAsyncResponse(String error, boolean result, int taskId) {
        this.getAsyncProgressSpinner().showIndeterminateProgress(false);
        if (!error.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(error);
            builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            /* Populate businesses from database into the listview. No need to wait for AsyncTask */

            AllBusinesses allBusinesses = new AllBusinesses(getContentResolver(),
                    mobileAccessToken);
            if (allBusinesses.getBusinessesCount() == 1 && !singleBusinessOverride) {
                allBusinesses
                        .getBusinesses()
                        .get(0)
                        .setAsCurrent(ListBusinessesActivity.this);
                launchLocationActivity();
            } else {
                businessesArrayAdapter = new BusinessesArrayAdapter(this, allBusinesses
                        .getBusinesses(), (ListView) findViewById(R.id.businesses_list));
            }

            /**************************************/
        }
    }
}
