package com.lenovo.dingjq1.globalexceptioncatcher;

import android.app.Application;

/**
 * Created by dingjq on 2017/10/24.
 */

public class CrashApplication extends Application {
    private CrashHandler mCrashHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(this);
    }
}
