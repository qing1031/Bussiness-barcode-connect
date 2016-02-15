package com.foodlogiq.distributormobile.entityClasses;

import com.foodlogiq.distributormobile.interfaces.JSONParceable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by djak250 on 1/21/16.
 */
public class CustomAttribute implements JSONParceable {
    private String id;
    private String commonName;
    private String storedAs;
    private boolean required;
    private ArrayList<String> options = new ArrayList<>();
    private String fieldType;
    private Object value;

    public CustomAttribute() {

    }

    public CustomAttribute(String id, String commonName, String storedAs, String fieldType,
                           Boolean required, ArrayList<String> options) {
        this.commonName = commonName;
        this.storedAs = storedAs;
        this.fieldType = fieldType;
        this.required = required;
        this.options = options;
        this.required = required;
        this.id = id;
    }

    public CustomAttribute(String storedAs, String value) {
        this.storedAs = storedAs;
        this.value = value;
    }

    /**
     * @param customAttributes Array list of CustomAttributes
     * @return JSONObject of mapped values
     */
    public static JSONObject toJSONObject(ArrayList<CustomAttribute> customAttributes) throws
            JSONException {
        JSONObject cao = new JSONObject();

        for (CustomAttribute ca : customAttributes) {
            cao.put(ca.getStoredAs(), ca.getValue());
        }

        return cao;
    }

    /**
     * @return A JSON representation of the implementing class.
     */
    @Override
    public JSONObject createJSON() {
        return null;
    }

    /**
     * Populates implementing class with data from JSONObject param.
     *
     * @param jsonObject JSONObject containing data for implementing class.
     */
    @Override
    public void parseJSON(JSONObject jsonObject) {
        if (jsonObject.has("commonName"))
            try {
                this.commonName = jsonObject.getString("commonName");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("storedAs"))
            try {
                this.storedAs = jsonObject.getString("storedAs");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("options") && !jsonObject.isNull("options"))
            try {
                JSONArray optionsArray = jsonObject.getJSONArray("options");
                for (int i = 0; i < optionsArray.length(); i++) {
                    this.options.add(optionsArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("fieldType"))
            try {
                this.fieldType = jsonObject.getString("fieldType");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("required"))
            try {
                this.required = jsonObject.getBoolean("required");
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public String getCommonName() {
        return commonName;
    }

    public String getStoredAs() {
        return storedAs;
    }

    public String getFieldType() {
        return fieldType;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public boolean getRequired() {
        return required;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
