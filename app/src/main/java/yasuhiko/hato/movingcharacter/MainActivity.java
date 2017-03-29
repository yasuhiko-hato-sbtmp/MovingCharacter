package yasuhiko.hato.movingcharacter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    private int REQUEST_OVERLAY_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(new CustomView(this));
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
                    stopService(new Intent(MainActivity.this, LayerService.class));
                }
            }
        });

    }

    private void checkCanDrawOverlaysAndStartMoving(){
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_CODE);
            }
            else{
                startService(new Intent(MainActivity.this, LayerService.class));
            }
        }
        else {
            startService(new Intent(MainActivity.this, LayerService.class));
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
