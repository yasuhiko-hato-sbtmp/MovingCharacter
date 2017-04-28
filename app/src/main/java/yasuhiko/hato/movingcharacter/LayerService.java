package yasuhiko.hato.movingcharacter;


import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.view.GestureDetector;
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
    private static boolean mStarted = false;
    private View mView;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private Point mDisplaySize;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private boolean mThreadFlag = true;
    private boolean mIsDragged = false;
    private final float TRAVELING_TIME_MILLI_SEC = 3000;
    private float STOP_TIME_MILLI_SEC = 5000;
    private ImageView mCharacterImageView;
    private Point mImageViewSize;
    final Handler mHandlerForMove = new Handler();
    private ValueAnimator mValueAnimator;
    private Runnable mMovingViewRunnable;
    private GestureDetector mGestureDetector;

    public LayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public static boolean isStarted(){
        return mStarted;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start " + LOG_TAG);
        super.onStartCommand(intent, flags, startId);


        // For foreground
        mStarted = true;
        Intent activityIntent = new Intent(this, SettingsActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // to call already existing activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat)
                .build();
        startForeground(startId, notification);


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
        //changeImageViewImage(R.drawable.robot_b_l);
        changeImageViewImage(Constants.imageL);

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


        mGestureDetector = new GestureDetector(mView.getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                Log.d(LOG_TAG, "GD.onDown() " + motionEvent.toString());
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {
                Log.d(LOG_TAG, "GD.onShowPress() " + motionEvent.toString());
            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                Log.d(LOG_TAG, "GD.onSingleTapUp() " + motionEvent.toString());
                if (mValueAnimator != null && mValueAnimator.isRunning()) {
                    mValueAnimator.pause();
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                Log.d(LOG_TAG, "GD.onScroll() " + motionEvent1.toString());
                int action = motionEvent1.getAction();
                int x = (int) motionEvent1.getRawX();
                int y = (int) motionEvent1.getRawY();

                if(action == MotionEvent.ACTION_MOVE) {
                    if (mValueAnimator != null && mValueAnimator.isRunning()) {
                        mValueAnimator.pause();
                    }
                    if(!mIsDragged) {
                        //changeImageViewImage(R.drawable.robot_b_u);
                        changeImageViewImage(Constants.imageU);
                    }
                    int centerX = x - (mDisplaySize.x / 2);
                    int centerY = y - (mDisplaySize.y / 2);

                    mParams.x = centerX;
                    mParams.y = centerY;

                    mWindowManager.updateViewLayout(mView, mParams);
                    mIsDragged = true;
                }
                else if (action == MotionEvent.ACTION_UP) { // never called here
                    //changeImageViewImage(R.drawable.robot_b_l);
                    changeImageViewImage(Constants.imageL);
                    mIsDragged = false;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                Log.d(LOG_TAG, "GD.onLongPress() " + motionEvent.toString());
            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                Log.d(LOG_TAG, "GD.onFling() " + motionEvent1.toString());
                return false;
            }
        });

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Log.d(LOG_TAG, motionEvent.toString());

                int action = motionEvent.getAction();

                mGestureDetector.onTouchEvent(motionEvent);
                if(mIsDragged && action == MotionEvent.ACTION_UP){
                    //changeImageViewImage(R.drawable.robot_b_l);
                    changeImageViewImage(Constants.imageL);
                    mIsDragged = false;
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
                    if(Constants.move) {
                        mValueAnimator.start();
                    }
                    //mHandlerForMove.postDelayed(mMovingViewRunnable, (long) (STOP_TIME_MILLI_SEC + TRAVELING_TIME_MILLI_SEC));
                    mHandlerForMove.postDelayed(mMovingViewRunnable, (long) (Constants.movingTimeIntervalMilliSec + TRAVELING_TIME_MILLI_SEC));
                }
            }
        };
        //mHandlerForMove.postDelayed(mMovingViewRunnable, (long) STOP_TIME_MILLI_SEC);
        mHandlerForMove.postDelayed(mMovingViewRunnable, Constants.movingTimeIntervalMilliSec);


        //return START_STICKY; // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestory()");
        super.onDestroy();
        if(mValueAnimator != null) {
            mValueAnimator.end();
        }
        mWindowManager.removeView(mView);
        mThreadFlag = false;
        mStarted = false;
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

        float toX = (float)(Math.random() * (displaySize.x - viewSize.x) - (displaySize.x/2 - viewSize.x/2));
        float toY = (float)(Math.random() * (displaySize.y - viewSize.y) - (displaySize.y/2 - viewSize.y/2));

        if(toX > source.x){
            //changeImageViewImage(R.drawable.robot_b_r);
            changeImageViewImage(Constants.imageR);
        }
        else{
            //changeImageViewImage(R.drawable.robot_b_l);
            changeImageViewImage(Constants.imageL);
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



    private void movingViewUpdateListenerLogic(ValueAnimator valueAnimator){
        float x = (float)valueAnimator.getAnimatedValue("translationX");
        float y = (float)valueAnimator.getAnimatedValue("translationY");
        //Log.d(LOG_TAG, "current coordinate: (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");
        mParams.x = (int)x;
        mParams.y = (int)y;
        mWindowManager.updateViewLayout(mView, mParams);
    }


}
