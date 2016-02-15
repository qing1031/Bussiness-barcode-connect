package com.foodlogiq.distributormobile.databases.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.foodlogiq.distributormobile.databases.tables.CustomAttributeTable;
import com.foodlogiq.distributormobile.entityClasses.CustomAttribute;

/**
 * Database helper to for
 * {@link CustomAttribute} database.
 * Any upgrades to the database version that require updates to the DB can be done in
 * {@link #onUpgrade(SQLiteDatabase, int, int)}
 */
public class CustomAttributeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "custom_attribute.db";
    private static final int DATABASE_VERSION = 1;

    public CustomAttributeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        CustomAttributeTable.onCreate(sqLiteDatabase);
    }

    /**
     * Handle migration of data into newly formatted tables on update
     *
     * @param i  old version
     * @param i2 new version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        CustomAttributeTable.onUpgrade(sqLiteDatabase, i, i2);
    }
}
