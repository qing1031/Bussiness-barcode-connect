package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.SupplyChainLocationContentProvider;
import com.foodlogiq.distributormobile.databases.tables.SupplyChainLocationTable;
import com.foodlogiq.distributormobile.entityClasses.Location;

/**
 * Return supply chain location with the matching sqlite ID.
 */
public class FindSupplyChainLocation {
    private Location.SupplyChainLocation supplyChainLocation;

    public FindSupplyChainLocation(ContentResolver contentResolver, String sql_id) {
        Cursor cur = contentResolver.query(
                SupplyChainLocationContentProvider.CONTENT_URI,
                new String[]{
                        SupplyChainLocationTable.COLUMN_ID,
                        SupplyChainLocationTable.COLUMN_NAME,
                        SupplyChainLocationTable.COLUMN_GLOBAL_LOCATION_NUMBER,
                        SupplyChainLocationTable.COLUMN_SUPPLIER_ID,
                        SupplyChainLocationTable.COLUMN_FOODLOGIQ_ID,
                },
                SupplyChainLocationTable.COLUMN_ID + " = ?",
                new String[]{sql_id},
                null,
                null
        );
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            String foodlogiqId = cur.getString(cur.getColumnIndex(SupplyChainLocationTable
                    .COLUMN_FOODLOGIQ_ID));
            String globalLocationNumber = cur.getString(cur.getColumnIndex
                    (SupplyChainLocationTable.COLUMN_GLOBAL_LOCATION_NUMBER));
            String supplierId = cur.getString(cur.getColumnIndex(SupplyChainLocationTable
                    .COLUMN_SUPPLIER_ID));
            String name = cur.getString(cur.getColumnIndex(SupplyChainLocationTable.COLUMN_NAME));
            String id = cur.getString(cur.getColumnIndex(SupplyChainLocationTable.COLUMN_ID));

            this.supplyChainLocation = new Location.SupplyChainLocation(
                    foodlogiqId,
                    globalLocationNumber,
                    name,
                    supplierId,
                    id
            );
            cur.close();
        }
    }

    public Location.SupplyChainLocation getSupplyChainLocation() {
        return supplyChainLocation;
    }
}
