package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.asyncTasks.EventCreateAsyncTask;
import com.foodlogiq.distributormobile.asyncTasks.LogOutAsyncTask;
import com.foodlogiq.distributormobile.asyncTasks.ProductSearchAsyncTask;
import com.foodlogiq.distributormobile.commands.BackToLocationActions;
import com.foodlogiq.distributormobile.customViews.BackNextButtons;
import com.foodlogiq.distributormobile.customViews.DataWedgeClickListener;
import com.foodlogiq.distributormobile.customViews.DatePickerEditText;
import com.foodlogiq.distributormobile.databases.commonTasks.AllEventTypes;
import com.foodlogiq.distributormobile.databases.commonTasks.FindApiTransaction;
import com.foodlogiq.distributormobile.entityClasses.ApiTransaction;
import com.foodlogiq.distributormobile.entityClasses.EventType;
import com.foodlogiq.distributormobile.entityClasses.Location;
import com.foodlogiq.distributormobile.entityClasses.Product;
import com.foodlogiq.distributormobile.entityClasses.SparseProduct;
import com.foodlogiq.distributormobile.interfaces.CustomAttributeView;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;
import com.foodlogiq.distributormobile.scanhelpers.BarcodeParser;
import com.foodlogiq.distributormobile.scanhelpers.DataWedgeParser;
import com.foodlogiq.distributormobile.viewAdapters.EntityTypeSpinner;
import com.foodlogiq.distributormobile.viewAdapters.SparseProductListAdapter;
import com.foodlogiq.flqassets.CustomActionBar;
import com.foodlogiq.flqassets.FLQActivity;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.intermec.aidc.BarcodeReadEvent;
import com.intermec.aidc.BarcodeReadListener;
import com.intermec.aidc.BarcodeReader;
import com.intermec.aidc.BarcodeReaderException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by djak250 on 1/14/16.
 */
