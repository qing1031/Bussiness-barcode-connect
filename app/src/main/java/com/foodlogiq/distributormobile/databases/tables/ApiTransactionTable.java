package com.foodlogiq.distributormobile.databases.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database definition for {@link com.foodlogiq.distributormobile.entityClasses.ApiTransaction}
 */
public class ApiTransactionTable {
    public static final String TABLE_API_TRANSACTIONS = "api_transactions";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_JSON = "json";
    public static final String COLUMN_PHOTO_PATHS = "photo_paths";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_ENTITY_TYPE = "entity_type";
    public static final String COLUMN_ENTITY_SUB_TYPE = "entity_sub_type";
    public static final String COLUMN_ENTITY_CLASS_NAME = "entity_class_name";
    public static final String COLUMN_SUCCESS = "success";
    public static final String COLUMN_FOODLOGIQ_ID = "foodlogiqid";
    public static final String COLUMN_SUB_OWNER = "sub_owner";
    public static final String COLUMN_BUSINESS_OWNER = "business_owner";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_API_TRANSACTIONS
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_JSON + " text, "
            + COLUMN_PHOTO_PATHS + " text, "
            + COLUMN_DATE + " text, " // iso. Use the isoFormatter to projecting to string and
            // parsing
            + COLUMN_ENTITY_TYPE + " text, "
            + COLUMN_ENTITY_SUB_TYPE + " text, "
            + COLUMN_ENTITY_CLASS_NAME + " text, "
            + COLUMN_SUCCESS + " int, " //0=false,1=true
            + COLUMN_FOODLOGIQ_ID + " text, "
            + COLUMN_SUB_OWNER + " text, "
            + COLUMN_BUSINESS_OWNER + " text"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE api_transactions ADD COLUMN entity_sub_type TEXT " +
                            "DEFAULT ''");
                    //Any event api transaction at this point would only be receiving events
                    db.execSQL("UPDATE api_transactions SET entity_sub_type = 'receiving' WHERE " +
                            "entity_type = 'events'");
                case 2:
                    //introduce db updates for db versions below version 3
                default:
            }
        }
    }
}
