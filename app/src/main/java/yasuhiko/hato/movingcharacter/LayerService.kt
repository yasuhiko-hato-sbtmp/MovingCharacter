package yasuhiko.hato.movingcharacter

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView

/**
 * @author Yasuhikohato
 * @since 3/22, 2017
 */
class LayerService : Service() {
    private val LOG_TAG = "LayerService"
    private var mView: View? = null
    private var mGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var mDisplaySize: Point? = null
    private var mWindowManager: WindowManager? = null
    private var mParams: WindowManager.LayoutParams? = null
    private var mThreadFlag = true
    private var mIsDragged = false
    private val TRAVELING_TIME_MILLI_SEC = 3000f
    private var mCharacterImageView: ImageView? = null
    private var mImageViewSize: Point? = null
    private val mHandlerForMove = Handler(Looper.getMainLooper())
    private var mValueAnimator: ValueAnimator? = null
    private var mMovingViewRunnable: Runnable? = null
    private var mGestureDetector: GestureDetector? = null

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "Start $LOG_TAG")
        super.onStartCommand(intent, flags, startId)

        mStarted = true
        val activityIntent = Intent(this, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val channelId = "moving_character_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Moving Character", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat)
                .build()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(startId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(startId, notification)
        }

        mWindowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplaySize = getDisplaySize()
        Log.d(LOG_TAG, "DisplaySize: ${mDisplaySize?.x} x ${mDisplaySize?.y}")

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        mParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        val layoutInflater = LayoutInflater.from(this)
        mView = layoutInflater.inflate(R.layout.overlay, null)
        mCharacterImageView = mView?.findViewById<View>(R.id.robot) as ImageView
        changeImageViewImage(Constants.imageL)

        mGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            mImageViewSize = Point(mView?.width ?: 0, mView?.height ?: 0)
            Log.d(LOG_TAG, "ImageViewSize: ${mImageViewSize?.x} x ${mImageViewSize?.y}")
            removeOnGlobalLayoutListener(mView?.viewTreeObserver, mGlobalLayoutListener)
        }
        mView?.viewTreeObserver?.addOnGlobalLayoutListener(mGlobalLayoutListener)

        mGestureDetector = GestureDetector(mView?.context, object : OnGestureListener {
            override fun onDown(motionEvent: MotionEvent): Boolean {
                Log.d(LOG_TAG, "GD.onDown() $motionEvent")
                return false
            }

            override fun onShowPress(motionEvent: MotionEvent) {
                Log.d(LOG_TAG, "GD.onShowPress() $motionEvent")
            }

            override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                Log.d(LOG_TAG, "GD.onSingleTapUp() $motionEvent")
                if (mValueAnimator?.isRunning == true) {
                    mValueAnimator?.pause()
                }
                return false
            }

            override fun onScroll(
                motionEvent: MotionEvent?,
                motionEvent1: MotionEvent,
                v: Float,
                v1: Float
            ): Boolean {
                Log.d(LOG_TAG, "GD.onScroll() $motionEvent1")
                val action = motionEvent1.action
                val x = motionEvent1.rawX.toInt()
                val y = motionEvent1.rawY.toInt()

                if (action == MotionEvent.ACTION_MOVE) {
                    if (mValueAnimator?.isRunning == true) {
                        mValueAnimator?.pause()
                    }
                    if (!mIsDragged) {
                        changeImageViewImage(Constants.imageU)
                    }
                    val centerX = x - (mDisplaySize?.x ?: 0) / 2
                    val centerY = y - (mDisplaySize?.y ?: 0) / 2

                    mParams?.x = centerX
                    mParams?.y = centerY

                    mWindowManager?.updateViewLayout(mView, mParams)
                    mIsDragged = true
                } else if (action == MotionEvent.ACTION_UP) {
                    changeImageViewImage(Constants.imageL)
                    mIsDragged = false
                }
                return false
            }

            override fun onLongPress(motionEvent: MotionEvent) {
                Log.d(LOG_TAG, "GD.onLongPress() $motionEvent")
            }

            override fun onFling(
                motionEvent: MotionEvent?,
                motionEvent1: MotionEvent,
                v: Float,
                v1: Float
            ): Boolean {
                Log.d(LOG_TAG, "GD.onFling() $motionEvent1")
                return false
            }
        })

        mView?.setOnTouchListener(OnTouchListener { view, motionEvent ->
            val action = motionEvent.action
            mGestureDetector?.onTouchEvent(motionEvent)
            if (mIsDragged && action == MotionEvent.ACTION_UP) {
                changeImageViewImage(Constants.imageL)
                mIsDragged = false
            }
            false
        })

        mWindowManager?.addView(mView, mParams)

        mMovingViewRunnable = object : Runnable {
            override fun run() {
                if (mThreadFlag) {
                    mValueAnimator = setAnimation(
                        Point(mParams?.x ?: 0, mParams?.y ?: 0),
                        mDisplaySize ?: Point(0, 0),
                        mImageViewSize ?: Point(0, 0)
                    )
                    mValueAnimator?.addUpdateListener { valueAnimator ->
                        movingViewUpdateListenerLogic(valueAnimator)
                    }
                    mValueAnimator?.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {}
                        override fun onAnimationEnd(animator: Animator) {
                            mValueAnimator?.removeAllUpdateListeners()
                        }
                        override fun onAnimationCancel(animator: Animator) {}
                        override fun onAnimationRepeat(animator: Animator) {}
                    })
                    if (Constants.move) {
                        mValueAnimator?.start()
                    }
                    mHandlerForMove.postDelayed(
                        this,
                        Constants.movingTimeIntervalMilliSec + TRAVELING_TIME_MILLI_SEC.toLong()
                    )
                }
            }
        }
        mHandlerForMove.postDelayed(mMovingViewRunnable!!, Constants.movingTimeIntervalMilliSec)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestory()")
        super.onDestroy()
        mValueAnimator?.end()
        if (mView != null) {
            mWindowManager?.removeView(mView)
        }
        mThreadFlag = false
        mStarted = false
    }

    private fun getDisplaySize(): Point {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = mWindowManager?.currentWindowMetrics
            Point(metrics?.bounds?.width() ?: 0, metrics?.bounds?.height() ?: 0)
        } else {
            val display = mWindowManager?.defaultDisplay
            val point = Point()
            @Suppress("DEPRECATION")
            display?.getSize(point)
            point
        }
    }

    private fun removeOnGlobalLayoutListener(
        observer: ViewTreeObserver?,
        listener: ViewTreeObserver.OnGlobalLayoutListener?
    ) {
        if (observer == null || listener == null) return
        observer.removeOnGlobalLayoutListener(listener)
    }

    private fun changeImageViewImage(resourceId: Int) {
        mHandlerForMove.post { mCharacterImageView?.setImageResource(resourceId) }
    }

    private fun setAnimation(source: Point, displaySize: Point, viewSize: Point): ValueAnimator {
        val toX = (Math.random() * (displaySize.x - viewSize.x) - (displaySize.x / 2 - viewSize.x / 2)).toFloat()
        val toY = (Math.random() * (displaySize.y - viewSize.y) - (displaySize.y / 2 - viewSize.y / 2)).toFloat()

        if (toX > source.x) {
            changeImageViewImage(Constants.imageR)
        } else {
            changeImageViewImage(Constants.imageL)
        }

        val holderX = PropertyValuesHolder.ofFloat("translationX", source.x.toFloat(), toX)
        val holderY = PropertyValuesHolder.ofFloat("translationY", source.y.toFloat(), toY)
        val valueAnimator = ValueAnimator.ofPropertyValuesHolder(holderX, holderY)
        valueAnimator.duration = TRAVELING_TIME_MILLI_SEC.toLong()
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()

        Log.d(LOG_TAG, "Move: (${source.x}, ${source.y}) -> ($toX, $toY)")
        return valueAnimator
    }

    private fun movingViewUpdateListenerLogic(valueAnimator: ValueAnimator) {
        val x = valueAnimator.getAnimatedValue("translationX") as Float
        val y = valueAnimator.getAnimatedValue("translationY") as Float
        mParams?.x = x.toInt()
        mParams?.y = y.toInt()
        mWindowManager?.updateViewLayout(mView, mParams)
    }

    companion object {
        private var mStarted = false
        @JvmStatic
        fun isStarted(): Boolean {
            return mStarted
        }
    }
}
