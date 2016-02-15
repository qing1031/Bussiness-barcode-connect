package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.SupplierContentProvider;
import com.foodlogiq.distributormobile.databases.tables.SupplierTable;
import com.foodlogiq.distributormobile.entityClasses.Business;
import com.foodlogiq.distributormobile.entityClasses.Location;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Return supplier with the matching sqlite ID.
 */
public class FindSupplier {
    private Location.Supplier supplier;

    public FindSupplier(ContentResolver contentResolver, String sql_id) {
        Cursor cur = contentResolver.query(
                SupplierContentProvider.CONTENT_URI,
                new String[]{
                        SupplierTable.COLUMN_ID,
                        SupplierTable.COLUMN_BUSINESS_JSON,
                        SupplierTable.COLUMN_FOODLOGIQ_ID,
                },
                SupplierTable.COLUMN_ID + " = ?",
                new String[]{sql_id},
                null,
                null
        );
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            String foodlogiqId = cur.getString(cur.getColumnIndex(SupplierTable
                    .COLUMN_FOODLOGIQ_ID));
            Business business = new Business();
            try {
                business.parseJSON(new JSONObject(cur.getString(cur.getColumnIndex(SupplierTable
                        .COLUMN_BUSINESS_JSON))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String id = cur.getString(cur.getColumnIndex(SupplierTable.COLUMN_ID));

            this.supplier = new Location.Supplier(
                    foodlogiqId,
                    business,
                    id
            );
            cur.close();
        }
    }

    public Location.Supplier getSupplier() {
        return supplier;
    }
}
