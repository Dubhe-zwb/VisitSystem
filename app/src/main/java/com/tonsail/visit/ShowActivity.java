package com.tonsail.visit;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.tonsail.visit.adapter.AllDetail;
import com.tonsail.visit.adapter.AllInfo;
import com.tonsail.visit.adapter.TodayInfo;
import com.tonsail.visit.utils.DisplayUtil;
import com.tonsail.visit.utils.Key;
import com.tonsail.visit.utils.QRCode;
import com.tonsail.visit.utils.Visitor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "zwb_ShowActivity";

    private List<Visitor.ContentBean> converted = new ArrayList<>();//强转后,总数据
    private List<Visitor.ContentBean> todayUndone = new ArrayList<>();//今日待办，未完成
    private List<Visitor.ContentBean> todayFinshed = new ArrayList<>();//今日待办，已完成
    private List<Visitor.ContentBean> AllUndone = new ArrayList<>();//今日待办,未完成
    private List<Visitor.ContentBean> AllFished = new ArrayList<>();//今日待办，已完成
    private TextView companyTitle;
    private String name;//左上角账号所属公司
    private TextView bottomLeft;
    private TextView bottomRight;
    private TextView bottomRightLeft;
    private TextView bottomRightRight;
    private RecyclerView left_rcv;
    private RecyclerView right_rcv;
    private View viewDetail;
    private View viewDetailRight;
    private AllInfo allInfo;
    private TodayInfo todayInfo;
    private TextView detailLeft;
    private TextView detailRight;
    private int LeftrightOrLeft;//1左   2右
    private int RightrightOrLeft;//1左   2右
    private RecyclerView rcvAllDetail;
    private View allDetail;
    private View showContent;
    private AllDetail allDetailAdapter;
    private EditText exploreName;
    private String selectName;
    private String token;
    private ImageView qrCode;
    private View qrSet;
    private TextView scanText;
    private TextView registerText;
    private ImageView largeBtn;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int httpFlag;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (httpFlag > 3) {
                Log.e(TAG, "run: httpFlag---->" + "重试超时");
                Toast.makeText(ShowActivity.this, "重试失败，请重新登陆或者联系管理员！", Toast.LENGTH_SHORT).show();
                httpFlag = 0;
                return;
            }
            try {
                okhttpRequest();
            } catch (Exception e) {
                httpFlag++;
                Log.e(TAG, "run: httpFlag---->" + "正在重试" + httpFlag);
                Log.e(TAG, "run: okhttpRequest()_error" + e.getMessage());
                Toast.makeText(ShowActivity.this, "后台数据更新出错，正在重试！", Toast.LENGTH_SHORT).show();
            }
            handler.postDelayed(this, 1500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        toConvert();

        initView();

        initData();
    }

    private void initData() {
        Log.e(TAG, "initData: " + name);
        companyTitle.setText(name);

        for (Visitor.ContentBean contentBean : converted) {
            //临时将待接待标志改为 0
            Log.e(TAG, "initData: 过滤开始" + contentBean.getTime());
            if ((contentBean.getStatus() == 0)) {
                AllUndone.add(contentBean);
            } else if ((contentBean.getStatus() == 1)) {
                AllFished.add(contentBean);
            }
            if (judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 0)) {
                Log.e(TAG, "initData: 过滤结果" + contentBean.getTime());
                todayUndone.add(contentBean);
            } else if (judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 1)) {
                todayFinshed.add(contentBean);
            }
        }


        left_rcv = findViewById(R.id.left_rcv);
        right_rcv = findViewById(R.id.right_rcv);
        allInfo = new AllInfo();
        allInfo.setData(AllUndone);
        if (AllUndone.size() > 0) {
            viewDetail.setVisibility(View.VISIBLE);
        } else {
            viewDetail.setVisibility(View.INVISIBLE);
        }
        todayInfo = new TodayInfo();
        todayInfo.setData(todayUndone);
        if (todayUndone.size() > 0) {
            viewDetailRight.setVisibility(View.VISIBLE);
        } else {
            viewDetailRight.setVisibility(View.INVISIBLE);
        }

        left_rcv.setAdapter(allInfo);
        right_rcv.setAdapter(todayInfo);
        LeftrightOrLeft = 1;
        RightrightOrLeft = 1;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ShowActivity.this);
        LinearLayoutManager linearLayoutManagerRight = new LinearLayoutManager(ShowActivity.this);
        right_rcv.setLayoutManager(linearLayoutManager);
        left_rcv.setLayoutManager(linearLayoutManagerRight);

        rcvAllDetail = findViewById(R.id.rcv_all_detail);
        allDetailAdapter = new AllDetail();
        allDetailAdapter.setData(AllUndone);
        rcvAllDetail.setAdapter(allDetailAdapter);
        LinearLayoutManager linear = new LinearLayoutManager(ShowActivity.this);
        rcvAllDetail.setLayoutManager(linear);


        exploreName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                selectName = exploreName.getText().toString();
                Log.e(TAG, "afterTextChanged: " + selectName);
                allDetailAdapter.setExplorData(selectName);
            }
        });

        generateQRCode();

    }

    public void exceptionHint(String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ShowActivity.this, info + "_解析异常，请联系管理员", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 根据链接生成二维码
     */
    public void generateQRCode() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://192.168.3.181:8000/api/visitor/key").addHeader("Authorization", token).get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: generateQRCode");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) {
                    Log.e(TAG, "onResponse: generateQRCode()---->response.body()==null");
                    return;
                }
                String data = response.body().string();
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    Key key = null;
                    try {
                        key = gson.fromJson(data, Key.class);
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "onResponse: key_json_error" + e.getMessage());
                        exceptionHint("key_json_error");
                        return;
                    }
                    Log.e(TAG, "onResponse: key----->" + key);
                    String key1 = key.data.key;
                    Log.e(TAG, "onResponse: key1" + key1);
                    String url = "http://192.168.3.181:8020/visitor/visitorCreate?key=" + key1;
                    Bitmap QRCodeBitmap = QRCode.createQRCodeBitmap(url,
                            DisplayUtil.dip2px(92, ShowActivity.this),
                            DisplayUtil.dip2px(92, ShowActivity.this),
                            "UTF-8", "H", "1", Color.BLACK, Color.WHITE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Glide.with(ShowActivity.this).load(QRCodeBitmap).into(qrCode);
                            Glide.with(ShowActivity.this).load(QRCodeBitmap).into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    qrCode.setImageDrawable(resource);
                                    qrSet.setVisibility(View.VISIBLE);
                                    scanText.setVisibility(View.VISIBLE);
                                    registerText.setVisibility(View.VISIBLE);
                                    largeBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            customDialog(QRCodeBitmap);
                                        }
                                    });
                                    refreshData();
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                        }
                    });

                }
            }
        });
    }

    /**
     * 定时请求服务器获取访客数据
     */
    public void refreshData() {
        handler.postDelayed(runnable, 1000);
    }

    public void okhttpRequest() {
        Log.d(TAG, "okhttpRequest: token---->" + token);
        if (TextUtils.isEmpty(token)) {
            Log.d(TAG, "okhttpRequest: token为空");
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://192.168.3.181:8000/api/visitor/query?size=100&sort=time,desc").addHeader("Authorization", token).get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure:  okhttpRequest---error");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) {
                    Log.d(TAG, "onResponse: response.body()==null");
                    return;
                }
                String data = response.body().string();
//                Log.e(TAG, "onResponse: data" + data);
                boolean successful = response.isSuccessful();
//                Log.e(TAG, "onResponse: " + successful);
                //响应码可能是404也可能是200都会走这个方法
                if (response.isSuccessful()) {
                    long time = System.currentTimeMillis();
                    Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
                    Visitor visitor = null;
                    try {
                        visitor = gson.fromJson(data, Visitor.class);
                    } catch (JsonSyntaxException e) {
                        Log.d(TAG, "onResponse: ShowActivity_data_json_error" + e.getMessage());
                        exceptionHint("ShowActivity_data_json_error");
                        return;
                    }
                    Log.d(TAG, "onResponse: gson" + gson.toJson(visitor));
                    Log.d(TAG, "onResponse: json解析时间--->" + (System.currentTimeMillis() - time) / 1000.0 + "visitor-->" + visitor.getContent().size() + "**" + visitor.getTotalElements());
                    converted.clear();
                    for (Visitor.ContentBean contentBean : visitor.getContent()) {
                        converted.add((Visitor.ContentBean) contentBean);
                    }
                    AllUndone.clear();
                    AllFished.clear();
                    todayUndone.clear();
                    todayFinshed.clear();
                    for (Visitor.ContentBean contentBean : converted) {
                        //临时将待接待标志改为 0
                        Log.d(TAG, "initData: 过滤开始" + contentBean.getTime());
                        if ((contentBean.getStatus() == 0)) {
                            AllUndone.add(contentBean);
                        } else if ((contentBean.getStatus() == 1)) {
                            AllFished.add(contentBean);
                        }
                        if (judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 0)) {
                            Log.d(TAG, "initData: 过滤结果" + contentBean.getTime());
                            todayUndone.add(contentBean);
                        } else if (judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 1)) {
                            todayFinshed.add(contentBean);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (LeftrightOrLeft == 1) {
                                allInfo.setData(AllUndone);
                            } else if (LeftrightOrLeft == 2)
                                allInfo.setData(AllFished);
                            if (RightrightOrLeft == 1) {
                                todayInfo.setData(todayUndone);
                            } else if (RightrightOrLeft == 2)
                                todayInfo.setData(todayFinshed);

                            allInfo.notifyDataSetChanged();
                            todayInfo.notifyDataSetChanged();

                            if (AllUndone.size() > 0) {
                                viewDetail.setVisibility(View.VISIBLE);
                            } else {
                                viewDetail.setVisibility(View.INVISIBLE);
                            }
                            if (todayUndone.size() > 0) {
                                viewDetailRight.setVisibility(View.VISIBLE);
                            } else {
                                viewDetailRight.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


                }
            }
        });
    }

    /**
     * 自定义对话框,放大二维码
     */
    private void customDialog(Bitmap bitmap) {
        final Dialog dialog = new Dialog(this, R.style.NormalDialogStyle);
        View view = View.inflate(this, R.layout.dialog_qr_code, null);
        ImageView imageView = view.findViewById(R.id.large_after);
        dialog.setContentView(view);
        //使得点击对话框外部不消失对话框
        dialog.setCanceledOnTouchOutside(true);
        //设置对话框的大小
//        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = DisplayUtil.dip2px(320, ShowActivity.this);
        lp.height = DisplayUtil.dip2px(320, ShowActivity.this);
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        imageView.setImageBitmap(bitmap);
        dialog.show();
    }

    private void initView() {
        largeBtn = findViewById(R.id.large_btn);
        scanText = findViewById(R.id.scan_text);
        registerText = findViewById(R.id.register_text);
        qrSet = findViewById(R.id.QR);
        qrCode = findViewById(R.id.QR_code);
        exploreName = findViewById(R.id.explore_name);
        showContent = findViewById(R.id.show_content);
        allDetail = findViewById(R.id.all_detail);
        detailRight = findViewById(R.id.detail_right);
        detailLeft = findViewById(R.id.detail_left);
        companyTitle = findViewById(R.id.company_title);
        bottomLeft = findViewById(R.id.bottom_left);
        bottomRight = findViewById(R.id.bottom_right);
        bottomRightLeft = findViewById(R.id.bottom_right_left);
        bottomRightRight = findViewById(R.id.bottom_right_right);
        viewDetail = findViewById(R.id.detail);
        viewDetailRight = findViewById(R.id.right_detail);

        detailRight.setOnClickListener(this);
        detailLeft.setOnClickListener(this);
        bottomLeft.setOnClickListener(this);
        bottomRight.setOnClickListener(this);
        bottomRightLeft.setOnClickListener(this);
        bottomRightRight.setOnClickListener(this);
        viewDetail.setOnClickListener(this);
        viewDetailRight.setOnClickListener(this);


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

    /**
     * 接收序列化后的对象，并进行强转存储
     */
    public void toConvert() {
        name = getIntent().getStringExtra("name");
        token = getIntent().getStringExtra("token");
        for (Visitor.ContentBean contentBean : ((Visitor) getIntent().getSerializableExtra("visitor")).getContent()) {
            converted.add((Visitor.ContentBean) contentBean);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bottom_left) {
            bottomLeft.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_left));
            bottomLeft.setBackgroundResource(R.drawable.shape_bottom_bg_left);
            bottomRight.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_right));
            bottomRight.setBackgroundResource(R.drawable.shape_bottom_bg_right);

            allInfo.setData(AllUndone);
            LeftrightOrLeft = 1;
            allInfo.notifyDataSetChanged();
            if (AllUndone.size() > 0) {
                viewDetail.setVisibility(View.VISIBLE);
            } else {
                viewDetail.setVisibility(View.INVISIBLE);
            }
        }
        if (v.getId() == R.id.bottom_right) {
            bottomLeft.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_right));
            bottomLeft.setBackgroundResource(R.drawable.shape_bottom_bg_left_close);
            bottomRight.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_left));
            bottomRight.setBackgroundResource(R.drawable.shape_bottom_bg_right_enable);

            allInfo.setData(AllFished);
            LeftrightOrLeft = 2;
            allInfo.notifyDataSetChanged();
            if (AllFished.size() > 0) {
                viewDetail.setVisibility(View.VISIBLE);
            } else {
                viewDetail.setVisibility(View.INVISIBLE);
            }
        }
        if (v.getId() == R.id.bottom_right_left) {
            bottomRightLeft.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_left));
            bottomRightLeft.setBackgroundResource(R.drawable.shape_bottom_bg_left);
            bottomRightRight.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_right));
            bottomRightRight.setBackgroundResource(R.drawable.shape_bottom_bg_right);

            todayInfo.setData(todayUndone);
            RightrightOrLeft = 1;
            todayInfo.notifyDataSetChanged();
            if (todayUndone.size() > 0) {
                viewDetailRight.setVisibility(View.VISIBLE);
            } else {
                viewDetailRight.setVisibility(View.INVISIBLE);
            }
        }
        if (v.getId() == R.id.bottom_right_right) {
            bottomRightLeft.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_right));
            bottomRightLeft.setBackgroundResource(R.drawable.shape_bottom_bg_left_close);
            bottomRightRight.setTextColor(ContextCompat.getColor(ShowActivity.this, R.color.bottom_left));
            bottomRightRight.setBackgroundResource(R.drawable.shape_bottom_bg_right_enable);

            todayInfo.setData(todayFinshed);
            RightrightOrLeft = 2;
            todayInfo.notifyDataSetChanged();
            if (todayFinshed.size() > 0) {
                viewDetailRight.setVisibility(View.VISIBLE);
            } else {
                viewDetailRight.setVisibility(View.INVISIBLE);
            }
        }
        if (v.getId() == R.id.detail_left) {
            showContent.setVisibility(View.GONE);
            allDetail.setVisibility(View.VISIBLE);
            if (LeftrightOrLeft == 1) {
                allDetailAdapter.setData(AllUndone);
                allDetailAdapter.notifyDataSetChanged();
            } else if (LeftrightOrLeft == 2) {
                allDetailAdapter.setData(AllFished);
                allDetailAdapter.notifyDataSetChanged();
            }
        }
        if (v.getId() == R.id.detail_right) {
            showContent.setVisibility(View.GONE);
            allDetail.setVisibility(View.VISIBLE);
            if (RightrightOrLeft == 1) {
                allDetailAdapter.setData(todayUndone);
                allDetailAdapter.notifyDataSetChanged();
            } else if (RightrightOrLeft == 2) {
                allDetailAdapter.setData(todayFinshed);
                allDetailAdapter.notifyDataSetChanged();
            }
        }


    }

    @Override
    public void onBackPressed() {
        if (showContent.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        }
        if (allDetail.getVisibility() == View.VISIBLE) {
            allDetail.setVisibility(View.GONE);
            showContent.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 判断是否为今日待办事项
     *
     * @param time
     * @return
     */
    public boolean judgeTimeIsToday(String time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        LocalDateTime localTime = LocalDateTime.parse(time, dtf);
        LocalDateTime startTime = LocalDate.now().atTime(0, 0, 0);
        LocalDateTime endTime = LocalDate.now().atTime(23, 59, 59);
        //如果大于今天的开始日期，小于今天的结束日期
        return localTime.isAfter(startTime) && localTime.isBefore(endTime);
    }

}