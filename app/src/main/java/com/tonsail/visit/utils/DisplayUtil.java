package com.tonsail.visit.utils;

import android.content.Context;


public class DisplayUtil {

    public static int dip2px(float dpValue, Context context){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue,Context context){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
