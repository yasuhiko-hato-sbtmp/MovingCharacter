package yasuhiko.hato.movingcharacter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;


/**
 * @author Yasuhikohato
 * @since 3/22, 2017
 */
public class LayerService extends Service {
    View mView;
    WindowManager mWindowManager;

    public LayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LayerService", "Start");
        super.onStartCommand(intent, flags, startId);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        //mView = layoutInflater.inflate(R.layout.overlay, null);
        mView = new CustomView(getApplicationContext());

        mWindowManager.addView(mView, params);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mView);
    }
}
