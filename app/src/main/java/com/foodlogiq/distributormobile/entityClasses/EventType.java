package com.foodlogiq.distributormobile.entityClasses;

import android.app.Activity;
import android.content.ContentValues;

import com.foodlogiq.distributormobile.databases.contentProviders.CustomAttributeContentProvider;
import com.foodlogiq.distributormobile.databases.contentProviders.EventTypeContentProvider;
import com.foodlogiq.distributormobile.databases.tables.CustomAttributeTable;
import com.foodlogiq.distributormobile.databases.tables.EventTypeTable;
import com.foodlogiq.distributormobile.interfaces.JSONParceable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by djak250 on 1/15/16.
 */
public class EventType implements JSONParceable {
    private boolean nullValue = false;
    private String businessId = "";
    private String name = "";
    private String associateWith = "";
    private ArrayList<CustomAttribute> attributes = new ArrayList<>();
    private String id = "";
    private String foodlogiqId = "";

    public EventType(String id, String foodlogiqId, String businessId, String name, String
            associateWith, ArrayList<CustomAttribute> customAttributes) {
        this.id = id;
        this.foodlogiqId = foodlogiqId;
        this.businessId = businessId;
        this.name = name;
        this.associateWith = associateWith;
        this.attributes = customAttributes;
    }

    public EventType() {
    }

    public EventType(boolean b) {
        nullValue = b;
    }

    /**
     * @param activity
     * @param eventTypeJSONArray
     * @return
     */
    public static int batchCreate(Activity activity, String businessId, JSONArray
            eventTypeJSONArray) {
        ArrayList<ContentValues> batchContentEventTypeValuesArrayList = new ArrayList<>();
        for (int i = 0; i < eventTypeJSONArray.length(); i++) {

            try {
                ContentValues values = new ContentValues();
                JSONObject eventTypeJSONObject = eventTypeJSONArray.getJSONObject(i);
                EventType eventType = new EventType();
                eventType.parseJSON(eventTypeJSONObject);
                values.put(EventTypeTable.COLUMN_FOODLOGIQ_ID, eventType.getFoodlogiqId());
                values.put(EventTypeTable.COLUMN_BUSINESS_ID, businessId);
                values.put(EventTypeTable.COLUMN_NAME, eventType.getName());
                values.put(EventTypeTable.COLUMN_ASSOCIATE_WITH, eventType.getAssociateWith());
                ArrayList<CustomAttribute> eventAttributes = eventType.getAttributes();
                ArrayList<ContentValues> batchContentCustomAttributesValuesArrayList = new
                        ArrayList<>();
                for (int j = 0; j < eventAttributes.size(); j++) {
                    ContentValues attributeValues = new ContentValues();
                    attributeValues.put(CustomAttributeTable.COLUMN_PARENT_TYPE_ID, eventType
                            .getFoodlogiqId());
                    attributeValues.put(CustomAttributeTable.COLUMN_COMMON_NAME, eventAttributes
                            .get(j).getCommonName());
                    attributeValues.put(CustomAttributeTable.COLUMN_STORED_AS, eventAttributes
                            .get(j).getStoredAs());
                    attributeValues.put(CustomAttributeTable.COLUMN_FIELD_TYPE, eventAttributes
                            .get(j).getFieldType());
                    ArrayList<String> options = eventAttributes.get(j).getOptions();
                    String pipeSeparatedOptions = "";
                    for (int k = 0; k < options.size(); k++) {
                        pipeSeparatedOptions = pipeSeparatedOptions.concat(options.get(k));
                        if (k != options.size() - 1) {
                            pipeSeparatedOptions = pipeSeparatedOptions.concat("|");
                        }
                    }
                    attributeValues.put(CustomAttributeTable.COLUMN_OPTIONS, pipeSeparatedOptions);
                    attributeValues.put(CustomAttributeTable.COLUMN_REQUIRED, eventAttributes.get
                            (j).getRequired());
                    batchContentCustomAttributesValuesArrayList.add(attributeValues);
                }
                ContentValues[] batchAttributeContentValues = new
                        ContentValues[batchContentCustomAttributesValuesArrayList.size()];
                batchContentCustomAttributesValuesArrayList.toArray(batchAttributeContentValues);
                activity.getContentResolver().bulkInsert(CustomAttributeContentProvider
                                .CONTENT_URI,
                        batchAttributeContentValues);
                batchContentEventTypeValuesArrayList.add(values);
            } catch (JSONException e) {
                //Don't try to add invalid invalid event type
                //If it gets to the add function it won't have thrown this error.
            }
        }
        ContentValues[] batchContentValues = new
                ContentValues[batchContentEventTypeValuesArrayList.size()];
        batchContentEventTypeValuesArrayList.toArray(batchContentValues);
        return activity.getContentResolver().bulkInsert(EventTypeContentProvider.CONTENT_URI,
                batchContentValues);
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
        if (jsonObject.has("business"))
            try {
                JSONObject businessJsonObject = jsonObject.getJSONObject("business");
                this.businessId = businessJsonObject.getString("_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("_id"))
            try {
                this.foodlogiqId = jsonObject.getString("_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("name"))
            try {
                this.name = jsonObject.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("associateWith"))
            try {
                this.associateWith = jsonObject.getString("associateWith");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (jsonObject.has("attributes"))
            try {
                this.attributes = new ArrayList<>();
                JSONArray attributesJson = jsonObject.getJSONArray("attributes");
                for (int i = 0; i < attributesJson.length(); i++) {
                    JSONObject attributeJson = attributesJson.getJSONObject(i);
                    CustomAttribute attribute = new CustomAttribute();
                    attribute.parseJSON(attributeJson);
                    this.attributes.add(attribute);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public String getFoodlogiqId() {
        return foodlogiqId;
    }

    public String getName() {
        return name;
    }

    public String getAssociateWith() {
        return associateWith;
    }

    public ArrayList<CustomAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        if (nullValue)
            return "Select an event type";
        return name;
    }

    public String getBusinessFoodlogiqId() {
        return businessId;
    }

    public boolean isNull() {
        return nullValue;
    }
}
