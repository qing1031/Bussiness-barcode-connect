package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.CustomAttributeContentProvider;
import com.foodlogiq.distributormobile.databases.tables.CustomAttributeTable;
import com.foodlogiq.distributormobile.entityClasses.CustomAttribute;

import java.util.ArrayList;

/**
 * Created by djak250 on 1/21/16.
 */
class FindCustomAttributes {
    private ArrayList<CustomAttribute> customAttributes = new ArrayList<>();

    public FindCustomAttributes(ContentResolver contentResolver, String foodlogiqId) {
        Cursor cur = contentResolver.query(
                CustomAttributeContentProvider.CONTENT_URI,
                new String[]{
                        CustomAttributeTable.COLUMN_ID,
                        CustomAttributeTable.COLUMN_COMMON_NAME,
                        CustomAttributeTable.COLUMN_STORED_AS,
                        CustomAttributeTable.COLUMN_FIELD_TYPE,
                        CustomAttributeTable.COLUMN_OPTIONS,
                        CustomAttributeTable.COLUMN_REQUIRED
                },
                CustomAttributeTable.COLUMN_PARENT_TYPE_ID + " = ?",
                new String[]{foodlogiqId},
                null,
                null
        );
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String _id = cur.getString(cur.getColumnIndex(CustomAttributeTable
                        .COLUMN_ID));
                String commonName = cur.getString(cur.getColumnIndex(CustomAttributeTable
                        .COLUMN_COMMON_NAME));
                String storedAs = cur.getString(cur.getColumnIndex(CustomAttributeTable
                        .COLUMN_STORED_AS));
                String fieldType = cur.getString(cur.getColumnIndex(CustomAttributeTable
                        .COLUMN_FIELD_TYPE));
                Boolean required = Boolean.valueOf(cur.getString(cur.getColumnIndex
                        (CustomAttributeTable.COLUMN_REQUIRED)));
                ArrayList<String> options = new ArrayList<>();
                String pipeSeparatedOptions = cur.getString(cur.getColumnIndex(CustomAttributeTable
                        .COLUMN_OPTIONS));
                if (!pipeSeparatedOptions.isEmpty()) {
                    for (String option : pipeSeparatedOptions.split("\\|")) {
                        if (!option.isEmpty()) {
                            options.add(option);
                        }
                    }
                }
                CustomAttribute customAttribute = new CustomAttribute(_id, commonName, storedAs,
                        fieldType, required, options);
                customAttributes.add(customAttribute);
                cur.moveToNext();
            }
            cur.close();
        }
    }

    public ArrayList<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }
}
