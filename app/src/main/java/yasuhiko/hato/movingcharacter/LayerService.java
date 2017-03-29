package yasuhiko.hato.movingcharacter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


/**
 * @author Yasuhikohato
 * @since 3/22, 2017
 */
public class LayerService extends Service {
    private final String LOG_TAG = "LayerService";
    private View mView;
    Point mDisplaySize;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private boolean mThreadFlag = true;
    private boolean mIsDragged = false;
    private float mMoveX = 1;
    private float mMoveY = 1;
    private float TRAVELING_TIME_MILLI_SEC = 3000;
    private float STOP_TIME_MILLI_SEC = 5000;
    private ImageView mCharacterImageView;

    public LayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start");
        super.onStartCommand(intent, flags, startId);

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplaySize = getDisplaySize();
        Log.d(LOG_TAG, mDisplaySize.toString());

        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // If TYPE_SYSTEM_OVERLAY, cannot be moved
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);


        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mView = layoutInflater.inflate(R.layout.overlay, null);
        mCharacterImageView = (ImageView)mView.findViewById(R.id.robot);

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(LOG_TAG, motionEvent.toString());

                int x = (int)motionEvent.getRawX();
                int y = (int)motionEvent.getRawY();
                int action = motionEvent.getAction();

                if(action == MotionEvent.ACTION_MOVE) {

                    int centerX = x - (mDisplaySize.x / 2);
                    int centerY = y - (mDisplaySize.y / 2);

                    mParams.x = centerX;
                    mParams.y = centerY;

                    mWindowManager.updateViewLayout(mView, mParams);
                    mIsDragged = true;
                }
                else if(action == MotionEvent.ACTION_UP) {
                    mIsDragged = false;
                    // TODO implement procedure to change the image
                }
                return false;
            }
        });

        mWindowManager.addView(mView, mParams);
        Log.d(LOG_TAG, "added view");


        final Handler handlerForMove = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run() {

                int count = 0;
                long start = System.currentTimeMillis();
                int previousMoveX = 1;
                while(mThreadFlag) {
                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;

                    if (!mIsDragged) {
                        if( elapsedTime < STOP_TIME_MILLI_SEC ){

                        }
                        else if( elapsedTime >= STOP_TIME_MILLI_SEC
                                && elapsedTime < STOP_TIME_MILLI_SEC + TRAVELING_TIME_MILLI_SEC){

                            if(elapsedTime == STOP_TIME_MILLI_SEC){
                                Log.d(LOG_TAG, "START Traveling");
                            }
                            if(elapsedTime % 100 != 0){
                                continue;
                            }

                            if(mParams.x < -mDisplaySize.x/2 || mParams.x > mDisplaySize.x/2) {
                                mMoveX = -mMoveX;
                            }
                            if(mParams.y < -mDisplaySize.y/2 || mParams.y > mDisplaySize.y/2){
                                mMoveY = -mMoveY;
                            }

                            // set left or right image
                            if(mMoveX < 0){
                                handlerForMove.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        mCharacterImageView.setImageResource(R.drawable.robot_head_b_mini_l);
                                    }
                                });
                            }
                            else{
                                handlerForMove.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        mCharacterImageView.setImageResource(R.drawable.robot_head_b_mini_r);
                                    }
                                });
                            }
                            mParams.x += mMoveX;
                            mParams.y += mMoveY;
                            //Log.d(LOG_TAG, String.valueOf(mParams.x) + ", " + String.valueOf(mParams.y));

                            handlerForMove.post(new Runnable() {
                                @Override
                                public void run() {
                                    mWindowManager.updateViewLayout(mView, mParams);
                                }
                            });
                        }
                        else{
                            start = System.currentTimeMillis();
                            Log.d(LOG_TAG, "STOP Traveling");
                        }
                    }
                }
                Log.d(LOG_TAG, "Moving thread finished");
            }
        }).start();



        //return START_STICKY; // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mView);
        mThreadFlag = false;
    }

    private Point getDisplaySize(){
        Display display = mWindowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }
}
