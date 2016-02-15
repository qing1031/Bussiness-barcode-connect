package com.foodlogiq.distributormobile.viewAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.activities.ListBusinessesActivity;
import com.foodlogiq.distributormobile.asyncTasks.GetEventTypes;
import com.foodlogiq.distributormobile.asyncTasks.GetIconImageAsyncTask;
import com.foodlogiq.distributormobile.entityClasses.Business;
import com.foodlogiq.flqassets.asyncHelpers.AsyncBooleanResponse;

import java.util.ArrayList;

/**
 * Lists business objects.
 * Sets the business that matches the currentBusinessId as selected, if it exists
 */
public class BusinessesArrayAdapter extends ArrayAdapter<Business> implements AsyncBooleanResponse {
    private final ListBusinessesActivity activity;
    private final ArrayList<Business> values;
    private final ArrayList<View> childViews = new ArrayList<>();
    private final ListView listView;
    private Business selectedBusiness;

    public BusinessesArrayAdapter(final ListBusinessesActivity activity, final
    ArrayList<Business> values, ListView listView) {
        super(activity, R.layout.partial_business, values);
        this.activity = activity;
        final SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Activity.MODE_PRIVATE);
        String currentBusinessId = sharedPreferences.getString(activity.getString(R.string
                .current_business_id), null);
        if (currentBusinessId != null) {
            selectedBusiness = findBusinessById(currentBusinessId, values);
        }
        this.listView = listView;
        this.values = values;
        listView.setAdapter(this);
    }

    /**
     * @param currentBusinessId business Id to look for in businesses arraylist.
     * @param values            list of businesses to search.
     * @return the business, if it is found, that has the same id as the search value.
     */
    private Business findBusinessById(String currentBusinessId, ArrayList<Business> values) {
        for (Business business : values) {
            if (business.getId().equals(currentBusinessId)) {
                return business;
            }
        }
        return null;
    }

    /**
     * Sets the business name and icon in the rowview. If the business for the row has been selected
     * then it is highlighted.
     *
     * @return the newly inflated rowView populated with the business data at the input position.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.partial_business, parent, false);

        //Gather values
        String businessName = values.get(position).getName();
        String businessIconUrl = values.get(position).getIconUrl();
        String businessId = values.get(position).getId();
        if (selectedBusiness != null) {
            if (businessId.equals(selectedBusiness.getId()))
                listView.setItemChecked(position, true);
        }

        //Gather views
        TextView businessNameView = (TextView) rowView.findViewById(R.id.business_name);
        ImageView businessIconView = (ImageView) rowView.findViewById(R.id.business_icon);

        //Update views
        businessNameView.setText(businessName);
        GetIconImageAsyncTask getIconImageAsyncTask = new GetIconImageAsyncTask(activity,
                businessIconUrl, businessIconView);
        getIconImageAsyncTask.execute();


        //Attach Row
        setClickListener(rowView, values.get(position));
        rowView.setId(position);
        childViews.add(rowView);
        return rowView;
    }

    /**
     * When a row is clicked, it should set the clicked business as the current business for the
     * application and start a new
     * {@link com.foodlogiq.distributormobile.activities.LocationSearchActivity}
     *
     * @param business The business to set as the current business on click.
     */
    private void setClickListener(final View rowView, final Business business) {
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                business.setAsCurrent(activity);
                final SharedPreferences.Editor editor = activity
                        .getSharedPreferences(activity.getPackageName(), Activity.MODE_PRIVATE)
                        .edit();
                editor.putBoolean(activity.getString(R.string.singleBusinessOverride), true);
                editor.apply();
                SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                        .getPackageName(), Context.MODE_PRIVATE);

                String customHelperHost = sharedPreferences.getString(activity.getString(R.string
                        .custom_helper_site), activity.getString(R.string
                        .default_helper_site_host));

                new GetEventTypes(activity, customHelperHost, business.getFoodlogiqId(), "all",
                        1, BusinessesArrayAdapter.this)
                        .execute();
            }
        });
    }

    @Override
    public void processAsyncResponse(String error, boolean result, int taskId) {
        //For right now, we really don't care if the event types failed to pull down
        //TODO: start caring
        activity.launchLocationActivity();
    }
}
