package com.foodlogiq.distributormobile.commands;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.asyncTasks.LogOutAsyncTask;
import com.foodlogiq.flqassets.FLQActivity;
import com.foodlogiq.flqassets.designPatterns.Command;

/**
 * Prompt user to confirm. If tru, then fires off logout activity.
 */
public class LogOutAction implements Command {
    private final FLQActivity activity;

    public LogOutAction(FLQActivity activity) {
        this.activity = activity;
    }

    @Override
    public void execute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new LogOutAsyncTask(
                        activity,
                        activity.getContentResolver()
                ).execute();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

}