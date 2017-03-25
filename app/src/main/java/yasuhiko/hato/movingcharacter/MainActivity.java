package yasuhiko.hato.movingcharacter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private int REQUEST_OVERLAY_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(new CustomView(this));
        setContentView(R.layout.activity_main);

        Button start = (Button)findViewById(R.id.start);
        start.setText("Start");
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCanDrawOverlays();
            }
        });
        Button stop = (Button)findViewById(R.id.stop);
        stop.setText("Stop");
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, LayerService.class));
            }
        });

        checkCanDrawOverlays();
    }

    private void checkCanDrawOverlays(){
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
            checkCanDrawOverlays();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
