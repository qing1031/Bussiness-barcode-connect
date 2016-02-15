package com.foodlogiq.distributormobile.databases.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database definition for {@link com.foodlogiq.distributormobile.entityClasses.Business}
 */
public class BusinessTable {
    public static final String TABLE_BUSINESS = "business";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICON = "icon_url";
    public static final String COLUMN_FOODLOGIQ_ID = "foodlogiqid";
    public static final String COLUMN_BUSINESS_OWNER = "business_owner";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_BUSINESS
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text, "
            + COLUMN_ICON + " text, "
            + COLUMN_FOODLOGIQ_ID + " text, "
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
