package com.foodlogiq.distributormobile.databases.commonTasks;

import android.content.ContentResolver;
import android.database.Cursor;

import com.foodlogiq.distributormobile.databases.contentProviders.EventTypeContentProvider;
import com.foodlogiq.distributormobile.databases.tables.EventTypeTable;
import com.foodlogiq.distributormobile.entityClasses.CustomAttribute;
import com.foodlogiq.distributormobile.entityClasses.EventType;

import java.util.ArrayList;

/**
 * Created by djak250 on 1/19/16.
 */
public class AllEventTypes {
    private ArrayList<EventType> eventTypes = new ArrayList<>();

    public AllEventTypes(ContentResolver contentResolver, String businessId, String
            eventTypeFilter) {
        Cursor cur = contentResolver.query(
                EventTypeContentProvider.CONTENT_URI,
                new String[]{
                        EventTypeTable.COLUMN_ID,
                        EventTypeTable.COLUMN_NAME,
                        EventTypeTable.COLUMN_ASSOCIATE_WITH,
                        EventTypeTable.COLUMN_BUSINESS_ID,
                        EventTypeTable.COLUMN_FOODLOGIQ_ID
                },
                EventTypeTable.COLUMN_BUSINESS_ID + " = ? AND " + EventTypeTable
                        .COLUMN_ASSOCIATE_WITH + " = ?",
                new String[]{businessId, eventTypeFilter},
                null,
                null
        );
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String _id = cur.getString(cur.getColumnIndex(EventTypeTable
                        .COLUMN_ID));
                String foodlogiqId = cur.getString(cur.getColumnIndex(EventTypeTable
                        .COLUMN_FOODLOGIQ_ID));
                String _businessId = cur.getString(cur.getColumnIndex(EventTypeTable
                        .COLUMN_BUSINESS_ID));
                String name = cur.getString(cur.getColumnIndex(EventTypeTable
                        .COLUMN_NAME));
                String associateWith = cur.getString(cur.getColumnIndex(EventTypeTable
                        .COLUMN_ASSOCIATE_WITH));
                ArrayList<CustomAttribute> customAttributes = new FindCustomAttributes
                        (contentResolver, foodlogiqId).getCustomAttributes();
                EventType eventType = new EventType(_id, foodlogiqId, _businessId, name,
                        associateWith, customAttributes);
                eventTypes.add(eventType);
                cur.moveToNext();
            }
            cur.close();
        }

    }

    public ArrayList<EventType> getEventTypes() {
        return this.eventTypes;
    }

}
