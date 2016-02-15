package com.foodlogiq.distributormobile.entityClasses;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.foodlogiq.distributormobile.databases.contentProviders.ApiTransactionContentProvider;
import com.foodlogiq.distributormobile.databases.tables.ApiTransactionTable;
import com.foodlogiq.distributormobile.interfaces.JSONParceable;
import com.foodlogiq.distributormobile.interfaces.LogDescriptor;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * ApiTransactions are details associated with posting data to the server for saving in the Connect
 * database. They contain the relevant ownership details, as well as a json(string) representation
 * of the data sent to the server. There is also a photoPaths attribute to handle if photographs are
 * associated with the entity.
 */
public class ApiTransaction {
    private String id;
    private String jsonString;
    private boolean success;
    private String foodlogiqId;
    private String subOwner;
    private String businessOwner;
    private String transactionEntitySubType;
    private String transactionEntityType;
    private Date date;
    private String photoPaths;
    private String entityClass;

    /**
     * @param transactionEntityType The type of entity that the json represents. Ex: incidents
     * @param entityClass           The actual class name of the entity type. For use with
     *                              reinstantiating a java
     *                              object from the json.
     * @param date                  Date of api transaction.
     * @param jsonString            String representation of json sent to server for storage.
     * @param businessOwner         Business id of the owner of the transaction.
     * @param subOwner              If a subownership is possible, for instance, a location id,
     *                              then it's id is stored
     *                              as well.
     * @param success               Whether or not the transaction was successful.
     * @param id                    SQL ID after insertion.
     */
    public ApiTransaction(
            String transactionEntityType,
            String transactionEntitySubType,
            String entityClass,
            Date date,
            String jsonString,
            String businessOwner,
            String subOwner,
            boolean success,
            String id
    ) {
        this.transactionEntityType = transactionEntityType;
        this.transactionEntitySubType = transactionEntitySubType;
        this.entityClass = entityClass;
        this.date = date == null ? new Date() : date;
        this.jsonString = jsonString;
        this.businessOwner = businessOwner;
        this.subOwner = subOwner;
        this.success = success;
        this.id = id;
    }

    /**
     * @param transactionEntityType The type of entity that the json represents. Ex: incidents
     * @param entityClass           The actual class name of the entity type. For use with
     *                              reinstantiating a java
     *                              object from the json.
     * @param dateString            Date of api transaction as string. Will be converted to a
     *                              date object
     * @param jsonString            String representation of json sent to server for storage.
     * @param businessOwner         Business id of the owner of the transaction.
     * @param subOwner              If a subownership is possible, for instance, a location id,
     *                              then it's id is stored
     *                              as well.
     * @param success               Whether or not the transaction was successful.
     * @param id                    SQL ID after insertion.
     */
    public ApiTransaction(
            String transactionEntityType,
            String transactionEntitySubType,
            String entityClass,
            String dateString,
            String jsonString,
            String businessOwner,
            String subOwner,
            boolean success,
            String id
    ) {
        this(transactionEntityType, transactionEntitySubType, entityClass, new Date(), jsonString,
                businessOwner, subOwner,
                success, id);
        Date parsedDate;
        try {
            parsedDate = DateFormatters.isoDateFormatter.parse(dateString);
        } catch (ParseException e) {
            //default to today if it's an invalid date string
            parsedDate = new Date();
        }
        this.setDate(parsedDate);
    }

    /**
     * Inserts the api transaction into the database and sets the resulting id on the api
     * transaction
     *
     * @param activity Used to get the content resolver for sqlite db interaction
     */
    public void saveToDB(Activity activity) {
        ContentValues values = new ContentValues();
        values.put(ApiTransactionTable.COLUMN_ID, getId());
        values.put(ApiTransactionTable.COLUMN_JSON, getJsonString());
        values.put(ApiTransactionTable.COLUMN_DATE, DateFormatters.isoDateFormatter.format
                (getDate()));
        values.put(ApiTransactionTable.COLUMN_ENTITY_TYPE, getTransactionEntityType());
        values.put(ApiTransactionTable.COLUMN_ENTITY_SUB_TYPE, getTransactionEntitySubType());
        values.put(ApiTransactionTable.COLUMN_ENTITY_CLASS_NAME, getEntityClass());
        values.put(ApiTransactionTable.COLUMN_SUCCESS, getSuccess());
        values.put(ApiTransactionTable.COLUMN_FOODLOGIQ_ID, getFoodlogiqId());
        values.put(ApiTransactionTable.COLUMN_SUB_OWNER, getSubOwner());
        values.put(ApiTransactionTable.COLUMN_BUSINESS_OWNER, getBusinessOwner());

        Uri insertedApiTransaction = activity.getContentResolver().insert
                (ApiTransactionContentProvider.CONTENT_URI, values);
        setId(String.valueOf(ContentUris.parseId(insertedApiTransaction)));
    }

