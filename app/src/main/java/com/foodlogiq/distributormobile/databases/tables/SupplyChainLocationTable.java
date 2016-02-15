package com.foodlogiq.distributormobile.databases.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database definition for
 * {@link com.foodlogiq.distributormobile.entityClasses.Location.SupplyChainLocation}
 */
public class SupplyChainLocationTable {
    public static final String TABLE_SUPPLY_CHAIN_LOCATION = "supply_chain_location";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_GLOBAL_LOCATION_NUMBER = "gln";
    public static final String COLUMN_SUPPLIER_ID = "supplier_id";
    public static final String COLUMN_FOODLOGIQ_ID = "foodlogiqid";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_SUPPLY_CHAIN_LOCATION
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text, "
            + COLUMN_GLOBAL_LOCATION_NUMBER + " text, "
            + COLUMN_SUPPLIER_ID + " text, "
            + COLUMN_FOODLOGIQ_ID + " text"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPLY_CHAIN_LOCATION);
        onCreate(db);
    }
}
