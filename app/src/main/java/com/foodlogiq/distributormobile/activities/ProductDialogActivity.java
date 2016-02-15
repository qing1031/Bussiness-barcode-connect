package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.asyncTasks.ProductSearchAsyncTask;
import com.foodlogiq.distributormobile.customViews.DatePickerEditText;
import com.foodlogiq.distributormobile.entityClasses.Product;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;
import com.foodlogiq.flqassets.asyncHelpers.AsyncObjectResponse;

import java.text.ParseException;
import java.util.regex.Pattern;


/**
 * Mocks a dialog window to allow the user to enter a case label manually. Returns the form
 * contents back
 * to the parent activity.
 */
public class ProductDialogActivity extends Activity implements View.OnClickListener,
        AsyncObjectResponse {
    private static final int PRODUCT_SEARCH_TASK_ID = 0;
    private String customHelperHost;
    private String currentCommunityFoodlogiqId;
    private Product foundProduct;
    private DatePickerEditText packDatePicker;
    private DatePickerEditText useThroughDatePicker;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_product);

        final SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        currentCommunityFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_community_foodlogiq_id), "");
        customHelperHost = sharedPreferences.getString(getString(R.string
                .custom_helper_site), getString(R.string.default_helper_site_host));

        EditText gtinEditText = (EditText) findViewById(R.id.dialog_gtin);
        gtinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String gtin = ((EditText) v).getText().toString();
                //Only fire off search if value is 14 digits. TODO: Add check digit validation
                if (!hasFocus && Pattern.matches("\\d{14}", gtin)) {
                    new ProductSearchAsyncTask(
                            ProductDialogActivity.this,
                            customHelperHost,
                            currentCommunityFoodlogiqId,
                            gtin,
                            PRODUCT_SEARCH_TASK_ID,
                            ProductDialogActivity.this)
                            .execute();
                }
            }
        });

        TextView productPackDate = (TextView) findViewById(R.id.dialog_pack_date);
        packDatePicker = new DatePickerEditText(ProductDialogActivity
                .this, productPackDate, false);
        TextView productUseThroughDate = (TextView) findViewById(R.id.dialog_use_thru_date);
        useThroughDatePicker = new DatePickerEditText
                (ProductDialogActivity.this, productUseThroughDate, false);

        findViewById(R.id.ok_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);

        checkForScannedItem();
    }

    private void checkForScannedItem() {
        Bundle b = this.getIntent().getExtras();
        if (b == null) return;
        String globalTradeItemNumber = b.getString("globalTradeItemNumber", "");
        String lot = b.getString("lot", "");
        String packDate = b.getString("packDate", "");
        String useThroughDate = b.getString("useThroughDate", "");
        String serialNumber = b.getString("serialNumber", "");
        int quantity = b.getInt("quantity", 1);

        EditText gtinField = (EditText) findViewById(R.id.dialog_gtin);

        gtinField.setText(globalTradeItemNumber);
        ((EditText) findViewById(R.id.dialog_lot)).setText(lot);
        try {
            packDatePicker.setDateFromDateObject(DateFormatters.gs1Format.parse(packDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            useThroughDatePicker.setDateFromDateObject(DateFormatters.gs1Format.parse
                    (useThroughDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((EditText) findViewById(R.id.dialog_quantity)).setText(String.valueOf(quantity));

        gtinField.getOnFocusChangeListener().onFocusChange(gtinField, false);
    }

    /**
     * Satisfies {@link View.OnClickListener} interface.
     * If Ok is clicked, pass resulting form to parent activity.
     * If Cancel is clicked, return back to parent activity, with {@link Activity#RESULT_CANCELED}
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
                Intent oIntent = new Intent();
                if (foundProduct != null)
                    oIntent.putExtra("name", foundProduct.getName());
                String gtin = ((EditText) findViewById(R.id.dialog_gtin)).getText()
                        .toString();
                String lot = ((EditText) findViewById(R.id.dialog_lot)).getText().toString();
                if (gtin.length() < 14) {
                    String paddedGtin = padStringTo14Characters(gtin);
                    oIntent.putExtra("gtin", paddedGtin);
                    oIntent.putExtra("lot", "UPC");
                } else if (gtin.length() == 14 && lot.isEmpty()) {
                    oIntent.putExtra("gtin", gtin);
                    oIntent.putExtra("lot", "ITF-14");
                } else {
                    oIntent.putExtra("gtin", gtin);
                    oIntent.putExtra("lot", lot);
                }
                oIntent.putExtra("quantity", ((EditText) findViewById(R.id.dialog_quantity)
                ).getText().toString());
                oIntent.putExtra("packDate", ((TextView) findViewById(R.id
                        .dialog_pack_date)).getText());
                oIntent.putExtra("useThroughDate", ((TextView) findViewById(R.id
                        .dialog_use_thru_date)).getText());
                setResult(RESULT_OK, oIntent);
                finish();
                break;

            case R.id.cancel_btn:
                Intent cIntent = new Intent();
                setResult(RESULT_CANCELED, cIntent);
                finish();
                break;
        }
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

    @Override
    public void processAsyncResponse(String error, Object result, int taskId) {
        if (taskId == PRODUCT_SEARCH_TASK_ID) {
            this.foundProduct = (Product) result;
            updateProductName();
        }
    }

    private void updateProductName() {
        TextView productWrapper = ((TextView) findViewById(R.id.dialog_product_name));
        if (this.foundProduct != null && !this.foundProduct.getName().isEmpty()) {
            productWrapper.setText(this.foundProduct.getName());
            productWrapper.setVisibility(View.VISIBLE);
        } else {
            productWrapper.setText("");
            productWrapper.setVisibility(View.GONE);
        }
    }
}