public class CreateShipmentActivity extends FLQActivity implements AsyncObjectResponse,
        BarcodeReadListener {
    private static final int SHIPMENT_CUSTOM_ATTRIBUTES = 1;
    private static final int SHIPMENT_INFO_SLIDE = 0;
    private static final int SHIPMENT_CONTENTS_SLIDE = 2;
    private static final int SHIPMENT_CONFIRM_SLIDE = 3;

    private static final int SHIPMENT_EVENT_CREATE_TASK_ID = 0;
    private static final int PRODUCT_SEARCH_TASK_ID = 1;
    private static final int MANUAL_PRODUCT_ENTRY = 2;

    private boolean datawedgeExists;
    private String currentCommunityFoodlogiqId;
    private String currentBusinessFoodlogiqId;
    private String currentLocationFoodlogiqId;
    private Location currentLocation;
    private ShipmentEvent shipment;

    private String customHelperHost;
    private String apiTransactionid;
    private int mSlideIndex = 0;
    private ArrayList<View> slides;
    private BackNextButtons backNextButton;
    private SparseProduct tempSparseProduct;
    private BroadcastReceiver scanBroadcastReceiver;
    private DatePickerEditText shipmentDatePicker;
    private EntityTypeSpinner eventType;
    private SparseProductListAdapter sparseProductListAdapter;
    private ArrayList<EventType> shipmentTypes;
    private BarcodeReader bcr;

    /**
     * Sets which layout should be visible in the main view. Hides the others.
     * Also hides the soft keyboard if it is visible.
     *
     * @param mSlideIndex Index of the slide to be shown.
     */
    public void setSlideIndex(int mSlideIndex) {
        //Hides keyboard if it's open
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if (imm != null && view != null && view.getWindowToken() != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        /////////////////////////////
        if (mSlideIndex > SHIPMENT_CONFIRM_SLIDE) {
            submitShipmentEvent();
            return;
        }

        this.mSlideIndex = mSlideIndex;
        for (int i = 0; i < slides.size(); i++) {
            if (mSlideIndex == i) {
                slides.get(i).setVisibility(View.VISIBLE);
            } else {
                slides.get(i).setVisibility(View.GONE);
            }
        }
        handleViewUpdates(mSlideIndex);
    }

    /**
     * Increment {@link #mSlideIndex} and fire off handlers in {@link #setSlideIndex}.
     */
    public void incrementSlideIndex() {
        if (mSlideIndex == SHIPMENT_CUSTOM_ATTRIBUTES - 1 && eventType != null && ((EventType)
                eventType.getSelectedItem()).isNull()) {
            setSlideIndex(mSlideIndex + 2);
        } else {
            setSlideIndex(mSlideIndex + 1);
        }
    }

    /**
     * Decrement {@link #mSlideIndex} and fire off handlers in {@link #setSlideIndex}.
     */
    public void decrementSlideIndex() {
        if (mSlideIndex == SHIPMENT_CUSTOM_ATTRIBUTES + 1 && eventType != null && ((EventType)
                eventType.getSelectedItem()).isNull()) {
            setSlideIndex(mSlideIndex - 2);
        } else {
            setSlideIndex(mSlideIndex - 1);
        }
    }


    private void handleViewUpdates(int mSlideIndex) {
        findViewById(R.id.bottom_back_button).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.bottom_next_button_text)).setText(getString(R.string.next));
        switch (mSlideIndex) {
            case SHIPMENT_INFO_SLIDE:
                findViewById(R.id.bottom_back_button).setVisibility(View.GONE);
                break;
            case SHIPMENT_CONFIRM_SLIDE:
                updateConfirmView();
                ((TextView) findViewById(R.id.bottom_next_button_text))
                        .setText(getString(R.string.submit));
                break;
        }
    }

    private void updateConfirmView() {
        ((TextView) findViewById(R.id.confirm_shipment_location))
                .setText(this.currentLocation.getName());
        ((TextView) findViewById(R.id.shipment_confirm_date))
                .setText(((TextView) findViewById(R.id.shipment_date)).getText());

        ListView contents = (ListView) (findViewById(R.id
                .confirm_contents_wrapper).findViewById(R.id.scanned_items_list_view));
        contents.setAdapter(sparseProductListAdapter);
        findViewById(R.id.confirm_contents_wrapper).findViewById(R.id.scan_list_header)
                .setVisibility(View.VISIBLE);
        TextView locationTextView = (TextView) findViewById(R.id.shipment_location_text_view);
        locationTextView.setText(currentLocation.getName());
        TextView destinationTextView = (TextView) findViewById(R.id.confirm_shipment_destination);
        Location destinationLocation = (Location) (
                (Spinner) findViewById(R.id.shipment_destination_supply_chain_location_spinner))
                .getSelectedItem();
        if (destinationLocation != null) {
            destinationTextView.setText(
                    destinationLocation.toString()
            );
        }

        ((TextView) findViewById(R.id.shipment_confirm_total_qty)).setText(String.valueOf
                (sparseProductListAdapter.getTotalQuantities()));

        LinearLayout customAttributesWrapper = (LinearLayout) findViewById(R.id
                .confirm_custom_attributes_wrapper);
        customAttributesWrapper.removeAllViews();
        HashMap<String, CustomAttributeView>
                customAttributeViews = eventType.getCustomAttributeViewsByStoredAs();
        Iterator iter = customAttributeViews.entrySet().iterator();
        int i = 0;
        LinearLayout rowView = new LinearLayout(getApplicationContext());
        while (iter.hasNext()) {
            final float scale = getResources().getDisplayMetrics().density;
            int twoDP = (int) (2 * scale + 0.5f);
            int twentyDP = (int) (20 * scale + 0.5f);
            rowView.setPadding(twentyDP, 0, twentyDP, twoDP);

            Map.Entry pair = (Map.Entry) iter.next();
            CustomAttributeView customAttributeView = (CustomAttributeView) pair.getValue();
            String customAttributeName = customAttributeView.getCustomAttribute().getCommonName();
            String customAttributeValue = customAttributeView.getValueAsString();
            rowView.addView(buildCustomView(customAttributeName, customAttributeValue));
            i++;
            if (i % 2 == 0) {
                customAttributesWrapper.addView(rowView);
                rowView = new LinearLayout(getApplicationContext());
            }
        }
        if (rowView.getChildCount() != 0)
            customAttributesWrapper.addView(rowView);
    }

    private LinearLayout buildCustomView(String customAttributeName, String customAttributeValue) {
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, .5f));
        ll.setOrientation(LinearLayout.VERTICAL);
        TextView nameView = new TextView(getApplicationContext());
        nameView.setText(customAttributeName);
        nameView.setTextColor(getResources().getColor(R.color.dk_gray));
        nameView.setTextSize(12);
        TextView valueView = new TextView(getApplicationContext());
        valueView.setText(customAttributeValue);
        valueView.setTextColor(getResources().getColor(android.R.color.black));
        ll.addView(nameView);
        ll.addView(valueView);
        return ll;
    }


    /**
     * Checks for existing apiTransaction to detect if activity was reached through the api logs
     * activity, and attempts to repopulate data from previous attempt.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        datawedgeExists = sharedPreferences.getBoolean(getString(R.string.datawedge_exists), false);
        currentCommunityFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_community_foodlogiq_id), "");
        currentBusinessFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_business_foodlogiq_id), "");
        customHelperHost = sharedPreferences.getString(getString(R.string.custom_helper_site),
                getString(R.string.default_helper_site_host));
        currentLocation = Location.getCurrent(CreateShipmentActivity.this);
        formTriggers();

        Typeface fontAwesome = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        ((TextView) findViewById(R.id.map_marker)).setTypeface(fontAwesome);
        ((TextView) findViewById(R.id.map_marker_custom_attributes)).setTypeface(fontAwesome);

        setSlideIndex(SHIPMENT_INFO_SLIDE);
        hardwareScannerSetup();
        intermecSetup();

        this.shipment = new ShipmentEvent(
                currentBusinessFoodlogiqId,
                currentLocation.getName(),
                currentLocation.getGlobalLocationNumber());

        Intent i = getIntent();
        apiTransactionid = i.getStringExtra("apiTransactionId");
        if (apiTransactionid != null && !apiTransactionid.isEmpty()) {
            populateExistingShipment(apiTransactionid);
        }
        handleViewUpdates(mSlideIndex);
    }

    /**
     * Used by FLQActivity to insert the layout below the customActionbar.
     *
     * @return layout for current activity
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_shipment;
    }

    /**
     * Used by FLQActivity to setup logic for action bar.
     */
    @Override
    protected void initializeActionBar() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String currentLocationName = "Shipment" + ": " + sharedPreferences.getString(getString(R
                .string.current_location_name), "");
        CustomActionBar customActionBar = new CustomActionBar(this, currentLocationName, /*
        Settings enabled */true, false, true);
        customActionBar.setCustomSuperBackAction(new BackToLocationActions
                (CreateShipmentActivity
                        .this));
        setENTER_ANIMATION(R.anim.slide_in_from_right);
        setENTER_ANIMATION_PREVIOUS(R.anim.slide_in_from_left);
        setEXIT_ANIMATION(R.anim.slide_out_to_left);
        setEXIT_ANIMATION_PREVIOUS(R.anim.slide_out_to_right);
        customActionBar.setUpListeners();
    }

    private void populateExistingShipment(String apiTransactionid) {
        /**
         * Parses the json stored in the database in order to repopulate and trigger the correct
         * switches.
         * In essence, mirrors the form to look like it did when the shipment creation was
         * attempted the first time.
         *
         * @param apiTransactionid SQL id of the previous attempt of an Receiving Event
         */
        ApiTransaction apiTransaction = new FindApiTransaction(getContentResolver(),
                apiTransactionid).getApiTransaction();
        if (apiTransaction != null) {
            try {
                (shipment = new ShipmentEvent()).parseJSON(new JSONObject(apiTransaction
                        .getJsonString()));

                String dateString = shipment.getDate();
                try {
                    ((TextView) findViewById(R.id.shipment_date)).setText(DateFormatters
                            .simpleFormat.format(DateFormatters
                                    .isoDateFormatter.parse(dateString)));
                } catch (ParseException e) {
                }

                sparseProductListAdapter.clear();
                sparseProductListAdapter.addAll(shipment.getContents());
                ArrayList<Location> suppliesLocations =
                        currentLocation.getSuppliesLocations();
                for (int i = 0; i < suppliesLocations.size(); i++) {
                    if (suppliesLocations.get(i).getGlobalLocationNumber().equals
                            (shipment
                                    .getDestinationGln())) {
                        ((Spinner) findViewById(R.id
                                .shipment_destination_supply_chain_location_spinner))
                                .setSelection(i);
                        break;
                    }
                }

                for (int i = 0; i < shipmentTypes.size(); i++) {
                    if (shipmentTypes.get(i).getFoodlogiqId().equals(shipment.getEventTypeId())) {
                        ((Spinner) findViewById(R.id
                                .shipment_event_type_spinner)).setSelection(i);
                        break;
                    }
                }
                eventType.populateCustomAttributeValues(shipment.getCustomAttributes());

                updateConfirmView();
                setSlideIndex(SHIPMENT_CONFIRM_SLIDE);
            } catch (JSONException e) {
                //TODO: HANDLE REPOPULATION ERROR
            }
        }
    }

    private void formTriggers() {
        slides = new ArrayList<>();
        slides.add(findViewById(R.id.shipment_info_layout));
        slides.add(findViewById(R.id.shipment_custom_attributes));
        slides.add(findViewById(R.id.contents_wrapper));
        slides.add(findViewById(R.id.shipment_confirm_layout));
        backNextButton = new BackNextButtons(CreateShipmentActivity.this, new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementSlideIndex();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementSlideIndex();
            }
        });
        LinearLayout contentsWrapper = (LinearLayout) findViewById(R.id.contents_wrapper);
        sparseProductListAdapter = new SparseProductListAdapter(
                this,
                new ArrayList<SparseProduct>(),
                contentsWrapper,
                (ListView) findViewById(R.id.scanned_items_list_view)
        );
        sparseProductListAdapter.addSlideToHiddenButtons(R.id.confirm_scanned_items_list_view);
        Button scanButton = (Button) contentsWrapper.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(getStartScanListener(contentsWrapper));

        Button inputManualButton = (Button) contentsWrapper.findViewById(R.id.manual_entry_button);
        View.OnClickListener manualClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent manualEntryActivity = new Intent(CreateShipmentActivity.this,
                        ProductDialogActivity.class);
                startActivityForResult(manualEntryActivity, MANUAL_PRODUCT_ENTRY);
            }
        };
        inputManualButton.setOnClickListener(manualClickListener);

        Spinner destinationSpinner = (Spinner) findViewById(R.id
                .shipment_destination_supply_chain_location_spinner);
        ArrayAdapter<Location> suppliesLocation = new
                ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currentLocation
                .getSuppliesLocations());
        suppliesLocation.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(suppliesLocation);
        destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) findViewById(R.id.shipment_contents_location_name)).setText(parent
                        .getSelectedItem().toString());
                ((TextView) findViewById(R.id.shipment_custom_attributes_location_name)).setText
                        (parent
                                .getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ((TextView) findViewById(R.id.shipment_contents_location_name)).setText("");
                ((TextView) findViewById(R.id.shipment_custom_attributes_location_name)).setText
                        (parent
                                .getSelectedItem().toString());
            }
        });

        TextView shipmentLocation = (TextView) findViewById(R.id.shipment_location_text_view);
        shipmentLocation.setText(currentLocation.getName());
        TextView shipmentDate = (TextView) findViewById(R.id.shipment_date);
        shipmentDatePicker = new DatePickerEditText(CreateShipmentActivity
                .this, shipmentDate, true);
        shipmentTypes = new AllEventTypes(getContentResolver(),
                currentBusinessFoodlogiqId,
                "shipping")
                .getEventTypes();

        LinearLayout eventTypeLayout = (LinearLayout) findViewById(R.id.shipment_event_type_wrapper);
        if (shipmentTypes == null || shipmentTypes.size() == 0) {
            eventTypeLayout.setVisibility(View.GONE);
        }

        eventType = (EntityTypeSpinner) findViewById(R.id
                .shipment_event_type_spinner);
        eventType.init(CreateShipmentActivity.this, shipmentTypes);

    }


    /**
     * <p>Creates a broadcast receiver to receive input from the datawedge application. This is used
     * for Motorala devices equipped with hardware scanners.</p>
     * <p>Attempts to gather gtin/lot/etc. data from barcode scanned to populate product
     * information</p>
     * <p>Devices tested on:</p>
     * <ul>
     * <li><a href="https://www.zebra.com/us/en/products/mobile-computers/handheld.html#mainpar-productseries_5e86">TC55</a></li>
     * <li><a href="https://www.zebra.com/us/en/products/mobile-computers/handheld.html#mainpar-productseries_e1a0">TC70</a></li>
     * </ul>
     * <p>Should function on any modern Android device equipped with DataWedge, though this is
     * not tested.</p>
     */
    private void hardwareScannerSetup() {
        scanBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "com.sonim.barcode_read":
                        parseSonimBarcode(intent);
                        break;
                    case "com.foodlogiq.mobile.scan":
                        parseDataWedgeBarcode(intent);
                        break;
                }
            }
        };
        CreateShipmentActivity.this.registerReceiver(scanBroadcastReceiver, new IntentFilter
                ("com.foodlogiq.mobile.scan"));
        CreateShipmentActivity.this.
                registerReceiver(
                        scanBroadcastReceiver, new IntentFilter("com.sonim.barcode_read")
                );
    }

    private void intermecSetup() {
        try {
            //get bar code instance from MainActivity
            bcr = MainActivity.getBarcodeObject();

            if (bcr != null) {
                //enable scanner
                bcr.setScannerEnable(true);

                //set listener
                bcr.addBarcodeReadListener(this);
            }

        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
    }

    private void parseDataWedgeBarcode(Intent intent) {
        Bundle extras = intent.getExtras();
        ArrayList<CharSequence> scanContent = extras.getCharSequenceArrayList("com" +
                ".motorolasolutions.emdk.datawedge.decode_data");
        String scannedString = extras.getString("com.motorolasolutions.emdk.datawedge" +
                ".data_string");
        String scanFormat = extras.getString("com.motorolasolutions.emdk.datawedge" +
                ".label_type");
        if (scanFormat == null) return;

        byte[] bytes = (byte[]) ((List<?>) scanContent).get(0);
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateShipmentActivity.this);

        Intent productDialogIntent = new Intent(CreateShipmentActivity.this,
                ProductDialogActivity.class);
        Bundle productExtras = new Bundle();
        switch (scanFormat) {
            case "LABEL-TYPE-EAN128":
                DataWedgeParser dataWedgeParser = new DataWedgeParser(bytes);
                productExtras = buildProductDialogIntent(
                        dataWedgeParser.getGlobalTradeItemNumber(),
                        dataWedgeParser.getLot(),
                        dataWedgeParser.getPackDate(),
                        dataWedgeParser.getUseThroughDate(),
                        dataWedgeParser.getSerialNumber(),
                        dataWedgeParser.getQuantity()
                );
                productDialogIntent.putExtras(productExtras);
                break;
            case "LABEL-TYPE-I2OF5":
                productExtras = buildProductDialogIntent(
                        scannedString,
                        "ITF-14",
                        "",
                        "",
                        "",
                        1
                );
                productDialogIntent.putExtras(productExtras);
                break;
            //These all have the same functionality. Just pad the content with zeros on
            // the left.
            case "LABEL-TYPE-EAN13":
            case "LABEL-TYPE-UPCE0":
            case "LABEL-TYPE-UPCA":
            case "UPC_E":
                String paddedGtin = padStringTo14Characters(scannedString);
                productExtras = buildProductDialogIntent(
                        paddedGtin,
                        "UPC",
                        "",
                        "",
                        "",
                        1
                );
                productDialogIntent.putExtras(productExtras);
                break;
            default:
                builder.setMessage("You scanned a '" + scanFormat + "' which is currently" +
                        " not supported");
                return;
        }
        startActivityForResult(productDialogIntent, MANUAL_PRODUCT_ENTRY);
    }

    private Bundle buildProductDialogIntent(String globalTradeItemNumber, String lot, String
            packDate, String useThroughDate, String serialNumber, int quantity) {
        Bundle b = new Bundle();
        b.putString("globalTradeItemNumber", globalTradeItemNumber);
        b.putString("lot", lot);
        b.putString("packDate", packDate);
        b.putString("useThroughDate", useThroughDate);
        b.putString("serialNumber", serialNumber);
        b.putInt("quantity", quantity);
        return b;
    }

    private void parseSonimBarcode(Intent intent) {
        Bundle extras = intent.getExtras();
        String scanContent = extras.getString("data");
        DataWedgeParser dataWedgeParser = new DataWedgeParser(scanContent.getBytes());
        Intent productDialogIntent = new Intent(CreateShipmentActivity.this,
                ProductDialogActivity.class);
        Bundle productExtras = buildProductDialogIntent(
                dataWedgeParser.getGlobalTradeItemNumber(),
                dataWedgeParser.getLot(),
                dataWedgeParser.getPackDate(),
                dataWedgeParser.getUseThroughDate(),
                dataWedgeParser.getSerialNumber(),
                dataWedgeParser.getQuantity()
        );
        productDialogIntent.putExtras(productExtras);
        startActivityForResult(productDialogIntent, MANUAL_PRODUCT_ENTRY);
    }

    private void parseIntermecBarcode(String scanContent, String symbologyName) {
        String paddedGtin = "";
        Intent productDialogIntent = new Intent(CreateShipmentActivity.this,
                ProductDialogActivity.class);
        Bundle productExtras = new Bundle();
        switch (symbologyName) {
            case "GS1_128":
                //Intermec has three bytes at the front that we don't need.
                byte[] slicedContent = Arrays.copyOfRange(scanContent.getBytes(), 3, scanContent
                        .length());

                DataWedgeParser barcodeParser = new DataWedgeParser(slicedContent);
                productExtras = buildProductDialogIntent(
                        barcodeParser.getGlobalTradeItemNumber(),
                        barcodeParser.getLot(),
                        barcodeParser.getPackDate(),
                        barcodeParser.getUseThroughDate(),
                        barcodeParser.getSerialNumber(),
                        barcodeParser.getQuantity()
                );
                productDialogIntent.putExtras(productExtras);
                break;
            case "EAN8":
            case "EAN13":
            case "UPCE":
                paddedGtin = padStringTo14Characters(scanContent);
                productExtras = buildProductDialogIntent(
                        paddedGtin,
                        "UPC",
                        "",
                        "",
                        "",
                        1
                );
                productDialogIntent.putExtras(productExtras);
                break;
            case "ITF":
                paddedGtin = padStringTo14Characters(scanContent);
                productExtras = buildProductDialogIntent(
                        paddedGtin,
                        "ITF-14",
                        "",
                        "",
                        "",
                        1
                );
                productDialogIntent.putExtras(productExtras);
                break;
            default:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage(String.format(
                        "Unfortunately, we don't yet support %s type barcodes on Intermec devices",
                        symbologyName, scanContent
                ));
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                b.create().show();
                return;
        }
        startActivityForResult(productDialogIntent, MANUAL_PRODUCT_ENTRY);
    }

    /**
     * <p>If successful:</p>
     * <ul>
     * <li>{@link #PRODUCT_SEARCH_TASK_ID}: Updates contents of shipment Event based on
     * product
     * returned from server.</li>
     * <li>{@link #SHIPMENT_EVENT_CREATE_TASK_ID}: Insert successful shipping Event
     * json
     * into db
     * .</li>
     * </ul>
     *
     * @param error  error response from server.
     * @param result Either a product from {@link ProductSearchAsyncTask}
     *               or a foodlogiq Id from {@link EventCreateAsyncTask}
     * @param taskId Indicates which task is being processed.
     * @see AsyncObjectResponse#processAsyncResponse(String, Object, int)
     */
    @Override
    public void processAsyncResponse(String error, Object result, int taskId) {
        if (!error.isEmpty()) {
            if (taskId == SHIPMENT_EVENT_CREATE_TASK_ID) {
                insertTransactionToDB(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Error creating the shipment. You can view and retry the" +
                        " " +
                        "shipment in the shipment logs");
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener
                        () {
                    public void onClick(DialogInterface dialog, int id) {
                        completeShippingingCreation();
                    }
                });
                builder.create().show();
                return;
            } else if (taskId == PRODUCT_SEARCH_TASK_ID) {
                addOrIncrementSparseProduct(getTempSparseProduct());
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(error);
            builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        switch (taskId) {
            case SHIPMENT_EVENT_CREATE_TASK_ID:
                getAsyncProgressSpinner().showIndeterminateProgress(false);
                insertTransactionToDB(true);
                completeShippingingCreation();
                break;
            case PRODUCT_SEARCH_TASK_ID:
                if (((Product) result).getName() != null) {
                    SparseProduct sp = getTempSparseProduct();
                    sp.setName(((Product) result).getName());
                    addOrIncrementSparseProduct(sp);
                }
                break;
        }
    }


    /**
     * @return onclicklistener that initiates a scan using the Barcode Scanner application
     */
    private View.OnClickListener getStartScanListener(final View wrapper) {
        if (!datawedgeExists) wrapper.findViewById(R.id.scan_selector_wrapper).setVisibility(View
                .GONE);
        final SwitchCompat hardwareSwitch = (SwitchCompat) wrapper.findViewById(R.id.scan_device);
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hardware = !hardwareSwitch.isChecked();
                if (hardware && datawedgeExists) {
                    Intent intent = new Intent();
                    intent.setAction("com.motorolasolutions.emdk.datawedge.api" +
                            ".ACTION_SOFTSCANTRIGGER");
                    intent.putExtra("com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER",
                            "TOGGLE_SCANNING");
                    sendBroadcast(intent);
                } else {
                    IntentIntegrator scanIntegrator = new IntentIntegrator
                            (CreateShipmentActivity.this);
                    scanIntegrator.initiateScan();
                }
            }
        };
    }

    /**
     * Stores a temporary value for a product, but doesn't add the product to the shipment
     * list
     * yet.
     * This
     * allows a search on the server first, and if it fails, this product is inserted. However,
     * if one
     * is returned from the server, it overrides the details stored in {@link #tempSparseProduct}
     */

    private void storeSparseProduct(String globalTradeItemNumber, String lot, String packDate,
                                    String useThroughDate, String serialNumber, int quantity) {
        tempSparseProduct = new SparseProduct("", globalTradeItemNumber, lot, packDate,
                useThroughDate, serialNumber, quantity);
    }

    /**
     * Helper function to expand UPC/ITF-14/EAN13 codes to a 14 digit GTIN.
     *
     * @param contents string of variable length digits to be preceded by 0's until  the string
     *                 is 14 digits
     * @return 0-padded string of input contents
     */
    private String padStringTo14Characters(String contents) {
        String paddedGtin;
        int padLength = 14 - contents.length();
        StringBuffer paddingBuffer = new StringBuffer(padLength);
        for (int i = 0; i < padLength; i++) {
            paddingBuffer.append("0");
        }
        paddedGtin = paddingBuffer.toString().concat(contents);
        return paddedGtin;
    }

    /**
     * Adds the scanned information in as a sparse product if it doesn't already match and existing
     * gtin/lot combonation. The pack/useBy dates are checked as well if they are different, and we
     * default to the oldest/newest date respectively.
     * <p/>
     * In either case, the most recently created/updated sparseProduct is moved to the top of the
     * list.
     *
     * @return return true if the spareproduct existed already, false otherwise
     */
    private boolean addOrIncrementSparseProduct(String name, String globalTradeItemNumber, String
            lot, String packDate, String useByDate, String serialNumber, int quantity) {
        int existingIndex = -1;
        for (int i = 0; i < sparseProductListAdapter.getCount(); i++) {
            SparseProduct p = sparseProductListAdapter.getItem(i);
            if (p.getGlobalLocationTradeItemNumber().equals(globalTradeItemNumber) && p.getLot()
                    .equals(lot)) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex == -1) {
            sparseProductListAdapter.insert(new SparseProduct(name, globalTradeItemNumber, lot,
                    packDate, useByDate, serialNumber, quantity), 0);
            sparseProductListAdapter.notifyDataSetChanged();
            return false;
        } else {
            SparseProduct existingSparseProduct = sparseProductListAdapter.getItem(existingIndex);
            existingSparseProduct.incrementQuantity(quantity);
            existingSparseProduct.setPackDate(getOlderDateFromString(existingSparseProduct
                    .getPackDateAsGs1String(), packDate));
            existingSparseProduct.setUseByDate(getNewerDateFromString(existingSparseProduct
                    .getUseByDateAsGs1String(), useByDate));
            sparseProductListAdapter.remove(existingSparseProduct);
            sparseProductListAdapter.insert(existingSparseProduct, 0);
            sparseProductListAdapter.notifyDataSetChanged();
            return true;
        }
    }

    /**
     * Underloaded method. Defaults missing parameters to blank strings, except quantity, which
     * defaults to 1.
     *
     * @param globalTradeItemNumber 14-digit string for product gtin
     * @param lot                   Alphanumeric string for product lot
     * @see #addOrIncrementSparseProduct(String, String, String, String, String, String, int)
     */
    private void addOrIncrementSparseProduct(String globalTradeItemNumber, String lot) {
        addOrIncrementSparseProduct("", globalTradeItemNumber, lot, "", "", "", 1);
    }

    /**
     * Modified method. Pulls data from input product.
     *
     * @param sp Product to be added to the SparseProduct ArrayList.
     * @see #addOrIncrementSparseProduct(String, String, String, String, String, String, int)
     */
    private void addOrIncrementSparseProduct(SparseProduct sp) {
        addOrIncrementSparseProduct(
                sp.getName(),
                sp.getGlobalLocationTradeItemNumber(),
                sp.getLot(),
                sp.getPackDateAsGs1String(),
                sp.getUseByDateAsGs1String(),
                sp.getSerialNumber(),
                sp.getQuantityAmount()
        );
    }

    /**
     * Helper method to get the older of two date strings. If one of them is null, the assume the
     * other is oldest.
     *
     * @param dateString1 yyMMdd formatted date string
     * @param dateString2 yyMMdd formatted date string
     * @return Date object created from the string that yields an older date
     */
    private Date getOlderDateFromString(String dateString1, String dateString2) {
        if (dateString1.isEmpty() && dateString2.isEmpty()) {
            return null;
        } else {
            DateFormat format = new SimpleDateFormat("yyMMdd", Locale.ENGLISH);
            Date date1 = null;
            Date date2 = null;
            try {
                date1 = format.parse(dateString1);
            } catch (ParseException e) {/*just check for null later*/}
            try {
                date2 = format.parse(dateString2);
            } catch (ParseException e) {/*just check for null later*/}
            if (date1 == null && date2 == null)
                return null;
            else if (date1 == null || (date2 != null && date1.after(date2)))
                return date2;
            else if (date2 == null || (date2.after(date1)))
                return date1;
            else //This is assuming that they are equal.
                return date1;
        }
    }

    /**
     * Helper method to get the more recent of two date strings. If one of them is null, the
     * assume the other is more recent.
     *
     * @param dateString1 yyMMdd formatted date string
     * @param dateString2 yyMMdd formatted date string
     * @return Date object created from the string that yields a more recent date
     */
    private Date getNewerDateFromString(String dateString1, String dateString2) {
        if (dateString1.isEmpty() && dateString2.isEmpty()) {
            return null;
        } else {
            DateFormat format = new SimpleDateFormat("yyMMdd", Locale.ENGLISH);
            Date date1 = null;
            Date date2 = null;
            try {
                date1 = format.parse(dateString1);
            } catch (ParseException e) {/*just check for null later*/}
            try {
                date2 = format.parse(dateString2);
            } catch (ParseException e) {/*just check for null later*/}
            if (date1 == null && date2 == null)
                return null;
            else if (date1 == null || (date2 != null && date1.before(date2)))
                return date2;
            else if (date2 == null || (date2.before(date1)))
                return date1;
            else //This is assuming that they are equal.
                return date1;
        }
    }

    private void submitShipmentEvent() {
        ArrayList<SparseProduct> shipmentContents = new ArrayList<>();
        for (int i = 0; i < sparseProductListAdapter.getCount(); i++)
            shipmentContents.add(sparseProductListAdapter.getItem(i));
        shipment.setContents(shipmentContents);

        Location destinationLocation = ((Location) (
                (Spinner) findViewById(R.id.shipment_destination_supply_chain_location_spinner)
        ).getSelectedItem());
        shipment.setDestinationGln(
                destinationLocation.getGlobalLocationNumber()
        );

        String date = ((TextView) findViewById(R.id.shipment_date)).getText().toString();
        try {
            Date dateObject = DateFormatters.simpleFormat.parse(date);
            date = DateFormatters.isoDateFormatter.format(dateObject);
            shipment.setDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        eventType.updateCustomAttributeValues();
        shipment.setCustomAttributes(eventType.getCustomAttributes());
        shipment.setEventType((EventType) ((Spinner) findViewById(R.id
                .shipment_event_type_spinner)).getSelectedItem());

        new EventCreateAsyncTask(
                CreateShipmentActivity.this,
                customHelperHost,
                currentBusinessFoodlogiqId,
                "shipping",
                shipment,
                SHIPMENT_EVENT_CREATE_TASK_ID,
                this
        )
                .execute();
        getAsyncProgressSpinner().showIndeterminateProgress(true);
    }

    /**
     * @see Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        CreateShipmentActivity.this.registerReceiver(
                scanBroadcastReceiver,
                new IntentFilter("com.foodlogiq.mobile.scan"));
        intermecSetup();
    }

    /**
     * @see Activity#onStop()
     */
    @Override
    public void onStop() {
        try {
            CreateShipmentActivity.this.unregisterReceiver(scanBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            //no one cares... that's who
        }
        super.onStop();
    }

    /**
     * Add the reload datawedge button if datawedge exists on the device.
     *
     * @see Activity#onCreateOptionsMenu(Menu)
     * @see DataWedgeClickListener#DataWedgeClickListener(Activity)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logout_only, menu);
        if (datawedgeExists) {
            MenuItem dwReload = menu.add(getString(R.string.reload_datawedge));
            dwReload.setOnMenuItemClickListener(new DataWedgeClickListener
                    (CreateShipmentActivity
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
     * <p>
     * If requestCode is equal to {@link #MANUAL_PRODUCT_ENTRY} then search for the product on the
     * api, if gtin is populated
     * </p>
     * <p>
     * Otherwise, we assume it's the result of the barcode scanner application returning it's
     * results.
     * These results are parsed into the relevant data to populate the product information.
     * </p>
     *
     * @param requestCode code to indicate which activity the result comes from
     * @param resultCode  if the activity failed, then do nothing.
     * @param intent      contains either the data from the photo selection, or the bytes from a
     *                    barcode
     *                    scan.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MANUAL_PRODUCT_ENTRY:
                    String name = "";
                    if (intent.hasExtra("name"))
                        name = intent.getStringExtra("name");
                    String gtin = intent.getStringExtra("gtin");
                    String lot = intent.getStringExtra("lot");
                    String quantityString = intent.getStringExtra("quantity");
                    int quantity = 1;
                    if (!quantityString.isEmpty()) {
                        quantity = Integer.parseInt(quantityString);
                    }
                    String packDateString = intent.getStringExtra("packDate");
                    String useThroughString = intent.getStringExtra("useThroughDate");
                    addOrIncrementSparseProduct(new SparseProduct(name, gtin, lot, packDateString,
                            useThroughString, "", quantity));
                    return;
                default:
                    IntentResult scanningResult = IntentIntegrator.parseActivityResult
                            (requestCode, resultCode, intent);
                    if (scanningResult != null) {
                        byte[] scanContent = scanningResult.getRawBytes();
                        String scanFormat = scanningResult.getFormatName();
                        if (scanFormat == null) return;

                        AlertDialog.Builder builder = new AlertDialog.Builder
                                (CreateShipmentActivity.this);

                        switch (scanFormat) {
                            case "CODE_128":
                                if (scanContent == null) return;
                                BarcodeParser barcodeParser = new BarcodeParser(scanContent);
                                storeSparseProduct(
                                        barcodeParser.getGlobalTradeItemNumber(),
                                        barcodeParser.getLot(),
                                        barcodeParser.getPackDate(),
                                        barcodeParser.getUseThroughDate(),
                                        barcodeParser.getSerialNumber(),
                                        barcodeParser.getQuantity()
                                );
                                if (!barcodeParser.getGlobalTradeItemNumber().isEmpty()) {
                                    new ProductSearchAsyncTask(
                                            CreateShipmentActivity.this,
                                            customHelperHost,
                                            currentCommunityFoodlogiqId,
                                            barcodeParser.getGlobalTradeItemNumber(),
                                            PRODUCT_SEARCH_TASK_ID,
                                            CreateShipmentActivity.this)
                                            .execute();
                                }
                                return;
                            case "ITF":
                                addOrIncrementSparseProduct(
                                        scanningResult.getContents(),
                                        "ITF-14"
                                );
                                return;
                            //These all have the same functionality. Just pad the content with
                            // zeros on the left.
                            case "EAN_13":
                            case "UPC_A":
                            case "UPC_E":
                                String paddedGtin = padStringTo14Characters(scanningResult
                                        .getContents());
                                addOrIncrementSparseProduct(
                                        paddedGtin,
                                        "UPC"
                                );
                                return;

                            default:
                                builder.setMessage("You scanned a '" + scanFormat + "' which is " +
                                        "currently not supported");
                                break;
                        }

                        builder.setPositiveButton(android.R.string.ok, new DialogInterface
                                .OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No scan data received!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
            }
        }
    }

    /**
     * getter method for {@link #tempSparseProduct}
     */
    public SparseProduct getTempSparseProduct() {
        return tempSparseProduct;
    }


    /**
     * Adds api transaction to Database
     *
     * @param success true if the transaction was successful, false if not.
     */
    private void insertTransactionToDB(boolean success) {
        if (apiTransactionid != null) {
            ApiTransaction apiTransaction = new ApiTransaction("events", "shipments", ShipmentEvent
                    .class.getName(), new Date(), shipment.createJSON().toString(),
                    currentBusinessFoodlogiqId, currentLocation.getFoodlogiqId(), success,
                    apiTransactionid);
            apiTransaction.updateInDB(CreateShipmentActivity.this);
        } else {
            ApiTransaction apiTransaction = new ApiTransaction("events", "shipments", ShipmentEvent
                    .class.getName(), new Date(), shipment.createJSON().toString(),
                    currentBusinessFoodlogiqId, currentLocation.getFoodlogiqId(), success,
                    null);
            apiTransaction.saveToDB(CreateShipmentActivity.this);
        }
    }


    /**
     * Toast the user and finish the activity.
     */
    private void completeShippingingCreation() {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Shipping Event created successfully!", Toast.LENGTH_LONG);
        toast.show();
        finish();
    }

    @Override
    public void barcodeRead(final BarcodeReadEvent barcodeReadEvent) {
        //Only support gs1 128 barcodes for now.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parseIntermecBarcode(
                        barcodeReadEvent.getBarcodeData(),
                        barcodeReadEvent.getSymbologyName());
            }
        });
    }
}
