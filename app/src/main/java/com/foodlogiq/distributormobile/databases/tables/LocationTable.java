package com.foodlogiq.distributormobile.databases.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database definition for {@link com.foodlogiq.distributormobile.entityClasses.Location}
 */
public class LocationTable {
    public static final String TABLE_LOCATION = "location";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_STREET_ADDRESS = "street_address";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_REGION = "region";
    public static final String COLUMN_POSTAL_CODE = "postal_code";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_GLOBAL_LOCATION_NUMBER = "gln";
    public static final String COLUMN_GLOBAL_LOCATION__EXTENSION = "gln_ext";
    public static final String COLUMN_FOODLOGIQ_ID = "foodlogiqid";
    public static final String COLUMN_BUSINESS_ID = "business_id";
    public static final String COLUMN_INTERNAL_ID = "internal_id";
    public static final String COLUMN_COMMUNITY_ID = "community_id";
    public static final String COLUMN_SUPPLY_CHAIN_LOCATION_IDS = "supply_chain_location_ids";
    public static final String COLUMN_SUPPLIES_IDS = "supplies_ids";
    public static final String COLUMN_SUPPLY_CHAIN_SUPPLIER_IDS = "supply_chain_supplier_ids";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_LOCATION
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text, "
            + COLUMN_STREET_ADDRESS + " text, "
            + COLUMN_CITY + " text, "
            + COLUMN_REGION + " text, "
            + COLUMN_POSTAL_CODE + " text, "
            + COLUMN_COUNTRY + " text, "
            + COLUMN_PHONE + " text, "
            + COLUMN_DESCRIPTION + " text, "
            + COLUMN_GLOBAL_LOCATION_NUMBER + " text, "
            + COLUMN_GLOBAL_LOCATION__EXTENSION + " text, "
            + COLUMN_FOODLOGIQ_ID + " text, "
            + COLUMN_BUSINESS_ID + " text, "
            + COLUMN_INTERNAL_ID + " text, "
            + COLUMN_COMMUNITY_ID + " text, "
            + COLUMN_SUPPLIES_IDS + " text, "
            + COLUMN_SUPPLY_CHAIN_LOCATION_IDS + " text, "
            + COLUMN_SUPPLY_CHAIN_SUPPLIER_IDS + " text"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE location ADD COLUMN supplies_ids TEXT " +
                            "DEFAULT ''");
                    //Any event api transaction at this point would only be receiving events
                    db.execSQL("UPDATE location SET supplies_ids = ''");
                case 2:
                    //introduce db updates for db versions below version 3
                default:
            }
        }
    }
}
