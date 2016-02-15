package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.CustomAttributeContentProvider;
import com.foodlogiq.distributormobile.databases.contentProviders.EventTypeContentProvider;
import com.foodlogiq.distributormobile.databases.tables.EventTypeTable;

import java.util.ArrayList;

/**
 * Created by djak250 on 1/21/16.
 */
public class DropEventTypes {
    public DropEventTypes(ContentResolver contentResolver, String businessId, String eventType) {
        Cursor droppableEventTypes;
        if (eventType.equals("all")) {
            droppableEventTypes = contentResolver.query(
                    EventTypeContentProvider.CONTENT_URI,
                    new String[]{
                            EventTypeTable.COLUMN_FOODLOGIQ_ID
                    },
                    EventTypeTable.COLUMN_BUSINESS_ID + " = ?",
                    new String[]{businessId},
                    null,
                    null
            );
        } else {
            droppableEventTypes = contentResolver.query(
                    EventTypeContentProvider.CONTENT_URI,
                    new String[]{
                            EventTypeTable.COLUMN_FOODLOGIQ_ID
                    },
                    EventTypeTable.COLUMN_BUSINESS_ID + " = ? AND " + EventTypeTable
                            .COLUMN_ASSOCIATE_WITH + " = ?",
                    new String[]{businessId, eventType},
                    null,
                    null
            );
        }
        ArrayList<String> eventTypesIdsArrayList = new ArrayList<>();
        if (droppableEventTypes != null && droppableEventTypes.getCount() != 0) {
            droppableEventTypes.moveToFirst();
            while (!droppableEventTypes.isAfterLast()) {

                String foodlogiqId = droppableEventTypes.getString(droppableEventTypes
                        .getColumnIndex(EventTypeTable
                                .COLUMN_FOODLOGIQ_ID));
                eventTypesIdsArrayList.add(foodlogiqId);
                droppableEventTypes
                        .moveToNext();
            }
            droppableEventTypes.close();
        }
        String[] idsArray = eventTypesIdsArrayList.toArray(new String[eventTypesIdsArrayList.size
                ()]);
        StringBuilder inList = new StringBuilder(idsArray.length * 2);
        for (int i = 0; i < idsArray.length; i++) {
            if (i > 0) inList.append(",");
            inList.append("?");
        }
        int v = contentResolver.delete(
                CustomAttributeContentProvider.CONTENT_URI,
                "parent_type_id IN (" + inList.toString() + ")", idsArray
        );

        if (eventType.equals("all")) {
            contentResolver.delete(
                    EventTypeContentProvider.CONTENT_URI,
                    EventTypeTable.COLUMN_BUSINESS_ID + " = ?",
                    new String[]{businessId}
            );
        } else {
            contentResolver.delete(
                    EventTypeContentProvider.CONTENT_URI,
                    EventTypeTable.COLUMN_BUSINESS_ID + " = ? AND " + EventTypeTable
                            .COLUMN_ASSOCIATE_WITH + " = ?",
                    new String[]{businessId, eventType}
            );
        }
    }
}
