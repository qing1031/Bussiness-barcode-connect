package com.foodlogiq.distributormobile.databases.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database definition for {@link com.foodlogiq.distributormobile.entityClasses.Location.Supplier}
 */
public class SupplierTable {
    public static final String TABLE_SUPPLIER = "suppliers";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOODLOGIQ_ID = "foodlogiqid";
    public static final String COLUMN_BUSINESS_JSON = "business_json";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_SUPPLIER
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_FOODLOGIQ_ID + " text, "
            + COLUMN_BUSINESS_JSON + " text"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPLIER);
        onCreate(db);
    }
}
