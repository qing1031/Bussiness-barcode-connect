package com.foodlogiq.distributormobile.viewAdapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.entityClasses.LocationAction;
import com.foodlogiq.flqassets.FLQActivity;

import java.util.ArrayList;

/**
 * Handles listing the input location actions and setting up their click listeners. Supports setting
 * color of button, if desired. Currently, only grey and green are supported, and defaults to green.
 * Clicked on a row triggers the start of the activity defined in the {@link LocationAction}
 */
public class LocationActionsAdapter extends ArrayAdapter<LocationAction> {
    private final FLQActivity activity;
    private final ArrayList<LocationAction> values;
    private final ArrayList<View> childViews = new ArrayList<>();
    private final ListView listView;
    private final Typeface fontAwesome;

    public LocationActionsAdapter(
            final FLQActivity activity,
            final ArrayList<LocationAction> values,
            ListView listView
    ) {
        super(activity, R.layout.partial_location_action, values);
        this.activity = activity;
        this.listView = listView;
        this.values = values;
        listView.setAdapter(this);
        fontAwesome = Typeface.createFromAsset(activity.getAssets(), "fontawesome-webfont.ttf");
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LocationAction locationAction = values.get(position);
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.partial_location_action, parent, false);
        View actualButton = rowView.findViewById(R.id.location_action_button);
        if (locationAction.getColor().equals("green")) {
            actualButton.setBackground(activity.getResources().getDrawable(R.drawable
                    .foodlogiq_green_button));
        } else if (locationAction.getColor().equals("grey")) {
            actualButton.setBackground(activity.getResources().getDrawable(R.drawable
                    .foodlogiq_grey_button));
        }

        //Gather values
        String actionTitle = locationAction.getActionTitle();
        //Gotta replace - with _ for using with string resource
        String locationIconValue = locationAction
                .getActionIcon()
                .replaceAll("-", "_");

        //Gather views
        TextView actionTitleView = (TextView) rowView.findViewById(R.id.location_action_text);
        TextView actionIconView = (TextView) rowView.findViewById(R.id.location_action_icon);
        actionIconView.setTypeface(fontAwesome);

        //Update views
        actionTitleView.setText(actionTitle);
        actionIconView.setText(
                activity.getString(
                        activity.getResources().getIdentifier(
                                locationIconValue,
                                "string",
                                activity.getPackageName()
                        )
                )
        );

        //Attach Row
        setClickListener(rowView, values.get(position));
        rowView.setId(position);
        childViews.add(rowView);
        return rowView;
    }

    /**
     * On click, a row should trigger the activity defiend in the child
     * {@link LocationAction#actionActivity}.
     * If the locationAction has a bundle associated with it, this is populated into the intent
     * before
     * starting.
     *
     * @param locationAction The location action to trigger when the row is clicked.
     */
    private void setClickListener(final View rowView, final LocationAction locationAction) {
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationActionIntent = new Intent(activity, locationAction
                        .getActionActivity());
                locationActionIntent.putExtras(locationAction.getBundle());
                activity.startActivity(locationActionIntent);
                activity.overridePendingTransition(activity.getENTER_ANIMATION(), activity
                        .getEXIT_ANIMATION());
            }
        });
    }

}
