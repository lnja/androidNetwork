package len.android.network.demo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import len.android.basic.AppBase;
import len.android.network.HttpCacheWrapper;
import len.android.network.RetrofitWrapper;
import len.tools.android.AndroidUtils;
import len.tools.android.Log;

import java.util.HashMap;
import java.util.Map;

public class App extends AppBase implements Thread.UncaughtExceptionHandler {
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("APP -> onCreate");
        instance = this;
        String processName = AndroidUtils.getProcessName(this, android.os.Process.myPid());
        if (processName != null) {
            boolean defaultProcess = processName.equals(getPackageName());
            if (defaultProcess) {
                Log.enableLog(BuildConfig.DEBUG);
//                initCrash();
                initRetrofit();
            } else if (processName.endsWith(":other")) {

            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e("APP -> onTerminate");
        destroy();
    }

    private void destroy() {
        RetrofitWrapper.getInstance().release();
    }

    private void initRetrofit() {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        Map<String, String> headerParms = new HashMap<>();
        headerParms.put("Connection", "keep-alive");
        RetrofitWrapper.getInstance().init(Config.SERVER_HOST, headerParms);
        HttpCacheWrapper.instance().initDiskCache(this);
    }

    private void initCrash() {
        try {
            Thread.setDefaultUncaughtExceptionHandler(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        relaunch(1);
    }

    public void relaunch(int status) {
        try {
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 100,
                    PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                            PendingIntent.FLAG_ONE_SHOT));
            System.exit(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}