package gruppe11.aufgabe_2.activities;


import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import gruppe11.aufgabe_2.R;
import gruppe11.aufgabe_2.map_items.LocalizableService;
import gruppe11.aufgabe_2.rest.RestService;
import gruppe11.aufgabe_2.rest.UpdateService;

/**
 * Activity showing a list of preferences
 * - Radius
 * - TimeInterval
 */
public class PreferencesActivity  extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String DEBUGLOG_TAG = "DEBUGLOG_PA";



    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
        Log.d(DEBUGLOG_TAG, "Pref changed - restService null");
        RestService restService = (RestService) getIntent().getSerializableExtra("restService");
        if (restService != null) {

            if (restService.isLoggedIn()) {
                LocalizableService.getInstance().clearCommunityItems();
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                restService.getCommunityData(Integer.valueOf(sPref.getString("radius", "25")));
                UpdateService updateService = (UpdateService) getIntent().getSerializableExtra("updateService");
                if (updateService != null) {
                    if (updateService.isTimerActive()) {
                        updateService.start(this);
                    }
                }
            }
        }
    }
}