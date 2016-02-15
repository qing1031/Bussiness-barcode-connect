package com.foodlogiq.distributormobile.databases.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database definition for {@link com.foodlogiq.distributormobile.entityClasses.EventType}
 */
public class EventTypeTable {
    public static final String TABLE_EVENT_TYPE = "event_type";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FOODLOGIQ_ID = "foodlogiqid";
    public static final String COLUMN_BUSINESS_ID = "business_id";
    public static final String COLUMN_ASSOCIATE_WITH = "associate_with";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_EVENT_TYPE
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text, "
            + COLUMN_ASSOCIATE_WITH + " text, "
            + COLUMN_BUSINESS_ID + " text, "
            + COLUMN_FOODLOGIQ_ID + " text"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_TYPE);
        onCreate(db);
    }
}
