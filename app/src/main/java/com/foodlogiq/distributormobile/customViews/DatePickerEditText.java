package com.foodlogiq.distributormobile.customViews;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * View to allow a datepicker to populate an edittext.
 */
public class DatePickerEditText {

    private final DatePickerDialog datePicker;
    private final TextView textView;
    private final SimpleDateFormat simpleDateFormat;
    private Date dateFromDateObject;

    /**
     * Sets up a click listener to show DatePickerDialog when the parent textview has been clicked.
     *
     * @param activity       Parent Activity
     * @param textView       View that shows the value of the date. When clicked, a datepicker
     *                       dialog is shown
     * @param defaultToToday if true, set date to today, otherwise leave blank.
     */
    public DatePickerEditText(Activity activity, final TextView textView, boolean defaultToToday) {
        Calendar today = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        this.textView = textView;
        datePicker = new DatePickerDialog(activity, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                updateTextView(year, monthOfYear, dayOfMonth);
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Clear", new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearTextView();
            }
        });
        if (defaultToToday) {
            textView.setText(simpleDateFormat.format(Calendar.getInstance().getTime()));
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker.show();
            }
        });
    }

    /**
     * Sets the date in the datepicker, as well as in the text view.
     *
     * @param monthOfYear 0-indexed month.
     */
    public void setDate(int year, int monthOfYear, int dayOfMonth) {
        this.datePicker.updateDate(year, monthOfYear, dayOfMonth);
        updateTextView(year, monthOfYear, dayOfMonth);
    }

    /**
     * pretty self-explanatory. Empties text view.
     */
    private void clearTextView() {
        textView.setText("");
    }

    /**
     * Updates the textview with a SimpleDate format of input year/month/day values.
     *
     * @param monthOfYear 0-indexed month.
     */
    private void updateTextView(int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        textView.setText(simpleDateFormat.format(c.getTime()));
    }

    /**
     * Updates the textview with a SimpleDate format of input Date object.
     */
    public void setDateFromDateObject(Date dateFromDateObject) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateFromDateObject);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        setDate(year, month, day);
    }
}