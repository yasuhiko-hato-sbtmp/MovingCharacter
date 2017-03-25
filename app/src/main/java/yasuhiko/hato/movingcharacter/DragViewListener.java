package yasuhiko.hato.movingcharacter;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Yasuhikohato
 */

public class DragViewListener implements View.OnTouchListener {
    private View mDraggedView;

    private int mOldX;
    private int mOldY;

    public DragViewListener(View draggedView){
        mDraggedView = draggedView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // get touched position
        int x = (int)motionEvent.getRawX();
        int y = (int)motionEvent.getRawY();

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_MOVE:
                Log.d("DragViewListener", "ACTION_MOVE");
                int left = mDraggedView.getLeft() + (x - mOldX);
                int top = mDraggedView.getTop() + (y - mOldY);

                mDraggedView.layout(left, top, left + mDraggedView.getWidth(), top + mDraggedView.getHeight());
                break;
        }

        mOldX = x;
        mOldY = y;

        return true;
    }
}
