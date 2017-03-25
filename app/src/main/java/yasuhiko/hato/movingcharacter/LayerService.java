package yasuhiko.hato.movingcharacter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


/**
 * @author Yasuhikohato
 * @since 3/22, 2017
 */
public class LayerService extends Service {
    private final String LOG_TAG = "LayerService";
    private View mView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private boolean mIsLongClick = false;

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


        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // If TYPE_SYSTEM_OVERLAY, cannot be moved
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mView = layoutInflater.inflate(R.layout.overlay, null);
//        mView = new CustomView(getApplicationContext());

        //DragViewListener dragViewListener = new DragViewListener(mView);
        //mView.setOnTouchListener(dragViewListener);
        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(LOG_TAG, "onLongClick");
                mIsLongClick = true;
                return false;
            }
        });
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int x = (int)motionEvent.getRawX();
                int y = (int)motionEvent.getRawY();

                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        Log.d(LOG_TAG, "ACTION_MOVE");
                        //(CustomView)mView.setDraggedStatus(true);
                        //if(mIsLongClick) {
                            Log.d(LOG_TAG, "ACTION_MOVE, mIsLongClick");
                            Point displaySize = getDisplaySize();
                            int centerX = x - (displaySize.x / 2);
                            int centerY = y - (displaySize.y / 2);

                            mParams.x = centerX;
                            mParams.y = centerY;

                            mWindowManager.updateViewLayout(mView, mParams);
                        //}
                    case MotionEvent.ACTION_UP:
                        mIsLongClick = false;
                        Log.d(LOG_TAG, "ACTION_UP");
                }

                return false;
            }
        });

        mWindowManager.addView(mView, mParams);
        Log.d(LOG_TAG, "added view");


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mView);
    }

    private Point getDisplaySize(){
        Display display = mWindowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }
}
