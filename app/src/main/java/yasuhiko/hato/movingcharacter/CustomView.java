package yasuhiko.hato.movingcharacter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * @author Yasuhikohato
 * @since 3/3, 2017
 */

public class CustomView extends View {

    private static final float IMAGE_WIDTH = 200;
    private static final float IMAGE_HEIGHT = 200;
    private static final long delay_millisec = 1;

    private float mDx = 5, mDy = 5;
    private float mXOffset = 0, mYOffset = 0; // top left corner coordinate of drawn image

    private float mScreenWidth, mScreenHeight;
    private boolean mIsAttached;

    Resources res = this.getContext().getResources();
    Bitmap bell = BitmapFactory.decodeResource(res, R.drawable.robot_head_b);

    public CustomView(Context context){
        super(context);
        setFocusable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        mScreenWidth = w;
        mScreenHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        Paint paint = new Paint();
        //paint.setColor(Color.GREEN);

        Rect imageCoordinate = new Rect((int)mXOffset, (int)mYOffset, (int)(mXOffset + IMAGE_WIDTH), (int)(mYOffset + IMAGE_HEIGHT));
        canvas.drawBitmap(bell, null, imageCoordinate, null);
    }

    @Override
    protected void onAttachedToWindow(){
        Handler handler = new Handler(){
            public void handleMessage(Message msg){
                //Log.d("onAttachedToWindow", String.format("isAttached=%b", mIsAttached));
                if (mIsAttached){
                    if(mXOffset < 0 || mXOffset + IMAGE_WIDTH > mScreenWidth){
                        mDx = -mDx;
                    }
                    if(mYOffset < 0 || mYOffset + IMAGE_HEIGHT > mScreenHeight){
                        mDy = -mDy;
                    }

                    mXOffset += mDx;
                    mYOffset += mDy;
                    invalidate();
                    sendEmptyMessageDelayed(0, delay_millisec);
                }
            }
        };
        mIsAttached = true;
        handler.sendEmptyMessageDelayed(0, delay_millisec);

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow(){
        mIsAttached = false;
        super.onDetachedFromWindow();
    }
}
