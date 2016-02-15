package com.foodlogiq.distributormobile.interfaces;

import android.view.View;

import com.foodlogiq.distributormobile.entityClasses.CustomAttribute;

/**
 * Created by djak250 on 1/22/16.
 */
public interface CustomAttributeView {
    CustomAttribute getCustomAttribute();

    View getWrapperView();

    View getValueView();

    String getValueAsString();
}
