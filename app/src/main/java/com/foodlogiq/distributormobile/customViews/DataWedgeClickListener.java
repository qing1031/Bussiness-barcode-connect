package com.foodlogiq.distributormobile.customViews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.MenuItem;

import com.foodlogiq.distributormobile.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Listener that will be added to all menus if datawedge is present on the device.
 */
public class DataWedgeClickListener implements MenuItem.OnMenuItemClickListener {
    private final Activity activity;

    public DataWedgeClickListener(Activity activity) {
        this.activity = activity;
    }

    /**
     * Prompts the user to confirm. If they confirm, run
     * {@link #importDataWedgeConfig(SharedPreferences)}
     */
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        final SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.datawedge_import_prompt));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    importDataWedgeConfig(sharedPreferences);
                } catch (IOException e) {
                    editor.putBoolean(activity.getString(R.string.datawedge_enabled), true);
                    editor.apply();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
        return false;
    }

    /**
     * Moves the datawedge configuration in assets to the datawedge autoimport folder, which
     * overwrites the current datawedge configuration.
     */
    private void importDataWedgeConfig(SharedPreferences sharedPreferences) throws IOException {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        InputStream in = activity.getResources().getAssets().open("datawedge.db");
        OutputStream out = new FileOutputStream
                ("/enterprise/device/settings/datawedge/autoimport/datawedge.db");
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
        Runtime.getRuntime().exec("chmod 777 " +
                "/enterprise/device/settings/datawedge/autoimport/datawedge.db");
        editor.putBoolean(activity.getString(R.string.datawedge_enabled), true);
        editor.apply();
    }
}
