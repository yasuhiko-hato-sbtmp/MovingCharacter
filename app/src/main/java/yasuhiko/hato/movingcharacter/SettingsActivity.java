package yasuhiko.hato.movingcharacter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {
    private final String LOG_TAG = "SettingsActivity";
    private int REQUEST_OVERLAY_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        /*
        setContentView(R.layout.activity_main);

        Switch presenceSwitch = (Switch)findViewById(R.id.presence_switch);
        presenceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    // start moving
                    checkCanDrawOverlaysAndStartMoving();
                }
                else{
                    // stop moving
                    if(LayerService.isStarted()) {
                        stopService(new Intent(SettingsActivity.this, LayerService.class));
                    }
                }
            }
        });
        */
    }

    private void checkCanDrawOverlaysAndStartMoving(){
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(SettingsActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_CODE);
            }
            else{
                if(!LayerService.isStarted()) {
                    startService(new Intent(SettingsActivity.this, LayerService.class));
                }
            }
        }
        else {
            if(!LayerService.isStarted()) {
                startService(new Intent(SettingsActivity.this, LayerService.class));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_OVERLAY_CODE){
            checkCanDrawOverlaysAndStartMoving();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
