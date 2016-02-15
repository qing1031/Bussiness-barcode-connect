package com.foodlogiq.distributormobile.entityClasses;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.foodlogiq.distributormobile.interfaces.JSONParceable;
import com.foodlogiq.distributormobile.interfaces.LogDescriptor;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * This class holds all the first level data for a receiving event.
 */
public class ReceiptEvent implements JSONParceable, LogDescriptor {
    public static final String EVENT_TYPE = "eventType";
    public static final String DATE = "date";
    public static final String BUSINESS = "business";
    public static final String ORIGIN = "origin";
    public static final String LOCATION = "location";
    public static final String CONTENTS = "contents";
    public static final String RECEIVING = "receiving";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String GLOBAL_LOCATION_NUMBER = "globalLocationNumber";
    public static final String STORE_TRANSFER = "storeTransfer";
    public static final String INTERNAL_ID = "internalId";

    private String date;
    private String businessId;
    private String originInternalId;
    private String originGln;
    private String locationGln;
    private String locationName;
    private ArrayList<SparseProduct> contents;
    private boolean storeTransfer = false;

    public ReceiptEvent() {

    }

    /**
     * @param businessId   Foodlogiq Id of business associated with receiving event.
     * @param locationName Name of location receiving the products.
     * @param locationGln  GlobalLocationNumber of location receiving the products.
     */
    public ReceiptEvent(String businessId, String locationName, String locationGln) {
        this.businessId = businessId;
        this.locationName = locationName;
        this.locationGln = locationGln;
        this.contents = new ArrayList<>();
        this.date = DateFormatters.isoDateFormatter.format(new Date());
    }

    /**
     * @see JSONParceable#createJSON()
     */
    @Override
    public JSONObject createJSON() {
        JSONObject re = new JSONObject();
        try {
            re.put(EVENT_TYPE, RECEIVING);
            re.put(DATE, this.date);
            re.put(BUSINESS, getBusinessIdObject());
            re.put(ORIGIN, getOriginObject());
            re.put(LOCATION, getLocationObject());
            re.put(STORE_TRANSFER, getStoreTransfer());
            re.put(CONTENTS, SparseProduct.toJSONArray(getContents()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return re;
    }

    /**
     * @see JSONParceable#parseJSON(JSONObject)
     */
    @Override
    public void parseJSON(JSONObject jsonObject) {
        if (jsonObject.has(LOCATION)) {
            try {
                JSONObject locationJson = jsonObject.getJSONObject(LOCATION);
                this.locationName = locationJson.has(NAME) ? locationJson.getString(NAME) : "";
                this.locationGln = locationJson.has(GLOBAL_LOCATION_NUMBER) ? locationJson
                        .getString(GLOBAL_LOCATION_NUMBER) : "";
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has(ORIGIN)) {
            try {
                JSONObject locationJson = jsonObject.getJSONObject(ORIGIN);
                this.originGln = locationJson.has(GLOBAL_LOCATION_NUMBER) ? locationJson
                        .getString(GLOBAL_LOCATION_NUMBER) : "";
                this.originInternalId = locationJson.has(INTERNAL_ID) ? locationJson.getString
                        (INTERNAL_ID) : "";
            } catch (JSONException ignored) {
            }
        }

        if (jsonObject.has(BUSINESS)) {
            try {
                JSONObject businessJson = jsonObject.getJSONObject(BUSINESS);
                this.businessId = businessJson.has(ID) ? businessJson.getString(ID) : "";
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has(DATE)) {
            try {
                this.date = jsonObject.getString(DATE);
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has(CONTENTS)) {
            try {
                this.contents = SparseProduct.fromJSONArray(jsonObject.getJSONArray(CONTENTS));
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has(STORE_TRANSFER)) {
            try {
                this.storeTransfer = jsonObject.getBoolean(STORE_TRANSFER);
            } catch (JSONException ignored) {
            }
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public JSONObject getBusinessIdObject() throws JSONException {
        JSONObject bid = new JSONObject();

        bid.put(ID, getBusinessId());

        return bid;
    }

    public String getOriginGln() {
        return originGln;
    }

    public void setOriginGln(String originGln) {
        this.originGln = originGln;
    }

    public JSONObject getOriginObject() throws JSONException {
        JSONObject oo = new JSONObject();
        if (storeTransfer) {
            oo.put(INTERNAL_ID, getOriginInternalId());
        } else {
            oo.put(GLOBAL_LOCATION_NUMBER, getOriginGln());
        }
        return oo;
    }

    public String getLocationGln() {
        return locationGln;
    }


    public JSONObject getLocationObject() throws JSONException {
        JSONObject lo = new JSONObject();

        lo.put(NAME, getLocationName());
        lo.put(GLOBAL_LOCATION_NUMBER, getLocationGln());

        return lo;
    }

    public String getLocationName() {
        return locationName;
    }


    public ArrayList<SparseProduct> getContents() {
        return contents;
    }

    public void setContents(ArrayList<SparseProduct> contents) {
        this.contents = contents;
    }

    public boolean getStoreTransfer() {
        return storeTransfer;
    }

    public void setStoreTransfer(boolean storeTransfer) {
        this.storeTransfer = storeTransfer;
    }

    public String getOriginInternalId() {
        return originInternalId;
    }

    public void setOriginInternalId(String originInternalId) {
        this.originInternalId = originInternalId;
    }

    /**
     * @see LogDescriptor#getDetails(boolean)
     */
    @Override
    public Spannable getDetails(boolean success) {
        String details;
        if (getStoreTransfer()) {
            details = "Store Transfer ";
            if (getOriginInternalId() != null && !getOriginInternalId().isEmpty())
                details = details.concat("from " + getOriginInternalId());
        } else {
            details = "DC Receipt ";
            if (getOriginGln() != null && !getOriginGln().isEmpty())
                details = details.concat("from " + getOriginGln());
        }
        details = details.concat(success ? " succeeded" : " failed");
        Spannable detailSpan = new SpannableString(details);
        if (success) {
            detailSpan.setSpan(new StyleSpan(Typeface.BOLD), detailSpan.length() - ("succeeded"
                    .length()), detailSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            detailSpan.setSpan(new ForegroundColorSpan(Color.RED), detailSpan.length() -
                    ("failed".length()), detailSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            detailSpan.setSpan(new StyleSpan(Typeface.BOLD), detailSpan.length() - ("failed"
                    .length()), detailSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return detailSpan;
    }

    @Override
    public Spannable getAdditionalDetails() {
        String details = "";
        if (!getContents().isEmpty()) details = "Contents:\n";
        for (SparseProduct sp : getContents()) {
            if (sp.getName().isEmpty()) {
                details = details.concat(String.format("%s\n%d %s\n%s\n",
                        sp.getGlobalLocationTradeItemNumber(),
                        sp.getQuantityAmount(),
                        sp.getQuantityUnits(),
                        this.getLocationName()));
            } else {
                if (sp.getDescription().isEmpty()) {
                    details = details.concat(String.format("%s\n%d %s\n%s\n",
                            sp.getName(),
                            sp.getQuantityAmount(),
                            sp.getQuantityUnits(),
                            this.getLocationName()));

                } else {
                    details = details.concat(String.format("%s\n%s\n%d %s\n%s\n",
                            sp.getName(),
                            sp.getDescription(),
                            sp.getQuantityAmount(),
                            sp.getQuantityUnits(),
                            this.getLocationName()));
                }
            }
        }
        Spannable detailSpan = new SpannableString(details);
        detailSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, "Contents:".length(), Spannable
                .SPAN_EXCLUSIVE_EXCLUSIVE);
        return detailSpan;
    }
}
