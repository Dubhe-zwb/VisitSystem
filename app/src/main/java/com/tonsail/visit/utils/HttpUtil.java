package com.tonsail.visit.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 网络请求工具类
 */
public class HttpUtil {
    private static OkHttpClient okHttpClient;
    private static Request.Builder builder;

    static {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
        builder = new Request.Builder();
    }

    /**
     * 发送OkHttp GET 请求的方法
     *
     * @param url      url形式参数
     * @param callback 回调
     */
    public static void sendOkHttpGetRequest(String url, Callback callback) {
        Request request = builder.url(url).build();
        okHttpClient.newCall(request).enqueue(callback);
    }


    /**
     * 发送OkHttp POST 异步请求的方法
     *
     * @param urlAddress  url地址
     * @param requestBody 请求体
     * @param callback    回调
     */
    public static void sendOkHttpPostRequest(String urlAddress, RequestBody requestBody, Callback callback) {
        Request request = builder.url(urlAddress).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(callback);
    }


    /**
     * 发送OkHttp 数据流 请求的方法
     * @param urlAddress
     * @param requestBody
     * @param callback
     */
    public static void sendMultipart(String urlAddress, RequestBody requestBody, Callback callback) {
        //这里根据需求传，不需要可以注释掉
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("title", "wangshu")
//                .addFormDataPart("image", "wangshu.jpg",
//                        RequestBody.create(MEDIA_TYPE_PNG, new File("/sdcard/wangshu.jpg")))
//                .build();
//        private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        Request request = builder.header("Authorization", "Client-ID " + "...").
                url(urlAddress).
                post(requestBody).
                build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
