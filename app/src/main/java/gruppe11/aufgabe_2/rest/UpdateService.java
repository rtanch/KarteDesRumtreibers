package gruppe11.aufgabe_2.rest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


/**
 * Holds timer which runs in specified intervals
 */
public class UpdateService extends Service implements Serializable {

    private final IBinder ibinder = new LocalService();
    private static Timer TIMER = null;
    private static Handler HANDLER = new Handler();
    private static Context CONTEXT = null;
    private boolean timerActive = false;

    private RestService restService = null;

    @Override
    public void onCreate() {
        CONTEXT = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return ibinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void start(Context context) {

        Log.d("DEBUGLOG-RS", "start update service");
        timerActive = true;
        if (TIMER != null) {
            TIMER.cancel();
            TIMER.purge();
        }

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("DEBUGLOG-RS", "Time value: " + sPref.getString("time", "10000"));
        int timeInterval = Integer.valueOf(sPref.getString("time", "10000"));

        TIMER = new Timer();
        Log.d("DEBUGLOG-US:", "Time interval:" + timeInterval);
        TimeDisplayTimerTask timeDisplayTimerTask = new TimeDisplayTimerTask();
        TIMER.schedule(timeDisplayTimerTask, 0, timeInterval);
    }

    public void stop() {
        Log.d("DEBUGLOG-RS", "stop update service");
        timerActive = false;
        TIMER.cancel();
        TIMER.purge();
    }

    public boolean isTimerActive() {
        return timerActive;
    }

    static class TimeDisplayTimerTask extends TimerTask implements Serializable {

        @Override
        public void run() {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new RestEvent(Event.UPDATE));
                }
            });
        }
    }

    public class LocalService extends Binder implements Serializable {

        public UpdateService getService() {
            return UpdateService.this;
        }
    }

}