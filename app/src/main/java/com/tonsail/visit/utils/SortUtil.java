package com.tonsail.visit.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SortUtil {
    private static final String TAG = "SortUtil_zwb";

    public static void sortDescTime(List<Visitor.ContentBean> converted) {
        Map<Integer, String> tempConvert = new HashMap<>();
        for (Visitor.ContentBean contentBean : converted) {
            tempConvert.put(contentBean.getId(), contentBean.getTime());
        }

        List<Map.Entry<Integer, String>> list = new ArrayList<>(tempConvert.entrySet());
        list.sort((o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));
        Map<Integer, String> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, String> integerStringEntry : sortedMap.entrySet()) {
            Log.e(TAG, "sortDescTime: id-->" + integerStringEntry.getKey() + "\n"
                    + "time-->" + integerStringEntry.getValue());
        }


    }

    public static Object[] Xtemp(Object[] X) {
        //得到旧数组零的个数
        int count = 0;
        for (Object x : X) {
            if ((int) x == 0) {
                count++;
            }
        }
        //定义新数组,由于这里需要得知新数组的长度，因此必须求出旧数组中零的个数
        Object[] newarr = new Object[X.length - count];
        //遍历原来的旧数组
        for (int i = 0, j = 0; i < X.length; i++) {
            //将旧数组中不等于0的元素赋给新数组
            if ((int) X[i] != 0) {
                newarr[j] = X[i];
                j++;
            }
        }
        return newarr;
    }

    public static String[] Ytemp(String[] Y) {
        //得到旧数组零的个数
        int count = 0;
        for (String s : Y) {
            if ("".equals(s)) {
                count++;
            }
        }
        //定义新数组,由于这里需要得知新数组的长度，因此必须求出旧数组中零的个数
        String[] newarr = new String[Y.length - count];
        //遍历原来的旧数组
        for (int i = 0, j = 0; i < Y.length; i++) {
            //将旧数组中不等于0的元素赋给新数组
            if (!"".equals(Y[i])) {
                newarr[j] = Y[i];
                j++;
            }
        }
        return newarr;
    }
}
