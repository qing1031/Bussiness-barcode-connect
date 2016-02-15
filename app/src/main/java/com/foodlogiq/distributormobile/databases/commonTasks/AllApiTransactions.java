package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.ApiTransactionContentProvider;
import com.foodlogiq.distributormobile.databases.tables.ApiTransactionTable;
import com.foodlogiq.distributormobile.entityClasses.ApiTransaction;

import java.util.ArrayList;

/**
 * Returns all of a business's api transactions with matching entity type from the database
 */
public class AllApiTransactions {
    private final ArrayList<ApiTransaction> apiTransactions = new ArrayList<>();

    public AllApiTransactions(ContentResolver contentResolver, String entityType, String
            entitySubType, String businessId, String subOwnerId) {
        String queryString = ApiTransactionTable.COLUMN_BUSINESS_OWNER + " = ? AND " +
                ApiTransactionTable
                        .COLUMN_SUB_OWNER + " = ? AND " + ApiTransactionTable.COLUMN_ENTITY_TYPE
                + " = ?";
        String[] queryArgs = new String[]{businessId, subOwnerId, entityType};
        if (entitySubType != null && !entitySubType.isEmpty()) {
            queryString = queryString.concat(" AND " + ApiTransactionTable.COLUMN_ENTITY_SUB_TYPE
                    + " = ?");
            queryArgs = new String[]{businessId, subOwnerId, entityType, entitySubType};
        }

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
                queryString,
                queryArgs,
                ApiTransactionTable.COLUMN_DATE + " DESC",
                null
        );
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String id = cur.getString(cur.getColumnIndex(ApiTransactionTable.COLUMN_ID));
                String jsonString = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_JSON));
                String photoPathsString = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_PHOTO_PATHS));
                String dateString = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_DATE));
                String _entityType = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_ENTITY_TYPE));
                String _entitySubType = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_ENTITY_SUB_TYPE));
                String entityClass = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_ENTITY_CLASS_NAME));
                int success = cur.getInt(cur.getColumnIndex(ApiTransactionTable.COLUMN_SUCCESS));
                String foodlogiqId = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_FOODLOGIQ_ID));
                subOwnerId = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_SUB_OWNER));
                businessId = cur.getString(cur.getColumnIndex(ApiTransactionTable
                        .COLUMN_BUSINESS_OWNER));

                ApiTransaction apiTransaction = new ApiTransaction(_entityType, _entitySubType,
                        entityClass,
                        dateString, jsonString, businessId, subOwnerId, success == 1, id);
                apiTransaction.setPhotoPaths(photoPathsString);
                apiTransactions.add(apiTransaction);
                cur.moveToNext();
            }
            cur.close();
        }
        //Sort them by date.
    }

    public ArrayList<ApiTransaction> getApiTransactions() {
        return apiTransactions;
    }

    public int getApiTransactionCount() {
        return apiTransactions.size();
    }
}
