package com.foodlogiq.distributormobile.entityClasses;

import android.os.Bundle;

/**
 * Location actions are used to list out all the possible activities that can be performed
 * on a location. They consist of an icon, a title, and an activity that is started when the
 * action is selected.
 */
public class LocationAction {
    private final String color;
    private String actionTitle;
    private String actionIcon;
    private Class<?> actionActivity;
    private Bundle bundle;

    /**
     * @param actionTitle    String to display for action
     * @param actionIcon     Icon (font awesome string) to be displayed on the action.
     * @param actionActivity Activity to be started when action is selected.
     * @param color          Background color for use with the action list adapter.
     * @param actionBundle   Bundle to store assets for use in the activity.
     */
    public LocationAction(String actionTitle, String actionIcon, Class<?> actionActivity, String
            color, Bundle actionBundle) {
        this.actionTitle = actionTitle;
        this.actionIcon = actionIcon;
        this.actionActivity = actionActivity;
        this.color = color;
        this.bundle = actionBundle;
    }

    /**
     * @param actionTitle    String to display for action
     * @param actionIcon     Icon (font awesome string) to be displayed on the action.
     * @param actionActivity Activity to be started when action is selected.
     * @param color          Background color for use with the action list adapter.
     */
    public LocationAction(String actionTitle, String actionIcon, Class<?> actionActivity, String
            color) {
        this(actionTitle, actionIcon, actionActivity, color, null);
        this.setBundle(new Bundle());
    }

    /**
     * @param actionTitle    String to display for action
     * @param actionIcon     Icon (font awesome string) to be displayed on the action.
     * @param actionActivity Activity to be started when action is selected.
     */
    public LocationAction(String actionTitle, String actionIcon, Class<?> actionActivity) {
        this(actionTitle, actionIcon, actionActivity, "green");
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public String getActionIcon() {
        return actionIcon;
    }

    public Class<?> getActionActivity() {
        return actionActivity;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public String getColor() {
        return color;
    }
}
