package com.tonsail.visit;

import android.app.Application;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;

public class VisitApplication extends Application {

    private static Application mApp;
    private static final String TAG = "VisitApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "18afd93137", BuildConfig.DEBUG);

        //启动服务
        Log.e(TAG, "onCreate: ");
        mApp = this;
    }


    public static Application getApp(){
        return mApp;
    }

}
