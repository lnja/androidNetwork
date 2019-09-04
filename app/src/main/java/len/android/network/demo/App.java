package len.android.network.demo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.HashMap;
import java.util.Map;

import len.android.basic.AppBase;
import len.android.network.HttpCacheWrapper;
import len.android.network.RetrofitWrapper;
import len.tools.android.AndroidUtils;
import len.tools.android.JsonInterfaceCheck;
import len.tools.android.Log;
import len.tools.android.StorageUtils;
import len.tools.android.extend.LnjaCsvFormatStrategy;

import static len.android.network.demo.Config.SERVER_HOST_360;
import static len.android.network.demo.Config.SERVER_HOST_TAOBAO;

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
                initJsonCheck();
                initLog();
//                initCrash();
//                initRetrofit();
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

    private void initJsonCheck(){
        JsonInterfaceCheck.setCheckable(BuildConfig.DEBUG);
    }

    private void initLog(){
        if(BuildConfig.DEBUG){
            Log.init("lnja",android.util.Log.VERBOSE);
        }else {
            Log.init("lnja",android.util.Log.INFO);
        }

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("lnja")
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                if(BuildConfig.DEBUG){
                    return true;
                }else {
                    if(priority < Logger.INFO){
                        return false;
                    }else {
                        return true;
                    }
                }
            }
        });
        FormatStrategy csvFormatStrategy = LnjaCsvFormatStrategy.newBuilder()
                .tag("lnja")
                .logPath(StorageUtils.getExtendDir(this,"logs").getAbsolutePath())
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(csvFormatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                if(BuildConfig.DEBUG){
                    return true;
                }else {
                    return false;
                }
            }
        });
    }

    public void initRetrofit360() {
        len.android.network.Config.isDebug(BuildConfig.DEBUG);
        Map<String, String> headerParms = new HashMap<>();
        headerParms.put("Connection", "keep-alive");
        RetrofitWrapper.getInstance().init(SERVER_HOST_360, headerParms);
        HttpCacheWrapper.instance().initDiskCache(this);
    }

    public void initRetrofitTaobao() {
        len.android.network.Config.isDebug(BuildConfig.DEBUG);
        Map<String, String> headerParms = new HashMap<>();
        headerParms.put("Connection", "keep-alive");
        RetrofitWrapper.getInstance().init(SERVER_HOST_TAOBAO, headerParms);
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