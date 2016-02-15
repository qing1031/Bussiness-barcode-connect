package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.asyncTasks.LogOutAsyncTask;
import com.foodlogiq.distributormobile.customViews.DataWedgeClickListener;
import com.foodlogiq.distributormobile.entityClasses.Location;
import com.foodlogiq.distributormobile.entityClasses.LocationAction;
import com.foodlogiq.distributormobile.viewAdapters.LocationActionsAdapter;
import com.foodlogiq.flqassets.CustomActionBar;
import com.foodlogiq.flqassets.FLQActivity;

import java.util.ArrayList;

/**
 * LocationActionsActivity creates a list of possible actions that a location can take.
 * Eventually this will be dynamic to handle whether a location is a restaurant (currently the
 * default
 * actions), or a distributor (shipping events, transformations)
 */
public class LocationActionsActivity extends FLQActivity {
    private LocationActionsAdapter locationActionsAdapter;
    private boolean datawedgeExists;
    private Location currentLocation;

    /**
     * Adds actions to the main view.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        datawedgeExists = sharedPreferences.getBoolean(getString(R.string.datawedge_exists), false);
        currentLocation = Location.getCurrent(LocationActionsActivity.this);

        addActionIcons();
    }

    /**
     * @see FLQActivity#getLayoutId()
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_location_actions;
    }

    /**
     * @see FLQActivity#initializeActionBar()
     */
    @Override
    protected void initializeActionBar() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String currentLocationName = sharedPreferences.getString(getString(R.string
                .current_location_name), "");
        CustomActionBar customActionBar = new CustomActionBar(this, currentLocationName, /*
        Settings enabled */true);
        setENTER_ANIMATION(R.anim.slide_in_from_right);
        setENTER_ANIMATION_PREVIOUS(R.anim.slide_in_from_left);
        setEXIT_ANIMATION(R.anim.slide_out_to_left);
        setEXIT_ANIMATION_PREVIOUS(R.anim.slide_out_to_right);
        customActionBar.setUpListeners();
    }

    /**
     * Adds location actions to the
     * {@link LocationActionsAdapter#LocationActionsAdapter(FLQActivity, ArrayList, ListView)}.
     */
    private void addActionIcons() {
        ArrayList<LocationAction> locationActions = new ArrayList<>();


        if (currentLocation.getSupplyChainLocations().size() > 0) {
            locationActions.add(new LocationAction(getString(R.string.create_receipt),
                    "fa-plus-circle", CreateReceiptActivity.class));
            Bundle receiptLogsBundle = new Bundle();
            receiptLogsBundle.putString("transactionType", "events");
            receiptLogsBundle.putString("transactionSubType", "receipts");
            locationActions.add(new LocationAction(getString(R.string.receipt_logs), "fa-list-ul",
                    ApiLogsActivity.class, "grey", receiptLogsBundle));
        }

        if (currentLocation.getSuppliesLocations().size() > 0) {
            locationActions.add(new LocationAction(getString(R.string.create_shipment),
                    "fa-plus-circle", CreateShipmentActivity.class));
            Bundle shipmentLogsBundle = new Bundle();
            shipmentLogsBundle.putString("transactionType", "events");
            shipmentLogsBundle.putString("transactionSubType", "shipments");
            locationActions.add(new LocationAction(getString(R.string.shipment_logs), "fa-list-ul",
                    ApiLogsActivity.class, "grey", shipmentLogsBundle));
        }

        locationActions.add(new LocationAction(getString(R.string.transform_products),
                "fa-plus-circle", CreateTransformationActivity.class));
        Bundle transformLogsBundle = new Bundle();
        transformLogsBundle.putString("transactionType", "events");
        transformLogsBundle.putString("transactionSubType", "transformations");
        locationActions.add(new LocationAction(getString(R.string.transform_logs), "fa-list-ul",
                ApiLogsActivity.class, "grey", transformLogsBundle));

//        locationActions.add(new LocationAction(getString(R.string.create_quality_report),
//                "fa-plus-circle", CreateIncidentActivity.class));
//        Bundle incidentLogsBundle = new Bundle();
//        incidentLogsBundle.putString("transactionType", "incidents");
//        locationActions.add(new LocationAction(getString(R.string.incident_logs), "fa-list-ul",
//                ApiLogsActivity.class, "grey", incidentLogsBundle));
        locationActionsAdapter = new LocationActionsAdapter(this, locationActions, (ListView)
                findViewById(R.id.location_actions_list));
    }

    /**
     * Add the reload datawedge button if datawedge exists on the device.
     *
     * @see Activity#onCreateOptionsMenu(Menu)
     * @see DataWedgeClickListener#DataWedgeClickListener(Activity)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location_actions, menu);
        if (datawedgeExists) {
            MenuItem dwReload = menu.add(getString(R.string.reload_datawedge));
            dwReload.setOnMenuItemClickListener(new DataWedgeClickListener
                    (LocationActionsActivity.this));
        }
        return true;
    }

    /**
     * Selecting the location select option will clear this activity and take the user start a
     * {@link LocationSearchActivity}
     *
     * @see Activity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            LogOutAsyncTask logout = new LogOutAsyncTask(this, getContentResolver());
            logout.execute();
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.select_location) {
            Intent locationSearchActivity = new Intent(LocationActionsActivity.this,
                    LocationSearchActivity.class);
            locationSearchActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(locationSearchActivity);
            overridePendingTransition(getENTER_ANIMATION_PREVIOUS(), getEXIT_ANIMATION_PREVIOUS());
            LocationActionsActivity.this.finish();
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
