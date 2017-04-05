package yasuhiko.hato.movingcharacter;


import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.view.ViewTreeObserver;


/**
 * @author Yasuhikohato
 * @since 3/22, 2017
 */
public class LayerService extends Service {
    private final String LOG_TAG = "LayerService";
    private View mView;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private Point mDisplaySize;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private boolean mThreadFlag = true;
    private boolean mIsDragged = false;
    private float mMoveX = 1;
    private float mMoveY = 1;
    private float TRAVELING_TIME_MILLI_SEC = 3000;
    private float STOP_TIME_MILLI_SEC = 5000;
    private ImageView mCharacterImageView;
    private Point mImageViewSize;
    final Handler mHandlerForMove = new Handler();
    private ValueAnimator mValueAnimator;
    private Runnable mMovingViewRunnable;

    public LayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start " + LOG_TAG);
        super.onStartCommand(intent, flags, startId);

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplaySize = getDisplaySize();
        Log.d(LOG_TAG, "DisplaySize: " + String.valueOf(mDisplaySize.x) + " x " + String.valueOf(mDisplaySize.y));

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
        changeImageViewImage(R.drawable.robot_b_l);

        // get width and height of robot imageView
        mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout() {
                mImageViewSize = new Point(mView.getWidth(), mView.getHeight());
                Log.d(LOG_TAG, "ImageViewSize: " + String.valueOf(mImageViewSize.x) + " x " + String.valueOf(mImageViewSize.y));
                removeOnGlobalLayoutListener(mView.getViewTreeObserver(), mGlobalLayoutListener);
            }
        };
        mView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);


        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(LOG_TAG, motionEvent.toString());

                int x = (int)motionEvent.getRawX();
                int y = (int)motionEvent.getRawY();
                int action = motionEvent.getAction();

                if(action == MotionEvent.ACTION_MOVE) {
                    if(mValueAnimator != null && mValueAnimator.isRunning()){
                        mValueAnimator.end();
                    }
                    changeImageViewImage(R.drawable.robot_b_u);

                    int centerX = x - (mDisplaySize.x / 2);
                    int centerY = y - (mDisplaySize.y / 2);

                    mParams.x = centerX;
                    mParams.y = centerY;

                    mWindowManager.updateViewLayout(mView, mParams);
                }
                else if(action == MotionEvent.ACTION_UP) {
                    changeImageViewImage(R.drawable.robot_b_l);
                }
                return false;
            }
        });

        mWindowManager.addView(mView, mParams);


        mMovingViewRunnable = new Runnable() {
            @Override
            public void run() {

                if(mThreadFlag) {
                    //mValueAnimator = setPropertyValuesHolderToValueAnimator(new Point(mParams.x, mParams.y), 45f, mDisplaySize.x / 4, (long) TRAVELING_TIME_MILLI_SEC);
                    mValueAnimator = setAnimation(new Point(mParams.x, mParams.y), mDisplaySize, mImageViewSize);
                    mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            movingViewUpdateListenerLogic(valueAnimator);
                        }
                    });
                    mValueAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mValueAnimator.removeAllUpdateListeners();
                            //Log.d(LOG_TAG, "onAnimationEnd()");
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    mValueAnimator.start();
                    mHandlerForMove.postDelayed(mMovingViewRunnable, (long)(STOP_TIME_MILLI_SEC + TRAVELING_TIME_MILLI_SEC));
                }
            }
        };
        mHandlerForMove.postDelayed(mMovingViewRunnable, (long)STOP_TIME_MILLI_SEC);



        /* old moving logic
        new Thread(new Runnable(){
            @Override
            public void run() {
                moveViewManually();
            }
        }).start();
        */


        //return START_STICKY; // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mValueAnimator != null) {
            mValueAnimator.end();
        }
        mWindowManager.removeView(mView);
        mThreadFlag = false;
    }

    private Point getDisplaySize(){
        Display display = mWindowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }


    private static void removeOnGlobalLayoutListener(ViewTreeObserver observer, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (observer == null) {
            return ;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            observer.removeGlobalOnLayoutListener(listener);
        } else {
            observer.removeOnGlobalLayoutListener(listener);
        }
    }


    private void changeImageViewImage(final int resourceId){
        mHandlerForMove.post(new Runnable(){
            @Override
            public void run() {
                mCharacterImageView.setImageResource(resourceId);
            }
        });
    }


    private ValueAnimator setAnimation(Point source, Point displaySize, Point viewSize){
//        int currentQuadrant = whichQuadrantInCenterCoordinateSys(source);
//
//        float toX = (float)(Math.random() * (displaySize.x / 2 - viewSize.x / 2));
//        float toY = (float)(Math.random() * (displaySize.y / 2 - viewSize.y / 2));
//        int nextQuadrant = (int)(Math.random() * 4) + 1;
//        if(nextQuadrant == 1){
//
//        }
//        else if(nextQuadrant == 2){
//            toY = -toY;
//        }
//        else if(nextQuadrant == 3){
//            toX = -toX;
//            toY = -toY;
//        }
//        else{
//            toX = -toX;
//        }

        float toX = (float)(Math.random() * (displaySize.x - viewSize.x) - (displaySize.x/2 - viewSize.x/2));
        float toY = (float)(Math.random() * (displaySize.y - viewSize.y) - (displaySize.y/2 - viewSize.y/2));

        if(toX > source.x){
            changeImageViewImage(R.drawable.robot_b_r);
        }
        else{
            changeImageViewImage(R.drawable.robot_b_l);
        }

        PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat("translationX", (float)source.x, toX);
        PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat("translationY", (float)source.y, toY);
        ValueAnimator valueAnimator = ValueAnimator.ofPropertyValuesHolder(holderX, holderY);
        valueAnimator.setDuration((long)TRAVELING_TIME_MILLI_SEC);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        //Log.d(LOG_TAG, "Move: " + String.valueOf(currentQuadrant) + "(" + String.valueOf(source.x) + ", " + String.valueOf(source.y) + ") -> " + String.valueOf(nextQuadrant) + "(" + String.valueOf(toX) + ", " + String.valueOf(toY) + ")");
        Log.d(LOG_TAG, "Move: " + "(" + String.valueOf(source.x) + ", " + String.valueOf(source.y) + ") -> " + "(" + String.valueOf(toX) + ", " + String.valueOf(toY) + ")");
        return valueAnimator;
    }

    private ValueAnimator setPropertyValuesHolderToValueAnimator(Point source, float degree, float distance, long duration ) {

        // 距離と角度から到達点となるX座標、Y座標を求めます
        float toX = (float) (distance * Math.cos(Math.toRadians(degree)));
        float toY = (float) (distance * Math.sin(Math.toRadians(degree)));

        // translationXプロパティを0fからtoXに変化させます
        PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat("translationX", (float)source.x, (float)source.x + toX);
        // translationYプロパティを0fからtoYに変化させます
        PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat("translationY", (float)source.y, (float)source.y + toY);
        // rotationプロパティを0fから360fに変化させます
        //PropertyValuesHolder holderRotaion = PropertyValuesHolder.ofFloat( "rotation", 0f, 360f );

        // targetに対してholderX, holderY, holderRotationを同時に実行させます
//        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
//                target, holderX, holderY, holderRotaion );
        ValueAnimator valueAnimator = ValueAnimator.ofPropertyValuesHolder(holderX, holderY);
        // duration秒かけて実行させます
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        return valueAnimator;
    }


    /**
     *
     * @param p
     * @return Quadrant whose coordinate system's original point is the center of the display.
     */
    private int whichQuadrantInCenterCoordinateSys(Point p){
        if(p.x >= 0 && p.y >= 0){
            return 1;
        }
        else if(p.x >= 0 && p.y < 0){
            return 2;
        }
        else if(p.x < 0 && p.y < 0){
            return 3;
        }
        else{
            return 4;
        }
    }

    private void movingViewUpdateListenerLogic(ValueAnimator valueAnimator){
        float x = (float)valueAnimator.getAnimatedValue("translationX");
        float y = (float)valueAnimator.getAnimatedValue("translationY");
        //Log.d(LOG_TAG, "current coordinate: (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");
        mParams.x = (int)x;
        mParams.y = (int)y;
        mWindowManager.updateViewLayout(mView, mParams);
    }


    /**
     * Old moving logic
     * @deprecated
     */
    private void moveViewManually(){
        long start = System.currentTimeMillis();
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

                    if(mParams.x - mImageViewSize.x/2 < -mDisplaySize.x/2 || mParams.x + mImageViewSize.x/2 > mDisplaySize.x/2) {
                        mMoveX = -mMoveX;
                    }
                    if(mParams.y - mImageViewSize.y/2 < -mDisplaySize.y/2 || mParams.y + mImageViewSize.y/2 > mDisplaySize.y/2){
                        mMoveY = -mMoveY;
                    }

                    // set left or right image
                    if(mMoveX < 0){
                        changeImageViewImage(R.drawable.robot_b_l);
                    }
                    else{
                        changeImageViewImage(R.drawable.robot_b_r);
                    }
                    mParams.x += mMoveX;
                    mParams.y += mMoveY;
                    //Log.d(LOG_TAG, String.valueOf(mParams.x) + ", " + String.valueOf(mParams.y));

                    mHandlerForMove.post(new Runnable() {
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

}
