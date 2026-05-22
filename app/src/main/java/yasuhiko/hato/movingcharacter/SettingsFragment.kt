package yasuhiko.hato.movingcharacter

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

/**
 * @author Yasuhikohato
 * @since 4/8, 2017
 */
class SettingsFragment : PreferenceFragmentCompat() {
    private val LOG_TAG = "SettingsFragment"
    private var mListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private val REQUEST_OVERLAY_CODE = 100

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val lpForColor = findPreference<ListPreference>(getString(R.string.preference_key_color))
        if (lpForColor != null && lpForColor.value == null) {
            val defaultValue = "0"
            lpForColor.value = defaultValue
        }

        val lpForFrequency = findPreference<ListPreference>(getString(R.string.preference_key_frequency))
        if (lpForFrequency != null && lpForFrequency.value == null) {
            val defaultValue = "1"
            lpForFrequency.value = defaultValue
        }

        mListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == getString(R.string.preference_key_show_character)) {
                val b = sharedPreferences.getBoolean(getString(R.string.preference_key_show_character), false)
                if (b) {
                    checkCanDrawOverlaysAndStartMoving()
                } else {
                    if (LayerService.isStarted()) {
                        val activity = activity
                        activity?.stopService(Intent(activity, LayerService::class.java))
                    }
                }
            } else if (key == getString(R.string.preference_key_color)) {
                val value = sharedPreferences.getString(getString(R.string.preference_key_color), "0")
                val id = value?.toInt() ?: 0
                Log.d(LOG_TAG, "Changed color id to $id")
                if (id == 0) {
                    Constants.changeImageToBlue()
                } else if (id == 1) {
                    Constants.changeImageToRed()
                }
            } else if (key == getString(R.string.preference_key_move)) {
                val b = sharedPreferences.getBoolean(getString(R.string.preference_key_move), true)
                Constants.move = b
            } else if (key == getString(R.string.preference_key_frequency)) {
                val value = sharedPreferences.getString(getString(R.string.preference_key_frequency), "1")
                val id = value?.toInt() ?: 1
                Log.d(LOG_TAG, "Changed frequency id to $id")
                when (id) {
                    0 -> Constants.changeMovingTimeIntervalToFrequently()
                    1 -> Constants.changeMovingTimeIntervalToStandard()
                    2 -> Constants.changeMovingTimeIntervalToSometimes()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            ?.registerOnSharedPreferenceChangeListener(mListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
            ?.unregisterOnSharedPreferenceChangeListener(mListener)
    }

    private fun checkCanDrawOverlaysAndStartMoving() {
        val activity = activity ?: return
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(activity)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.packageName)
                )
                @Suppress("DEPRECATION")
                startActivityForResult(intent, REQUEST_OVERLAY_CODE)
            } else {
                startLayerService(activity)
            }
        } else {
            startLayerService(activity)
        }
    }

    private fun startLayerService(activity: Activity) {
        if (!LayerService.isStarted()) {
            val intent = Intent(activity, LayerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(intent)
            } else {
                activity.startService(intent)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_OVERLAY_CODE) {
            checkCanDrawOverlaysAndStartMoving()
        }
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }
}
