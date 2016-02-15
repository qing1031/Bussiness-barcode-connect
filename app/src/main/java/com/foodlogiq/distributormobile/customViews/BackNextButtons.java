package com.foodlogiq.distributormobile.customViews;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;

/**
 * Handles the movement from step to step. Pins a navigation bar to the bottom of the view
 * instantiating this class
 */
public class BackNextButtons {
    private final LinearLayout backNextButtons;
    private final LinearLayout backButton;
    private final LinearLayout nextButton;
    private final LinearLayout nextDummyButton;
    private final LinearLayout backDummyButton;

    /**
     * Sets click listeners for each button.
     * Uses fontawesome to add back/next arrows to the buttons.
     * There are dummy buttons that have no click listeners in order to effectively disable the
     * actual button.
     *
     * @param activity          Parent activity where the BackNextButtons should be pinned to.
     * @param backClickListener What should happen when the back button is clicked.
     * @param nextClickListener What should happen when the next button is clicked.
     */
    public BackNextButtons(Activity activity, View.OnClickListener backClickListener, View
            .OnClickListener nextClickListener) {
        ViewGroup topView = (ViewGroup) activity.findViewById(android.R.id.content);
        ViewGroup rootView = (ViewGroup) topView.getChildAt(0);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        backNextButtons = (LinearLayout) layoutInflater.inflate(R.layout
                .partial_back_next_button, rootView, false);
        //Use this to get the view after the actionBar if it exists
        View mainView = rootView.getChildAt(1);
        mainView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        0.9f
                )
        );
        backNextButtons.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
        backButton = (LinearLayout) backNextButtons.findViewById(R.id.bottom_back_button);
        backDummyButton = (LinearLayout) backNextButtons.findViewById(R.id
                .bottom_back_dummy_button);
        nextButton = (LinearLayout) backNextButtons.findViewById(R.id.bottom_next_button);
        nextDummyButton = (LinearLayout) backNextButtons.findViewById(R.id
                .bottom_next_dummy_button);

        Typeface fontAwesome = Typeface.createFromAsset(activity.getAssets(),
                "fontawesome-webfont.ttf");

        TextView backButtonFaIcon = (TextView) backButton.findViewById(R.id.back_fa_icon);
        TextView backDummyButtonFaIcon = (TextView) backDummyButton.findViewById(R.id
                .back_dummy_fa_icon);
        TextView nextButtonFaIcon = (TextView) nextButton.findViewById(R.id.next_fa_icon);
        TextView nextDummyButtonFaIcon = (TextView) nextDummyButton.findViewById(R.id
                .next_dummy_fa_icon);
        backButtonFaIcon.setTypeface(fontAwesome);
        backDummyButtonFaIcon.setTypeface(fontAwesome);
        nextButtonFaIcon.setTypeface(fontAwesome);
        nextDummyButtonFaIcon.setTypeface(fontAwesome);

        //Update views
        backButtonFaIcon.setText(activity.getString(R.string.fa_angle_left));
        backDummyButtonFaIcon.setText(activity.getString(R.string.fa_angle_left));
        nextButtonFaIcon.setText(activity.getString(R.string.fa_angle_right));
        nextDummyButtonFaIcon.setText(activity.getString(R.string.fa_angle_right));


        if (backClickListener == null) {
            backButton.setVisibility(View.INVISIBLE);
        } else {
            backButton.setOnClickListener(backClickListener);
        }
        if (nextClickListener == null) {
            nextButton.setVisibility(View.INVISIBLE);
        } else {
            nextButton.setOnClickListener(nextClickListener);
        }
        rootView.addView(backNextButtons);
    }

    /**
     * Hides the real back button and displays the dummy version
     */
    public void disableBackButton() {
        this.backButton.setVisibility(View.GONE);
        this.backDummyButton.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the dummy back button and displays the real version
     */
    public void enableBackButton() {
        this.backButton.setVisibility(View.VISIBLE);
        this.backDummyButton.setVisibility(View.GONE);
    }

    /**
     * Hides the real next button and displays the dummy version
     */
    public void disableNextButton() {
        this.nextButton.setVisibility(View.GONE);
        this.nextDummyButton.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the dummy next button and displays the real version
     */
    public void enableNextButton() {
        this.nextButton.setVisibility(View.VISIBLE);
        this.nextDummyButton.setVisibility(View.GONE);
    }
}
