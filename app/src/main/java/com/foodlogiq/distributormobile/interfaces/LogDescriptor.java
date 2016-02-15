package com.foodlogiq.distributormobile.interfaces;

import android.text.Spannable;


public interface LogDescriptor {
    /**
     * @param success whether the incident submission was successful.
     * @return String of details related to server submission
     */
    Spannable getDetails(boolean success);

    Spannable getAdditionalDetails();
}
