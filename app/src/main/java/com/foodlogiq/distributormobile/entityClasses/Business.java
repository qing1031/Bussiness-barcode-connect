package com.foodlogiq.distributormobile.entityClasses;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.databases.contentProviders.BusinessContentProvider;
import com.foodlogiq.distributormobile.databases.tables.BusinessTable;
import com.foodlogiq.distributormobile.interfaces.JSONParceable;
import com.foodlogiq.flqassets.FLQActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class holds all the first level data for a business.
 */

public class Business implements JSONParceable {
    private String foodlogiqId = "";
    private String name = "";
    private String iconUrl = "";
    private String id = "";

    public Business() {
    }

    /**
     * @param foodlogiqId Id of business on Connect server
     * @param name        Business name
     * @param iconUrl     S3 url of business's icon. Used to pull down picture file to display.
     * @param id          Sqlite database id.
     */
    public Business(String foodlogiqId, String name, String iconUrl, String id) {
        this.foodlogiqId = foodlogiqId;
        this.name = name;
        this.iconUrl = iconUrl;
        this.id = id;
    }

    /**
     * @param foodlogiqId Id of business on Connect server
     * @param name        Business name
     * @param iconUrl     S3 url of business's icon. Used to pull down picture file to display.
     */
    public Business(String foodlogiqId, String name, String iconUrl) {
        this(foodlogiqId, name, iconUrl, null);
    }

    /**
     * Stores multiple businesses in the sqlite database.
     *
     * @param activity            Used to get the content resolve for sqlite db interaction.
     * @param mobileAccessToken   User token used to associate businesses to current user.
     * @param businessesJSONArray Json array of businesses. They will be parsed into java
     *                            objects, then
     *                            inserted into the sqlite database.
     * @return number of businesses inserted.
     */
    public static int batchCreate(Activity activity, String mobileAccessToken, JSONArray
            businessesJSONArray) {
        ArrayList<ContentValues> batchContentValuesArrayList = new ArrayList<>();
        for (int i = 0; i < businessesJSONArray.length(); i++) {
            try {
                ContentValues values = new ContentValues();
                JSONObject business = businessesJSONArray.getJSONObject(i);
                values.put(BusinessTable.COLUMN_FOODLOGIQ_ID, business.has("_id") ? business
                        .getString("_id") : "");
                values.put(BusinessTable.COLUMN_NAME, business.has("name") ? business.getString
                        ("name") : "");
                values.put(BusinessTable.COLUMN_ICON, business.has("iconURL") ? business
                        .getString("iconURL") : "");
                values.put(BusinessTable.COLUMN_BUSINESS_OWNER, mobileAccessToken);
                batchContentValuesArrayList.add(values);
            } catch (JSONException e) {
                //Don't try to add invalid businesses
                //If it gets to the add function it won't have thrown this error.
            }
        }
        ContentValues[] batchContentValues = new ContentValues[batchContentValuesArrayList.size()];
        batchContentValuesArrayList.toArray(batchContentValues);
        return activity.getContentResolver().bulkInsert(BusinessContentProvider.CONTENT_URI,
                batchContentValues);
    }

    /**
     * Removes preference of current business.
     */
    public static void removeCurrent(FLQActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(activity.getString(R.string.current_business_id));
        editor.remove(activity.getString(R.string.current_business_foodlogiq_id));
        editor.remove(activity.getString(R.string.current_business_name));
        editor.apply();
    }

    /**
     * Sets business as the default business for the application
     */
    public void setAsCurrent(FLQActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(activity.getString(R.string.current_business_id), getId());
        editor.putString(activity.getString(R.string.current_business_foodlogiq_id),
                getFoodlogiqId());
        editor.putString(activity.getString(R.string.current_business_name), getName());
        editor.apply();
    }

    /**
     * @see JSONParceable#createJSON()
     */
    @Override
    public JSONObject createJSON() {
        JSONObject business = new JSONObject();
        try {
            business.put("_id", getFoodlogiqId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            business.put("name", getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return business;
    }

    /**
     * @see JSONParceable#parseJSON(JSONObject)
     */
    @Override
    public void parseJSON(JSONObject businessJson) {
        try {
            this.foodlogiqId = businessJson.has("_id") ? businessJson.getString("_id") : "";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            this.name = businessJson.has("name") ? businessJson.getString("name") : "";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            this.iconUrl = businessJson.has("iconUrl") ? businessJson.getString("iconUrl") : "";
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

    public String getId() {
        return id;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
