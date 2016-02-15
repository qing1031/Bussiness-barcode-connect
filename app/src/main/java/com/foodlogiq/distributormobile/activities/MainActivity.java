package com.foodlogiq.distributormobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.flqassets.LoginActivity;
import com.intermec.aidc.AidcManager;
import com.intermec.aidc.BarcodeReader;
import com.intermec.aidc.BarcodeReaderException;
import com.intermec.aidc.VirtualWedge;
import com.intermec.aidc.VirtualWedgeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * MainActivity handles which preferences are set.
 * Redirects to the correct activity based on which step the user is on.
 */
public class MainActivity extends Activity {
    static final int LOGIN_REQUEST = 1;  // The request code
    private static BarcodeReader bcr;
    private static VirtualWedge wedge;

    static BarcodeReader getBarcodeObject() {
        return bcr;
    }

    /**
     * Prompts the user to import our datawedge profile, if it hasn't already been imported.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            intermecSetup();
        } catch (IllegalArgumentException e) {
            //Probably not the intermec device
        }
        final SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(),
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if ((new File("/enterprise/device/settings/datawedge/autoimport/")).isDirectory()) {
            editor.putBoolean(getString(R.string.datawedge_exists), true);
            editor.apply();
            if (!sharedPreferences.getBoolean(getString(R.string.ignore_datawedge), false)
                    && !sharedPreferences.getBoolean(getString(R.string.datawedge_enabled),
                    false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getString(R.string.datawedge_import_prompt));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            importDataWedgeConfig(sharedPreferences);
                        } catch (IOException e) {
                            editor.putBoolean(getString(R.string.datawedge_enabled), true);
                            editor.apply();
                            initializeStartActivity(sharedPreferences);
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.putBoolean(getString(R.string.datawedge_enabled), false);
                        editor.putBoolean(getString(R.string.ignore_datawedge), true);
                        editor.apply();
                        initializeStartActivity(sharedPreferences);
                    }
                });
                builder.create().show();
            } else {
                initializeStartActivity(sharedPreferences);
            }
        } else {
            editor.putBoolean(getString(R.string.datawedge_exists), false);
            editor.putBoolean(getString(R.string.datawedge_enabled), false);
            editor.apply();
            initializeStartActivity(sharedPreferences);
        }
    }

    /**
     * @see Activity#onRestart()
     */
    protected void onRestart() {
        super.onRestart();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (wedge != null) {
                wedge.setEnable(true);
                wedge = null;
            }

            if (bcr != null) {
                bcr.close();
                bcr = null;
            }

        } catch (VirtualWedgeException e) {
            e.printStackTrace();
        }

        //disconnect from data collection service
        AidcManager.disconnectService();
    }

    /**
     * Handles a return from the LoginActivity to redirect the user to the next appropriate
     * activity.
     *
     * @param requestCode Only going to be {@link #LOGIN_REQUEST}
     * @param resultCode  Success or Failure
     * @see Activity#onActivityResult(int, int, Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent listBusinessIntent = new Intent(this, ListBusinessesActivity.class);
                startActivity(listBusinessIntent);
            }
        }
    }

    private void intermecSetup() {
        // Make sure the BarcodeReader depended service is connected and
        // register a callback for service connect and disconnect events.
        AidcManager.connectService(MainActivity.this, new AidcManager.IServiceListener() {
            public void onConnect() {

                // The depended service is connected and it is ready
                // to receive bar code requests and virtual wedge
                try {
                    //Initial bar code reader instance
                    bcr = new BarcodeReader();

                    //disable virtual wedge
                    wedge = new VirtualWedge();
                    wedge.setEnable(false);

                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                } catch (VirtualWedgeException e) {
                    e.printStackTrace();
                }
            }

            public void onDisconnect() {
                //add disconnect message/action here
            }

        });
    }

    /**
     * If the user hasn't been logged in, redirect to {@link LoginActivity}.
     * If business hasn't been selected, redirect to {@link ListBusinessesActivity}
     * If no default location has been selected, {@link LocationSearchActivity}
     * If all of the above has been satisfied, redirect to {@link LocationActionsActivity}
     */
    private void initializeStartActivity(SharedPreferences sharedPreferences) {
        String mobileAccessKey = sharedPreferences.getString(getString(R.string
                .mobile_access_token), null);
        String currentBusinessFoodlogiqId = sharedPreferences.getString(getString(R.string
                .current_business_foodlogiq_id), null);
        Boolean locationDefaulted = sharedPreferences.getBoolean(getString(R.string
                .default_location), false);
        if (mobileAccessKey == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, LOGIN_REQUEST);
        } else if (currentBusinessFoodlogiqId == null) {
            Intent listBusinessIntent = new Intent(this, ListBusinessesActivity.class);
            startActivity(listBusinessIntent);
        } else if (!locationDefaulted) {
            Intent listLocationsIntent = new Intent(this, LocationSearchActivity.class);
            startActivity(listLocationsIntent);
        } else {
            Intent locationActionsActivity = new Intent(this, LocationActionsActivity.class);
            startActivity(locationActionsActivity);
        }
    }

    /**
     * Loads the datawedge profile from assets into the datawedge autoimport folder, which is picked
     * up by datawedge and overwrites the current configuration with the one needed for our
     * implementations.
     */
    private void importDataWedgeConfig(SharedPreferences sharedPreferences) throws IOException {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        InputStream in = getResources().getAssets().open("datawedge.db");
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
        editor.putBoolean(getString(R.string.datawedge_enabled), true);
        editor.apply();
        initializeStartActivity(sharedPreferences);
    }
}
