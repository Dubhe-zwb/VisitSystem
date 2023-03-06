package com.tonsail.visit.utils;

/**
 * 网络请求路径工具类
 */
public class HttpUrlUtil {


    /**
     * 请求路径
     */
    private static final String REQUEST_IP = "http://www.sztonsail.com";
    private static final String DOMAIN = ":9080";

    private static final String REQUEST_IP_TEST = "http://192.168.3.181";
    private static final String DOMAIN_TEST = ":8000";

    /**
     * 登录 8000 9080
     */
    public static String getLoginUrl(){
        return (getIsRelease() ? REQUEST_IP + DOMAIN :  REQUEST_IP_TEST + DOMAIN_TEST ) +"/auth/mlogin";
    }

    /**
     * 注册
     */

    public static String getRegister(){
        return (getIsRelease() ? REQUEST_IP : REQUEST_IP_TEST) + ":8013/register";
    }


    /**
     * 找回密码
     */
    public static String getForgotPassword(){
        return (getIsRelease() ? REQUEST_IP : REQUEST_IP_TEST) + ":8013/resetpass";
    }

    private static boolean getIsRelease(){
        return SPManager.getInstance().getBoolean(SPManager.IS_RELEASE, true);
    }

}
