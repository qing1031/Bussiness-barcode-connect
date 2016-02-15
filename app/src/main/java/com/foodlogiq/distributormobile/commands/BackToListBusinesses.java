package com.foodlogiq.distributormobile.commands;

import android.content.Intent;

import com.foodlogiq.distributormobile.activities.ListBusinessesActivity;
import com.foodlogiq.flqassets.FLQActivity;
import com.foodlogiq.flqassets.designPatterns.Command;

/**
 * Take user to {@link ListBusinessesActivity} and clear other activities
 */
public class BackToListBusinesses implements Command {
    private final FLQActivity activity;

    public BackToListBusinesses(FLQActivity activity) {
        this.activity = activity;
    }

    @Override
    public void execute() {
        Intent intent = new Intent(activity, ListBusinessesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        int enterAnimation = activity.getENTER_ANIMATION_PREVIOUS();
        int exitAnimation = activity.getEXIT_ANIMATION_PREVIOUS();
        if (enterAnimation != 0 && exitAnimation != 0)
            activity.overridePendingTransition(enterAnimation, exitAnimation);
        activity.finish();
    }
}
