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
import java.util.Iterator;

/**
 * This class holds all the first level data for a receiving event.
 */
public class TransformEvent implements JSONParceable, LogDescriptor {
    public static final String TRANSFORMING = "transforming";
    public static final String EVENT_TYPE = "eventType";
    public static final String DATE = "date";
    public static final String BUSINESS = "business";
    public static final String LOCATION = "location";
    public static final String INPUTS = "inputs";
    public static final String OUTPUTS = "outputs";
    public static final String TYPE_SPECIFIC_ATTRIBUTES = "typeSpecificAttributes";
    public static final String ID = "_id";
    public static final String TYPE = "type";

    private String date;
    private String businessId;
    private String locationGln;
    private String locationName;
    private ArrayList<SparseProduct> inputs = new ArrayList<>();
    private ArrayList<SparseProduct> outputs = new ArrayList<>();
    private EventType type;
    private ArrayList<CustomAttribute> customAttributes = new ArrayList<>();
    private String eventTypeId;

    public TransformEvent() {
    }

    /**
     * @param businessId   Foodlogiq Id of business associated with receiving event.
     * @param locationName Name of location receiving the products.
     * @param locationGln  GlobalLocationNumber of location receiving the products.
     */
    public TransformEvent(String businessId, String locationName, String locationGln) {
        this.businessId = businessId;
        this.locationName = locationName;
        this.locationGln = locationGln;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.type = null;
        this.date = DateFormatters.isoDateFormatter.format(new Date());
    }

    /**
     * @see JSONParceable#createJSON()
     */
    @Override
    public JSONObject createJSON() {
        JSONObject te = new JSONObject();
        try {
            te.put(EVENT_TYPE, TRANSFORMING);
            te.put(DATE, this.date);
            te.put(BUSINESS, getBusinessIdObject());
            te.put(INPUTS, SparseProduct.toJSONArray(getInputs()));
            te.put(OUTPUTS, SparseProduct.toJSONArray(getOutputs()));
            if (!getCustomAttributes().isEmpty())
                te.put(TYPE_SPECIFIC_ATTRIBUTES, CustomAttribute.toJSONObject(getCustomAttributes
                        ()));
            if (eventTypeId != null && !eventTypeId.isEmpty())
                te.put(TYPE, getEventTypeJSONObject());
            te.put(LOCATION, getLocationObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return te;
    }

    /**
     * @see JSONParceable#parseJSON(JSONObject)
     */
    @Override
    public void parseJSON(JSONObject jsonObject) {
        if (jsonObject.has(LOCATION)) {
            try {
                JSONObject locationJson = jsonObject.getJSONObject(LOCATION);
                this.locationName = locationJson.has(Location.NAME_KEY) ? locationJson.getString
                        (Location.NAME_KEY) : "";
                this.locationGln = locationJson.has(Location.GLOBAL_LOCATION_NUMBER_KEY) ?
                        locationJson
                                .getString(Location.GLOBAL_LOCATION_NUMBER_KEY) : "";
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has(INPUTS)) {
            try {
                this.inputs = SparseProduct.fromJSONArray(jsonObject.getJSONArray(INPUTS));
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has(OUTPUTS)) {
            try {
                this.outputs = SparseProduct.fromJSONArray(jsonObject.getJSONArray(INPUTS));
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

    public String getLocationGln() {
        return locationGln;
    }

    public JSONObject getLocationObject() throws JSONException {
        JSONObject lo = new JSONObject();

        lo.put(Location.NAME_KEY, getLocationName());
        lo.put(Location.GLOBAL_LOCATION_NUMBER_KEY, getLocationGln());

        return lo;
    }

    public String getLocationName() {
        return locationName;
    }


    /**
     * @see LogDescriptor#getDetails(boolean)
     */
    @Override
    public Spannable getDetails(boolean success) {
        String details;
        details = "Transforming Event ";
        if (getLocationName() != null && !getLocationName().isEmpty()) {
            details = details.concat("at " + getLocationName());
        } else if (getLocationGln() != null && !getLocationGln().isEmpty()) {
            details = details.concat("at " + getLocationGln());
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
        if (!getInputs().isEmpty()) details = "Inputs:\n";
        for (SparseProduct sp : getInputs()) {
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
        if (!getOutputs().isEmpty()) details = "Outputs:\n";
        for (SparseProduct sp : getOutputs()) {
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
        detailSpan.setSpan(new StyleSpan(Typeface.BOLD),
                0, "Inputs:".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailSpan.setSpan(new StyleSpan(Typeface.BOLD),
                details.indexOf("Outputs:"), "Outputs:".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return detailSpan;
    }

    public ArrayList<SparseProduct> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<SparseProduct> inputs) {
        this.inputs = inputs;
    }

    public ArrayList<SparseProduct> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<SparseProduct> outputs) {
        this.outputs = outputs;
    }

    public EventType getType() {
        return type;
    }

    public ArrayList<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(ArrayList<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public void setEventType(EventType eventType) {
        this.eventTypeId = eventType.getFoodlogiqId();
    }


    public String getEventTypeId() {
        return eventTypeId;
    }

    public JSONObject getEventTypeJSONObject() throws JSONException {
        JSONObject etid = new JSONObject();
        etid.put(ID, getEventTypeId());
        return etid;
    }
}
