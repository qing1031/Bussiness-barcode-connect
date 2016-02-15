package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;

import com.foodlogiq.distributormobile.databases.contentProviders.LocationContentProvider;
import com.foodlogiq.distributormobile.databases.tables.LocationTable;


/**
 * Remove locations for the current businessId.
 */
public class DropLocations {

    private final int rowsDeleted;

    public DropLocations(ContentResolver contentResolver, String businessId) {
        this.rowsDeleted = contentResolver.delete(
                LocationContentProvider.CONTENT_URI,
                LocationTable.COLUMN_BUSINESS_ID + " = ?",
                new String[]{businessId}
        );
    }

    public DropLocations(ContentResolver contentResolver) {
        this.rowsDeleted = contentResolver.delete(
                LocationContentProvider.CONTENT_URI,
                null,
                null
        );
    }

    public int getRowsDeleted() {
        return rowsDeleted;
    }
}
