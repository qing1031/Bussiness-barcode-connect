package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.asyncTasks.LocationSearchAsyncTask;
import com.foodlogiq.distributormobile.asyncTasks.LogOutAsyncTask;
import com.foodlogiq.distributormobile.commands.BackToListBusinesses;
import com.foodlogiq.distributormobile.customViews.BackNextButtons;
import com.foodlogiq.distributormobile.customViews.DataWedgeClickListener;
import com.foodlogiq.distributormobile.databases.commonTasks.FindLocation;
import com.foodlogiq.distributormobile.entityClasses.Business;
import com.foodlogiq.distributormobile.entityClasses.Location;
import com.foodlogiq.flqassets.CustomActionBar;
import com.foodlogiq.flqassets.FLQActivity;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;

/**
 * LocationSearchActivity handles searching for a location to perform actions on.
 * Implements AsyncObjectResponse to responses from server on location search
 */
public class LocationSearchActivity extends FLQActivity implements AsyncObjectResponse {
    private static final int SEARCH_LOCATION_TASK_ID = 0;
    private boolean datawedgeExists;
    private String customHelperHost;
    private String currentBusinessFoodlogiqId;
    private Location foundLocation;
    private BackNextButtons backNextButtons;

    /**
     * Setups the form for location search. Also updates the location partial if
     * it's already been selected in the past.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        datawedgeExists = sharedPreferences.getBoolean(getString(R.string.datawedge_exists), false);
        currentBusinessFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_business_foodlogiq_id), "");
        customHelperHost = sharedPreferences.getString(getString(R.string.custom_helper_site),
                getString(R.string.default_helper_site_host));

        final Button locationSearchButton = (Button) findViewById(R.id.location_search_button);
        Typeface fontAwesome = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        locationSearchButton.setTypeface(fontAwesome);
        locationSearchButton.setText(getString(R.string.fa_search));
        locationSearchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performLocationSearchRequest();
                    }
                }
        );
        checkForExistingLocationSelection(sharedPreferences);
        updateLocationPartial();
        initializeSearchListener();
        initializeBackNextButtons();
    }

    /**
     * @see FLQActivity#getLayoutId()
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_location_search;
    }

    /**
     * @see FLQActivity#initializeActionBar()
     */
    @Override
    protected void initializeActionBar() {
        CustomActionBar customActionBar = new CustomActionBar(this, getString(R.string
                .enter_restaurant_number), /* Settings enabled */true, true);
        customActionBar.setCustomBackAction(new BackToListBusinesses(LocationSearchActivity.this));
        setENTER_ANIMATION(R.anim.slide_in_from_right);
        setENTER_ANIMATION_PREVIOUS(R.anim.slide_in_from_left);
        setEXIT_ANIMATION(R.anim.slide_out_to_left);
        setEXIT_ANIMATION_PREVIOUS(R.anim.slide_out_to_right);
        customActionBar.setUpListeners();
    }

    /**
     * Verifies that a location has not been selected already. If it has been then populate the
     * form.
     * If default business was checked earlier, then repopulate. If not, show blank form.
     */
    private void checkForExistingLocationSelection(SharedPreferences sharedPreferences) {
        final boolean defaultLocation = sharedPreferences.getBoolean(getString(R.string
                .default_location), false);
        if (defaultLocation) {
            String currentLocationId = sharedPreferences.getString(getString(R.string
                    .current_location_id), "");
            if (!currentLocationId.isEmpty()) {
                this.foundLocation = new FindLocation(getContentResolver(), currentLocationId)
                        .getLocation();
                ((EditText) findViewById(R.id.location_search_edit_text))
                        .setText(this.foundLocation.getInternalId());
                if (this.foundLocation == null) {
                    Location.removeDefault(this);
                    Location.removeCurrent(this);
                    return;
                }
            } else {
                Location.removeDefault(this);
                Location.removeCurrent(this);
                return;
            }
            ((SwitchCompat) findViewById(R.id.location_default_checkbox)).setChecked(true);
        } else {
            Location.removeCurrent(this);
        }
    }

