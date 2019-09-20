package com.hzf.demo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;
import com.squareup.leakcanary.LeakCanary;


public class BaseApplication extends Application {

    private static Context appContext = null;

    public BaseApplication() {
        super();
    }

    public static void setAppContext(Context context) {
        appContext = context;
    }

    public static Context getAppContext() {
        return appContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        BaseApplication.setAppContext(getApplicationContext());
//        BlockCanary.install(this, new BlockCanaryContext()).start();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
    }
}

