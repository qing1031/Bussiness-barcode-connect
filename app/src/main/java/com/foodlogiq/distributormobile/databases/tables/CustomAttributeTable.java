package com.foodlogiq.distributormobile.databases.tables;

import android.database.sqlite.SQLiteDatabase;

import com.foodlogiq.distributormobile.entityClasses.CustomAttribute;

/**
 * Database definition for
 * {@link CustomAttribute}
 */
public class CustomAttributeTable {
    public static final String TABLE_CUSTOM_ATTRIBUTE = "custom_attribute";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PARENT_TYPE_ID = "parent_type_id";
    public static final String COLUMN_COMMON_NAME = "common_name";
    public static final String COLUMN_STORED_AS = "stored_as";
    public static final String COLUMN_FIELD_TYPE = "field_type";
    public static final String COLUMN_OPTIONS = "options";
    public static final String COLUMN_REQUIRED = "required";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_CUSTOM_ATTRIBUTE
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_PARENT_TYPE_ID + " text, "
            + COLUMN_COMMON_NAME + " text, "
            + COLUMN_STORED_AS + " text, "
            + COLUMN_FIELD_TYPE + " text, "
            + COLUMN_OPTIONS + " text DEFAULT '', "
            + COLUMN_REQUIRED + " text"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOM_ATTRIBUTE);
        onCreate(db);
    }
}