    /**
     * Set up location search field to fire off the search when user hits enter with the field
     * focused.
     */
    private void initializeSearchListener() {
        EditText locationSearchEditText = (EditText) findViewById(R.id.location_search_edit_text);
        locationSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent
                        .KEYCODE_ENTER) {
                    performLocationSearchRequest();
                    return false;
                }
                return false;
            }
        });
    }

    /**
     * Send request to server with internalId of location to look for.
     */
    private void performLocationSearchRequest() {
        EditText locationSearchEditText = (EditText) findViewById(R.id.location_search_edit_text);
        // hide virtual keyboard
        InputMethodManager imm;
        imm = (InputMethodManager) getApplicationContext().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(locationSearchEditText.getWindowToken(), 0);
        if (locationSearchEditText.getText().toString().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LocationSearchActivity.this);
            builder.setMessage("You need to enter an internal location id to proceed");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            new LocationSearchAsyncTask(
                    LocationSearchActivity.this,
                    customHelperHost,
                    currentBusinessFoodlogiqId,
                    locationSearchEditText
                            .getText()
                            .toString(),
                    SEARCH_LOCATION_TASK_ID,
                    LocationSearchActivity.this)
                    .execute();
        }
    }

    /**
     * BackButton press: Allow business selection again and take the user to the
     * {@link ListBusinessesActivity}
     * NextButton press: If location isn't selected, then alert the user. If it is selected, then
     * set the location
     * as the current location in the preferences.
     */
    protected void initializeBackNextButtons() {
        View.OnClickListener backClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences.Editor editor = LocationSearchActivity.this
                        .getSharedPreferences(getPackageName(), MODE_PRIVATE)
                        .edit();
                editor.putBoolean(LocationSearchActivity.this.getString(R.string
                        .singleBusinessOverride), false);
                editor.apply();
                Business.removeCurrent(LocationSearchActivity.this);
                Location.removeCurrent(LocationSearchActivity.this);
                Location.removeDefault(LocationSearchActivity.this);
                Intent listBusinessActivity = new Intent(LocationSearchActivity.this,
                        ListBusinessesActivity.class);
                listBusinessActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(listBusinessActivity);
                overridePendingTransition(getENTER_ANIMATION_PREVIOUS(),
                        getEXIT_ANIMATION_PREVIOUS());
                finish();
            }
        };

        View.OnClickListener nextClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foundLocation == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LocationSearchActivity
                            .this);
                    builder.setMessage("You need to select a location before proceeding");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface
                            .OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                } else {
                    foundLocation.saveToDB(LocationSearchActivity.this);
                    foundLocation.setAsCurrent(LocationSearchActivity.this);
                    SwitchCompat defaultCheckBox = (SwitchCompat) findViewById(R.id
                            .location_default_checkbox);
                    if (defaultCheckBox.isChecked()) {
                        Location.setDefault(LocationSearchActivity.this);
                    } else {
                        Location.removeDefault(LocationSearchActivity.this);
                    }
                    Intent locationActionsActivity = new Intent(LocationSearchActivity.this,
                            LocationActionsActivity.class);
                    startActivity(locationActionsActivity);
                    overridePendingTransition(getENTER_ANIMATION(), getEXIT_ANIMATION());
                }
            }
        };
        backNextButtons = new BackNextButtons(this, backClickListener, nextClickListener);
    }

    /**
     * Add the reload datawedge button if datawedge exists on the device.
     *
     * @see Activity#onCreateOptionsMenu(Menu)
     * @see DataWedgeClickListener#DataWedgeClickListener(Activity)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout_only, menu);
        if (datawedgeExists) {
            MenuItem dwReload = menu.add(getString(R.string.reload_datawedge));
            dwReload.setOnMenuItemClickListener(new DataWedgeClickListener(LocationSearchActivity
                    .this));
        }
        return true;
    }

    /**
     * @see Activity#onOptionsItemSelected(MenuItem) ()
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            LogOutAsyncTask logout = new LogOutAsyncTask(this, getContentResolver());
            logout.execute();
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * <p>If successful:</p>
     * <ul>
     * <li>
     * {@link #SEARCH_LOCATION_TASK_ID}: Updates contents of receiving event based on location
     * returned from server.
     * Checks for supply chain, and notifies if it's not setup.
     * Also checks for association with community. <b>If not this disables the next button.</b>
     * </li>
     * </ul>
     *
     * @param error  error response from server.
     * @param result a location matching the internalId from the search.
     * @param taskId Indicates which task is being processed.
     * @see AsyncObjectResponse#processAsyncResponse(String, Object, int)
     */
    @Override
    public void processAsyncResponse(String error, Object result, int taskId) {
        if (taskId == SEARCH_LOCATION_TASK_ID) {
            getAsyncProgressSpinner().showIndeterminateProgress(false);
            if (!error.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(error);
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener
                        () {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                updateLocationPartial();
            } else {
                switch (taskId) {
                    case SEARCH_LOCATION_TASK_ID:
                        this.foundLocation = (Location) result;
                        boolean disableNext = false;
                        String message = this.foundLocation.getName().concat(" is missing:\n");
                        boolean missingSupplier = false;
                        boolean missingSupplied = false;
                        if (this.foundLocation.getSuppliers() == null
                                || this.foundLocation.getSuppliers().size() == 0) {
                            missingSupplier = true;
                            message = message.concat("-Suppliers\n");
                        }
                        if (this.foundLocation.getSupplyChainLocations() == null
                                || this.foundLocation.getSupplyChainLocations().size() == 0) {
                            missingSupplier = true;
                            message = message.concat("-Supplier Locations (Needed for Receiving\n");
                        }
                        if (this.foundLocation.getSuppliesLocations() == null
                                || this.foundLocation.getSuppliesLocations().size() == 0) {
                            missingSupplied = true;
                            message = message.concat("-Locations you supply (Needed for " +
                                    "Shipping\n");
                        }
                        if (this.foundLocation.getCommunityId().isEmpty()) {
                            disableNext = true;
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            String locName = this.foundLocation.getName().isEmpty() ?
                                    "This location" : this.foundLocation.getName();
                            message = locName.concat(" does not have a community associated with " +
                                    "it!\nIt needs to be associated with a community before it " +
                                    "can be used. You can do so here:\n");
                            message = message.concat
                                    (("CONNECT_URL/\n" +
                                            "businesses/\n" +
                                            "BUSINESS_ID\n/" +
                                            "locations/\n" +
                                            "LOCATION_ID")
                                            .replace("CONNECT_URL", customHelperHost)
                                            .replace("BUSINESS_ID", currentBusinessFoodlogiqId)
                                            .replace("LOCATION_ID", this.foundLocation
                                                    .getFoodlogiqId()));
                            builder.setMessage(message);
                            builder.setNeutralButton(android.R.string.ok, new DialogInterface
                                    .OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            builder.create().show();
                        } else if (missingSupplied || missingSupplier) {
                            if (missingSupplied && missingSupplier) {
                                disableNext = true;
                            }
                            AlertDialog.Builder builder = new
                                    AlertDialog.Builder(this);
                            message = message.concat("We recommend setting up your supply chain. " +
                                    "You can do so here:\n");
                            message = message.concat("CONNECT_URL/businesses/BUSINESS_ID/locations")
                                    .replace("CONNECT_URL", customHelperHost)
                                    .replace("BUSINESS_ID", currentBusinessFoodlogiqId);

                            builder.setMessage(message);
                            builder.setNeutralButton(android.R.string.ok, new DialogInterface
                                    .OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            builder.create().show();
                        }
                        updateLocationPartial();
                        if (disableNext) {
                            this.backNextButtons.disableNextButton();
                        } else {
                            this.backNextButtons.enableNextButton();
                            final SwitchCompat defaultLocationSwitch = (SwitchCompat)
                                    findViewById(R.id.location_default_checkbox);
                            defaultLocationSwitch.setChecked(true);
                        }
                        return;
                    default:
                }
            }
        }

    }

    /**
     * Updates partials with contents of found location, or empties fields if one isn't there.
     */
    private void updateLocationPartial() {
        LinearLayout locationPartial = (LinearLayout) findViewById(R.id.location_partial);
        if (foundLocation == null) {
            ((TextView) locationPartial.findViewById(R.id.location_name)).setText("");
            ((TextView) locationPartial.findViewById(R.id.location_street_address)).setText("");
            ((TextView) locationPartial.findViewById(R.id.location_city_state)).setText("");
            ((TextView) locationPartial.findViewById(R.id.location_postal_code)).setText("");
            ((TextView) locationPartial.findViewById(R.id.location_country)).setText("");
            ((TextView) locationPartial.findViewById(R.id.location_phone)).setText("");
            findViewById(R.id.location_default_wrapper)
                    .setVisibility(View.GONE);
        } else {
            ((TextView) locationPartial.findViewById(R.id.location_name)).setText(foundLocation
                    .getName());
            ((TextView) locationPartial.findViewById(R.id.location_street_address)).setText
                    (foundLocation.getStreetAddress());
            ((TextView) locationPartial.findViewById(R.id.location_city_state)).setText
                    (foundLocation.getCity() + ", " + foundLocation.getRegion());
            ((TextView) locationPartial.findViewById(R.id.location_postal_code)).setText
                    (foundLocation.getPostalCode());
            ((TextView) locationPartial.findViewById(R.id.location_country)).setText
                    (foundLocation.getCountry());
            ((TextView) locationPartial.findViewById(R.id.location_phone)).setText(foundLocation
                    .getPhone());
            findViewById(R.id.location_default_wrapper)
                    .setVisibility(View.VISIBLE);
        }
    }
}
