package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.LocationContentProvider;
import com.foodlogiq.distributormobile.databases.tables.LocationTable;
import com.foodlogiq.distributormobile.entityClasses.Location;

import java.util.ArrayList;

/**
 * Return location with the matching sqlite ID.
 * Also involves finding all of the locations suppliers and distributors.
 */
public class FindLocation {
    private Location location;

    public FindLocation(ContentResolver contentResolver, String sql_id) {
        Cursor cur = contentResolver.query(
                LocationContentProvider.CONTENT_URI,
                new String[]{
                        LocationTable.COLUMN_ID,
                        LocationTable.COLUMN_DESCRIPTION,
                        LocationTable.COLUMN_NAME,
                        LocationTable.COLUMN_STREET_ADDRESS,
                        LocationTable.COLUMN_CITY,
                        LocationTable.COLUMN_REGION,
                        LocationTable.COLUMN_POSTAL_CODE,
                        LocationTable.COLUMN_COUNTRY,
                        LocationTable.COLUMN_PHONE,
                        LocationTable.COLUMN_GLOBAL_LOCATION_NUMBER,
                        LocationTable.COLUMN_GLOBAL_LOCATION__EXTENSION,
                        LocationTable.COLUMN_FOODLOGIQ_ID,
                        LocationTable.COLUMN_BUSINESS_ID,
                        LocationTable.COLUMN_INTERNAL_ID,
                        LocationTable.COLUMN_COMMUNITY_ID,
                        LocationTable.COLUMN_SUPPLY_CHAIN_LOCATION_IDS,
                        LocationTable.COLUMN_SUPPLY_CHAIN_SUPPLIER_IDS,
                        LocationTable.COLUMN_SUPPLIES_IDS
                },
                LocationTable.COLUMN_ID + " = ?",
                new String[]{sql_id},
                null,
                null
        );
        if (cur != null && cur.getCount() > 0) {
            cur.moveToFirst();
            String foodlogiqId = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_FOODLOGIQ_ID));
            String internalId = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_INTERNAL_ID));
            String _businessId = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_BUSINESS_ID));
            String communityId = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_COMMUNITY_ID));
            String globalLocationNumber = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_GLOBAL_LOCATION_NUMBER));
            String globalLocationNumberExtension = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_GLOBAL_LOCATION__EXTENSION));
            String name = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_NAME));
            String streetAddress = cur.getString(cur.getColumnIndexOrThrow(LocationTable
                    .COLUMN_STREET_ADDRESS));
            String city = cur.getString(cur.getColumnIndexOrThrow(LocationTable.COLUMN_CITY));
            String region = cur.getString(cur.getColumnIndexOrThrow(LocationTable.COLUMN_REGION));
            String postalCode = cur.getString(cur.getColumnIndexOrThrow(LocationTable
                    .COLUMN_POSTAL_CODE));
            String country = cur.getString(cur.getColumnIndexOrThrow(LocationTable.COLUMN_COUNTRY));
            String phone = cur.getString(cur.getColumnIndexOrThrow(LocationTable.COLUMN_PHONE));
            String description = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_DESCRIPTION));
            String supplyChainLocationIds = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_SUPPLY_CHAIN_LOCATION_IDS));
            String supplyChainSupplierIds = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_SUPPLY_CHAIN_SUPPLIER_IDS));
            String suppliesIds = cur.getString(cur.getColumnIndex(LocationTable
                    .COLUMN_SUPPLIES_IDS));

            String id = cur.getString(cur.getColumnIndex(LocationTable.COLUMN_ID));

            ArrayList<Location.SupplyChainLocation> supplyChainLocations = new ArrayList<>();
            String[] supplyChainLocationIdsArray = supplyChainLocationIds.split("\\|");
            for (int i = 0; i < supplyChainLocationIdsArray.length; i++) {
                if (!supplyChainLocationIdsArray[i].isEmpty()) {
                    FindSupplyChainLocation foundSupplyChainLocation = new
                            FindSupplyChainLocation(contentResolver,
                            supplyChainLocationIdsArray[i]);
                    supplyChainLocations.add(foundSupplyChainLocation.getSupplyChainLocation());
                }
            }
            ArrayList<Location.Supplier> supplyChainSuppliers = new ArrayList<>();
            String[] supplyChainSupplierIdsArray = supplyChainSupplierIds.split("\\|");
            for (int i = 0; i < supplyChainSupplierIdsArray.length; i++) {
                if (!supplyChainSupplierIdsArray[i].isEmpty()) {
                    FindSupplier foundSupplier = new FindSupplier(contentResolver,
                            supplyChainSupplierIdsArray[i]);
                    supplyChainSuppliers.add(foundSupplier.getSupplier());
                }
            }

            ArrayList<Location> suppliesLocations = new ArrayList<>();
            String[] suppliesIdsArray = suppliesIds.split("\\|");
            for (int i = 0; i < suppliesIdsArray.length; i++) {
                if (!suppliesIdsArray[i].isEmpty()) {
                    FindLocation foundLocation = new FindLocation(contentResolver,
                            suppliesIdsArray[i]);
                    suppliesLocations.add(foundLocation.getLocation());
                }
            }

            this.location = new Location(
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
                    suppliesLocations,
                    id
            );
            cur.close();
        }
    }

    public Location getLocation() {
        return location;
    }
}
