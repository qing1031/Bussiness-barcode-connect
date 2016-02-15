package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.ApiTransactionContentProvider;
import com.foodlogiq.distributormobile.databases.tables.ApiTransactionTable;
import com.foodlogiq.distributormobile.entityClasses.ApiTransaction;

/**
 * Return api transaction with the matching sqlite ID.
 */
public class FindApiTransaction {
    private ApiTransaction apiTransaction;

    public FindApiTransaction(ContentResolver contentResolver, String sql_id) {
        Cursor cur = contentResolver.query(
                ApiTransactionContentProvider.CONTENT_URI,
                new String[]{
                        ApiTransactionTable.COLUMN_ID,
                        ApiTransactionTable.COLUMN_JSON,
                        ApiTransactionTable.COLUMN_PHOTO_PATHS,
                        ApiTransactionTable.COLUMN_DATE,
                        ApiTransactionTable.COLUMN_ENTITY_TYPE,
                        ApiTransactionTable.COLUMN_ENTITY_SUB_TYPE,
                        ApiTransactionTable.COLUMN_ENTITY_CLASS_NAME,
                        ApiTransactionTable.COLUMN_SUCCESS,
                        ApiTransactionTable.COLUMN_FOODLOGIQ_ID,
                        ApiTransactionTable.COLUMN_SUB_OWNER,
                        ApiTransactionTable.COLUMN_BUSINESS_OWNER
                },
                ApiTransactionTable.COLUMN_ID + " = ?",
                new String[]{sql_id},
                null,
                null
        );
        if (cur != null && cur.getCount() > 0) {
            cur.moveToFirst();
            String id = cur.getString(cur.getColumnIndex(ApiTransactionTable.COLUMN_ID));
            String jsonString = cur.getString(cur.getColumnIndex(ApiTransactionTable.COLUMN_JSON));
            String photoPathsString = cur.getString(cur.getColumnIndex(ApiTransactionTable
                    .COLUMN_PHOTO_PATHS));
            String dateString = cur.getString(cur.getColumnIndex(ApiTransactionTable.COLUMN_DATE));
            String entityType = cur.getString(cur.getColumnIndex(ApiTransactionTable
                    .COLUMN_ENTITY_TYPE));
            String entitySubType = cur.getString(cur.getColumnIndex(ApiTransactionTable
                    .COLUMN_ENTITY_SUB_TYPE));
            String entityClass = cur.getString(cur.getColumnIndex(ApiTransactionTable
                    .COLUMN_ENTITY_CLASS_NAME));
            int success = cur.getInt(cur.getColumnIndex(ApiTransactionTable.COLUMN_SUCCESS));
            String foodlogiqId = cur.getString(cur.getColumnIndex(ApiTransactionTable
                    .COLUMN_FOODLOGIQ_ID));
            String subOwnerId = cur.getString(cur.getColumnIndex(ApiTransactionTable
                    .COLUMN_SUB_OWNER));
            String businessId = cur.getString(cur.getColumnIndex(ApiTransactionTable
                    .COLUMN_BUSINESS_OWNER));

            apiTransaction = new ApiTransaction(entityType, entitySubType, entityClass, dateString,
                    jsonString,
                    businessId, subOwnerId, success == 1, id);
            apiTransaction.setPhotoPaths(photoPathsString);
            cur.close();
        }
    }

    public ApiTransaction getApiTransaction() {
        return apiTransaction;
    }
}
