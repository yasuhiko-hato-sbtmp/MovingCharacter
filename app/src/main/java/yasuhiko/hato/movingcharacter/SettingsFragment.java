package yasuhiko.hato.movingcharacter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.provider.Settings;


/**
 * @author Yasuhikohato
 * @since 4/8, 2017
 */
public class SettingsFragment extends PreferenceFragment {

    private final String LOG_TAG = "SettingsFragment";
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    private int REQUEST_OVERLAY_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

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
                            activity.stopService(new Intent(activity, LayerService.class));
                        }
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
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_CODE);
            }
            else{
                if(!LayerService.isStarted()) {
                    activity.startService(new Intent(activity, LayerService.class));
                }
            }
        }
        else {
            if(!LayerService.isStarted()) {
                activity.startService(new Intent(activity, LayerService.class));
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
