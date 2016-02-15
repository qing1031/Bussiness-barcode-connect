package com.foodlogiq.distributormobile.databases.contentProviders;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.foodlogiq.distributormobile.databases.helpers.SupplyChainLocationDatabaseHelper;
import com.foodlogiq.distributormobile.databases.tables.SupplyChainLocationTable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Content provider to access the
 * {@link com.foodlogiq.distributormobile.entityClasses.Location.SupplyChainLocation} database
 */
public class SupplyChainLocationContentProvider extends ContentProvider {
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/todos";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/todo";
    private static final int TODOS = 10;
    private static final int TODO_ID = 20;
    private static final String AUTHORITY = "com.foodlogiq.distributormobile" +
            ".supplychainlocationcontentprovider";
    private static final String BASE_PATH = "supply_chain_location";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
    }

    private SupplyChainLocationDatabaseHelper database;

    @Override
    public boolean onCreate() {
        database = new SupplyChainLocationDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TODOS:
                break;
            case TODO_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(SupplyChainLocationTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case TODOS:
                id = sqlDB.insert(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION, null,
                        values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case TODOS:
                rowsDeleted = sqlDB.delete(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION,
                        selection,
                        selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION,
                            SupplyChainLocationTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION,
                            SupplyChainLocationTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs
                    );
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case TODOS:
                rowsUpdated = sqlDB.update(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION,
                            values,
                            SupplyChainLocationTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(SupplyChainLocationTable.TABLE_SUPPLY_CHAIN_LOCATION,
                            values,
                            SupplyChainLocationTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs
                    );
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                SupplyChainLocationTable.COLUMN_NAME,
                SupplyChainLocationTable.COLUMN_GLOBAL_LOCATION_NUMBER,
                SupplyChainLocationTable.COLUMN_FOODLOGIQ_ID,
                SupplyChainLocationTable.COLUMN_SUPPLIER_ID,
                SupplyChainLocationTable.COLUMN_ID,
        };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
