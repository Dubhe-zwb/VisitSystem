package com.tonsail.visit;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.tonsail.visit.model.User;
import com.tonsail.visit.utils.AgainRequest;
import com.tonsail.visit.utils.HttpUrlUtil;
import com.tonsail.visit.utils.HttpUtil;
import com.tonsail.visit.utils.IdInfo;
import com.tonsail.visit.utils.NetTest;
import com.tonsail.visit.utils.SPManager;
import com.tonsail.visit.utils.SpUtils;
import com.tonsail.visit.utils.Visitor;
import com.tonsail.visit.utils.encode.RSAUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 访客系统
 *
 * @author: zwb
 * @date: 2023.2.8
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, AgainRequest {

    private TextView id_pwd;
    private TextView code;
    private TextView login;
    private EditText id;
    private EditText pwd;
    private static final String TAG = "zwb";
    private int tryFlag;
    private ImageView exit;
    private View lineLeft;
    private View lineRight;
    private ImageView pwdVisible;
    private boolean pwdFlag;
    private String name;
    private String userStrTemp;
    private ImageView saveBtn;
    private boolean selectFlag;
    private long timeTest;//登陆时间测试
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int machineFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        android.view.WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//        layoutParams.gravity = Gravity.CENTER;
//        layoutParams.width = DisplayUtil.dip2px(360, this);
//        layoutParams.height = DisplayUtil.dip2px(400, this);
//        getWindow().setAttributes(layoutParams);

        initView();

        initData();
    }

    public void initView() {
        pwdVisible = findViewById(R.id.show_password_switch);
        lineLeft = findViewById(R.id.line_left);
        lineRight = findViewById(R.id.line_right);
        exit = findViewById(R.id.exit);
        id_pwd = findViewById(R.id.id_pwd);
        code = findViewById(R.id.code);
        login = findViewById(R.id.login);
        id = findViewById(R.id.id);
        pwd = findViewById(R.id.pwd);
        saveBtn = findViewById(R.id.select_bkg);

        pwdVisible.setOnClickListener(this);
        exit.setOnClickListener(this);
        id_pwd.setOnClickListener(this);
        code.setOnClickListener(this);
        login.setOnClickListener(this);
        id.setOnClickListener(this);
        pwd.setOnClickListener(this);
        pwd.setTypeface(Typeface.DEFAULT);
        saveBtn.setOnClickListener(this);

        id_pwd.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.press_text));
        code.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.orignal_text));

        pwdChanged();
    }

    public void pwdChanged() {
        pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(id.getText()) && !TextUtils.isEmpty(pwd.getText())) {
                    login.setBackgroundResource(R.drawable.shape_login_bg_enable);
                } else {
                    login.setBackgroundResource(R.drawable.shape_login_bg);
                }

                //特殊切换环境入口
                String pwd = s.toString().trim();
                if (TextUtils.equals(pwd, "test@tonsail") && TextUtils.isEmpty(id.getText())) {
                    SPManager.getInstance().putBoolean(SPManager.IS_RELEASE, false);
                    Toast.makeText(MainActivity.this, "已进入test模式", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.equals(pwd, "release@tonsail") && TextUtils.isEmpty(id.getText())) {
                    SPManager.getInstance().putBoolean(SPManager.IS_RELEASE, true);
                    Toast.makeText(MainActivity.this, "已进入release模式", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void initData() {
        judgeMachine();

    }

    public void judgeMachine() {
        float density = getResources().getDisplayMetrics().density;
        Log.e(TAG, "zwbonCreate: " + density);
        if (density == 1.5)
            machineFlag = 1;//848
        else if (density == 2.0)
            machineFlag = 2;//2853
        else if (density == 3.0)
            machineFlag = 3;//9666
        Log.e(TAG, "judgeMachine: "+machineFlag);
        SpUtils.encode("machineFlag",machineFlag);
    }

    public void login() {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("username", id.getText().toString());
        final String password = pwd.getText().toString();
        PublicKey publicKey = null;
        try {
            publicKey = RSAUtils.loadPublicKey(RSAUtils.PUBLIC_KEY);
        } catch (Exception e) {
            Log.e(TAG, "login: publicKey" + e.getMessage());
        }
        if (publicKey != null) {
            String encryptPassword = null;
            try {
                encryptPassword = RSAUtils.encryptData(password.getBytes(), publicKey);
            } catch (Exception e) {
                Log.e(TAG, "login: encryptPassword" + e.getMessage());
            }
            requestParams.put("password", encryptPassword);
            //code固定值
            requestParams.put("code", "256953efee6948a9b0c264e587c97313");
            RequestBody formBody = RequestBody
                    .create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(requestParams));

            final String finalEncryptPassword = encryptPassword;
            HttpUtil.sendOkHttpPostRequest(HttpUrlUtil.getLoginUrl(), formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e(TAG, "error: sendOkHttpPostRequest---onFailure" + e.getMessage());
                    showExceptionHint();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.body() == null) {
                        Log.e(TAG, "onResponse: login_response.body()==null");
                        loginBtnEnable();
                        return;
                    }
                    String result = response.body().string();
//                    Log.e(TAG + " onResponse---result", result);
                    Map<Object, Object> resultMap = null;
                    Gson gson = new Gson();
                    try {
                        resultMap = gson.fromJson(result, new TypeToken<Map<Object, Object>>() {
                        }.getType());
                    } catch (JsonSyntaxException ex) {
                        Log.e(TAG, "resultMap: error" + ex.getMessage());
                        showExceptionHint();
                        return;
                    }
                    Log.e(TAG, "onResponse: resultMap--->" + resultMap);
                    if (response.isSuccessful()) {
                        //获取登录成功返回的token
                        if (resultMap.containsKey("user")) {
                            String userMapStr = gson.toJson(resultMap.get("user"));
                            Map<Object, Object> userMap = null;
                            try {
                                userMap = gson.fromJson(userMapStr, new TypeToken<Map<Object, Object>>() {
                                }.getType());
                            } catch (JsonSyntaxException e) {
                                Log.e(TAG, "onResponse: resultMap.get(\"user\")_json_error" + e.getMessage());
                                exceptionHint("resultMap.get(\"user\")_json_error");
                                return;
                            }
//                            Log.e(TAG, "onResponse: userMap" + userMap);
                            User currentUser = null;
                            if (userMap.containsKey("user")) {   //获取登录User的基本信息
                                String userStr = gson.toJson(userMap.get("user"));
                                userStrTemp = userStr;
                                try {
                                    currentUser = new Gson().fromJson(userStr, User.class);
                                } catch (JsonSyntaxException e) {
                                    Log.e(TAG, "onResponse: userMap.get(\"user\")_json_error" + e.getMessage());
                                    exceptionHint("userMap.get(\"user\")");
                                    return;
                                }
                            }
                            if (resultMap.containsKey("token")) {    //获取登录账号的token
                                if (currentUser == null) {
                                    currentUser = new User();
                                }
                                currentUser.setToken((String) resultMap.get("token"));
                                SPManager.getInstance().putString("token", (String) resultMap.get("token"));

                            }
                            currentUser.setAccount(id.getText().toString());
                            currentUser.setPassword(finalEncryptPassword);
//                            UserDao.getInstance().add(currentUser);暂不存储用户
//                            Log.e(TAG, "onResponse: 789" + userMap.get("user"));

                            Map<Object, Object> o = null;//获取登录账号所属公司
                            try {
                                o = gson.fromJson(userStrTemp, new TypeToken<Map<Object, Object>>() {
                                }.getType());
                            } catch (JsonSyntaxException e) {
                                Log.e(TAG, "onResponse: userStrTemp_json_error" + e.getMessage());
                                exceptionHint("userStrTemp_json_error");
                                return;
                            }
                            if (o.containsKey("org")) {
                                String org = gson.toJson(o.get("org"));
                                IdInfo idInfo = null;
                                try {
                                    idInfo = new Gson().fromJson(org, IdInfo.class);
                                } catch (JsonSyntaxException e) {
                                    Log.e(TAG, "onResponse: o.get(\"org\")_json_error" + e.getMessage());
                                    exceptionHint("o.get(\"org\")");
                                    return;
                                }
                                name = idInfo.getName();
//                                Log.e(TAG, "onResponse123: " + name);

                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    Log.e("test11", "run: 11---->" + (System.currentTimeMillis() - timeTest));
                                    Toast.makeText(MainActivity.this, getString(R.string.login_successful), Toast.LENGTH_LONG)
                                            .show();
                                    overridePendingTransition(R.anim.anim_no, R.anim.anim_no);
                                    okhttpRequest();

                                }
                            });
                        }
                    } else {
                        if (resultMap.containsKey("message")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    login.setBackgroundResource(R.drawable.shape_login_bg_enable);
                                    login.setEnabled(true);
                                    Toast.makeText(MainActivity.this, getString(R.string.login_faliure), Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }
                    }

                }
            });
        }
    }

    public void exceptionHint(String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                login.setBackgroundResource(R.drawable.shape_login_bg_enable);
                login.setEnabled(true);
                Toast.makeText(MainActivity.this, info + "_解析异常，请联系管理员", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginBtnEnable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                login.setBackgroundResource(R.drawable.shape_login_bg_enable);
                login.setEnabled(true);
            }
        });
    }

    private void showExceptionHint() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                login.setBackgroundResource(R.drawable.shape_login_bg_enable);
                login.setEnabled(true);
                Toast.makeText(MainActivity.this, getString(R.string.server_exception), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * 登录前进行网络检测
     * 网络检测工具类必须在子线程执行
     */
    public void testNetwork() {
        login.setBackgroundResource(R.drawable.shape_login_bg);
        login.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isAvailableByDns = NetTest.isAvailableByDns();
                if (isAvailableByDns) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.e(TAG, "run: " + "网络畅通");
                            login();
                        }
                    });
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            login.setBackgroundResource(R.drawable.shape_login_bg_enable);
                            login.setEnabled(true);
                            Log.e(TAG, "run: " + "网络存在问题");
                            Toast.makeText(MainActivity.this, "网络似乎存在问题...", Toast.LENGTH_SHORT).show();
                        }
                    }, 1000);
                }
            }
        }).start();
    }

    public void okhttpRequest() {
        String token = SPManager.getInstance().getString("token", "");
        Log.e(TAG, "okhttpRequest: token---->" + token);
        if (TextUtils.isEmpty(token)) {
            Log.e(TAG, "okhttpRequest: token为空");
            loginBtnEnable();
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://192.168.3.181:8000/api/visitor/query?size=100000&sort=createtime,desc").addHeader("Authorization", token).get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure:  okhttpRequest---error");
                tryAgain();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) {
                    Log.e(TAG, "onResponse: response.body()==null");
                    tryAgain();
                    return;
                }
                String data = response.body().string();
//                Log.e(TAG, "onResponse: data" + data);
                //响应码可能是404也可能是200都会走这个方法
                if (response.isSuccessful()) {
                    long time = System.currentTimeMillis();
                    Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
                    Visitor visitor = null;
                    try {
                        visitor = gson.fromJson(data, Visitor.class);
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "onResponse: visitor_json_error" + e.getMessage());
                        exceptionHint("visitor");
                        return;
                    }
//                    Log.e(TAG, "onResponse: visitor.getTotalElements" + visitor.getTotalElements());
//                    Log.e(TAG, "onResponse: gson.toJson" + gson.toJson(visitor));
//                    Log.e(TAG, "onResponse: json解析时间--->" + (System.currentTimeMillis() - time) / 1000.0 + "visitor-->" + visitor.getContent().size() + "**" + visitor.getTotalElements());
                    Intent intent = new Intent(MainActivity.this, ShowActivity.class);
                    intent.putExtra("visitor", visitor);
                    intent.putExtra("name", name);
                    intent.putExtra("token", token);
                    startActivity(intent);
//                    Log.e("test11", "run: 22---->" + (System.currentTimeMillis() - timeTest));
                    finish();
                }
            }
        });
    }

    public static void deleteUserName(Context context, String userName) {
        Uri uri_user = Uri.parse("content://com.tonsail.visit.CustomProvider/user_table");
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            return;
        }
        //resolver.delete(uri_user, null, null);
        Cursor cursor = resolver.query(uri_user, null, null, null);
        String url = "nickName" + "=?";
        try {
            if (cursor == null) return;
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //int columnIndex = cursor.getColumnIndex("nickName");
                    //String nickName = cursor.getString(columnIndex);
                    //Log.d(TAG, "delete nickName = " + nickName);
                    resolver.delete(uri_user, url, new String[]{userName});
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getUserName(Context context) {
        Uri uri_user = Uri.parse("content://com.tonsail.visit.CustomProvider/user_table");
        ContentResolver resolver = context.getContentResolver();
        String nickName = "";
        if (resolver != null) {
            try {
                Cursor cursor = resolver.query(uri_user, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int columnIndex = cursor.getColumnIndex("nickName");
                        nickName = cursor.getString(columnIndex);
                        Log.e(TAG, "user nickName..." + nickName);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "getUserName error..." + e.getMessage());
            }
        }
        return nickName;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.id_pwd) {
            id_pwd.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.press_text));
            code.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.orignal_text));
            lineLeft.setVisibility(View.VISIBLE);
            lineRight.setVisibility(View.INVISIBLE);
        }
        if (v.getId() == R.id.code) {
            code.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.press_text));
            id_pwd.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.orignal_text));
            lineLeft.setVisibility(View.INVISIBLE);
            lineRight.setVisibility(View.VISIBLE);
        }
        if (v.getId() == R.id.login) {
            if (TextUtils.isEmpty(id.getText()) || TextUtils.isEmpty(pwd.getText())) return;
            timeTest = System.currentTimeMillis();

            testNetwork();
        }
        if (v.getId() == R.id.exit) {
            finish();
        }
        if (v.getId() == R.id.show_password_switch) {
            if (!pwdFlag) {
                pwdFlag = true;
                pwdVisible.setBackgroundResource(R.drawable.visible);
                pwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                pwdFlag = false;
                pwdVisible.setBackgroundResource(R.drawable.invisible);
                pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            }
            pwd.setTypeface(Typeface.DEFAULT);//此行可以避免明文和密文转变时，造成密文字符间距改变
            pwd.setSelection(pwd.getText().length());//使光标移动到行尾
        }
        /* 暂时屏蔽记住账号
        if (v.getId() == R.id.select_bkg) {
            if (!selectFlag) {
                saveBtn.setBackgroundResource(R.drawable.select);
                selectFlag = true;
            } else {
                saveBtn.setBackgroundResource(R.drawable.un_select);
                selectFlag = false;
            }
        }*/


    }

    @Override
    public void tryAgain() {
        if (tryFlag <= 10) {
            Log.e(TAG, "tryAgain: " + tryFlag);
            okhttpRequest();
            tryFlag++;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginBtnEnable();
                    Toast.makeText(MainActivity.this, "访客数据获取失败，请退出后重试！", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}