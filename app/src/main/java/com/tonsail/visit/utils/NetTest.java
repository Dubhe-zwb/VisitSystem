package com.tonsail.visit.utils;

import static android.Manifest.permission.INTERNET;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.RequiresPermission;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetTest {
    /**
     *
     *根据ping百度判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkAvailable(final Context context) {
        long l = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        try {
            Process pingProcess = runtime.exec("/system/bin/ping -c 1 www.baidu.com");
            int exitCode = pingProcess.waitFor();
            return (exitCode == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据DNs解析判断网络是否可用
     *
     * @return
     */
    @RequiresPermission(INTERNET)
    public static boolean isAvailableByDns() {
        return isAvailableByDns("");
    }
    @RequiresPermission(INTERNET)
    public static boolean isAvailableByDns(final String domain) {
        final String realDomain = TextUtils.isEmpty(domain) ? "www.baidu.com" : domain;
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(realDomain);
            return inetAddress != null;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }
}
