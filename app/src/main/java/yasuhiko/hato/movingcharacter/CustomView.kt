package yasuhiko.hato.movingcharacter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View

/**
 * @author Yasuhikohato
 * @since 3/3, 2017
 */
class CustomView(context: Context?) : View(context) {
    private var mDx = 5f
    private var mDy = 5f
    private var mXOffset = 0f
    private var mYOffset = 0f // top left corner coordinate of drawn image
    private var mScreenWidth = 0f
    private var mScreenHeight = 0f
    private var mIsAttached = false
    private var mIsDragged = false

    private val res = this.context.resources
    private val bell: Bitmap = BitmapFactory.decodeResource(res, R.drawable.robot_head_b)

    init {
        isFocusable = true
    }

    fun setDraggedStatus(status: Boolean) {
        mIsDragged = status
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mScreenWidth = w.toFloat()
        mScreenHeight = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val imageCoordinate = Rect(
            mXOffset.toInt(),
            mYOffset.toInt(),
            (mXOffset + IMAGE_WIDTH).toInt(),
            (mYOffset + IMAGE_HEIGHT).toInt()
        )
        canvas.drawBitmap(bell, null, imageCoordinate, null)
    }

    override fun onAttachedToWindow() {
        val handler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (!mIsDragged) {
                    if (mIsAttached) {
                        if (mXOffset < 0 || mXOffset + IMAGE_WIDTH > mScreenWidth) {
                            mDx = -mDx
                        }
                        if (mYOffset < 0 || mYOffset + IMAGE_HEIGHT > mScreenHeight) {
                            mDy = -mDy
                        }
                        mXOffset += mDx
                        mYOffset += mDy
                        invalidate()
                        sendEmptyMessageDelayed(0, DELAY_MILLISEC)
                    }
                }
            }
        }
        mIsAttached = true
        handler.sendEmptyMessageDelayed(0, DELAY_MILLISEC)
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        mIsAttached = false
        super.onDetachedFromWindow()
    }

    companion object {
        private const val IMAGE_WIDTH = 200f
        private const val IMAGE_HEIGHT = 200f
        private const val DELAY_MILLISEC = 1L
    }
}
