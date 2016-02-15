package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;

import com.foodlogiq.distributormobile.databases.contentProviders.BusinessContentProvider;
import com.foodlogiq.distributormobile.databases.tables.BusinessTable;

/**
 * Remove businesses for the current mobileAccessToken.
 */
public class DropBusinesses {

    private final int rowsDeleted;

    public DropBusinesses(ContentResolver contentResolver, String mobileAccessToken) {
        this.rowsDeleted = contentResolver.delete(
                BusinessContentProvider.CONTENT_URI,
                BusinessTable.COLUMN_BUSINESS_OWNER + " = ?",
                new String[]{mobileAccessToken}
        );
    }

    public int getRowsDeleted() {
        return rowsDeleted;
    }
}
