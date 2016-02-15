package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.BusinessContentProvider;
import com.foodlogiq.distributormobile.databases.tables.BusinessTable;
import com.foodlogiq.distributormobile.entityClasses.Business;

import java.util.ArrayList;

/**
 * Returns all of a businesses for the current mobileAccessToken from the database
 */
public class AllBusinesses {
    private final ArrayList<Business> businesses = new ArrayList<>();

    public AllBusinesses(ContentResolver contentResolver, String mobileAccessToken) {
        String[] projection = new String[]{
                BusinessTable.COLUMN_FOODLOGIQ_ID,
                BusinessTable.COLUMN_NAME,
                BusinessTable.COLUMN_ICON,
                BusinessTable.COLUMN_ID
        };
        Cursor cur = contentResolver.query(
                BusinessContentProvider.CONTENT_URI,
                projection,
                BusinessTable.COLUMN_BUSINESS_OWNER + " = ?",
                new String[]{mobileAccessToken},
                null,
                null
        );
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String foodlogiqId = cur.getString(cur.getColumnIndex(BusinessTable
                        .COLUMN_FOODLOGIQ_ID));
                String name = cur.getString(cur.getColumnIndex(BusinessTable.COLUMN_NAME));
                String iconUrl = cur.getString(cur.getColumnIndex(BusinessTable.COLUMN_ICON));
                String id = cur.getString(cur.getColumnIndex(BusinessTable.COLUMN_ID));
                Business businessObject = new Business(foodlogiqId, name, iconUrl, id);
                businesses.add(businessObject);
                cur.moveToNext();
            }
            cur.close();
        }
    }

    public ArrayList<Business> getBusinesses() {
        return businesses;
    }

    public int getBusinessesCount() {
        return businesses.size();
    }
}
