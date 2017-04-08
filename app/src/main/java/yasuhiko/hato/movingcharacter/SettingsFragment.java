package yasuhiko.hato.movingcharacter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author Yasuhikohato
 * @since 4/8, 2017
 */
public class SettingsFragment extends PreferenceFragment {

    private final String LOG_TAG = "SettingsFragment";
    public static final String KEY_PREF_SHOW_CHARACTER = "ShowCharacter";
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(KEY_PREF_SHOW_CHARACTER)){
                    SwitchPreference switchPreference = (SwitchPreference)findPreference(key);
                    Log.d(LOG_TAG, "\"" + switchPreference.toString() + "\" was changed");
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

}
