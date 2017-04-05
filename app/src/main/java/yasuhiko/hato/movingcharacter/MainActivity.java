package yasuhiko.hato.movingcharacter;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = "MainActivity";
    private int REQUEST_OVERLAY_CODE = 100;
    private ObjectAnimator mObjectAnimator;
    private boolean isPaused = false;

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
                    if(LayerService.isStarted()) {
                        stopService(new Intent(MainActivity.this, LayerService.class));
                    }
                }
            }
        });


//        ImageView iv = (ImageView)findViewById(R.id.robot);
//        iv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(mObjectAnimator.isRunning() && !isPaused){
//                    Log.d(LOG_TAG, "pause");
//                    mObjectAnimator.pause();
//                    isPaused = true;
//                }
////                else if(mObjectAnimator.isPaused()){
////                    mObjectAnimator.resume();
////                    Log.d(LOG_TAG, "resume");
////                }
//                else{
//                    mObjectAnimator.resume();
//                    isPaused = false;
//                    Log.d(LOG_TAG, "resume");
//                }
//            }
//        });
//        mObjectAnimator = animatePropertyValuesHolderSample(iv, 45, 1000, 2000);
//        mObjectAnimator.start();


    }

    /**
     * durationミリ秒かけてdegree角度とdistance距離の位置にターゲットを移動させる
     *
     * @param target
     * @param degree
     * @param distance
     * @param duration
     */
    private ObjectAnimator animatePropertyValuesHolderSample( ImageView target, float degree, float distance, long duration ) {

        // 距離と角度から到達点となるX座標、Y座標を求めます
        float toX = (float) ( distance * Math.cos( Math.toRadians( degree ) ) );
        float toY = (float) ( distance * Math.sin( Math.toRadians( degree ) ) );

        // translationXプロパティを0fからtoXに変化させます
        PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", 0f, toX );
        // translationYプロパティを0fからtoYに変化させます
        PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", 0f, toY );
        // rotationプロパティを0fから360fに変化させます
        //PropertyValuesHolder holderRotaion = PropertyValuesHolder.ofFloat( "rotation", 0f, 360f );

        // targetに対してholderX, holderY, holderRotationを同時に実行させます
//        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
//                target, holderX, holderY, holderRotaion );
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);

        // 2秒かけて実行させます
        objectAnimator.setDuration( duration );

        // アニメーションを開始します
        //objectAnimator.start();

        return objectAnimator;
    }


    private void checkCanDrawOverlaysAndStartMoving(){
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_CODE);
            }
            else{
                if(!LayerService.isStarted()) {
                    startService(new Intent(MainActivity.this, LayerService.class));
                }
            }
        }
        else {
            if(!LayerService.isStarted()) {
                startService(new Intent(MainActivity.this, LayerService.class));
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
