package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.foodlogiq.distributormobile.databases.commonTasks.FindApiTransaction;
import com.foodlogiq.distributormobile.entityClasses.ApiTransaction;
import com.foodlogiq.distributormobile.entityClasses.Location;
import com.foodlogiq.distributormobile.entityClasses.Product;
import com.foodlogiq.distributormobile.entityClasses.ReceiptEvent;
import com.foodlogiq.distributormobile.entityClasses.SparseProduct;
import com.foodlogiq.distributormobile.scanhelpers.BarcodeParser;
import com.foodlogiq.distributormobile.scanhelpers.DataWedgeParser;
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
import java.util.List;
import java.util.Locale;

/**
 * CreateReceiptActivity handles the creation of a receiving receipt.
 * Implements AsyncObjectResponse to responses from server on receiving receipt creation
 */
public class CreateReceiptActivity extends FLQActivity implements AsyncObjectResponse,
        BarcodeReadListener {
    private static final int RECEIPT_CONTENTS_SLIDE = 0;
    private static final int RECEIPT_INFO_SLIDE = 1;
    private static final int RECEIPT_CONFIRM_SLIDE = 2;

    private static final int RECEIPT_EVENT_CREATE_TASK_ID = 0;
    private static final int PRODUCT_SEARCH_TASK_ID = 1;
    private static final int MANUAL_PRODUCT_ENTRY = 2;

    private boolean datawedgeExists;
    private String customHelperHost;
    private String currentBusinessFoodlogiqId;
    private String currentCommunityFoodlogiqId;
    private ReceiptEvent receipt;
    private Location currentLocation;
    private SparseProductListAdapter sparseProductListAdapter;
    private String apiTransactionid;
    private BroadcastReceiver scanBroadcastReceiver;

    private int mSlideIndex = 0;
    private ArrayList<View> slides;
    private BackNextButtons backNextButton;
    private SparseProduct tempSparseProduct;
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
        if (mSlideIndex > RECEIPT_CONFIRM_SLIDE) {
            submitEvent();
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
     *     {@link #RECEIPT_CONTENTS_SLIDE}:
     *     Hide back button
     *     {@link #RECEIPT_CONFIRM_SLIDE}:
     *     Update confirm view with contents of previous view forms, and change text of next
     *     button to submit</pre>
     *
     * @param mSlideIndex slide index to handle the view update for.
     */
    private void handleViewUpdates(int mSlideIndex) {
        findViewById(R.id.bottom_back_button).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.bottom_next_button_text)).setText(getString(R.string.next));
        switch (mSlideIndex) {
            case RECEIPT_CONTENTS_SLIDE:
                findViewById(R.id.bottom_back_button).setVisibility(View.GONE);
                break;
            case RECEIPT_CONFIRM_SLIDE:
                updateConfirmView();
                ((TextView) findViewById(R.id.bottom_next_button_text)).setText(getString(R
                        .string.submit));
                break;
        }
    }

    /**
     * Checks for existing apiTransaction to detect if activity was reached through the api logs
     * activity, and attempts to repopulate data from previous attempt.
     * Initializes sparseProduct list adapter to hide/show scanned items when they are
     * removed/added respectively.
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
        currentLocation = Location.getCurrent(CreateReceiptActivity.this);
        if (currentLocation == null) currentLocation = new Location();
        this.receipt = new ReceiptEvent(
                currentBusinessFoodlogiqId,
                currentLocation.getName(),
                currentLocation.getGlobalLocationNumber());

        sparseProductListAdapter = new SparseProductListAdapter(
                this,
                new ArrayList<SparseProduct>(),
                findViewById(R.id.contents_wrapper),
                (ListView) findViewById(R.id.scanned_items_list_view)
        );
        sparseProductListAdapter.addSlideToHiddenButtons(R.id.confirm_scanned_items_list_view);
        formTriggers();
        hardwareScannerSetup();
        intermecSetup();

        //Set current location's gln in the info page. This isn't changeable by the user.
        ((TextView) findViewById(R.id.receipt_location_text_view)).setText(currentLocation
                .getName());
        ((TextView) findViewById(R.id.receipt_confirm_location_text_view)).setText(currentLocation
                .getName());

        Intent i = getIntent();
        apiTransactionid = i.getStringExtra("apiTransactionId");
        if (apiTransactionid != null && !apiTransactionid.isEmpty()) {
            populateExistingReceipt(apiTransactionid);
        }
        handleViewUpdates(mSlideIndex);
    }


    /**
     * @see FLQActivity#getLayoutId()
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_receipt;
    }

    /**
     * @see FLQActivity#initializeActionBar()
     */
    @Override
    protected void initializeActionBar() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String currentLocationName = "Receiving" + ": " + sharedPreferences.getString(getString(R
                .string.current_location_name), "");
        CustomActionBar customActionBar = new CustomActionBar(this, currentLocationName, /*
        Settings enabled */true, false, true);
        customActionBar.setCustomSuperBackAction(new BackToLocationActions(CreateReceiptActivity
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
     * In essence, mirrors the form to look like it did when the receiving receipt creation was
     * attempted the first time.
     *
     * @param apiTransactionid SQL id of the previous attempt of an Receiving Event
     */
    private void populateExistingReceipt(String apiTransactionid) {
        ApiTransaction apiTransaction = new FindApiTransaction(getContentResolver(),
                apiTransactionid).getApiTransaction();
        if (apiTransaction != null) {
            try {
                (receipt = new ReceiptEvent()).parseJSON(new JSONObject(apiTransaction
                        .getJsonString()));

                sparseProductListAdapter.clear();
                sparseProductListAdapter.addAll(receipt.getContents());
                final SwitchCompat storeTransferSwitch = (SwitchCompat) findViewById(R.id
                        .receipt_store_transfer_checkbox);
                if (receipt.getStoreTransfer()) {
                    storeTransferSwitch.setChecked(true);
                    ((EditText) findViewById(R.id.receipt_origin_text_view)).setText(receipt
                            .getOriginInternalId());
                } else {
                    storeTransferSwitch.setChecked(false);
                    ArrayList<Location.SupplyChainLocation> supplyChainLocations =
                            currentLocation.getSupplyChainLocations();
                    for (int i = 0; i < supplyChainLocations.size(); i++) {
                        if (supplyChainLocations.get(i).getGlobalLocationNumber().equals(receipt
                                .getOriginGln())) {
                            ((Spinner) findViewById(R.id
                                    .receipt_origin_supply_chain_location_spinner)).setSelection(i);
                            break;
                        }
                    }
                }

                updateConfirmView();
                setSlideIndex(RECEIPT_CONFIRM_SLIDE);
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
     * </ul>
     */
    private void formTriggers() {
        slides = new ArrayList<>();
        slides.add(findViewById(R.id.contents_wrapper));
        slides.add(findViewById(R.id.receipt_info_layout));
        slides.add(findViewById(R.id.receipt_confirm_layout));

        backNextButton = new BackNextButtons(CreateReceiptActivity.this, new View.OnClickListener
                () {
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
        Button scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(getStartScanListener((SwitchCompat) findViewById(R.id
                .scan_device)));

        Button enterManuallyButton = (Button) findViewById(R.id.manual_entry_button);
        enterManuallyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent manualEntryActivity = new Intent(CreateReceiptActivity.this,
                        ProductDialogActivity.class);
                startActivityForResult(manualEntryActivity, MANUAL_PRODUCT_ENTRY);
            }
        });

        Spinner supplyChainLocationsSpinner = (Spinner) findViewById(R.id
                .receipt_origin_supply_chain_location_spinner);
        ArrayAdapter<Location.SupplyChainLocation> supplyChainLocationsAdapter = new
                ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currentLocation
                .getSupplyChainLocations());
        supplyChainLocationsAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        supplyChainLocationsSpinner.setAdapter(supplyChainLocationsAdapter);

        SwitchCompat storeTransferCheckbox = (SwitchCompat) findViewById(R.id
                .receipt_store_transfer_checkbox);
        storeTransferCheckbox.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener
                () {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.receipt_origin_supply_chain_location_spinner).setVisibility
                            (View.GONE);
                    findViewById(R.id.receipt_origin_text_view).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.receipt_origin_supply_chain_location_spinner).setVisibility
                            (View.VISIBLE);
                    findViewById(R.id.receipt_origin_text_view).setVisibility(View.GONE);
                }
            }
        });
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
        CreateReceiptActivity.this.registerReceiver(scanBroadcastReceiver, new IntentFilter
                ("com" +
                        ".foodlogiq.mobile.scan"));
        CreateReceiptActivity.this.
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
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateReceiptActivity.this);
        switch (scanFormat) {
            case "LABEL-TYPE-EAN128":
                DataWedgeParser dataWedgeParser = new DataWedgeParser(bytes);
                storeSparseProduct(
                        dataWedgeParser.getGlobalTradeItemNumber(),
                        dataWedgeParser.getLot(),
                        dataWedgeParser.getPackDate(),
                        dataWedgeParser.getUseThroughDate(),
                        dataWedgeParser.getSerialNumber(),
                        dataWedgeParser.getQuantity()
                );
                new ProductSearchAsyncTask(
                        CreateReceiptActivity.this,
                        customHelperHost,
                        currentCommunityFoodlogiqId,
                        dataWedgeParser.getGlobalTradeItemNumber(),
                        PRODUCT_SEARCH_TASK_ID,
                        CreateReceiptActivity.this)
                        .execute();
                return;
            case "LABEL-TYPE-I2OF5":
                storeSparseProduct(
                        scannedString,
                        "ITF-14",
                        "",
                        "",
                        "",
                        1
                );
                new ProductSearchAsyncTask(
                        CreateReceiptActivity.this,
                        customHelperHost,
                        currentCommunityFoodlogiqId,
                        scannedString,
                        PRODUCT_SEARCH_TASK_ID,
                        CreateReceiptActivity.this)
                        .execute();
                return;
            //These all have the same functionality. Just pad the content with zeros on
            // the left.
            case "LABEL-TYPE-EAN13":
            case "LABEL-TYPE-UPCE0":
            case "LABEL-TYPE-UPCA":
            case "UPC_E":
                String paddedGtin = padStringTo14Characters(scannedString);
                addOrIncrementSparseProduct(
                        paddedGtin,
                        "UPC"
                );
                return;

            default:
                builder.setMessage("You scanned a '" + scanFormat + "' which is currently" +
                        " not supported");
                break;
        }
    }

    private void parseSonimBarcode(Intent intent) {
        Bundle extras = intent.getExtras();
        String scanContent = extras.getString("data");
        DataWedgeParser dataWedgeParser = new DataWedgeParser(scanContent.getBytes());
        storeSparseProduct(
                dataWedgeParser.getGlobalTradeItemNumber(),
                dataWedgeParser.getLot(),
                dataWedgeParser.getPackDate(),
                dataWedgeParser.getUseThroughDate(),
                dataWedgeParser.getSerialNumber(),
                dataWedgeParser.getQuantity()
        );
        new ProductSearchAsyncTask(
                CreateReceiptActivity.this,
                customHelperHost,
                currentCommunityFoodlogiqId,
                dataWedgeParser.getGlobalTradeItemNumber(),
                PRODUCT_SEARCH_TASK_ID,
                CreateReceiptActivity.this)
                .execute();
    }

    private void parseIntermecBarcode(String scanContent, String symbologyName) {
        String paddedGtin = "";
        switch (symbologyName) {
            case "GS1_128":
                //Intermec has three bytes at the front that we don't need.
                byte[] slicedContent = Arrays.copyOfRange(scanContent.getBytes(), 3, scanContent
                        .length());

                DataWedgeParser barcodeParser = new DataWedgeParser(slicedContent);
                storeSparseProduct(
                        barcodeParser.getGlobalTradeItemNumber(),
                        barcodeParser.getLot(),
                        barcodeParser.getPackDate(),
                        barcodeParser.getUseThroughDate(),
                        barcodeParser.getSerialNumber(),
                        barcodeParser.getQuantity()
                );
                new ProductSearchAsyncTask(
                        CreateReceiptActivity.this,
                        customHelperHost,
                        currentCommunityFoodlogiqId,
                        barcodeParser.getGlobalTradeItemNumber(),
                        PRODUCT_SEARCH_TASK_ID,
                        CreateReceiptActivity.this)
                        .execute();
                return;
            case "EAN8":
            case "EAN13":
            case "UPCE":
                paddedGtin = padStringTo14Characters(scanContent);
                addOrIncrementSparseProduct(
                        paddedGtin,
                        "UPC"
                );
                return;
            case "ITF":
                paddedGtin = padStringTo14Characters(scanContent);
                addOrIncrementSparseProduct(
                        paddedGtin,
                        "ITF-14"
                );
                return;
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
        }
    }

    /**
     * Pulls data from form and populates a summary of the data for the confirm page. This is
     * where everything
     * on the confirm page is set.
     */
    private void updateConfirmView() {
        ListView receiptContentsView = (ListView) findViewById(R.id
                .confirm_scanned_items_list_view);
        receiptContentsView.setAdapter(sparseProductListAdapter);
        TextView locationTextView = (TextView) findViewById(R.id.receipt_location_text_view);
        locationTextView.setText(currentLocation.getName());
        TextView originTextView = (TextView) findViewById(R.id.receipt_confirm_origin_text_view);
        if (((SwitchCompat) findViewById(R.id.receipt_store_transfer_checkbox)).isChecked()) {
            originTextView.setText(
                    ((EditText) findViewById(R.id.receipt_origin_text_view)).getText().toString()
            );
        } else {
            Location.SupplyChainLocation supplyChainLocation = (Location.SupplyChainLocation) (
                    (Spinner) findViewById(R.id.receipt_origin_supply_chain_location_spinner))
                    .getSelectedItem();
            if (supplyChainLocation != null) {
                originTextView.setText(
                        supplyChainLocation.toString()
                );
            }

        }
    }

    /**
     * @return onclicklistener that initiates a scan using the Barcode Scanner application
     */
    private View.OnClickListener getStartScanListener(final SwitchCompat hardwareSwitch) {
        if (!datawedgeExists) findViewById(R.id.scan_selector_wrapper).setVisibility(View.GONE);
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
                    IntentIntegrator scanIntegrator = new IntentIntegrator(CreateReceiptActivity
                            .this);
                    scanIntegrator.initiateScan();
                }
            }
        };
    }

    /**
     * Stores a temporary value for a product, but doesn't add the product to the receipt list yet.
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

    /**
     * Creates a receiving receipt based on the form values, and attempts to submit that receipt to
     * the api.
     */
    private void submitEvent() {
        ArrayList<SparseProduct> receiptContents = new ArrayList<>();
        for (int i = 0; i < sparseProductListAdapter.getCount(); i++)
            receiptContents.add(sparseProductListAdapter.getItem(i));
        receipt.setContents(receiptContents);
        if (((SwitchCompat) findViewById(R.id.receipt_store_transfer_checkbox)).isChecked()) {
            receipt.setStoreTransfer(true);
            receipt.setOriginInternalId(
                    ((EditText) findViewById(R.id.receipt_origin_text_view)).getText().toString()
            );
        } else {
            receipt.setStoreTransfer(false);
            Location.SupplyChainLocation originLocation = ((Location.SupplyChainLocation) (
                    (Spinner) findViewById(R.id.receipt_origin_supply_chain_location_spinner)
            ).getSelectedItem());
            receipt.setOriginGln(
                    originLocation.getGlobalLocationNumber()
            );
        }
        new EventCreateAsyncTask(
                CreateReceiptActivity.this,
                customHelperHost,
                currentBusinessFoodlogiqId,
                "receiving",
                receipt,
                RECEIPT_EVENT_CREATE_TASK_ID,
                this
        )
                .execute();
        getAsyncProgressSpinner().showIndeterminateProgress(true);
    }

    /**
     * <p>If successful:</p>
     * <ul>
     * <li>{@link #PRODUCT_SEARCH_TASK_ID}: Updates contents of receiving receipt based on product
     * returned from server.</li>
     * <li>{@link #RECEIPT_EVENT_CREATE_TASK_ID}: Insert successful receiving receipt json into db
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
            if (taskId == RECEIPT_EVENT_CREATE_TASK_ID) {
                insertTransactionToDB(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Error creating the receipt. You can view and retry the " +
                        "receipt in the receipt logs");
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener
                        () {
                    public void onClick(DialogInterface dialog, int id) {
                        completeReceiptCreation();
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
            case RECEIPT_EVENT_CREATE_TASK_ID:
                getAsyncProgressSpinner().showIndeterminateProgress(false);
                insertTransactionToDB(true);
                completeReceiptCreation();
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
     * Adds api transaction to Database
     *
     * @param success true if the transaction was successful, false if not.
     */
    private void insertTransactionToDB(boolean success) {
        if (apiTransactionid != null) {
            ApiTransaction apiTransaction = new ApiTransaction("events", "receipts", ReceiptEvent
                    .class
                    .getName(), new Date(), receipt.createJSON().toString(),
                    currentBusinessFoodlogiqId, currentLocation.getFoodlogiqId(), success,
                    apiTransactionid);
            apiTransaction.updateInDB(CreateReceiptActivity.this);
        } else {
            ApiTransaction apiTransaction = new ApiTransaction("events", "receipts", ReceiptEvent
                    .class
                    .getName(), new Date(), receipt.createJSON().toString(),
                    currentBusinessFoodlogiqId, currentLocation.getFoodlogiqId(), success, null);
            apiTransaction.saveToDB(CreateReceiptActivity.this);
        }
    }

    /**
     * Toast the user and finish the activity.
     */
    private void completeReceiptCreation() {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Receipt created successfully!", Toast.LENGTH_LONG);
        toast.show();
        finish();
    }

    /**
     * @see Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        CreateReceiptActivity.this.registerReceiver(
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
            CreateReceiptActivity.this.unregisterReceiver(scanBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            //no one cares... that's who
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
            dwReload.setOnMenuItemClickListener(new DataWedgeClickListener(CreateReceiptActivity
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
                    String gtin = intent.getStringExtra("gtin");
                    String lot = intent.getStringExtra("lot");
                    String quantityString = intent.getStringExtra("quantity");
                    int quantity = 1;
                    if (!quantityString.isEmpty()) {
                        quantity = Integer.parseInt(quantityString);
                    }
                    String packDateString = intent.getStringExtra("packDate");
                    String useThroughString = intent.getStringExtra("useThroughDate");
                    storeSparseProduct(gtin, lot, packDateString, useThroughString, "", quantity);
                    if (!gtin.isEmpty()) {
                        new ProductSearchAsyncTask(
                                CreateReceiptActivity.this,
                                customHelperHost,
                                currentCommunityFoodlogiqId,
                                gtin,
                                PRODUCT_SEARCH_TASK_ID,
                                CreateReceiptActivity.this)
                                .execute();
                    }
                    return;
                default:
                    IntentResult scanningResult = IntentIntegrator.parseActivityResult
                            (requestCode, resultCode, intent);
                    if (scanningResult != null) {
                        byte[] scanContent = scanningResult.getRawBytes();
                        String scanFormat = scanningResult.getFormatName();
                        if (scanFormat == null) return;

                        AlertDialog.Builder builder = new AlertDialog.Builder
                                (CreateReceiptActivity.this);

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
                                            CreateReceiptActivity.this,
                                            customHelperHost,
                                            currentCommunityFoodlogiqId,
                                            barcodeParser.getGlobalTradeItemNumber(),
                                            PRODUCT_SEARCH_TASK_ID,
                                            CreateReceiptActivity.this)
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
