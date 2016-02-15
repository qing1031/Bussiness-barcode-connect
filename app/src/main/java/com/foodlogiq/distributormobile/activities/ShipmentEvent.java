package com.foodlogiq.distributormobile.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.foodlogiq.distributormobile.entityClasses.CustomAttribute;
import com.foodlogiq.distributormobile.entityClasses.EventType;
import com.foodlogiq.distributormobile.entityClasses.SparseProduct;
import com.foodlogiq.distributormobile.interfaces.JSONParceable;
import com.foodlogiq.distributormobile.interfaces.LogDescriptor;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by djak250 on 1/28/16.
 */
public class ShipmentEvent implements JSONParceable, LogDescriptor {
    public static final String EVENT_TYPE = "eventType";
    public static final String DATE = "date";
    public static final String BUSINESS = "business";
    public static final String DESTINATION = "destination";
    public static final String LOCATION = "location";
    public static final String CONTENTS = "contents";
    public static final String SHIPPING = "shipping";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String TYPE_SPECIFIC_ATTRIBUTES = "typeSpecificAttributes";
    public static final String GLOBAL_LOCATION_NUMBER = "globalLocationNumber";
    private static final String TYPE = "type";

    private String date;
    private String businessId;
    private String locationGln;
    private String locationName;
    private ArrayList<SparseProduct> contents = new ArrayList<>();
    private String destinationGln;
    private ArrayList<CustomAttribute> customAttributes = new ArrayList<>();
    private String eventTypeId;

    public ShipmentEvent(String businessId, String locationName, String
            locationGln) {
        this.businessId = businessId;
        this.locationName = locationName;
        this.locationGln = locationGln;
        this.contents = new ArrayList<>();
        this.date = DateFormatters.isoDateFormatter.format(new Date());
    }

    public ShipmentEvent() {
    }

    /**
     * @return A JSON representation of the implementing class.
     */
    @Override
    public JSONObject createJSON() {
        JSONObject se = new JSONObject();
        try {
            se.put(EVENT_TYPE, SHIPPING);
            se.put(DATE, this.date);
            se.put(BUSINESS, getBusinessIdObject());
            se.put(DESTINATION, getOriginObject());
            se.put(LOCATION, getLocationObject());
            se.put(CONTENTS, SparseProduct.toJSONArray(getContents()));
            if (!getCustomAttributes().isEmpty())
                se.put(TYPE_SPECIFIC_ATTRIBUTES, CustomAttribute.toJSONObject(getCustomAttributes
                        ()));
            if (eventTypeId != null && !eventTypeId.isEmpty())
                se.put(TYPE, getEventTypeJSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return se;
    }

    /**
     * Populates implementing class with data from JSONObject param.
     *
     * @param jsonObject JSONObject containing data for implementing class.
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
        if (jsonObject.has(DESTINATION)) {
            try {
                JSONObject locationJson = jsonObject.getJSONObject(DESTINATION);
                this.destinationGln = locationJson.has(GLOBAL_LOCATION_NUMBER) ? locationJson
                        .getString(GLOBAL_LOCATION_NUMBER) : "";
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
        if (jsonObject.has(TYPE)) {
            try {
                JSONObject eventTypeJson = jsonObject.getJSONObject(TYPE);
                this.eventTypeId = eventTypeJson.getString(ID);
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has(TYPE_SPECIFIC_ATTRIBUTES)) {
            try {
                JSONObject customAttributeValuesJson = jsonObject.getJSONObject
                        (TYPE_SPECIFIC_ATTRIBUTES);
                Iterator<String> keys = customAttributeValuesJson.keys();
                while (keys.hasNext()) {
                    String storedAs = keys.next();
                    String value = customAttributeValuesJson.getString(storedAs);
                    this.customAttributes.add(new CustomAttribute(storedAs, value));
                }
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

    public JSONObject getOriginObject() throws JSONException {
        JSONObject oo = new JSONObject();
        oo.put(GLOBAL_LOCATION_NUMBER, getDestinationGln());
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

    public String getDestinationGln() {
        return destinationGln;
    }

    public void setDestinationGln(String destinationGln) {
        this.destinationGln = destinationGln;
    }

    public ArrayList<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(ArrayList<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public String getEventTypeId() {
        return eventTypeId;
    }

    public void setEventType(EventType eventType) {
        this.eventTypeId = eventType.getFoodlogiqId();
    }

    public JSONObject getEventTypeJSONObject() throws JSONException {
        JSONObject etid = new JSONObject();
        etid.put(ID, getEventTypeId());
        return etid;
    }

    /**
     * @param success whether the incident submission was successful.
     * @return String of details related to server submission
     */
    @Override
    public Spannable getDetails(boolean success) {
        String details;
        details = "Shipment ";
        if (getDestinationGln() != null && !getDestinationGln().isEmpty())
            details = details.concat("to " + getDestinationGln());
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

