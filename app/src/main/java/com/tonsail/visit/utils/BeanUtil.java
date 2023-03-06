package com.tonsail.visit.utils;

import java.lang.reflect.Method;

/**
 * Java Bean工具类
 */
public class BeanUtil {

    public static Method findMethod(String setMethodName,
                                    Method[] declaredMethods) {
        Method result = null ;
        for (Method method : declaredMethods) {
            if(method.getName().equalsIgnoreCase(setMethodName)){
                result = method ;
            }
        }
        return result ;
    }

}
