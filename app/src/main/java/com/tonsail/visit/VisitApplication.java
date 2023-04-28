package com.tonsail.visit;

import android.app.Application;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.tonsail.visit.utils.SpUtils;


public class VisitApplication extends MultiDexApplication {

    private static Application mApp;
    private static final String TAG = "VisitApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "18afd93137",false);

        String versionName = BuildConfig.VERSION_NAME;
        Log.e(TAG,"VisitApplication_Version Name--->"+versionName);

        String rootDir = MMKV.initialize(this);
        //启动服务
        Log.e(TAG, "onCreate: "+rootDir);
        SpUtils.getInstance();

        mApp = this;
    }


    public static Application getApp(){
        return mApp;
    }

}
