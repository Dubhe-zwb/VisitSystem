package com.tonsail.visit.utils;

import android.gesture.GestureUtils;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class TimeJudge {
    private static final String TAG = "TimeJudge_zwb";

    /**
     * 去除返回数据的秒
     *
     * @param time
     * @return
     */
    public static String splitTime(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        try {
            Date parse = simpleDateFormat.parse(time);
            if (parse == null) {
                Log.e(TAG, "splitTime: pase--->null");
                return time;
            }
            return simpleDateFormat.format(parse);
        } catch (ParseException e) {
            Log.e(TAG, "splitTime: 解析异常");
            return time;
        }

    }

    /**
     * 返回年月日
     *
     * @param time
     * @return
     */
    public static String getDayDate(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date parse = simpleDateFormat.parse(time);
            if (parse == null) {
                Log.e(TAG, "splitTime: pase--->null");
                return time;
            }
            return simpleDateFormat.format(parse);
        } catch (ParseException e) {
            Log.e(TAG, "splitTime: 解析异常");
            return time;
        }

    }

    /**
     * 返回年月日对应的long millions
     *
     * @param time
     * @return
     */
    public static long getDateToLong(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date date = dateFormat.parse(time);
            if (date == null) {
                Log.e(TAG, "getDateToLong: date--->null");
                return 1685097600;
            }
            return date.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "getDateToLong: 解析异常");
            return 1685097600;
        }

    }

    /**
     * 返回long millions对应的年月日
     *
     * @param time
     * @return
     */
    public static String getLongToDate(long time) {
        Date monthAgo = new Date(time);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return ft.format(monthAgo);
    }

    /**
     * 返回小时
     *
     * @param time
     * @return
     */
    public static String getDayHours(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        try {
            Date parse = simpleDateFormat.parse(time);
            if (parse == null) {
                Log.e(TAG, "splitTime: pase--->null");
                return time;
            }
            String format = simpleDateFormat.format(parse);
            int i = format.indexOf(":");
            return format.substring(i - 2, i);
        } catch (ParseException e) {
            Log.e(TAG, "splitTime: 解析异常");
            return time;
        }

    }

    /**
     * 判断是否为今日
     *
     * @param time
     * @return
     */
    public static boolean judgeTimeIsToday(String time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        LocalDateTime localTime = LocalDateTime.parse(time, dtf);
        LocalDateTime startTime = LocalDate.now().atTime(0, 0, 0);
        LocalDateTime endTime = LocalDate.now().atTime(23, 59, 59);
        //如果大于今天的开始日期，小于今天的结束日期
        return localTime.isAfter(startTime) && localTime.isBefore(endTime);
    }

    /**
     * 判断当前日期是否为近一个月之内
     *
     * @param time
     * @return
     */
    public static boolean judgeAmonthRecent(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
        // 将LocalDateTime转化为Instant对象
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        long millis = instant.toEpochMilli();

//        Long currentTime = System.currentTimeMillis(); //获取当前时间
//        Long oneMonthAgo = currentTime - 30 * 24 * 60 * 60 * 1000L; //计算一个月前的时间
//        Long specifiedTime = millis; //指定时间的毫秒数

        LocalDateTime startTime = LocalDate.now().atTime(0, 0, 0);
        LocalDateTime endTime = LocalDate.now().atTime(23, 59, 59);

        Instant instant1 = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant instant2 = endTime.atZone(ZoneId.systemDefault()).toInstant();

        long start = instant1.toEpochMilli();
        long end = instant2.toEpochMilli();
        long oneMonthAgo = start - 30 * 24 * 60 * 60 * 1000L; //计算一个月前的时间

        if (millis >= oneMonthAgo && millis <= end) {
            //指定时间在近一个月范围内
            return true;
        } else {
            //指定时间不在近一个月范围内
            return false;
        }
    }

    /**
     * 判断当前日期是否为近一周之内
     *
     * @param time
     * @return
     */
    public static boolean judgeAweekRecent(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
        // 将LocalDateTime转化为Instant对象
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        long millis = instant.toEpochMilli();

//        Long currentTime = System.currentTimeMillis(); //获取当前时间
//        Long oneMonthAgo = currentTime - 30 * 24 * 60 * 60 * 1000L; //计算一月前的时间
//        Long specifiedTime = millis; //指定时间的毫秒数

        LocalDateTime startTime = LocalDate.now().atTime(0, 0, 0);
        LocalDateTime endTime = LocalDate.now().atTime(23, 59, 59);

        Instant instant1 = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant instant2 = endTime.atZone(ZoneId.systemDefault()).toInstant();

        long start = instant1.toEpochMilli();
        long end = instant2.toEpochMilli();
        long oneMonthAgo = start - 7 * 24 * 60 * 60 * 1000L; //计算一周前的时间

        if (millis >= oneMonthAgo && millis <= end) {
            //指定时间在近一周范围内
            return true;
        } else {
            //指定时间不在近一周范围内
            return false;
        }
    }


    /**
     * 获取一月前0时的日期，今天的日期
     *
     * @return
     */
    public static String[] getAmonthAgoZeroHour() {
        LocalDateTime startTime = LocalDate.now().atTime(0, 0, 0);
        LocalDateTime endTime = LocalDate.now().atTime(23, 59, 59);

        Instant instant1 = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant instant2 = endTime.atZone(ZoneId.systemDefault()).toInstant();

        long start = instant1.toEpochMilli();
        long end = instant2.toEpochMilli();
        long oneMonthAgo = start - 30 * 24 * 60 * 60 * 1000L; //计算一个月前的时间

        Date monthAgo = new Date(oneMonthAgo);
        Date endDay = new Date(end);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        String format1 = ft.format(monthAgo);
        String format2 = ft.format(endDay);
        String dayDate1 = getDayDate(format1);
        String dayDate2 = getDayDate(format2);
        String[] back = new String[2];
        back[0] = dayDate1;
        back[1] = dayDate2;
        return back;
    }

    /**
     * 获取一周前0时的日期，今天的日期
     *
     * @return
     */
    public static String[] getAweekAgoZeroHour() {
        LocalDateTime startTime = LocalDate.now().atTime(0, 0, 0);
        LocalDateTime endTime = LocalDate.now().atTime(23, 59, 59);

        Instant instant1 = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant instant2 = endTime.atZone(ZoneId.systemDefault()).toInstant();

        long start = instant1.toEpochMilli();
        long end = instant2.toEpochMilli();
        long oneMonthAgo = start - 7 * 24 * 60 * 60 * 1000L; //计算一个月前的时间

        Date monthAgo = new Date(oneMonthAgo);
        Date endDay = new Date(end);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        String format1 = ft.format(monthAgo);
        String format2 = ft.format(endDay);
        String dayDate1 = getDayDate(format1);
        String dayDate2 = getDayDate(format2);
        String[] back = new String[2];
        back[0] = dayDate1;
        back[1] = dayDate2;
        return back;
    }

    /**
     * 判断离开时间和到达时间差
     * Long
     *
     * @return
     */
    public static int getStayTime(String arriveTime, String leaveTime) {
        Log.d(TAG, "getStayTime: arriveTime--->" + arriveTime + "---" + "leaveTime--->" + leaveTime);
        if (TextUtils.isEmpty(arriveTime) || TextUtils.isEmpty(leaveTime)) return -1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date arrive = null;
        Date leave = null;
        long timeArrive = 0L, timeLeave = 0L;
        try {
            arrive = sdf.parse(arriveTime);
            leave = sdf.parse(leaveTime);
            if (arrive == null || leave == null) {
                Log.e(TAG, "getStayTime: arrive==null||leave==null");
                return -1;
            }
            timeArrive = arrive.getTime();
            timeLeave = leave.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "getStayTime: " + "时间解析失败");
            return -1;
        }

        if (timeLeave > timeArrive) {
            long result = timeLeave - timeArrive;
            if (result >= 0 && result <= 3600000) return 1;
            if (result > 3600000 && result <= 7200000) return 2;
            if (result > 7200000 && result <= 10800000) return 3;
            if (result > 10800000 && result <= 14400000) return 4;
            if (result > 14400000 && result <= 18000000) return 5;
            if (result > 18000000 && result <= 21600000) return 6;
            else return 7;
        } else {
            Log.e(TAG, "getStayTime: timeLeave<=timeArrive");
            return -1;
        }

    }
}
