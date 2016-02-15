package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.asyncTasks.IncidentCreateAsyncTask;
import com.foodlogiq.distributormobile.asyncTasks.LogOutAsyncTask;
import com.foodlogiq.distributormobile.asyncTasks.ProductSearchAsyncTask;
import com.foodlogiq.distributormobile.asyncTasks.UploadIncidentPhotoAsyncTask;
import com.foodlogiq.distributormobile.commands.BackToLocationActions;
import com.foodlogiq.distributormobile.customViews.BackNextButtons;
import com.foodlogiq.distributormobile.customViews.DataWedgeClickListener;
import com.foodlogiq.distributormobile.customViews.DatePickerEditText;
import com.foodlogiq.distributormobile.customViews.ImageSelectorClickListener;
import com.foodlogiq.distributormobile.databases.commonTasks.FindApiTransaction;
import com.foodlogiq.distributormobile.entityClasses.ApiTransaction;
import com.foodlogiq.distributormobile.entityClasses.Incident;
import com.foodlogiq.distributormobile.entityClasses.Location;
import com.foodlogiq.distributormobile.entityClasses.Photo;
import com.foodlogiq.distributormobile.entityClasses.Product;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;
import com.foodlogiq.distributormobile.miscellaneousHelpers.RealPathUtil;
import com.foodlogiq.distributormobile.scanhelpers.BarcodeParser;
import com.foodlogiq.distributormobile.scanhelpers.DataWedgeParser;
import com.foodlogiq.distributormobile.viewAdapters.PhotoArrayAdapter;
import com.foodlogiq.flqassets.ConnectionChecker;
import com.foodlogiq.flqassets.CustomActionBar;
import com.foodlogiq.flqassets.FLQActivity;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;
import com.foodlogiq.flqassets.httpHandlers.AuthenticatedHttpServiceHandler;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.intermec.aidc.BarcodeReadEvent;
import com.intermec.aidc.BarcodeReadListener;
import com.intermec.aidc.BarcodeReader;
import com.intermec.aidc.BarcodeReaderException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * CreateIncidentActivity handles the creation of a quality report.
 * Implements AsyncObjectResponse to responses from server on incident creation, photo upload, and
 * photo taking/choosing.
 */
