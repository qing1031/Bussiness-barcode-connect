package com.foodlogiq.distributormobile.viewAdapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.entityClasses.ApiTransaction;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;
import com.foodlogiq.flqassets.FLQActivity;

import java.util.ArrayList;
import java.util.Date;

/**
 * Lists all the transactions made to the API, whether or not they succeeded.
 * Failed transactions can be reattempted, by opening the activity and restoring it with the former
 * data from the attempted transaction.
 */
public class ApiTransactionListAdapter extends ArrayAdapter<ApiTransaction> {
    public static final int RESYNC_ACTIVITY = 1;
    private final FLQActivity activity;
    private final ListView listView;
    private final ArrayList<ApiTransaction> values;
    private Class<?> resyncActivity;

    public ApiTransactionListAdapter(
            final FLQActivity activity,
            final ArrayList<ApiTransaction> values,
            ListView listView,
            Class<?> resyncActivity
    ) {
        super(activity, R.layout.list_item_api_transaction, values);
        this.activity = activity;
        this.listView = listView;
        this.values = values;
        this.resyncActivity = resyncActivity;
        listView.setAdapter(this);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ApiTransaction apiTransaction = values.get(position);

        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.list_item_api_transaction, parent, false);

        //Gather values
        Date date = apiTransaction.getDate();
        boolean successful = apiTransaction.getSuccess();
        //Gather views
        TextView dateView = (TextView) rowView.findViewById(R.id.api_transaction_date);
        TextView detailsView = (TextView) rowView.findViewById(R.id.api_transaction_details);
        final TextView additionalDetailsView = (TextView) rowView.findViewById(R.id
                .api_transaction_additional_details);

        if (successful) additionalDetailsView.setVisibility(View.GONE);

        //Update views
        dateView.setText(DateFormatters.simpleTimeFormat.format(date));
        detailsView.setText(apiTransaction.getDetails());
        additionalDetailsView.setText(apiTransaction.getAdditionalDetails());

        Typeface fontAwesome = Typeface.createFromAsset(activity.getAssets(),
                "fontawesome-webfont.ttf");


        TextView statusImage = (TextView) rowView.findViewById(R.id.api_transaction_status);
        statusImage.setTypeface(fontAwesome);
        statusImage.setText(successful ? activity.getString(R.string.fa_check) : activity
                .getString(R.string.fa_times));


        //Remove Button Functionality
        Button removeButton = (Button) rowView.findViewById(R.id.remove_api_transaction);
        removeButton.setTypeface(fontAwesome);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiTransaction.deleteFromDB(activity);
                ApiTransactionListAdapter.this.remove(values.get(position));
                notifyDataSetChanged();
            }
        });
        //Only allow clicking if the api transaction failed.
        if (!apiTransaction.getSuccess()) {
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent resyncActivity = new Intent(activity, getResyncActivity());
                    resyncActivity.putExtra("apiTransactionId", apiTransaction.getId());
                    activity.startActivityForResult(resyncActivity, RESYNC_ACTIVITY);
                    activity.overridePendingTransition(activity.getENTER_ANIMATION(), activity
                            .getEXIT_ANIMATION());
                }
            });
        } else {
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (additionalDetailsView.getVisibility() == View.GONE) {
                        additionalDetailsView.setVisibility(View.VISIBLE);
                    } else {
                        additionalDetailsView.setVisibility(View.GONE);
                    }
                }
            });
        }
        rowView.setId(position);
        return rowView;
    }

    public Class<?> getResyncActivity() {
        return resyncActivity;
    }
}