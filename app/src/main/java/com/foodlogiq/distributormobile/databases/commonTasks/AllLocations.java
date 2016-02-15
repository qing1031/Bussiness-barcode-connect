package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.LocationContentProvider;
import com.foodlogiq.distributormobile.databases.tables.LocationTable;
import com.foodlogiq.distributormobile.entityClasses.Location;

import java.util.ArrayList;

/**
 * Returns all of a business's locations from the database.
 */
public class AllLocations {
    private final ArrayList<Location> locations = new ArrayList<>();

    public AllLocations(ContentResolver contentResolver, String businessId) {
        Cursor cur = contentResolver.query(
                LocationContentProvider.CONTENT_URI,
                new String[]{
                        LocationTable.COLUMN_FOODLOGIQ_ID,
                        LocationTable.COLUMN_BUSINESS_ID,
                        LocationTable.COLUMN_INTERNAL_ID,
                        LocationTable.COLUMN_GLOBAL_LOCATION_NUMBER,
                        LocationTable.COLUMN_GLOBAL_LOCATION__EXTENSION,
                        LocationTable.COLUMN_NAME,
                        LocationTable.COLUMN_STREET_ADDRESS,
                        LocationTable.COLUMN_CITY,
                        LocationTable.COLUMN_REGION,
                        LocationTable.COLUMN_POSTAL_CODE,
                        LocationTable.COLUMN_COUNTRY,
                        LocationTable.COLUMN_PHONE,
                        LocationTable.COLUMN_DESCRIPTION,
                        LocationTable.COLUMN_COMMUNITY_ID,
                        LocationTable.COLUMN_SUPPLY_CHAIN_LOCATION_IDS,
                        LocationTable.COLUMN_SUPPLY_CHAIN_SUPPLIER_IDS,
                        LocationTable.COLUMN_ID
                },
                LocationTable.COLUMN_BUSINESS_ID + " = ?",
                new String[]{businessId},
                null,
                null
        );
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String foodlogiqId = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_FOODLOGIQ_ID));
                String internalId = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_INTERNAL_ID));
                String _businessId = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_BUSINESS_ID));
                String globalLocationNumber = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_GLOBAL_LOCATION_NUMBER));
                String globalLocationNumberExtension = cur.getString(cur.getColumnIndex
                        (LocationTable.COLUMN_GLOBAL_LOCATION__EXTENSION));
                String name = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_NAME));
                String streetAddress = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_STREET_ADDRESS));
                String city = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_CITY));
                String region = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_REGION));
                String postalCode = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_POSTAL_CODE));
                String country = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_COUNTRY));
                String phone = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_PHONE));
                String description = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_DESCRIPTION));
                String communityId = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_COMMUNITY_ID));
                String supplyChainLocationIds = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_SUPPLY_CHAIN_LOCATION_IDS));
                String supplyChainSupplierIds = cur.getString(cur.getColumnIndex(LocationTable
                        .COLUMN_SUPPLY_CHAIN_SUPPLIER_IDS));
                String id = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_ID));

                ArrayList<Location.SupplyChainLocation> supplyChainLocations = new ArrayList<>();
                for (String supplyChainLocationId : supplyChainLocationIds.split("\\|")) {
                    if (!supplyChainLocationId.isEmpty()) {
                        FindSupplyChainLocation foundSupplyChainLocation = new
                                FindSupplyChainLocation(contentResolver, supplyChainLocationId);
                        supplyChainLocations.add(foundSupplyChainLocation.getSupplyChainLocation());
                    }
                }
                ArrayList<Location.Supplier> supplyChainSuppliers = new ArrayList<>();
                for (String supplierId : supplyChainSupplierIds.split("\\|")) {
                    if (!supplierId.isEmpty()) {
                        FindSupplier foundSupplier = new FindSupplier(contentResolver, supplierId);
                        supplyChainSuppliers.add(foundSupplier.getSupplier());
                    }
                }
                Location location = new Location(
                        foodlogiqId,
                        _businessId,
                        internalId,
                        globalLocationNumber,
                        globalLocationNumberExtension,
                        name,
                        streetAddress,
                        city,
                        region,
                        postalCode,
                        country,
                        phone,
                        description,
                        communityId,
                        supplyChainLocations,
                        supplyChainSuppliers,
                        null,
                        id
                );
                locations.add(location);
                cur.moveToNext();
            }
            cur.close();
        }


    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public int getLocationCount() {
        return locations.size();
    }
}
