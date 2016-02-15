package com.foodlogiq.distributormobile.viewAdapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.customViews.DatePickerEditText;
import com.foodlogiq.distributormobile.entityClasses.CustomAttribute;
import com.foodlogiq.distributormobile.entityClasses.EventType;
import com.foodlogiq.distributormobile.interfaces.CustomAttributeView;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by djak250 on 1/22/16.
 */
public class EntityTypeSpinner extends Spinner {
    private Activity activity;
    private boolean customAttributesExist;
    private HashMap<String, CustomAttributeView> customAttributeViewsByStoredAs = new HashMap<>();
    private ArrayList<CustomAttribute> customAttributes = new ArrayList<>();
    private String eventTypeId = "";

    /**
     * Construct a new spinner with the given context's theme.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public EntityTypeSpinner(Context context) {
        super(context);
    }

    public EntityTypeSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(final Activity activity, ArrayList<EventType> eventTypes) {
        this.activity = activity;
        final ArrayAdapter<EventType> eventTypesArrayAdapter = new
                ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, eventTypes);
        eventTypesArrayAdapter.insert(new EventType(true), 0);
        eventTypesArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        this.setAdapter(eventTypesArrayAdapter);
        this.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!((EventType) parent.getSelectedItem()).isNull()) {
                    populateCustomAttributes((EventType) parent.getSelectedItem());
                } else {
                    populateCustomAttributes(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                populateCustomAttributes(null);
            }
        });
        populateCustomAttributes((eventTypes.get(0)));
        //triggers initial onItemSelected
        this.setSelection(this.getSelectedItemPosition(), true);
    }

    private void populateCustomAttributes(EventType selectedItem) {
        LinearLayout customAttributesLayout = (LinearLayout) activity.findViewById(R.id
                .custom_attributes_layout);
        if (selectedItem == null) {
            customAttributesExist = false;
            customAttributesLayout.removeAllViews();
            customAttributes = new ArrayList<>();
            customAttributeViewsByStoredAs = new HashMap<>();
            eventTypeId = "";
        } else if (!this.eventTypeId.equals(selectedItem.getFoodlogiqId())) {
            customAttributesLayout.removeAllViews();
            this.eventTypeId = selectedItem.getFoodlogiqId();
            customAttributesExist = true;
            for (CustomAttribute customAttribute : selectedItem.getAttributes()) {
                CustomAttributeView customAttributeView = null;
                switch (customAttribute.getFieldType()) {
                    case "bool":
                        customAttributeView = new BooleanSwitchView(activity, customAttribute);
                        break;
                    case "date":
                        customAttributeView = new DatePickerView(activity, customAttribute);
                        break;
                    case "list":
                        customAttributeView = new SpinnerView(activity, customAttribute);
                        break;
                    case "float":
                    case "int":
                    case "text":
                        customAttributeView = new TextInput(activity, customAttribute);
                        break;
                }
                if (customAttributeView != null)
                    customAttributesLayout.addView(customAttributeView.getWrapperView());
                customAttributeViewsByStoredAs.put(customAttribute.getStoredAs(),
                        customAttributeView);
                customAttributes.add(customAttribute);
            }
        }
    }

    public HashMap<String, CustomAttributeView> getCustomAttributeViewsByStoredAs() {
        return customAttributeViewsByStoredAs;
    }

    public ArrayList<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    public void updateCustomAttributeValues() {
        Iterator iter = this.getCustomAttributeViewsByStoredAs().entrySet().iterator();
        int i = 0;

        while (iter.hasNext()) {

            Map.Entry pair = (Map.Entry) iter.next();
            CustomAttributeView customAttributeView = (CustomAttributeView) pair.getValue();
            String value = customAttributeView.getValueAsString();
            switch (customAttributeView.getCustomAttribute().getFieldType()) {
                case "text":
                case "list":
                    String sVal = value.isEmpty() ? null : value;
                    customAttributeView.getCustomAttribute().setValue(sVal);
                    break;
                case "date":
                    Date dVal = null;
                    try {
                        dVal = DateFormatters.simpleFormat.parse(((TextView) customAttributeView
                                .getValueView())
                                .getText().toString());
                        customAttributeView.getCustomAttribute().setValue(DateFormatters
                                .isoDateFormatter.format(dVal));
                    } catch (ParseException e) {
                    }

                    break;
                case "int":
                    Integer iVal = null;
                    try {
                        iVal = Integer.valueOf(
                                ((EditText) customAttributeView.getValueView()).getText()
                                        .toString());
                    } catch (NumberFormatException e) {
                    }
                    customAttributeView.getCustomAttribute().setValue(iVal);
                    break;
                case "float":
                    Float fVal = null;
                    try {
                        fVal = Float.valueOf(customAttributeView.getValueAsString());
                    } catch (NumberFormatException e) {
                    }
                    customAttributeView.getCustomAttribute().setValue(fVal);
                    break;
                case "bool":
                    Boolean bVal = null;
                    bVal = value.equalsIgnoreCase("yes");
                    customAttributeView.getCustomAttribute().setValue(bVal);
                    break;
            }
        }
    }

    public void populateCustomAttributeValues(ArrayList<CustomAttribute> customAttributes) {
        for (CustomAttribute ca : customAttributes) {
            CustomAttributeView customAttributeView = this.customAttributeViewsByStoredAs.get(ca
                    .getStoredAs());
            switch (customAttributeView.getCustomAttribute().getFieldType()) {
                case "text":
                case "int":
                case "float":
                    String sVal = ((String) ca.getValue()).isEmpty() ? "" : (String) ca
                            .getValue();
                    ((EditText) customAttributeView.getValueView()).setText(sVal);
                    break;
                case "list":
                    String lVal = ((String) ca.getValue()).isEmpty() ? "" : (String) ca
                            .getValue();
                    ArrayList<String> options = customAttributeView.getCustomAttribute()
                            .getOptions();
                    for (int i = 0; i < options.size(); i++) {
                        if (options.get(i).equals(lVal)) {
                            ((Spinner) customAttributeView.getValueView()).setSelection(i);
                            break;
                        }
                    }
                    break;
                case "date":
                    String dVal = null;
                    try {
                        dVal = ((String) ca.getValue()).isEmpty() ? "" :
                                DateFormatters.simpleFormat.format(
                                        DateFormatters.isoDateFormatter.parse((String) ca
                                                .getValue()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    ((TextView) customAttributeView.getValueView()).setText(dVal);
                    break;
                case "bool":
                    String bVal = ((String) ca.getValue()).isEmpty() ? "" : (String) ca.getValue();
                    if (bVal.equalsIgnoreCase("true")) {
                        ((SwitchCompat) customAttributeView.getValueView()).setChecked(true);
                    } else {
                        ((SwitchCompat) customAttributeView.getValueView()).setChecked(false);
                    }
                    break;
            }
        }
    }

    private class BooleanSwitchView implements CustomAttributeView {
        private final RelativeLayout wrapperView;
        private final SwitchCompat valueView;
        private final CustomAttribute customAttribute;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public BooleanSwitchView(Activity activity, CustomAttribute customAttribute) {
            this.customAttribute = customAttribute;

            LayoutInflater inflater = activity.getLayoutInflater();
            this.wrapperView = (RelativeLayout) inflater.inflate(R.layout.custom_boolean_switch,
                    (ViewGroup) findViewById(R.id.custom_attributes_layout), false);
            TextView labelView = (TextView) this.wrapperView.findViewById(R.id.label);
            labelView.setText(customAttribute.getCommonName());

            this.valueView = (SwitchCompat) this.wrapperView.findViewById(R.id.value);
        }

        @Override
        public CustomAttribute getCustomAttribute() {
            return this.customAttribute;
        }

        @Override
        public View getWrapperView() {
            return this.wrapperView;
        }

        @Override
        public View getValueView() {
            return this.valueView;
        }

        @Override
        public String getValueAsString() {
            return ((SwitchCompat) getValueView()).isChecked() ? "Yes" : "No";
        }
    }


    private class DatePickerView implements CustomAttributeView {
        private final LinearLayout wrapperView;
        private final TextView valueView;
        private final CustomAttribute customAttribute;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public DatePickerView(Activity activity, CustomAttribute customAttribute) {
            this.customAttribute = customAttribute;

            LayoutInflater inflater = activity.getLayoutInflater();
            this.wrapperView = (LinearLayout) inflater.inflate(R.layout.custom_date_picker,
                    (ViewGroup) findViewById(R.id.custom_attributes_layout), false);
            TextView labelView = (TextView) this.wrapperView.findViewById(R.id.label);
            labelView.setText(customAttribute.getCommonName());

            this.valueView = (TextView) this.wrapperView.findViewById(R.id.value);
            new DatePickerEditText(activity,
                    this.valueView, false);
        }

        @Override
        public CustomAttribute getCustomAttribute() {
            return this.customAttribute;
        }

        @Override
        public View getWrapperView() {
            return this.wrapperView;
        }

        @Override
        public View getValueView() {
            return this.valueView;
        }

        @Override
        public String getValueAsString() {
            return String.valueOf(((TextView) getValueView()).getText());
        }
    }

    private class TextInput implements CustomAttributeView {
        private final TextInputLayout wrapperView;
        private final EditText valueView;
        private final CustomAttribute customAttribute;

        public TextInput(Activity activity, CustomAttribute customAttribute) {
            this.customAttribute = customAttribute;

            LayoutInflater inflater = activity.getLayoutInflater();
            this.wrapperView = (TextInputLayout) inflater.inflate(R.layout.custom_text_input,
                    (ViewGroup) findViewById(R.id.custom_attributes_layout), false);

            TextInputLayout labelView = (TextInputLayout) this.wrapperView.findViewById(R.id.label);
            labelView.setHint(customAttribute.getCommonName());

            this.valueView = (EditText) this.wrapperView.findViewById(R.id.value);


            switch (customAttribute.getFieldType()) {
                case "text":
                    this.valueView.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case "int":
                    this.valueView.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case "float":
                    this.valueView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType
                            .TYPE_NUMBER_FLAG_DECIMAL);
                    break;
            }

        }

        @Override
        public CustomAttribute getCustomAttribute() {
            return this.customAttribute;
        }

        @Override
        public View getWrapperView() {
            return this.wrapperView;
        }

        @Override
        public View getValueView() {
            return this.valueView;
        }

        @Override
        public String getValueAsString() {
            return String.valueOf(((TextView) getValueView()).getText());
        }
    }

    private class SpinnerView implements CustomAttributeView {
        private final LinearLayout wrapperView;
        private final Spinner valueView;
        private final CustomAttribute customAttribute;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public SpinnerView(Activity activity, CustomAttribute customAttribute) {
            this.customAttribute = customAttribute;

            LayoutInflater inflater = activity.getLayoutInflater();
            this.wrapperView = (LinearLayout) inflater.inflate(R.layout.custom_spinner_view,
                    (ViewGroup) findViewById(R.id.custom_attributes_layout), false);
            TextView labelView = (TextView) this.wrapperView.findViewById(R.id.label);
            labelView.setText(customAttribute.getCommonName());

            this.valueView = (Spinner) this.wrapperView.findViewById(R.id.value);
            ArrayAdapter<String> optionAdapter = new ArrayAdapter<>(activity, android.R.layout
                    .simple_spinner_item, customAttribute.getOptions());
            optionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.valueView.setAdapter(optionAdapter);
        }

        @Override
        public CustomAttribute getCustomAttribute() {
            return this.customAttribute;
        }

        @Override
        public View getWrapperView() {
            return this.wrapperView;
        }

        @Override
        public View getValueView() {
            return this.valueView;
        }

        @Override
        public String getValueAsString() {
            return String.valueOf(((Spinner) getValueView()).getSelectedItem());
        }
    }
}