    /**
     * Updates the api transaction in the sqlite db.
     *
     * @param activity Used to get the content resolve for sqlite db interaction.
     * @return sqlite id of the api transaction that was updated.
     */
    public int updateInDB(Activity activity) {
        ContentValues values = new ContentValues();
        values.put(ApiTransactionTable.COLUMN_ID, getId());
        values.put(ApiTransactionTable.COLUMN_JSON, getJsonString());
        values.put(ApiTransactionTable.COLUMN_DATE, DateFormatters.isoDateFormatter.format
                (getDate()));
        values.put(ApiTransactionTable.COLUMN_ENTITY_TYPE, getTransactionEntityType());
        values.put(ApiTransactionTable.COLUMN_ENTITY_SUB_TYPE, getTransactionEntitySubType());
        values.put(ApiTransactionTable.COLUMN_SUCCESS, getSuccess());
        values.put(ApiTransactionTable.COLUMN_FOODLOGIQ_ID, getFoodlogiqId());
        values.put(ApiTransactionTable.COLUMN_SUB_OWNER, getSubOwner());
        values.put(ApiTransactionTable.COLUMN_BUSINESS_OWNER, getBusinessOwner());

        return activity.getContentResolver().update(ApiTransactionContentProvider.CONTENT_URI,
                values, "_id=" + getId(), null);
    }

    /**
     * removes the api transaction from the sqlite db.
     *
     * @param activity Used to get the content resolve for sqlite db interaction.
     */
    public void deleteFromDB(Activity activity) {
        if (getId() != null && !getId().isEmpty())
            activity.getContentResolver().delete(ApiTransactionContentProvider.CONTENT_URI,
                    "_id=" + getId(), null);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonString() {
        return jsonString;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTransactionEntityType() {
        return transactionEntityType;
    }

    public String getTransactionEntitySubType() {
        return transactionEntitySubType;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getFoodlogiqId() {
        return foodlogiqId;
    }

    public void setFoodlogiqId(String foodlogiqId) {
        this.foodlogiqId = foodlogiqId;
    }

    public String getSubOwner() {
        return subOwner;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public String getPhotoPaths() {
        return photoPaths;
    }

    public void setPhotoPaths(String photoPaths) {
        this.photoPaths = photoPaths;
    }

    /**
     * @param activity Used to get the content resolve for sqlite db interaction.
     * @param photos   List of photos who's paths will be pipe separated and stored in the sqlite
     *                 database
     * @return sqlite id of the api transaction that was updated.
     */
    public int addPhotoPaths(Activity activity, ArrayList<Photo> photos) {
        String pipeSeparatedPaths = "";
        for (int i = 0; i < photos.size(); i++) {
            pipeSeparatedPaths = pipeSeparatedPaths.concat(photos.get(i).getPath());
            if (i != photos.size() - 1) {
                pipeSeparatedPaths = pipeSeparatedPaths.concat("|");
            }
        }
        ContentValues values = new ContentValues();
        values.put(ApiTransactionTable.COLUMN_PHOTO_PATHS, pipeSeparatedPaths);
        return activity.getContentResolver().update(ApiTransactionContentProvider.CONTENT_URI,
                values, "_id=" + getId(), null);
    }

    /**
     * Removes photo paths from api transaction.
     *
     * @param activity Used to get the content resolve for sqlite db interaction.
     * @return sqlite id of the api transaction that was updated.
     */
    public int removePhotoPaths(Activity activity) {
        ContentValues values = new ContentValues();
        values.put(ApiTransactionTable.COLUMN_PHOTO_PATHS, "");
        return activity.getContentResolver().update(ApiTransactionContentProvider.CONTENT_URI,
                values, "_id=" + getId(), null);
    }

    /**
     * Attempts to build a detailed description of the api transaction from the
     * {@link LogDescriptor}
     * object.
     *
     * @return A spannable string with the success/fail details, and whatever relevant details
     * are present
     * in the class.
     */
    public Spannable getDetails() {
        Spannable details;
        try {
            Class clazz = Class.forName(getEntityClass());
            Constructor ctor = clazz.getConstructor();
            LogDescriptor logDescriptiveObject = (LogDescriptor) ctor.newInstance();
            if (logDescriptiveObject instanceof JSONParceable) {
                ((JSONParceable) logDescriptiveObject).parseJSON(new JSONObject(getJsonString()));
                details = logDescriptiveObject.getDetails(success);
            } else {
                throw new Exception(getEntityClass().concat(" doesn't implement JSONParceable"));
            }
        } catch (Exception e) {
            //If the entity doesn't exist in the code base anymore, just throw a generic
            // fail/success
            details = new SpannableString("Api Transaction ".concat(getSuccess() ? "succeeded" :
                    "failed"));
            if (getSuccess()) {
                details.setSpan(new StyleSpan(Typeface.BOLD), details.length() - ("succeeded"
                        .length()), details.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                details.setSpan(new ForegroundColorSpan(Color.RED), details.length() - ("failed"
                        .length()), details.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                details.setSpan(new StyleSpan(Typeface.BOLD), details.length() - ("failed".length
                        ()), details.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            e.printStackTrace();
        }
        return details;
    }

    public Spannable getAdditionalDetails() {
        Spannable additionalDetails;
        try {
            Class clazz = Class.forName(getEntityClass());
            Constructor ctor = clazz.getConstructor();
            LogDescriptor logDescriptiveObject = (LogDescriptor) ctor.newInstance();
            if (logDescriptiveObject instanceof JSONParceable) {
                ((JSONParceable) logDescriptiveObject).parseJSON(new JSONObject(getJsonString()));
                additionalDetails = logDescriptiveObject.getAdditionalDetails();
            } else {
                throw new Exception(getEntityClass().concat(" doesn't implement JSONParceable"));
            }
        } catch (Exception e) {
            //If the entity doesn't exist in the code base anymore, just throw a generic
            // fail/success
            additionalDetails = new SpannableString("");
            e.printStackTrace();
        }
        return additionalDetails;
    }
}