public class CreateIncidentActivity extends FLQActivity implements AsyncObjectResponse,
        BarcodeReadListener {
    private static final int PRODUCT_SEARCH_TASK_ID = 0;
    private static final int INCIDENT_CREATE_TASK_ID = 1;
    private static final int INCIDENT_UPLOAD_PHOTO_TASK_ID = 2;

    private static final int INCIDENT_SCAN_SLIDE = 0;
    private static final int INCIDENT_MANUAL_ENTRY = 1;
    private static final int INCIDENT_SOURCING_SLIDE = 2;
    private static final int INCIDENT_PROBLEM_SLIDE = 3;
    private static final int INCIDENT_PHOTOS_SLIDE = 4;
    private static final int INCIDENT_CONFIRM_SLIDE = 5;
    private static final int INCIDENT_REPORT_TASK_ID = 6;

    private boolean datawedgeExists;
    private String customHelperHost;
    private String currentBusinessFoodlogiqId;
    private String currentCommunityFoodlogiqId;
    private DatePickerEditText incidentUseThroughDatePicker;
    private DatePickerEditText incidentPackedDatePicker;
    private DatePickerEditText incidentInvoiceDatePicker;
    private ArrayList<Photo> incidentPhotos = new ArrayList<>();
    private PhotoArrayAdapter photoArrayAdapter;
    private ImageSelectorClickListener imageSelectClickListener;

    private Product foundProduct;
    private BackNextButtons backNextButton;
    private Incident incident;
    private String currentLocationFoodlogiqId;
    private Location currentLocation;
    private String apiTransactionid;
    private BroadcastReceiver scanBroadcastReceiver;

    private int mSlideIndex = 0;
    private ArrayList<View> slides;
    private ArrayList<String> distributionIncidentTypesAsStrings = new ArrayList<>();
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
        if (mSlideIndex > INCIDENT_CONFIRM_SLIDE) {
            submitIncidentReport();
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
        setSlideIndex(mSlideIndex + 1);
    }

    /**
     * Decrement {@link #mSlideIndex} and fire off handlers in {@link #setSlideIndex}.
     */
    public void decrementSlideIndex() {
        setSlideIndex(mSlideIndex - 1);
    }

    /**
     * Handles specific view updates that aren't handled in {@link #setSlideIndex(int)}
     * <pre>
     *     {@link #INCIDENT_SCAN_SLIDE}:
     *     Hide product partial, hide back button
     *     {@link #INCIDENT_MANUAL_ENTRY}:
     *     Hide Product partial
     *     {@link #INCIDENT_SOURCING_SLIDE}:
     *     If gtin has been populated, initiate a product search on the server to attempt and
     *     retrieve additional details.
     *     If lot is populated, then copy it to the product partial's corresponding field
     *     {@link #INCIDENT_PROBLEM_SLIDE}/{@link #INCIDENT_PHOTOS_SLIDE}:
     *     Hide product name.
     *     {@link #INCIDENT_CONFIRM_SLIDE}:
     *     Hide product partial, Fire off {@link #updateConfirmView()}, and change text of next
     *     button to submit</pre>
     *
     * @param mSlideIndex slide index to handle the view update for.
     */
    private void handleViewUpdates(int mSlideIndex) {
        findViewById(R.id.gtin_lot_wrapper).setBackground(getResources().getDrawable(R.color
                .white));
        findViewById(R.id.bottom_back_button).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.bottom_next_button_text)).setText(getString(R.string.next));
        String gtin = ((EditText) findViewById(R.id.incident_manual_gtin)).getText().toString();
        String lot = ((EditText) findViewById(R.id.incident_lot)).getText().toString();
        if (!gtin.isEmpty() || !lot.isEmpty()) {
            findViewById(R.id.product_partial).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.product_partial).setVisibility(View.GONE);
        }
        switch (mSlideIndex) {
            case INCIDENT_SCAN_SLIDE:
                findViewById(R.id.product_partial).setVisibility(View.GONE);
                findViewById(R.id.bottom_back_button).setVisibility(View.GONE);
                return;
            case INCIDENT_MANUAL_ENTRY:
                findViewById(R.id.product_partial).setVisibility(View.GONE);
                return;
            case INCIDENT_SOURCING_SLIDE:
                if (!gtin.isEmpty()) {
                    new ProductSearchAsyncTask(
                            CreateIncidentActivity.this,
                            customHelperHost,
                            currentCommunityFoodlogiqId,
                            gtin,
                            PRODUCT_SEARCH_TASK_ID,
                            CreateIncidentActivity.this)
                            .execute();
                    ((TextView) findViewById(R.id.product_header_gtin)).setText(gtin);
                }
                if (!lot.isEmpty())
                    ((TextView) findViewById(R.id.product_header_lot)).setText(lot);
                return;
            case INCIDENT_PROBLEM_SLIDE:
            case INCIDENT_PHOTOS_SLIDE:
                findViewById(R.id.gtin_lot_wrapper).setBackground(getResources().getDrawable(R
                        .color.lt_gray));
                findViewById(R.id.dialog_product_name).setVisibility(View.GONE);
                return;
            case INCIDENT_CONFIRM_SLIDE:
                findViewById(R.id.product_partial).setVisibility(View.GONE);
                updateConfirmView();
                ((TextView) findViewById(R.id.bottom_next_button_text)).setText(getString(R
                        .string.submit));
        }
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
        currentLocationFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_location_foodlogiq_id), "");
        currentLocation = Location.getCurrent(CreateIncidentActivity.this);
        customHelperHost = sharedPreferences.getString(getString(R.string.custom_helper_site),
                getString(R.string.default_helper_site_host));

        formTriggers();
        setSlideIndex(INCIDENT_SCAN_SLIDE);

        dataWedgeAndSonimSetup();
        intermecSetup();

        Intent i = getIntent();
        apiTransactionid = i.getStringExtra("apiTransactionId");
        if (apiTransactionid != null && !apiTransactionid.isEmpty()) {
            populateExistingIncident(apiTransactionid);
        }
        handleViewUpdates(mSlideIndex);
    }

    /**
     * @see FLQActivity#getLayoutId()
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_incident;
    }

    /**
     * @see FLQActivity#initializeActionBar()
     */
    @Override
    protected void initializeActionBar() {
        CustomActionBar customActionBar = new CustomActionBar(this, getString(R.string
                .create_quality_report), /* Settings enabled */true, false, true);
        customActionBar.setCustomSuperBackAction(new BackToLocationActions(CreateIncidentActivity
                .this));
        setENTER_ANIMATION(R.anim.slide_in_from_right);
        setENTER_ANIMATION_PREVIOUS(R.anim.slide_in_from_left);
        setEXIT_ANIMATION(R.anim.slide_out_to_left);
        setEXIT_ANIMATION_PREVIOUS(R.anim.slide_out_to_right);
        customActionBar.setUpListeners();
    }

    /**
     * Parses the json stored in the database in order to repopulate and trigger the correct
     * switches.
     * In essence, mirrors the form to look like it did when the quality report creation was
     * attempted the first time.
     *
     * @param apiTransactionid SQL id of the previous attempt of an Incident
     */
    private void populateExistingIncident(String apiTransactionid) {
        ApiTransaction apiTransaction = new FindApiTransaction(getContentResolver(),
                apiTransactionid).getApiTransaction();
        if (apiTransaction != null) {
            try {
                (incident = new Incident()).parseJSON(new JSONObject(apiTransaction.getJsonString
                        ()));
                ((EditText) findViewById(R.id.incident_found_by_name)).setText(incident
                        .getFoundByName());
                ((EditText) findViewById(R.id.incident_found_by_title)).setText(incident
                        .getFoundByTitle());
                if (incident.getUseByDate() != null)
                    ((TextView) findViewById(R.id.incident_use_thru_date)).setText(DateFormatters
                            .simpleFormat.format(incident.getUseByDate()));
                if (incident.getPackedDate() != null)
                    ((TextView) findViewById(R.id.incident_packed_date)).setText(DateFormatters
                            .simpleFormat.format(incident.getPackedDate()));
                if (incident.getCustomerComplaint()) {
                    ((SwitchCompat) findViewById(R.id.incident_customer_complaint_checkbox))
                            .setChecked(true);
                }
                if (incident.isPackagingExists()) {
                    ((SwitchCompat) findViewById(R.id.incident_packaging_checkbox)).setChecked
                            (true);
                }
                ((EditText) findViewById(R.id.incident_quantity_affected)).setText(String.valueOf
                        (incident.getQuantityAffected()));
                ((EditText) findViewById(R.id.incident_additional_description)).setText(incident
                        .getAdditionalDescription());
                if (incident.getSourceStore().isEmpty()) {
                    ((RadioButton) findViewById(R.id.product_source_supply_chain_supplier_radio))
                            .setChecked(true);
                    if (incident.getCreditRequest()) {
                        ((SwitchCompat) findViewById(R.id.incident_request_credit_checkbox))
                                .setChecked(true);
                    }
                } else {
                    ((RadioButton) findViewById(R.id.product_source_store_bought_radio))
                            .setChecked(true);
                    ((TextView) findViewById(R.id.incident_store)).setText(incident
                            .getSourceStore());
                }
                if (incident.getCreditRequest()) {
                    ((SwitchCompat) findViewById(R.id.incident_request_credit_checkbox))
                            .setChecked(true);
                    ((EditText) findViewById(R.id.incident_invoice_number)).setText(incident
                            .getInvoiceNumber());
                    if (incident.getInvoiceDate() != null)
                        ((TextView) findViewById(R.id.incident_invoice_date)).setText
                                (DateFormatters.simpleFormat.format(incident.getInvoiceDate()));
                    ArrayList<Location.SupplyChainLocation> distributors = currentLocation
                            .getSupplyChainLocations();
                    if (incident.getInvoiceDistributor() != null) {
                        for (int i = 0; i < distributors.size(); i++) {
                            if (distributors.get(i).getFoodlogiqId().equals(incident
                                    .getInvoiceDistributor().getFoodlogiqId())) {
                                ((Spinner) findViewById(R.id.incident_invoice_distributor))
                                        .setSelection(i);
                                break;
                            }
                        }
                    }
                }
                ((EditText) findViewById(R.id.incident_manual_gtin)).setText(incident.getProduct
                        ().getGlobalTradeItemNumber());
                ((EditText) findViewById(R.id.incident_lot)).setText(incident.getProduct().getLot
                        ());
                foundProduct = incident.getProduct();
                updateProductDetails(foundProduct);
                ArrayList<SpinnerOption> iTypes = getIncidentTypes();
                for (int i = 0; i < iTypes.size(); i++) {
                    if (iTypes.get(i).getValue().equals(incident.getType())) {
                        ((Spinner) findViewById(R.id.incident_type)).setSelection(i);
                        break;
                    }
                }

                if (apiTransaction.getPhotoPaths() != null && !apiTransaction.getPhotoPaths()
                        .isEmpty()) {
                    String[] photoPaths = apiTransaction.getPhotoPaths().split("\\|");
                    for (String pPath : photoPaths) {
                        Photo photo = new Photo(pPath);
                        incidentPhotos.add(photo);
                        photoArrayAdapter.notifyDataSetChanged();
                    }
                }
                setSlideIndex(INCIDENT_CONFIRM_SLIDE);
            } catch (JSONException e) {
                //TODO: HANDLE REPOPULATION ERROR
            }
        }
    }

    /**
     * <ul>
     * <li>Adds individual layouts to the slide handler.</li>
     * <li>Creates back/next button functionality for the bottom of the view.</li>
     * <li>Set up scan integrator to fire off when scan button is clicked.</li>
     * <li>Allows user to skip past scanning by clicking the manual button.</li>
     * <li>Sets up switch logic to hide/show relevant views.</li>
     * <li>Sets up date pickers for the form.</li>
     * <li>Sets up photo selector to allow camera capture/storage loaded images</li>
     * </ul>
     */
    private void formTriggers() {
        slides = new ArrayList<>();
        slides.add(findViewById(R.id.incident_scan_layout));
        slides.add(findViewById(R.id.incident_lot_layout));
        slides.add(findViewById(R.id.incident_source_info_layout));
        slides.add(findViewById(R.id.incident_problem_layout));
        slides.add(findViewById(R.id.incident_photo_layout));
        slides.add(findViewById(R.id.incident_confirm_layout));

        backNextButton = new BackNextButtons(CreateIncidentActivity.this, new View
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


        /*
        SCANNING TAB
         */
        Button scanButton = (Button) findViewById(R.id.incident_scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(CreateIncidentActivity.this);
                scanIntegrator.initiateScan();
            }
        });

        Button enterManuallyButton = (Button) findViewById(R.id.incident_manual_entry_button);
        enterManuallyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementSlideIndex();
            }
        });

        /*
        SOURCING INFO TAB
         */


        final RadioButton supplyChainLocation = (RadioButton) findViewById(R.id
                .product_source_supply_chain_supplier_radio);
        final RadioButton storeBought = (RadioButton) findViewById(R.id
                .product_source_store_bought_radio);
        supplyChainLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener
                () {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    storeBought.setChecked(false);
                    findViewById(R.id.product_source_store_bought_wrapper).setVisibility(View.GONE);
                    findViewById(R.id.incident_request_credit_wrapper).setVisibility(View.VISIBLE);

                }
            }
        });
        storeBought.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    supplyChainLocation.setChecked(false);
                    findViewById(R.id.product_source_store_bought_wrapper).setVisibility(View
                            .VISIBLE);
                    findViewById(R.id.incident_request_credit_wrapper).setVisibility(View.GONE);
                    ((SwitchCompat) findViewById(R.id.incident_request_credit_checkbox))
                            .setChecked(false);
                }
            }
        });
        supplyChainLocation.setChecked(true);


        TextView incidentUseThroughDateView = (TextView) findViewById(R.id.incident_use_thru_date);
        incidentUseThroughDatePicker = new DatePickerEditText(CreateIncidentActivity.this,
                incidentUseThroughDateView, false);

        TextView incidentPackedDateView = (TextView) findViewById(R.id.incident_packed_date);
        incidentPackedDatePicker = new DatePickerEditText(CreateIncidentActivity.this,
                incidentPackedDateView, false);

        SwitchCompat requestCreditCheckbox = (SwitchCompat) findViewById(R.id
                .incident_request_credit_checkbox);
        requestCreditCheckbox.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener
                () {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.incident_invoice_wrapper).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.incident_invoice_wrapper).setVisibility(View.GONE);
                }
            }
        });

        TextView incidentInvoiceDate = (TextView) findViewById(R.id.incident_invoice_date);
        incidentInvoiceDatePicker = new DatePickerEditText(CreateIncidentActivity.this,
                incidentInvoiceDate, false);
        Spinner distributorSpinner = (Spinner) findViewById(R.id.incident_invoice_distributor);
        ArrayAdapter<Location.SupplyChainLocation> supplyChainLocationsAdapter = new
                ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currentLocation
                .getSupplyChainLocations());
        supplyChainLocationsAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        distributorSpinner.setAdapter(supplyChainLocationsAdapter);


        /*
        NEXT TAB
         */
        Spinner incidentType = (Spinner) findViewById(R.id.incident_type);
        ArrayAdapter<SpinnerOption> incidentTypeAdapter = new ArrayAdapter<>(this, android.R
                .layout.simple_spinner_item, getIncidentTypes());
        incidentTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incidentType.setAdapter(incidentTypeAdapter);

        Button takePictureButton = (Button) findViewById(R.id.incident_take_picture);
        imageSelectClickListener = new ImageSelectorClickListener(CreateIncidentActivity.this);
        takePictureButton.setOnClickListener(imageSelectClickListener);

        GridView photoGrid = (GridView) findViewById(R.id.incident_pictures);
        photoArrayAdapter = new PhotoArrayAdapter(this, incidentPhotos, photoGrid);
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
    private void dataWedgeAndSonimSetup() {
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
        CreateIncidentActivity.this.registerReceiver(scanBroadcastReceiver, new IntentFilter
                ("com.foodlogiq.mobile.scan"));
        CreateIncidentActivity.this.
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
        String scanFormat = extras.getString("com.motorolasolutions.emdk.datawedge" +
                ".label_type");
        if (scanFormat == null) return;

        byte[] bytes = (byte[]) ((List<?>) scanContent).get(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateIncidentActivity
                .this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        switch (scanFormat) {
            case "LABEL-TYPE-EAN128":
                DataWedgeParser dataWedgeParser = new DataWedgeParser(bytes);
                String globalTradeItemNumber = dataWedgeParser.getGlobalTradeItemNumber();
                EditText gtinView = (EditText) findViewById(R.id.incident_manual_gtin);
                gtinView.setText(globalTradeItemNumber);
                if (currentCommunityFoodlogiqId.isEmpty()) {
                    //Can't search without this, so don't bother
                    incrementSlideIndex();
                } else if (!globalTradeItemNumber.isEmpty()) {

                } else {
                    builder.setMessage("No GTIN found in that GS1-128 Barcode");
                }
                String lot = dataWedgeParser.getLot();
                EditText lotView = (EditText) findViewById(R.id.incident_lot);
                lotView.setText(lot);
                int quantity = dataWedgeParser.getQuantity();
                EditText quantityView = (EditText) findViewById(R.id
                        .incident_quantity_affected);
                if (quantity != 0)
                    quantityView.setText(String.valueOf(quantity));
                try {
                    String useThroughDateString = dataWedgeParser.getUseThroughDate();
                    if (!useThroughDateString.isEmpty()) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                        Date useThroughDate = simpleDateFormat.parse(useThroughDateString);
                        incidentUseThroughDatePicker.setDateFromDateObject(useThroughDate);
                    }
                } catch (ParseException e) {
                    //Sorry bud, your date don't work.
                    e.printStackTrace();
                }
                try {
                    String packedDateString = dataWedgeParser.getPackDate();
                    if (!packedDateString.isEmpty()) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                        Date packedDate = simpleDateFormat.parse(packedDateString);
                        incidentPackedDatePicker.setDateFromDateObject(packedDate);
                    }
                } catch (ParseException e) {
                    //Sorry bud, your date don't work.
                    e.printStackTrace();
                }
                if (mSlideIndex == INCIDENT_SCAN_SLIDE)
                    incrementSlideIndex();
                return;
            default:
                builder.setMessage("You scanned a '" + scanFormat + "' which is currently" +
                        " not supported");
                break;
        }

        builder.create().
                show();
    }

    private void parseSonimBarcode(Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateIncidentActivity
                .this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        Bundle extras = intent.getExtras();
        String scanContent = extras.getString("data");
        DataWedgeParser dataWedgeParser = new DataWedgeParser(scanContent.getBytes());
        String globalTradeItemNumber = dataWedgeParser.getGlobalTradeItemNumber();
        EditText gtinView = (EditText) findViewById(R.id.incident_manual_gtin);
        gtinView.setText(globalTradeItemNumber);
        if (currentCommunityFoodlogiqId.isEmpty()) {
            //Can't search without this, so don't bother
            incrementSlideIndex();
        } else if (!globalTradeItemNumber.isEmpty()) {

        } else {
            builder.setMessage("No GTIN found in that GS1-128 Barcode");
        }
        String lot = dataWedgeParser.getLot();
        EditText lotView = (EditText) findViewById(R.id.incident_lot);
        lotView.setText(lot);
        int quantity = dataWedgeParser.getQuantity();
        EditText quantityView = (EditText) findViewById(R.id
                .incident_quantity_affected);
        if (quantity != 0)
            quantityView.setText(String.valueOf(quantity));
        try {
            String useThroughDateString = dataWedgeParser.getUseThroughDate();
            if (!useThroughDateString.isEmpty()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                Date useThroughDate = simpleDateFormat.parse(useThroughDateString);
                incidentUseThroughDatePicker.setDateFromDateObject(useThroughDate);
            }
        } catch (ParseException e) {
            //Sorry bud, your date don't work.
            e.printStackTrace();
        }
        try {
            String packedDateString = dataWedgeParser.getPackDate();
            if (!packedDateString.isEmpty()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                Date packedDate = simpleDateFormat.parse(packedDateString);
                incidentPackedDatePicker.setDateFromDateObject(packedDate);
            }
        } catch (ParseException e) {
            //Sorry bud, your date don't work.
            e.printStackTrace();
        }
        if (mSlideIndex == INCIDENT_SCAN_SLIDE)
            incrementSlideIndex();
        return;
    }

    private void parseIntermecBarcode(String scanContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateIncidentActivity
                .this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //Intermec has three bytes at the front that we don't need.
        byte[] slicedContent = Arrays.copyOfRange(scanContent.getBytes(), 3, scanContent.length());
        DataWedgeParser dataWedgeParser = new DataWedgeParser(slicedContent);
        String globalTradeItemNumber = dataWedgeParser.getGlobalTradeItemNumber();
        EditText gtinView = (EditText) findViewById(R.id.incident_manual_gtin);
        gtinView.setText(globalTradeItemNumber);
        if (currentCommunityFoodlogiqId.isEmpty()) {
            //Can't search without this, so don't bother
            incrementSlideIndex();
        } else if (!globalTradeItemNumber.isEmpty()) {

        } else {
            builder.setMessage("No GTIN found in that GS1-128 Barcode");
        }
        String lot = dataWedgeParser.getLot();
        EditText lotView = (EditText) findViewById(R.id.incident_lot);
        lotView.setText(lot);
        int quantity = dataWedgeParser.getQuantity();
        EditText quantityView = (EditText) findViewById(R.id
                .incident_quantity_affected);
        if (quantity != 0)
            quantityView.setText(String.valueOf(quantity));
        try {
            String useThroughDateString = dataWedgeParser.getUseThroughDate();
            if (!useThroughDateString.isEmpty()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                Date useThroughDate = simpleDateFormat.parse(useThroughDateString);
                incidentUseThroughDatePicker.setDateFromDateObject(useThroughDate);
            }
        } catch (ParseException e) {
            //Sorry bud, your date don't work.
            e.printStackTrace();
        }
        try {
            String packedDateString = dataWedgeParser.getPackDate();
            if (!packedDateString.isEmpty()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                Date packedDate = simpleDateFormat.parse(packedDateString);
                incidentPackedDatePicker.setDateFromDateObject(packedDate);
            }
        } catch (ParseException e) {
            //Sorry bud, your date don't work.
            e.printStackTrace();
        }
        if (mSlideIndex == INCIDENT_SCAN_SLIDE)
            incrementSlideIndex();
        return;
    }

    /**
     * <p>If successful:</p>
     * <ul>
     * <li>{@link #PRODUCT_SEARCH_TASK_ID}: Updates contents of incident based on product
     * returned from server.</li>
     * <li>{@link #INCIDENT_CREATE_TASK_ID}: Checks for photos on incident, and uploads them.
     * Otherwise, insert successful incident json into db.</li>
     * <li>{@link #INCIDENT_UPLOAD_PHOTO_TASK_ID}: Updates progress spinner until it reaches 100,
     * then inserts the completed incident json into db.</li>
     * </ul>
     *
     * @param error  error response from server.
     * @param result Either a product from {@link ProductSearchAsyncTask}
     *               or a foodlogiq Id from
     *               {@link IncidentCreateAsyncTask} or {@link UploadIncidentPhotoAsyncTask}
     * @param taskId Indicates which task is being processed.
     * @see AsyncObjectResponse#processAsyncResponse(String, Object, int)
     */
    @Override
    public void processAsyncResponse(String error, Object result, int taskId) {
        if (!error.isEmpty()) {
            if (taskId == INCIDENT_CREATE_TASK_ID) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Error creating the incident. You can view and retry the " +
                        "incident in the incident logs");
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener
                        () {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.create().show();
                completeIncidentCreation(false, null);
                return;
            } else if (taskId == PRODUCT_SEARCH_TASK_ID) {
                //We don't care about the missing connection to the server.
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
        String newFoodlogiqId;
        switch (taskId) {
            case PRODUCT_SEARCH_TASK_ID:
                this.foundProduct = (Product) result;
                if (((EditText) findViewById(R.id.incident_lot)).getText() != null && !(
                        (EditText) findViewById(R.id.incident_lot)).getText().toString().isEmpty())
                    foundProduct.setLot(((EditText) findViewById(R.id.incident_lot)).getText()
                            .toString());
                updateProductDetails(foundProduct);
                break;
            case INCIDENT_CREATE_TASK_ID:
                newFoodlogiqId = (String) result;
                getAsyncProgressSpinner().showIndeterminateProgress(false);
                if (incidentPhotos.isEmpty()) {
                    completeIncidentCreation(true, newFoodlogiqId);
                } else {
                    getAsyncProgressSpinner().showDeterminateProgress(true);
                    getAsyncProgressSpinner().setDeterminateProgressIntervals(incidentPhotos.size
                            ());
                    uploadIncidentPhotos(newFoodlogiqId);
                }
                break;
            case INCIDENT_UPLOAD_PHOTO_TASK_ID:
                newFoodlogiqId = (String) result;
                getAsyncProgressSpinner().incrementDeterminateInterval();
                if (getAsyncProgressSpinner().getDeterminateProgress() >= 100) {
                    completeIncidentCreation(true, newFoodlogiqId);
                }
                break;
        }
    }

    /**
     * Adds api transaction to Database
     *
     * @param success true if the transaction was successful, false if not.
     * @return sql id from db insertion.
     */
    private ApiTransaction insertTransactionToDB(boolean success) {
        if (apiTransactionid != null) {
            ApiTransaction apiTransaction = new ApiTransaction("incidents", "", Incident.class
                    .getName(), new Date(), incident.createJSON().toString(),
                    currentBusinessFoodlogiqId, currentLocationFoodlogiqId, success,
                    apiTransactionid);
            apiTransaction.updateInDB(CreateIncidentActivity.this);
            return apiTransaction;
        } else {
            ApiTransaction apiTransaction = new ApiTransaction("incidents", "", Incident.class
                    .getName(), new Date(), incident.createJSON().toString(),
                    currentBusinessFoodlogiqId, currentLocationFoodlogiqId, success, null);
            apiTransaction.saveToDB(CreateIncidentActivity.this);
            return apiTransaction;
        }
    }

    /**
     * If the incident has a credit request, ping the server to set off an emailed request to the
     * product owner.
     * If there are photos on the incident, then add their paths on the device to the
     * apiTransaction.
     * Insert the api transaction to the DB.
     *
     * @param success        true if the transaction was successful, false if not.
     * @param newFoodlogiqId used to ping the relevant incident for email notificationiterm.
     */
    private void completeIncidentCreation(boolean success, String newFoodlogiqId) {
        if (success) {
            if (incident.getCreditRequest())
                new FireOffIncidentEmail(CreateIncidentActivity.this,
                        customHelperHost,
                        currentBusinessFoodlogiqId,
                        newFoodlogiqId,
                        INCIDENT_REPORT_TASK_ID,
                        CreateIncidentActivity.this).execute();
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Incident created successfully!", Toast.LENGTH_LONG);
            toast.show();
        }
        ApiTransaction insertedApiTransaction = insertTransactionToDB(success);
        if (!incidentPhotos.isEmpty()) {
            insertedApiTransaction.addPhotoPaths(CreateIncidentActivity.this, incidentPhotos);
        } else {
            insertedApiTransaction.removePhotoPaths(CreateIncidentActivity.this);
        }
        finish();
    }

    /**
     * Iterate through all photos in the incident. Fire off the upload tasks.
     *
     * @param newFoodlogiqId API ID of the incident photos should be linked to.
     */
    private void uploadIncidentPhotos(String newFoodlogiqId) {
        for (Photo incidentPhoto : incidentPhotos) {
            UploadIncidentPhotoAsyncTask uploadIncidentPhotoAsyncTask = new
                    UploadIncidentPhotoAsyncTask(
                    CreateIncidentActivity.this,
                    customHelperHost,
                    currentBusinessFoodlogiqId,
                    newFoodlogiqId,
                    incidentPhoto,
                    INCIDENT_UPLOAD_PHOTO_TASK_ID,
                    CreateIncidentActivity.this
            );
            uploadIncidentPhotoAsyncTask.execute();
        }
    }

    /**
     * Updates the visible product details in the product header, based on the values from a
     * pulled in
     * product, or from the manually entered gtin/lots.
     *
     * @param product Can be a product with just a gtin/lot. If it is, then don't show the name
     *                view.
     *                Otherwise, show it.
     */
    private void updateProductDetails(Product product) {
        View productPartial = findViewById(R.id.product_partial);
        if (product.getName().isEmpty()) {
            findViewById(R.id.dialog_product_name).setVisibility(View.GONE);
            ((TextView) productPartial.findViewById(R.id.product_header_gtin)).setText(product
                    .getGlobalTradeItemNumber());
            ((TextView) productPartial.findViewById(R.id.product_header_lot)).setText(product
                    .getLot());
        } else {
            ((TextView) productPartial.findViewById(R.id.dialog_product_name)).setText(product
                    .getName());
            ((TextView) productPartial.findViewById(R.id.product_header_gtin)).setText(product
                    .getGlobalTradeItemNumber());
            ((TextView) productPartial.findViewById(R.id.product_header_lot)).setText(product
                    .getLot());
            findViewById(R.id.dialog_product_name).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Pulls data from form and populates a summary of the data for the confirm page. This is
     * where everything
     * on the confirm page is set.
     */
    private void updateConfirmView() {
        TextView confirmProductName = (TextView) findViewById(R.id.confirm_product_name);
        if (getFoundProduct() != null && !getFoundProduct().getName().isEmpty()) {
            confirmProductName.setText(getFoundProduct().getName());
        } else {
            confirmProductName.setText("N/A");
        }

        TextView confirmGtin = (TextView) findViewById(R.id.confirm_product_gtin);
        String gtin = ((EditText) findViewById(R.id.incident_manual_gtin)).getText().toString();
        if (!gtin.isEmpty()) {
            confirmGtin.setText(gtin);
        } else {
            confirmGtin.setText("N/A");
        }

        TextView confirmProblemType = (TextView) findViewById(R.id.confirm_incident_type);
        String incidentType = ((SpinnerOption) ((Spinner) findViewById(R.id.incident_type))
                .getSelectedItem()).getValue().toString();
        confirmProblemType.setText(incidentType);
        if (distributionIncidentTypesAsStrings.indexOf(incidentType) == -1) {
            ((TextView) findViewById(R.id.confirm_incident_distribution_issue)).setText(getString
                    (android.R.string.no));
        } else {
            ((TextView) findViewById(R.id.confirm_incident_distribution_issue)).setText(getString
                    (android.R.string.yes));
        }

        TextView confirmFoundBy = (TextView) findViewById(R.id.confirm_incident_found_by_name);
        confirmFoundBy.setText(((EditText) findViewById(R.id.incident_found_by_name)).getText());

        TextView confirmFoundByTitle = (TextView) findViewById(R.id
                .confirm_incident_found_by_title);
        confirmFoundByTitle.setText(((EditText) findViewById(R.id.incident_found_by_title))
                .getText());

        TextView confirmAdditionalDetails = (TextView) findViewById(R.id
                .confirm_incident_additional_description);
        confirmAdditionalDetails.setText(((EditText) findViewById(R.id
                .incident_additional_description)).getText());

        TextView confirmUseThroughDate = (TextView) findViewById(R.id.confirm_product_use_by_date);
        confirmUseThroughDate.setText(
                ((TextView) findViewById(R.id.incident_use_thru_date)).getText()
        );

        TextView confirmPackedDate = (TextView) findViewById(R.id.confirm_product_pack_date);
        confirmPackedDate.setText(
                ((TextView) findViewById(R.id.incident_packed_date)).getText()
        );
        TextView confirmLot = (TextView) findViewById(R.id.confirm_product_lot);
        confirmLot.setText(
                ((EditText) findViewById(R.id.incident_lot)).getText()
        );
        TextView confirmCustomerComplaint = (TextView) findViewById(R.id
                .confirm_incident_customer_complaint);
        if (((SwitchCompat) findViewById(R.id.incident_customer_complaint_checkbox)).isChecked()) {
            confirmCustomerComplaint.setText(getString(android.R.string.yes));
        } else {
            confirmCustomerComplaint.setText(getString(android.R.string.no));
        }
        TextView confirmCreditRequest = (TextView) findViewById(R.id
                .confirm_incident_credit_request);
        if (((SwitchCompat) findViewById(R.id.incident_request_credit_checkbox)).isChecked()) {
            confirmCreditRequest.setText(getString(android.R.string.yes));
        } else {
            confirmCreditRequest.setText(getString(android.R.string.no));
        }
        TextView confirmQuantity = (TextView) findViewById(R.id.confirm_product_quantity);
        confirmQuantity.setText(
                ((EditText) findViewById(R.id.incident_quantity_affected)).getText().toString()
        );
        GridView confirmPhotos = (GridView) findViewById(R.id.confirm_incident_photo_grid);
        confirmPhotos.setAdapter(photoArrayAdapter);
        TextView photoLabel = (TextView) findViewById(R.id.confirm_incident_photo_label);
        photoLabel.setText(getString(R.string.attached_photos).concat(" (" + photoArrayAdapter
                .getCount() + ")"));
    }

    /**
     * Creates an incident based on the form values, and attempts to submit that incident to the
     * api.
     */
    private void submitIncidentReport() {
        Location location = Location.getCurrent(this);
        Product product;
        if (getFoundProduct() == null) {
            product = new Product(
                    ((EditText) findViewById(R.id.incident_manual_gtin)).getText().toString()
            );
        } else {
            product = getFoundProduct();
        }
        String type = ((SpinnerOption) ((Spinner) findViewById(R.id.incident_type))
                .getSelectedItem()).getValue().toString();
        Boolean distributionIssue = distributionIncidentTypesAsStrings.indexOf(type) != -1;
        product.setLot(((EditText) findViewById(R.id.incident_lot)).getText().toString());
        String foundByName = ((EditText) findViewById(R.id.incident_found_by_name)).getText()
                .toString();
        String foundByTitle = ((EditText) findViewById(R.id.incident_found_by_title)).getText()
                .toString();
        String sourceType = "";
        boolean creditRequested = false;
        if (((RadioButton) findViewById(R.id.product_source_supply_chain_supplier_radio))
                .isChecked()) {
            sourceType = "supplier";
            creditRequested = ((SwitchCompat) findViewById(R.id.incident_request_credit_checkbox)
            ).isChecked();
        } else if (((RadioButton) findViewById(R.id.product_source_store_bought_radio)).isChecked
                ()) {
            sourceType = "store";
            //No credit request unless they have selected a supplyChainLocation
        }
        String useThroughDateString = ((TextView) findViewById(R.id.incident_use_thru_date))
                .getText().toString();
        String useThroughDate = "";
        String packedDateString = ((TextView) findViewById(R.id.incident_packed_date)).getText()
                .toString();
        String packedDate = "";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date useThroughDateObject = simpleDateFormat.parse(useThroughDateString);
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat isoDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" +
                    ".SSS'Z'");
            isoDateFormatter.setTimeZone(tz);
            useThroughDate = isoDateFormatter.format(useThroughDateObject);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date packedDateObject = simpleDateFormat.parse(packedDateString);
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat isoDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" +
                    ".SSS'Z'");
            isoDateFormatter.setTimeZone(tz);
            packedDate = isoDateFormatter.format(packedDateObject);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        boolean customerComplaint = ((SwitchCompat) findViewById(R.id
                .incident_customer_complaint_checkbox)).isChecked();
        String quantityAffectedString = ((EditText) findViewById(R.id.incident_quantity_affected)
        ).getText().toString();
        int quantityAffected = new Integer(!quantityAffectedString.isEmpty() ?
                quantityAffectedString : "0");
        String additionalDescription = ((EditText) findViewById(R.id
                .incident_additional_description)).getText().toString();
        String invoiceNumber = "";
        String invoiceDate = "";
        Location.SupplyChainLocation invoiceDistributor = null;
        if (((SwitchCompat) findViewById(R.id.incident_request_credit_checkbox)).isChecked()) {
            invoiceNumber = ((EditText) findViewById(R.id.incident_invoice_number)).getText()
                    .toString();
            String invoiceDateString = ((TextView) findViewById(R.id.incident_invoice_date))
                    .getText().toString();
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date invoiceDateObject = simpleDateFormat.parse(invoiceDateString);
                TimeZone tz = TimeZone.getTimeZone("UTC");
                SimpleDateFormat isoDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" +
                        ".SSS'Z'");
                isoDateFormatter.setTimeZone(tz);
                invoiceDate = isoDateFormatter.format(invoiceDateObject);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            invoiceDistributor = (Location.SupplyChainLocation) ((Spinner) findViewById(R.id
                    .incident_invoice_distributor)).getSelectedItem();
        }
        boolean packagingExists = ((SwitchCompat) findViewById(R.id.incident_packaging_checkbox))
                .isChecked();
        incident = new Incident(
                currentCommunityFoodlogiqId,
                location,
                product,
                type,
                distributionIssue,
                foundByName,
                foundByTitle,
                useThroughDate,
                packedDate,
                customerComplaint,
                quantityAffected,
                additionalDescription,
                creditRequested,
                invoiceNumber,
                invoiceDate,
                invoiceDistributor,
                packagingExists,
                sourceType,
                incidentPhotos
        );
        if (((RadioButton) findViewById(R.id.product_source_store_bought_radio)).isChecked()) {
            incident.setSourceStore(((EditText) findViewById(R.id.incident_store)).getText()
                    .toString());
        }
        new IncidentCreateAsyncTask(
                CreateIncidentActivity.this,
                customHelperHost,
                currentBusinessFoodlogiqId,
                incident,
                INCIDENT_CREATE_TASK_ID,
                this
        )
                .execute();
        getAsyncProgressSpinner().showIndeterminateProgress(true);
    }

    /**
     * Gets found product.
     *
     * @return the found product
     */
    public Product getFoundProduct() {
        return foundProduct;
    }

    /**
     * Gets incident types.
     * Also assigns values to a special ArrayList for distributor incident types.
     * {@link #distributionIncidentTypesAsStrings} stores the values to check if the selected
     * value matches one of them.
     *
     * @return the incident types options to use in incident Type spinner.
     */
    public ArrayList<SpinnerOption> getIncidentTypes() {
        ArrayList<SpinnerOption> incidentTypes = new ArrayList<>();
        incidentTypes.add(new SpinnerOption("", getString(R.string.problem_type)));
        incidentTypes.add(new SpinnerOption("Foreign Object", getString(R.string.foreign_object)));
        incidentTypes.add(new SpinnerOption("Off Color", getString(R.string.off_color)));
        incidentTypes.add(new SpinnerOption("Off Flavor", getString(R.string.off_flavor)));
        incidentTypes.add(new SpinnerOption("Off Odor", getString(R.string.off_odor)));
        incidentTypes.add(new SpinnerOption("Mold", getString(R.string.mold)));
        incidentTypes.add(new SpinnerOption("Packaging Issue", getString(R.string
                .packaging_issue)));
        incidentTypes.add(new SpinnerOption("Piece Size", getString(R.string.piece_size)));
        incidentTypes.add(new SpinnerOption("Short Shelf-Life Delivery", getString(R.string
                .short_shelf_life_delivery)));
        incidentTypes.add(new SpinnerOption("Bad Label", getString(R.string.bad_label)));

        ArrayList<SpinnerOption> distributionIncidentTypes = new ArrayList<>();
        distributionIncidentTypes.add(new SpinnerOption("Short Delivery", getString(R.string
                .short_delivery)));
        distributionIncidentTypes.add(new SpinnerOption("Late Delivery", getString(R.string
                .late_delivery)));
        distributionIncidentTypes.add(new SpinnerOption("Missing Delivery", getString(R.string
                .missing_delivery)));
        distributionIncidentTypes.add(new SpinnerOption("Inaccurate Delivery", getString(R.string
                .inaccurate_delivery)));
        distributionIncidentTypes.add(new SpinnerOption("Over Delivered", getString(R.string
                .over_delivered)));
        distributionIncidentTypes.add(new SpinnerOption("Damaged Delivery", getString(R.string
                .damaged_delivery)));

        distributionIncidentTypesAsStrings = new ArrayList<>();
        for (SpinnerOption dit : distributionIncidentTypes) {
            //TODO: Check if this still works when the phone is set to spanish.
            distributionIncidentTypesAsStrings.add(dit.label);
        }

        incidentTypes.addAll(distributionIncidentTypes);

        incidentTypes.add(new SpinnerOption("Other", getString(R.string.other)));
        return incidentTypes;
    }

    /**
     * @see Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        CreateIncidentActivity.this.registerReceiver(scanBroadcastReceiver, new IntentFilter("com" +
                ".foodlogiq.mobile.scan"));
    }

    /**
     * @see Activity#onStop()
     */
    @Override
    public void onStop() {
        try {
            CreateIncidentActivity.this.unregisterReceiver(scanBroadcastReceiver);
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
            dwReload.setOnMenuItemClickListener(new DataWedgeClickListener(CreateIncidentActivity
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
     * If requestCode is equal to {@link ImageSelectorClickListener#REQUEST_SELECT_PICTURE}
     * or {@link ImageSelectorClickListener#REQUEST_TAKE_PHOTO} then the photo is added to the
     * {@link #incidentPhotos} array to be submitted with the incident.
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
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == ImageSelectorClickListener.REQUEST_TAKE_PHOTO || requestCode ==
                ImageSelectorClickListener.REQUEST_SELECT_PICTURE) {
            if (requestCode == ImageSelectorClickListener.REQUEST_SELECT_PICTURE) {
                Uri selectedImageUri = intent.getData();
                String realPath;
                if (Build.VERSION.SDK_INT < 11) {
                    realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, selectedImageUri);
                } else if (Build.VERSION.SDK_INT < 19) {
                    realPath = RealPathUtil.getRealPathFromURI_API11to18(this, selectedImageUri);
                } else {
                    realPath = RealPathUtil.getRealPathFromURI_API19(this, selectedImageUri);
                }
                Photo photo = new Photo(realPath);
                incidentPhotos.add(photo);
            } else {
                Photo photo = new Photo(imageSelectClickListener.getCurrentPhotoPath());
                incidentPhotos.add(photo);
            }
            photoArrayAdapter.notifyDataSetChanged();
        }
        // We don't set the requestCode for the zxing scan, so assume it's the one we don't catch
        else {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode,
                    resultCode, intent);
            if (scanningResult != null) {
                byte[] scanContent = scanningResult.getRawBytes();
                String scanFormat = scanningResult.getFormatName();
                if (scanContent == null || scanFormat == null) return;

                AlertDialog.Builder builder = new AlertDialog.Builder(CreateIncidentActivity.this);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                switch (scanFormat) {
                    case "CODE_128":
                        BarcodeParser barcodeParser = new BarcodeParser(scanContent);
                        String globalTradeItemNumber = barcodeParser.getGlobalTradeItemNumber();
                        EditText gtinView = (EditText) findViewById(R.id.incident_manual_gtin);
                        gtinView.setText(globalTradeItemNumber);
                        if (currentCommunityFoodlogiqId.isEmpty()) {
                            //Can't search without this, so don't bother
                            incrementSlideIndex();
                        } else if (!globalTradeItemNumber.isEmpty()) {

                        } else {
                            builder.setMessage("No GTIN found in that GS1-128 Barcode");
                        }
                        String lot = barcodeParser.getLot();
                        EditText lotView = (EditText) findViewById(R.id.incident_lot);
                        lotView.setText(lot);
                        int quantity = barcodeParser.getQuantity();
                        EditText quantityView = (EditText) findViewById(R.id
                                .incident_quantity_affected);
                        if (quantity != 0)
                            quantityView.setText(String.valueOf(quantity));
                        try {
                            String useThroughDateString = barcodeParser.getUseThroughDate();
                            if (!useThroughDateString.isEmpty()) {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                                Date useThroughDate = simpleDateFormat.parse(useThroughDateString);
                                incidentUseThroughDatePicker.setDateFromDateObject(useThroughDate);
                            }
                        } catch (ParseException e) {
                            //Sorry bud, your date don't work.
                            e.printStackTrace();
                        }
                        try {
                            String packedDateString = barcodeParser.getPackDate();
                            if (!packedDateString.isEmpty()) {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
                                Date packedDate = simpleDateFormat.parse(packedDateString);
                                incidentPackedDatePicker.setDateFromDateObject(packedDate);
                            }
                        } catch (ParseException e) {
                            //Sorry bud, your date don't work.
                            e.printStackTrace();
                        }
                        if (mSlideIndex == INCIDENT_SCAN_SLIDE)
                            incrementSlideIndex();
                        return;
                    default:
                        builder.setMessage("You scanned a '" + scanFormat + "' which is currently" +
                                " not supported");
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

    @Override
    public void barcodeRead(final BarcodeReadEvent barcodeReadEvent) {
        //update UI list
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                parseIntermecBarcode(barcodeReadEvent.getBarcodeData());
            }
        });
    }

    /**
     * Simple class to add a label to an object value.
     */
    private class SpinnerOption {
        private final Object value;
        private final String label;

        /**
         * @param value Value that is returned for the spinner selection.
         * @param label String that is shown to symbolize the spinner selection.
         */
        public SpinnerOption(Object value, String label) {
            this.value = value;
            this.label = label;
        }

        /**
         * Gives the intended label for the spinner to display
         *
         * @see String#toString()
         */
        @Override
        public String toString() {
            return this.label;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public Object getValue() {
            return value;
        }
    }

    /**
     * Sends ping to api server to fire off a credit request email.
     */
    private class FireOffIncidentEmail extends AsyncTask<Void, Void, Object> {
        private final Activity activity;
        private final ConnectionChecker connectionChecker;
        private final String host;
        private final String path;
        private final int taskId;
        private final AsyncObjectResponse delegate;
        private String error = "";

        /**
         * Instantiates a new Fire off incident email.
         *
         * @param activity   the activity
         * @param host       the host
         * @param businessId the business id
         * @param incidentId the incident id
         * @param taskId     the task id
         * @param delegate   the delegate
         */
        public FireOffIncidentEmail(Activity activity, String host, String businessId, String
                incidentId, int taskId, AsyncObjectResponse delegate) {
            this.activity = activity;
            this.host = host;
            this.path = activity.getString(R.string.api_send_incident_report)
                    .replaceAll(":businessId", businessId)
                    .replaceAll(":incidentId", incidentId);
            this.taskId = taskId;
            this.delegate = delegate;
            this.connectionChecker = new ConnectionChecker(activity);
        }

        @Override
        protected Object doInBackground(Void... params) {
            if (connectionChecker.isOnline()) {
                AuthenticatedHttpServiceHandler apiRequestHandler = new
                        AuthenticatedHttpServiceHandler(activity);
                apiRequestHandler.makeHttpServiceRequest(
                        host,
                        path,
                        "GET",
                        null,
                        null
                );
            }
            return null;
        }
    }
}
