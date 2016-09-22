package kiwi.jordancrawford.kiwiexplorer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Jordan on 22/09/16.
 */
public class BackgroundLocationService extends Service {
    private Date startTime;

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        System.out.println("On start command. Start time: " + DateFormat.getDateTimeInstance().format(startTime));

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startTime = new Date();

        System.out.println("Service created.");
    }

    @Override
    public void onDestroy() {
        System.out.println("Service destroyed. Start time: " + DateFormat.getDateTimeInstance().format(startTime));
    }
}
