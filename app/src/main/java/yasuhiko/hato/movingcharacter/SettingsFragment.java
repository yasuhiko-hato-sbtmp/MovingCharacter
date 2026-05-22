package yasuhiko.hato.movingcharacter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;


/**
 * @author Yasuhikohato
 * @since 4/8, 2017
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private final String LOG_TAG = "SettingsFragment";
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    private int REQUEST_OVERLAY_CODE = 100;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // for initial launch
        ListPreference lpForColor = findPreference(getString(R.string.preference_key_color));
        if(lpForColor != null && lpForColor.getValue()==null) {
            String defaultValue = "0"; // Changed from "Blue" to "0" assuming it's the index or value in XML
            lpForColor.setValue(defaultValue);
        }

        ListPreference lpForFrequency = findPreference(getString(R.string.preference_key_frequency));
        if(lpForFrequency != null && lpForFrequency.getValue()==null) {
            String defaultValue = "1"; // Standard
            lpForFrequency.setValue(defaultValue);
        }

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                // Show character
                if(key.equals(getString(R.string.preference_key_show_character))){
                    boolean b = sharedPreferences.getBoolean(getString(R.string.preference_key_show_character), false);
                    if(b){
                        // start moving
                        checkCanDrawOverlaysAndStartMoving();
                    }
                    else{
                        // stop moving
                        if(LayerService.isStarted()) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.stopService(new Intent(activity, LayerService.class));
                            }
                        }
                    }
                }
                else if(key.equals(getString(R.string.preference_key_color))){
                    String value = sharedPreferences.getString(getString(R.string.preference_key_color), "0");
                    int id = Integer.parseInt(value);
                    Log.d(LOG_TAG, "Changed color id to " + String.valueOf(id));
                    if(id == 0){
                        Constants.changeImageToBlue();
                    }
                    else if(id == 1){
                        Constants.changeImageToRed();
                    }
                }
                else if(key.equals(getString(R.string.preference_key_move))){
                    boolean b = sharedPreferences.getBoolean(getString(R.string.preference_key_move), true);
                    Constants.move = b;
                }
                else if(key.equals(getString(R.string.preference_key_frequency))){
                    String value = sharedPreferences.getString(getString(R.string.preference_key_frequency), "1");
                    int id = Integer.parseInt(value);
                    Log.d(LOG_TAG, "Changed frequency id to " + String.valueOf(id));
                    if(id == 0){
                        Constants.changeMovingTimeIntervalToFrequently();
                    }
                    else if(id == 1){
                        Constants.changeMovingTimeIntervalToStandard();
                    }
                    else if(id == 2){
                        Constants.changeMovingTimeIntervalToSometimes();
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mListener);
    }


    private void checkCanDrawOverlaysAndStartMoving(){
        Activity activity = getActivity();
        if (activity == null) return;

        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_CODE);
            }
            else{
                startLayerService(activity);
            }
        }
        else {
            startLayerService(activity);
        }
    }

    private void startLayerService(Activity activity) {
        if(!LayerService.isStarted()) {
            Intent intent = new Intent(activity, LayerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(intent);
            } else {
                activity.startService(intent);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_OVERLAY_CODE){
            checkCanDrawOverlaysAndStartMoving();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}
