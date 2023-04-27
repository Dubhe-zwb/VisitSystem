package com.tonsail.visit;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.tonsail.visit.AAChartCoreLib.AAChartCreator.AAChartEvents;
import com.tonsail.visit.AAChartCoreLib.AAChartCreator.AAChartModel;
import com.tonsail.visit.AAChartCoreLib.AAChartCreator.AAChartView;
import com.tonsail.visit.AAChartCoreLib.AAChartCreator.AASeriesElement;
import com.tonsail.visit.AAChartCoreLib.AAChartEnum.AAChartType;
import com.tonsail.visit.AAChartCoreLib.AAOptionsModel.AALabels;
import com.tonsail.visit.AAChartCoreLib.AAOptionsModel.AAOptions;
import com.tonsail.visit.AAChartCoreLib.AAOptionsModel.AAXAxis;
import com.tonsail.visit.AAChartCoreLib.AATools.AAGradientColor;
import com.tonsail.visit.AAChartCoreLib.AATools.AALinearGradientDirection;
import com.tonsail.visit.adapter.AllDetail;
import com.tonsail.visit.adapter.AllInfo;
import com.tonsail.visit.adapter.TodayInfo;
import com.tonsail.visit.utils.DisplayUtil;
import com.tonsail.visit.utils.Key;
import com.tonsail.visit.utils.QRCode;
import com.tonsail.visit.utils.SortUtil;
import com.tonsail.visit.utils.SpUtils;
import com.tonsail.visit.utils.TimeJudge;
import com.tonsail.visit.utils.Visitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "zwb_ShowActivity";

    private final List<Visitor.ContentBean> converted = new ArrayList<>();//强转后,总数据
    private final List<Visitor.ContentBean> todayUndone = new ArrayList<>();//今日待办，未完成
    private final List<Visitor.ContentBean> todayFinshed = new ArrayList<>();//今日待办，已完成
    private final List<Visitor.ContentBean> AllUndone = new ArrayList<>();//今日待办,未完成
    private final List<Visitor.ContentBean> AllFished = new ArrayList<>();//今日待办，已完成
    private final List<Visitor.ContentBean> chartList = new ArrayList<>();//图表数据源
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
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int httpFlag;
    private final Runnable runnable = new Runnable() {
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
            handler.postDelayed(this, 15000);
        }
    };
    private TextView chartOpen;
    private View chartShow;
    private int pageFlag;//  1--->第一页统计图  2--->第二页统计图
    private View show_text;
    private AAChartView aaChartViewVisitReason;
    private AAChartView aaChartViewVisitWay;
    private AAChartView aaChartViewVisitChange;
    private PieChart pieChart;
    private LineChart lineChart;
    private BarChart barChart;
    private TextView recentAmonth;
    private TextView numToday;
    private TextView numArrive;
    private TextView numLeave;
    private int a, b, c, d, e;
    private Spinner spinner;
    private int spinnerFlag = 1;        //  1-->近一月   2-->近一周  3-->近一天
    private int spinnerFlagOne = 1;     //  1-->近一月   2-->近一周  3-->近一天
    private int spinnerFlagTwo = 1;     //  1-->近一月   2-->近一周  3-->近一天
    private int spinnerFlagThree = 1;   //  1-->近一月   2-->近一周  3-->近一天
    private int spinnerFlagFour = 1;    //  1-->近一月   2-->近一周  3-->近一天
    private int machineFlag;//1-->848  2-->2853  3-->9666
    private View many;
    private View chartOne;
    private View chartTwo;
    private AAChartView aaChartManyOne;
    private AAChartView aaChartManyTwo;
    private AAChartView aaChartManyThree;
    private AAChartView aaChartManyFour;
    private HorizontalBarChart horizontalBarChart;
    private HorizontalBarChart horizontalBarChartTwo;
    private PieChart pieChartManyThree;
    private PieChart pieChartManyFour;
    private Spinner spinnerManyOne;
    private Spinner spinnerManyTwo;
    private Spinner spinnerManyThree;
    private Spinner spinnerManyFour;
    private TextView noDataOne;
    private TextView noDataTwo;
    private TextView noDataThree;
    private TextView noDataFour;
    private View back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        toConvert();

        initView();

        initData();
    }

    private void initData() {
//        Log.e(TAG, "initData: " + name);
        companyTitle.setText(name);

        for (Visitor.ContentBean contentBean : converted) {
            //临时将待接待标志改为 0
//            Log.e(TAG, "initData: 过滤开始" + contentBean.getTime());
            if ((contentBean.getStatus() == 0)) {
                AllUndone.add(contentBean);
            } else if ((contentBean.getStatus() == 1 || contentBean.getStatus() == 2)) {
                AllFished.add(contentBean);
            }
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 0)) {
//                Log.e(TAG, "initData: 过滤结果" + contentBean.getTime());
                todayUndone.add(contentBean);
            } else if (TimeJudge.judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 1 || contentBean.getStatus() == 2)) {
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
//                Log.e(TAG, "afterTextChanged: " + selectName);
                allDetailAdapter.setExplorData(selectName);
            }
        });

        generateQRCode();
        spinnerSet();

    }

    private void spinnerSet() {
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ShowActivity.this.getResources().getStringArray(R.array.option)[position];
                switch (text) {
                    case "近一月":
                        spinnerFlag = 1;
                        visitChangeChart();
                        break;
                    case "近一周":
                        spinnerFlag = 2;
                        visitChangeChart();
                        break;
                    case "近一天":
                        spinnerFlag = 3;
                        visitChangeChart();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerManyOne.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ShowActivity.this.getResources().getStringArray(R.array.option)[position];
                switch (text) {
                    case "近一月":
                        Log.e(TAG, "onItemSelected111: ");
                        spinnerFlagOne = 1;
                        noDataOne.setVisibility(View.GONE);
                        visitNum();
                        break;
                    case "近一周":
                        spinnerFlagOne = 2;
                        noDataOne.setVisibility(View.GONE);
                        visitNum();
                        break;
                    case "近一天":
                        spinnerFlagOne = 3;
                        noDataOne.setVisibility(View.GONE);
                        visitNum();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerManyTwo.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ShowActivity.this.getResources().getStringArray(R.array.option)[position];
                switch (text) {
                    case "近一月":
                        spinnerFlagTwo = 1;
                        noDataTwo.setVisibility(View.GONE);
                        visitMainTime();
                        break;
                    case "近一周":
                        spinnerFlagTwo = 2;
                        noDataTwo.setVisibility(View.GONE);
                        visitMainTime();
                        break;
                    case "近一天":
                        spinnerFlagTwo = 3;
                        noDataTwo.setVisibility(View.GONE);
                        visitMainTime();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerManyThree.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ShowActivity.this.getResources().getStringArray(R.array.option)[position];
                switch (text) {
                    case "近一月":
                        spinnerFlagThree = 1;
                        pieChartManyThree.setVisibility(View.VISIBLE);
                        noDataThree.setVisibility(View.GONE);
                        visitStayTime();
                        break;
                    case "近一周":
                        spinnerFlagThree = 2;
                        pieChartManyThree.setVisibility(View.VISIBLE);
                        noDataThree.setVisibility(View.GONE);
                        visitStayTime();
                        break;
                    case "近一天":
                        spinnerFlagThree = 3;
                        pieChartManyThree.setVisibility(View.VISIBLE);
                        noDataThree.setVisibility(View.GONE);
                        visitStayTime();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerManyFour.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ShowActivity.this.getResources().getStringArray(R.array.option)[position];
                switch (text) {
                    case "近一月":
                        spinnerFlagFour = 1;
                        noDataFour.setVisibility(View.GONE);
                        visitSuccessPossibility();
                        break;
                    case "近一周":
                        spinnerFlagFour = 2;
                        noDataFour.setVisibility(View.GONE);
                        visitSuccessPossibility();
                        break;
                    case "近一天":
                        spinnerFlagFour = 3;
                        noDataFour.setVisibility(View.GONE);
                        visitSuccessPossibility();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
//                    Log.e(TAG, "onResponse: key----->" + key);
                    String key1 = key.data.key;
//                    Log.e(TAG, "onResponse: key1" + key1);
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

    public List<Visitor.ContentBean> AllUndoneTemp = new ArrayList<>();
    public List<Visitor.ContentBean> AllFishedTemp = new ArrayList<>();
    public List<Visitor.ContentBean> todayUndoneTemp = new ArrayList<>();
    public List<Visitor.ContentBean> todayFinshedTemp = new ArrayList<>();
    public List<Visitor.ContentBean> chartListTemp = new ArrayList<>();

    public void okhttpRequest() {
//        Log.d(TAG, "okhttpRequest: token---->" + token);
        if (TextUtils.isEmpty(token)) {
            Log.d(TAG, "okhttpRequest: token为空");
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://192.168.3.181:8000/api/visitor/query?size=100000&sort=createtime,desc").addHeader("Authorization", token).get().build();
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
//                    Log.d(TAG, "onResponse: gson" + gson.toJson(visitor));
//                    Log.d(TAG, "onResponse: json解析时间--->" + (System.currentTimeMillis() - time) / 1000.0 + "visitor-->" + visitor.getContent().size() + "**" + visitor.getTotalElements());
                    converted.clear();
                    for (Visitor.ContentBean contentBean : visitor.getContent()) {
                        converted.add((Visitor.ContentBean) contentBean);
                    }
                    AllUndoneTemp.clear();
                    AllFishedTemp.clear();
                    todayUndoneTemp.clear();
                    todayFinshedTemp.clear();
                    chartListTemp.clear();

                    chartListTemp.addAll(converted);
                    try {
                        for (Visitor.ContentBean contentBean : converted) {
                            //临时将待接待标志改为 0
//                            Log.d(TAG, "initData: 过滤开始" + contentBean.getTime());
                            if ((contentBean.getStatus() == 0)) {
                                AllUndoneTemp.add(contentBean);
                            } else if ((contentBean.getStatus() == 1 || contentBean.getStatus() == 2)) {
                                AllFishedTemp.add(contentBean);
                            }
                            if (TimeJudge.judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 0)) {
//                                Log.d(TAG, "initData: 过滤结果" + contentBean.getTime());
                                todayUndoneTemp.add(contentBean);
                            } else if (TimeJudge.judgeTimeIsToday(contentBean.getTime()) && (contentBean.getStatus() == 1 || contentBean.getStatus() == 2)) {
                                todayFinshedTemp.add(contentBean);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: converted_error------>");
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AllUndone.clear();
                            AllFished.clear();
                            todayUndone.clear();
                            todayFinshed.clear();
                            chartList.clear();


                            AllUndone.addAll(AllUndoneTemp);
                            AllFished.addAll(AllFishedTemp);
                            todayUndone.addAll(todayUndoneTemp);
                            todayFinshed.addAll(todayFinshedTemp);
                            chartList.addAll(chartListTemp);


                            if (LeftrightOrLeft == 1) {
                                allInfo.setData(AllUndone);
                                if (AllUndone.size() > 0) {
                                    viewDetail.setVisibility(View.VISIBLE);
                                } else {
                                    viewDetail.setVisibility(View.INVISIBLE);
                                }
                            } else if (LeftrightOrLeft == 2) {
                                allInfo.setData(AllFished);
                                if (AllFished.size() > 0) {
                                    viewDetail.setVisibility(View.VISIBLE);
                                } else {
                                    viewDetail.setVisibility(View.INVISIBLE);
                                }
                            }


                            if (RightrightOrLeft == 1) {
                                todayInfo.setData(todayUndone);
                                if (todayUndone.size() > 0) {
                                    viewDetailRight.setVisibility(View.VISIBLE);
                                } else {
                                    viewDetailRight.setVisibility(View.INVISIBLE);
                                }
                            } else if (RightrightOrLeft == 2) {
                                todayInfo.setData(todayFinshed);
                                if (todayFinshed.size() > 0) {
                                    viewDetailRight.setVisibility(View.VISIBLE);
                                } else {
                                    viewDetailRight.setVisibility(View.INVISIBLE);
                                }
                            }

                            allInfo.notifyDataSetChanged();
                            todayInfo.notifyDataSetChanged();
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
        back = findViewById(R.id.eight);
        many = findViewById(R.id.night);
        noDataOne = findViewById(R.id.no_data_one);
        noDataTwo = findViewById(R.id.no_data_two);
        noDataThree = findViewById(R.id.no_data_three);
        noDataFour = findViewById(R.id.no_data_four);
        spinnerManyOne = findViewById(R.id.spinner_many_one);
        spinnerManyTwo = findViewById(R.id.spinner_many_two);
        spinnerManyThree = findViewById(R.id.spinner_many_three);
        spinnerManyFour = findViewById(R.id.spinner_many_four);
        horizontalBarChartTwo = findViewById(R.id.HorizontalBarChart_two);
        horizontalBarChart = findViewById(R.id.HorizontalBarChart);
        aaChartManyOne = findViewById(R.id.aa_chart_view_many_one);
        aaChartManyTwo = findViewById(R.id.aa_chart_view_many_two);
        aaChartManyThree = findViewById(R.id.aa_chart_view_many_three);
        aaChartManyFour = findViewById(R.id.aa_chart_view_many_four);
        chartTwo = findViewById(R.id.chart_two);
        chartOne = findViewById(R.id.chart_one);
        spinner = findViewById(R.id.spinner);
        recentAmonth = findViewById(R.id.a_month_visitors_num);
        numLeave = findViewById(R.id.a_month_visitors_num_leave);
        numToday = findViewById(R.id.a_month_visitors_num_today);
        numArrive = findViewById(R.id.a_month_visitors_num_arrive);
        aaChartViewVisitChange = findViewById(R.id.aa_chart_view_visit_change);
        aaChartViewVisitWay = findViewById(R.id.aa_chart_view_visit_way);
        aaChartViewVisitReason = findViewById(R.id.aa_chart_view_visit_reason);
        show_text = findViewById(R.id.show_text);
        chartShow = findViewById(R.id.chart_show);
        chartOpen = findViewById(R.id.chart_open);
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

        back.setOnClickListener(this);
        many.setOnClickListener(this);
        aaChartViewVisitChange.setOnClickListener(this);
        chartOpen.setOnClickListener(this);
        detailRight.setOnClickListener(this);
        detailLeft.setOnClickListener(this);
        bottomLeft.setOnClickListener(this);
        bottomRight.setOnClickListener(this);
        bottomRightLeft.setOnClickListener(this);
        bottomRightRight.setOnClickListener(this);
        viewDetail.setOnClickListener(this);
        viewDetailRight.setOnClickListener(this);
    }

    /**
     * 接收序列化后的对象，并进行强转存储
     */
    public void toConvert() {
        machineFlag = SpUtils.decodeInt("machineFlag");
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
        if (v.getId() == R.id.chart_open) {
            allDetail.setVisibility(View.GONE);
            chartShow.setVisibility(View.VISIBLE);
            show_text.setVisibility(View.VISIBLE);
            pageFlag = 1;

            prepareData();
        }
        if (v.getId() == R.id.night) {
            chartOne.setVisibility(View.GONE);
            chartTwo.setVisibility(View.VISIBLE);
            show_text.setVisibility(View.INVISIBLE);
            pageFlag = 2;

            SortUtil.sortDescTime(chartList);
            newChartPage();
        }
        if (v.getId() == R.id.eight) {
            onBackPressed();
        }

    }

    private void visitNumData(String[] YData, int[] XData) {
//        visitNumMP_Chart(YData, XData);
        Log.e(TAG, "visitNumData111: YData--->" + Arrays.toString(YData) + " XData--->" + Arrays.toString(XData));

        Object[] X = new Object[XData.length];
        String[] Y = new String[YData.length];
        //        逆序后填入图表
        for (int i = 0; i < XData.length; i++) {
            X[XData.length - 1 - i] = XData[i];
            Y[YData.length - 1 - i] = YData[i];
        }
        Log.e(TAG, "visitNumData111: 逆序后：Y-->" + Arrays.toString(Y) + " X-->" + Arrays.toString(X));

        int judgeFlag = 0;
        for (Object x : X) {
            if ((int) x != 0) judgeFlag++;
        }
        Log.e(TAG, "visitNumData111: judgeFlag-->" + judgeFlag);

        //去除图标上方的 "0" 数据
        Object[] XTemp;
        String[] YTemp;
        if (judgeFlag > 0) {
            XTemp = SortUtil.Xtemp(X);
            YTemp = SortUtil.Ytemp(Y);
        } else {
            XTemp = X;
            YTemp = Y;
        }
        Log.e(TAG, "visitNumData111: 填入数组：XTemp-->" + Arrays.toString(XTemp));

        AAChartModel aaChartChange = new AAChartModel()
                .chartType(AAChartType.Column)
                .animationDuration(1200)
//                .title("THE HEAT OF PROGRAMMING LANGUAGE")
//                .subtitle("Virtual Data")
//                .backgroundColor("#4b2b7f")
                .categories(YTemp)
                .dataLabelsEnabled(false)
                .legendEnabled(false)
                .yAxisGridLineWidth(0f)
                .dataLabelsEnabled(true)
                .inverted(true)
                .yAxisVisible(false)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("")
                                .data(XTemp)
                                .color("#0085FF")
//                                .data(new Object[]{0, 0, 0, 0, 0}),
//                        new AASeriesElement()
//                                .name("NewYork")
//                                .data(new Object[]{0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 2.5}),
//                        new AASeriesElement()
//                                .name("London")
//                                .data(new Object[]{0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0}),
//                        new AASeriesElement()
//                                .name("Berlin")
//                                .data(new Object[]{3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8})
                })
                .markerRadius(2f);//圆点大小
        if (judgeFlag == 0) aaChartChange.dataLabelsEnabled(false);//数据为0不显示数字
        if (judgeFlag == 0) {
            aaChartChange.xAxisVisible(false);
            noDataOne.setVisibility(View.VISIBLE);
        }
        AAOptions aaOptions = aaChartChange.aa_toAAOptions();
//        aaOptions.xAxis(xAxis);
//        aaOptions.yAxis
//                .min(0).minRange(1)//使Y轴数据均为0时Y轴起点在X轴
////                .tickAmount(6)
//                .allowDecimals(false);
        aaOptions.chart
                .events(new AAChartEvents()
                        .load("function() {\n" +
                                "   const chart = this;\n" +
                                "   Highcharts.addEvent(\n" +
                                "       chart.tooltip,\n" +
                                "       'refresh',\n" +
                                "       function () {\n" +
                                "           chart.tooltip.hide(1000);\n" + //设置 tooltip 自动隐藏的时间，单位为 ms
                                "   });\n" +
                                "}"));

//        aaChartViewVisitChange.aa_drawChartWithChartModel(aaChartChange);
        aaChartManyOne.aa_drawChartWithChartOptions(aaOptions);//旋转角度设置


    }

    private void visitNumMP_Chart(String[] YData, int[] XData) {
        //去除" Description Label"
        Description description = new Description();
        description.setEnabled(false);
        horizontalBarChart.setDescription(description);

        // 禁用图例（图例是标识不同数据集的小方块）
        Legend legend = horizontalBarChart.getLegend();
        legend.setEnabled(false);

        // 设置x轴的标签和数据
        List<String> xAxisLabels = new ArrayList<>();
        xAxisLabels.add(YData[4]);
        xAxisLabels.add(YData[3]);
        xAxisLabels.add(YData[2]);
        xAxisLabels.add(YData[1]);
        xAxisLabels.add(YData[0]);
        BarDataSet barDataSet = new BarDataSet(getDataNum(XData), "");
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if ((int) value == 0) return "";//去除次数为0时的条状图
                return (int) value + "";
            }
        });
        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(12f);      //图形上方字体大小
        horizontalBarChart.setData(barData);

        YAxis leftAxis = horizontalBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        // 将 axisMinimum 属性设置为 0
        leftAxis.setAxisMinimum(0f);
        // 设置x轴的标签的位置、旋转角度和数量
        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(0);
        xAxis.setLabelCount(xAxisLabels.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        // 设置BarChart的一般属性
        horizontalBarChart.setDoubleTapToZoomEnabled(false);//设置为false以禁止通过在其上双击缩放图表
        horizontalBarChart.animateY(1400);
        horizontalBarChart.setDrawValueAboveBar(true);
        horizontalBarChart.setTouchEnabled(true);
        horizontalBarChart.setDragEnabled(true);
        horizontalBarChart.setScaleEnabled(false);//启用/禁用缩放图表上的两个轴
        horizontalBarChart.setNoDataText("");
        horizontalBarChart.invalidate();

        //关闭背景参考线
        horizontalBarChart.getAxisLeft().setDrawGridLines(false);
        horizontalBarChart.getXAxis().setDrawGridLines(false);
        horizontalBarChart.getAxisRight().setDrawGridLines(false);

        horizontalBarChart.getAxisLeft().setEnabled(false);       // 去除左侧纵坐标轴
        horizontalBarChart.getAxisRight().setEnabled(false);      // 去除右侧纵坐标
    }

    public void newChartPage() {
        visitNum();
        visitMainTime();
        visitStayTime();
        visitSuccessPossibility();
    }

    private void visitSuccessPossibility() {
        pieChartManyFour = findViewById(R.id.pie_chart_many_four);

        //去除" Description Label"
        Description description = new Description();
        description.setEnabled(false);
        pieChartManyFour.setDescription(description);

        // 设置pieChart的属性
        pieChartManyFour.setRotationEnabled(true);      // 可旋转
        pieChartManyFour.setCenterTextSize(12f);         // 饼图描述字体大小为20
        pieChartManyFour.setHoleRadius(0f);          // 饼状图中间空白圆心半径
        pieChartManyFour.setTransparentCircleRadius(0f); // 饼状图中间透明圈半径
        pieChartManyFour.setDrawCenterText(false);     // 饼状图中间文字可见
        pieChartManyFour.setDrawEntryLabels(false);   //设置pieChart是否只显示饼图上百分比不显示文字
        pieChartManyFour.animateY(1400);
        pieChartManyFour.setHoleColor(ContextCompat.getColor(ShowActivity.this, R.color.white));//中心背景
        pieChartManyFour.setNoDataText("");
        pieChartManyFour.setDrawHoleEnabled(false);
//        pieChart.setUsePercentValues(true);

        // 设置饼状图中间的文字
//        pieChartManyFour.setCenterTextColor(Color.BLACK);
//        pieChartManyFour.setCenterText(numToday.getText());
//        pieChartManyFour.setCenterTextSize(24);

//        pieChart.setExtraOffsets(5, 5, 1, 5);//设置四周相隔距离属性

        pieSuccessPossibilityData();
    }

    private void pieSuccessPossibilityData() {
        int centerNumFlag = 0;
        List<Visitor.ContentBean> contentBeans = new ArrayList<>(chartList);

        //近一月，一周，一天数据集
        List<Visitor.ContentBean> monthData = new ArrayList<Visitor.ContentBean>();
        List<Visitor.ContentBean> weekData = new ArrayList<Visitor.ContentBean>();
        List<Visitor.ContentBean> todayData = new ArrayList<Visitor.ContentBean>();
        int status_0 = 0, status_1 = 0, status_2 = 0, status_num = 0;//    未到访  确认到访  已离开

        for (Visitor.ContentBean contentBean : chartList) {
            if (TimeJudge.judgeAmonthRecent(contentBean.getTime())) {
                monthData.add(contentBean);
            }
            if (TimeJudge.judgeAweekRecent(contentBean.getTime())) {
                weekData.add(contentBean);
            }
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime())) {
                todayData.add(contentBean);
            }
        }
        switch (spinnerFlagFour) {
            case 1:
                for (Visitor.ContentBean monthDatum : monthData) {
                    if (monthDatum.getStatus() == 0) status_0++;
                    if (monthDatum.getStatus() == 1) status_1++;
                    if (monthDatum.getStatus() == 2) status_2++;
                }
                break;
            case 2:
                for (Visitor.ContentBean weekDatum : weekData) {
                    if (weekDatum.getStatus() == 0) status_0++;
                    if (weekDatum.getStatus() == 1) status_1++;
                    if (weekDatum.getStatus() == 2) status_2++;
                }
                break;
            case 3:
                for (Visitor.ContentBean todayDatum : todayData) {
                    if (todayDatum.getStatus() == 0) status_0++;
                    if (todayDatum.getStatus() == 1) status_1++;
                    if (todayDatum.getStatus() == 2) status_2++;
                }
                break;
        }
        status_num = status_0 + status_1 + status_2;
        Log.e(TAG, "pieSuccessPossibilityData111: status_num-->" + status_num + "\n"
                + "status_0-->" + status_0 + "\n"
                + "status_1-->" + status_1 + "\n"
                + "status_2-->" + status_2);
        Log.d(TAG, "newPageData: monthData-->" + monthData.size() + "\n"
                + "weekData-->" + weekData.size() + "\n"
                + "todayData-->" + todayData.size());


        float valueA = status_0, valueB = status_num - status_0, num;//未到访率  到访率
        num = status_num;

        Log.e(TAG, "pieSuccessPossibilityData: 222-->" + valueA / num);

        int percentA = (int) (valueA / num * 1000);
        int i = percentA % 10;
        if (i > 4)
            percentA = (int) (valueA / num * 100) + 1;
        else percentA = (int) (valueA / num * 100);
        int percentB = 100 - percentA;

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (percentA > 0)
            entries.add(new PieEntry(percentA, "未到访率"));
        if (percentB > 0)
            entries.add(new PieEntry(percentB, "到访率"));
        if ((percentA <= 0) && (percentB <= 0)) {
            // 禁用图例（图例是标识不同数据集的小方块）
            Legend legend = pieChartManyFour.getLegend();
            legend.setEnabled(false);
            entries.add(new PieEntry(100, ""));
            centerNumFlag = 1;//如果进入这里去除图例，并且只获取一种color
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String s = (int) value + "%";
                if ((int) value == 100 && Integer.parseInt("" + numToday.getText()) == 0) return "";
                else return s;

            }
        });
        dataSet.setSliceSpace(3f);              // 每个饼块之间的间隙
        dataSet.setSelectionShift(5f);          // 选中的饼块离饼形图中心的距离

        // 设置每个饼块的颜色
        ArrayList<Integer> colors = new ArrayList<>();
        if (centerNumFlag != 1) {
            colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_1));
        }
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_2));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
//        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);             // 图上方字体的大小为20

        pieChartManyFour.setData(data);
        pieChartManyFour.invalidate();                 // refresh
    }

    private void visitStayTime() {
        pieChartManyThree = findViewById(R.id.pie_chart_many_three);

        //去除" Description Label"
        Description description = new Description();
        description.setEnabled(false);
        pieChartManyThree.setDescription(description);

        // 设置pieChart的属性
        pieChartManyThree.setRotationEnabled(true);      // 可旋转
        pieChartManyThree.setCenterTextSize(12f);         // 饼图描述字体大小为20
        pieChartManyThree.setHoleRadius(60f);          // 饼状图中间空白圆心半径
        pieChartManyThree.setTransparentCircleRadius(68f); // 饼状图中间透明圈半径
        pieChartManyThree.setDrawCenterText(false);     // 饼状图中间文字可见
        pieChartManyThree.setDrawEntryLabels(false);   //设置pieChart是否只显示饼图上百分比不显示文字
        pieChartManyThree.animateY(1400);
        pieChartManyThree.setHoleColor(ContextCompat.getColor(ShowActivity.this, R.color.white));//中心背景
        pieChartManyThree.setNoDataText("");
//        pieChart.setUsePercentValues(true);

        // 设置饼状图中间的文字
        pieChartManyThree.setCenterTextColor(Color.BLACK);
        pieChartManyThree.setCenterText(numToday.getText());
        pieChartManyThree.setCenterTextSize(24);

//        pieChart.setExtraOffsets(5, 5, 1, 5);//设置四周相隔距离属性

        pieStayTimeData();
    }

    private void pieStayTimeData() {
        int centerNumFlag = 0;
        List<Visitor.ContentBean> contentBeans = new ArrayList<>(chartList);
        List<Visitor.ContentBean> contentBeansTemp = new ArrayList<>();
        float valueA = 0, valueB = 0, valueC = 0, valueD = 0,
                valueE = 0, valueF = 0, valueG = 0, num;

        //近一月，一周，一天数据集
        List<Visitor.ContentBean> monthData = new ArrayList<Visitor.ContentBean>();
        List<Visitor.ContentBean> weekData = new ArrayList<Visitor.ContentBean>();
        List<Visitor.ContentBean> todayData = new ArrayList<Visitor.ContentBean>();

        for (Visitor.ContentBean contentBean : chartList) {
            if (TimeJudge.judgeAmonthRecent(contentBean.getTime())) {
                monthData.add(contentBean);
            }
            if (TimeJudge.judgeAweekRecent(contentBean.getTime())) {
                weekData.add(contentBean);
            }
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime())) {
                todayData.add(contentBean);
            }
        }
        Log.d(TAG, "newPageData: monthData-->" + monthData.size() + monthData + "\n"
                + "weekData-->" + weekData.size() + weekData + "\n"
                + "todayData-->" + todayData.size() + todayData);

        switch (spinnerFlagThree) {
            case 1:
                contentBeansTemp = monthData;
                break;
            case 2:
                contentBeansTemp = weekData;
                break;
            case 3:
                contentBeansTemp = todayData;
                break;
        }
        for (Visitor.ContentBean contentBean : contentBeansTemp) {
            switch (TimeJudge.getStayTime(contentBean.getConfirmTime(), contentBean.getLeaveTime())) {
                case -1:
                    Log.d(TAG, "pieStayTimeData: 日期解析错误");
                    break;
                case 1:
                    valueA++;
                    break;
                case 2:
                    valueB++;
                    break;
                case 3:
                    valueC++;
                    break;
                case 4:
                    valueD++;
                    break;
                case 5:
                    valueE++;
                    break;
                case 6:
                    valueF++;
                    break;
                case 7:
                    valueG++;
                    break;
            }
        }
        num = valueA + valueB + valueC + valueD + valueE + valueF + valueG;
        Log.e(TAG, "pieStayTimeData: A " + valueA + "num--->" + num);

        float percentA = (valueA / num) * 100;
        float percentB = (valueB / num) * 100;
        float percentC = (valueC / num) * 100;
        float percentD = (valueD / num) * 100;
        float percentE = (valueE / num) * 100;
        float percentF = (valueF / num) * 100;
        float percentG = (valueG / num) * 100;

        Log.e(TAG, "pieStayTimeData: B " + percentA);
//        int i = percentA % 10;
//        if (i > 4) percentA = (int)
//                (valueA / num * 100) + 1;
//        else percentA = (int) (valueA / num * 100);
//        int percentB = 100 - percentA;

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (percentA > 0)
            entries.add(new PieEntry(percentA, "0-1小时"));
        if (percentB > 0)
            entries.add(new PieEntry(percentB, "1-2小时"));
        if (percentC > 0)
            entries.add(new PieEntry(percentC, "2-3小时"));
        if (percentD > 0)
            entries.add(new PieEntry(percentD, "3-4小时"));
        if (percentE > 0)
            entries.add(new PieEntry(percentE, "4-5小时"));
        if (percentF > 0)
            entries.add(new PieEntry(percentF, "5-6小时"));
        if (percentG > 0)
            entries.add(new PieEntry(percentG, "6小时以上"));

        Log.e(TAG, "pieStayTimeData: --->");
        if (percentA == 0 && percentB == 0 && percentC == 0 && percentD == 0 && percentE == 0 && percentF == 0 && percentG == 0) {
            Log.e(TAG, "pieStayTimeData: --->legend.setEnabled");
            // 禁用图例（图例是标识不同数据集的小方块）
            Legend legend = pieChartManyThree.getLegend();
            legend.setEnabled(false);
            entries.add(new PieEntry(100, ""));
            centerNumFlag = 1;//如果进入这里去除图例，并且只获取一种color

            pieChartManyThree.setVisibility(View.GONE);
            noDataThree.setVisibility(View.VISIBLE);
            return;
        }
        if (centerNumFlag != 1) {
            //下拉列表切换时，及时恢复图例初始状态
            Legend legend = pieChartManyThree.getLegend();
            legend.setEnabled(true);
            legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        int finalCenterNumFlag = centerNumFlag;
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String s = (int) value + "%";
                if ((int) value == 100 && finalCenterNumFlag == 1) return "";
                else return s;

            }
        });
        dataSet.setSliceSpace(3f);              // 每个饼块之间的间隙
        dataSet.setSelectionShift(5f);          // 选中的饼块离饼形图中心的距离

        // 设置每个饼块的颜色
        ArrayList<Integer> colors = new ArrayList<>();
        if (centerNumFlag != 1) {
            colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_stay_tim_one));
        }
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_stay_tim_two));
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_stay_tim_three));
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_stay_tim_four));
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_stay_tim_five));
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_stay_tim_six));
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_stay_tim_seven));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
//        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);             // 图上方字体的大小为20

        pieChartManyThree.setData(data);
        pieChartManyThree.invalidate();                 // refresh
    }

    private void visitMainTime() {
        //近一月，一周，一天数据集
        List<String> monthData = new ArrayList<>();
        List<String> weekData = new ArrayList<>();
        List<String> todayData = new ArrayList<>();
        for (Visitor.ContentBean contentBean : chartList) {
            if (TimeJudge.judgeAmonthRecent(contentBean.getTime())) {
                String date = TimeJudge.getDayDate(contentBean.getTime());
                monthData.add(date);
            }
            if (TimeJudge.judgeAweekRecent(contentBean.getTime())) {
                String date = TimeJudge.getDayDate(contentBean.getTime());
                weekData.add(date);
            }
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime())) {
                String dayHours = TimeJudge.getDayHours(contentBean.getTime());
                todayData.add(dayHours);
            }
        }
        Log.d(TAG, "visitMainTime111: monthData-->" + monthData.size() + "\n"
                + "weekData-->" + weekData.size() + "\n"
                + "todayData-->" + todayData.size());

        //近一月，一周，一天数据集，各个数据出现的次数
        Map<String, Integer> aMonth = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        Map<String, Integer> aWeek = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        Map<String, Integer> aDay = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        for (String data : monthData) {
            if (aMonth.containsKey(data))
                aMonth.put(data, aMonth.get(data) + 1);
            else aMonth.put(data, 1);
        }
        for (String data : weekData) {
            if (aWeek.containsKey(data))
                aWeek.put(data, aWeek.get(data) + 1);
            else aWeek.put(data, 1);
        }
        for (String data : todayData) {
            if (aDay.containsKey(data + ":00"))
                aDay.put(data + ":00", aDay.get(data + ":00") + 1);
            else aDay.put(data + ":00", 1);
        }
        Log.d(TAG, "AAChart222: aMonth-->" + aMonth + "\n" + "aWeek-->" + aWeek + "\n" + "aDay-->" + aDay + aDay.isEmpty());

        String[] YData = new String[]{"", "", ""};
        int[] XData = new int[]{0, 0, 0};
        switch (spinnerFlagTwo) {
            case 1:
                TreeMap<String, Integer> sortedMap = new TreeMap<>(aMonth);
                // 将 TreeMap 转换为 List
                List<Map.Entry<String, Integer>> list = new ArrayList<>(sortedMap.entrySet());

                // 使用 Collections.sort() 方法对 List 进行排序
                list.sort(new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });
                Log.e(TAG, "visitNum: sort-->" + list + "\n" + list.size());

                if (list.size() > 0) {
                    int saveFlag = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (saveFlag >= 3) break;
                        YData[i] = list.get(i).getKey();
                        XData[i] = list.get(i).getValue();
                        saveFlag++;
                    }
                }

                break;
            case 2:
                TreeMap<String, Integer> sortedMapTwo = new TreeMap<>(aWeek);
                // 将 TreeMap 转换为 List
                List<Map.Entry<String, Integer>> listTwo = new ArrayList<>(sortedMapTwo.entrySet());

                // 使用 Collections.sort() 方法对 List 进行排序
                listTwo.sort(new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });
                Log.e(TAG, "visitNum: sort-->" + listTwo + "\n" + listTwo.size());

                if (listTwo.size() > 0) {
                    int saveFlag = 0;
                    for (int i = 0; i < listTwo.size(); i++) {
                        if (saveFlag >= 3) break;
                        YData[i] = listTwo.get(i).getKey();
                        XData[i] = listTwo.get(i).getValue();
                        saveFlag++;
                    }
                }
                break;
            case 3:
                TreeMap<String, Integer> sortedMapThree = new TreeMap<>(aDay);
                // 将 TreeMap 转换为 List
                List<Map.Entry<String, Integer>> listThree = new ArrayList<>(sortedMapThree.entrySet());

                // 使用 Collections.sort() 方法对 List 进行排序
                listThree.sort(new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });
                Log.e(TAG, "visitNum: sort-->" + listThree + "\n" + listThree.size());

                if (listThree.size() > 0) {
                    int saveFlag = 0;
                    for (int i = 0; i < listThree.size(); i++) {
                        if (saveFlag >= 3) break;
                        YData[i] = listThree.get(i).getKey();
                        XData[i] = listThree.get(i).getValue();
                        saveFlag++;
                    }
                }
                break;
        }


        visitMainTimeData(YData, XData);
    }

    public void visitMainTimeData(String[] YData, int[] XData) {
//        visitMainTimeMP_Chart(YData, XData);
        Log.e(TAG, "visitMainTimeData111:111 YData-->" + Arrays.toString(YData) + " XData-->" + Arrays.toString(XData));

        Object[] X = new Object[XData.length];
        String[] Y = new String[YData.length];
//        逆序后填入图表
        for (int i = 0; i < XData.length; i++) {
            X[XData.length - 1 - i] = XData[i];
            Y[YData.length - 1 - i] = YData[i];
        }
        int judgeFlag = 0;
        for (Object x : X) {
            if ((int) x != 0) judgeFlag++;
        }

        //去除图标上方的 "0" 数据
        Object[] XTemp;
        String[] YTemp;
        if (judgeFlag > 0) {
            XTemp = SortUtil.Xtemp(X);
            YTemp = SortUtil.Ytemp(Y);
        } else {
            XTemp = X;
            YTemp = Y;
        }
        Log.e(TAG, "visitMainTimeData222: 222-->" + Arrays.toString(XTemp));


        AAChartModel aaChartChange = new AAChartModel()
                .chartType(AAChartType.Column)
                .animationDuration(1200)

//                .title("THE HEAT OF PROGRAMMING LANGUAGE")
//                .subtitle("Virtual Data")
//                .backgroundColor("#4b2b7f")
                .categories(YTemp)
                .dataLabelsEnabled(true)
                .legendEnabled(false)
                .yAxisGridLineWidth(0f)
                .inverted(true)
                .yAxisVisible(false)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("")
                                .data(XTemp)
                                .color("#0085FF")
//                                .data(new Object[]{0, 0, 0, 0, 0}),
//                        new AASeriesElement()
//                                .name("NewYork")
//                                .data(new Object[]{0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 2.5}),
//                        new AASeriesElement()
//                                .name("London")
//                                .data(new Object[]{0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0}),
//                        new AASeriesElement()
//                                .name("Berlin")
//                                .data(new Object[]{3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8})
                })
                .markerRadius(2f);//圆点大小
        if (judgeFlag == 0) {
            aaChartChange.xAxisVisible(false);
            noDataTwo.setVisibility(View.VISIBLE);
        }
        AAOptions aaOptions = aaChartChange.aa_toAAOptions();
//        aaOptions.xAxis(xAxis);
//        aaOptions.yAxis
//                .min(0).minRange(1)//使Y轴数据均为0时Y轴起点在X轴
////                .tickAmount(6)
//                .allowDecimals(false);
        aaOptions.chart
                .events(new AAChartEvents()
                        .load("function() {\n" +
                                "   const chart = this;\n" +
                                "   Highcharts.addEvent(\n" +
                                "       chart.tooltip,\n" +
                                "       'refresh',\n" +
                                "       function () {\n" +
                                "           chart.tooltip.hide(1000);\n" + //设置 tooltip 自动隐藏的时间，单位为 ms
                                "   });\n" +
                                "}"));

//        aaChartViewVisitChange.aa_drawChartWithChartModel(aaChartChange);
        aaChartManyTwo.aa_drawChartWithChartOptions(aaOptions);//旋转角度设置
    }

    private void visitMainTimeMP_Chart(String[] YData, int[] XData) {
        //去除" Description Label"
        Description description = new Description();
        description.setEnabled(false);
        horizontalBarChartTwo.setDescription(description);

        // 禁用图例（图例是标识不同数据集的小方块）
        Legend legend = horizontalBarChartTwo.getLegend();
        legend.setEnabled(false);

        // 设置x轴的标签和数据
        List<String> xAxisLabels = new ArrayList<>();
        xAxisLabels.add(YData[2]);
        xAxisLabels.add(YData[1]);
        xAxisLabels.add(YData[0]);
        BarDataSet barDataSet = new BarDataSet(getDataMainTime(XData), "");
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if ((int) value == 0) return "";//去除次数为0时的条状图
                return (int) value + "";
            }
        });
        BarData barData = new BarData(barDataSet);
//        barData.setBarWidth(0.4f);
        barData.setValueTextSize(12f);      //图形上方字体大小
        horizontalBarChartTwo.setData(barData);

        YAxis leftAxis = horizontalBarChartTwo.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        // 将 axisMinimum 属性设置为 0
        leftAxis.setAxisMinimum(0f);
        // 设置x轴的标签的位置、旋转角度和数量
        XAxis xAxis = horizontalBarChartTwo.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(0);
        xAxis.setLabelCount(xAxisLabels.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        // 设置BarChart的一般属性
        horizontalBarChartTwo.setDoubleTapToZoomEnabled(false);//设置为false以禁止通过在其上双击缩放图表
        horizontalBarChartTwo.animateY(1400);
        horizontalBarChartTwo.setDrawValueAboveBar(true);
        horizontalBarChartTwo.setTouchEnabled(true);
        horizontalBarChartTwo.setDragEnabled(true);
        horizontalBarChartTwo.setScaleEnabled(false);//启用/禁用缩放图表上的两个轴
        horizontalBarChartTwo.setNoDataText("");
        horizontalBarChartTwo.invalidate();

        //关闭背景参考线
        horizontalBarChartTwo.getAxisLeft().setDrawGridLines(false);
        horizontalBarChartTwo.getXAxis().setDrawGridLines(false);
        horizontalBarChartTwo.getAxisRight().setDrawGridLines(false);

        horizontalBarChartTwo.getAxisLeft().setEnabled(false);       // 去除左侧纵坐标轴
        horizontalBarChartTwo.getAxisRight().setEnabled(false);      // 去除右侧纵坐标
    }

    private void visitNum() {
        //近一月，一周，一天数据集
        List<Visitor.ContentBean> monthData = new ArrayList<Visitor.ContentBean>();
        List<Visitor.ContentBean> weekData = new ArrayList<Visitor.ContentBean>();
        List<Visitor.ContentBean> todayData = new ArrayList<Visitor.ContentBean>();

        for (Visitor.ContentBean contentBean : chartList) {
            if (TimeJudge.judgeAmonthRecent(contentBean.getTime())) {
                monthData.add(contentBean);
            }
            if (TimeJudge.judgeAweekRecent(contentBean.getTime())) {
                weekData.add(contentBean);
            }
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime())) {
                todayData.add(contentBean);
            }
        }
        Log.d(TAG, "visitNum111: monthData.size()-->" + monthData.size() + "\n"
                + "weekData.size()-->" + weekData.size() + "\n"
                + "todayData.size()-->" + todayData.size());

        //近一月，一周，一天数据集，各个数据出现的次数
        Map<String, Integer> aMonth = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        Map<String, Integer> aWeek = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        Map<String, Integer> aDay = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        for (Visitor.ContentBean data : monthData) {
            if (aMonth.containsKey(data.getName()))
                aMonth.put(data.getName(), aMonth.get(data.getName()) + 1);
            else aMonth.put(data.getName(), 1);
        }
        for (Visitor.ContentBean data : weekData) {
            if (aWeek.containsKey(data.getName()))
                aWeek.put(data.getName(), aWeek.get(data.getName()) + 1);
            else aWeek.put(data.getName(), 1);
        }
        for (Visitor.ContentBean data : todayData) {
            if (aDay.containsKey(data.getName()))
                aDay.put(data.getName(), aDay.get(data.getName()) + 1);
            else aDay.put(data.getName(), 1);
        }
        Log.d(TAG, "visitNum222: aMonth-->" + aMonth + "\n" + "aWeek-->" + aWeek + "\n" + "aDay-->" + aDay + aDay.isEmpty());

        String[] YData = new String[]{"", "", "", "", ""};
        int[] XData = new int[]{0, 0, 0, 0, 0};
        switch (spinnerFlagOne) {
            case 1:
                TreeMap<String, Integer> sortedMap = new TreeMap<>(aMonth);
                // 将 TreeMap 转换为 List
                List<Map.Entry<String, Integer>> list = new ArrayList<>(sortedMap.entrySet());
                // 使用 Collections.sort() 方法对 List 进行排序
                list.sort(new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });
                Log.e(TAG, "visitNum333: sort-->" + list + "\n" + list.size());

                if (list.size() > 0) {
                    int saveFlag = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (saveFlag >= 5) break;
                        YData[i] = list.get(i).getKey();
                        XData[i] = list.get(i).getValue();
                        saveFlag++;
                    }
                }
                Log.e(TAG, "visitNum333: YData[]--->" + Arrays.toString(YData) + " XData--->" + Arrays.toString(XData));

                break;
            case 2:
                TreeMap<String, Integer> sortedMapTwo = new TreeMap<>(aWeek);
                // 将 TreeMap 转换为 List
                List<Map.Entry<String, Integer>> listTwo = new ArrayList<>(sortedMapTwo.entrySet());
                // 使用 Collections.sort() 方法对 List 进行排序
                listTwo.sort(new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });
                Log.e(TAG, "visitNum444: sort-->" + listTwo + "\n" + listTwo.size());

                if (listTwo.size() > 0) {
                    int saveFlag = 0;
                    for (int i = 0; i < listTwo.size(); i++) {
                        if (saveFlag >= 5) break;
                        YData[i] = listTwo.get(i).getKey();
                        XData[i] = listTwo.get(i).getValue();
                        saveFlag++;
                    }
                }
                Log.e(TAG, "visitNum444: YData[]--->" + Arrays.toString(YData) + " XData--->" + Arrays.toString(XData));

                break;
            case 3:
                TreeMap<String, Integer> sortedMapThree = new TreeMap<>(aDay);
                // 将 TreeMap 转换为 List
                List<Map.Entry<String, Integer>> listThree = new ArrayList<>(sortedMapThree.entrySet());

                // 使用 Collections.sort() 方法对 List 进行排序
                listThree.sort(new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });
                Log.e(TAG, "visitNum555: sort-->" + listThree + "\n" + listThree.size());

                if (listThree.size() > 0) {
                    int saveFlag = 0;
                    for (int i = 0; i < listThree.size(); i++) {
                        if (saveFlag >= 5) break;
                        YData[i] = listThree.get(i).getKey();
                        XData[i] = listThree.get(i).getValue();
                        saveFlag++;
                    }
                }
                Log.e(TAG, "visitNum555: YData[]--->" + Arrays.toString(YData) + " XData--->" + Arrays.toString(XData));

                break;
        }
        visitNumData(YData, XData);

    }

    public void prepareData() {
        List<Visitor.ContentBean> dataList = new ArrayList<>(chartList);
        List<Visitor.ContentBean> recentMonth = new ArrayList<>();
        List<Visitor.ContentBean> todayNum = new ArrayList<>();
        List<Visitor.ContentBean> arriveNum = new ArrayList<>();
        List<Visitor.ContentBean> leaveNum = new ArrayList<>();

        a = b = c = d = e = 0;//绘图前清除过时数据
        for (Visitor.ContentBean contentBean : dataList) {
            if (TimeJudge.judgeAmonthRecent(contentBean.getTime()))
                recentMonth.add(contentBean);
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime()))
                todayNum.add(contentBean);
            if (contentBean.getStatus() == 1)
                arriveNum.add(contentBean);
            if (contentBean.getStatus() == 2)
                leaveNum.add(contentBean);
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime())) {
                if ("面试".equals(contentBean.getReason())) {
                    a++;
                } else if ("商务洽谈".equals(contentBean.getReason())) {
                    b++;
                } else if ("入职".equals(contentBean.getReason())) {
                    c++;
                } else if ("驻场供应商".equals(contentBean.getReason())) {
                    d++;
                } else if ("外包驻场".equals(contentBean.getReason())) {
                    e++;
                }
            }
        }
        recentAmonth.setText("" + recentMonth.size());
        numToday.setText("" + todayNum.size());
        numArrive.setText("" + arriveNum.size());
        numLeave.setText("" + leaveNum.size());

        showChart();

    }

    public void showChart() {
        visitReasonChart();
        visitWayChart();
        visitChangeChart();
    }

    public void visitReasonChart() {
//        AAChartModel aaChartReason = new AAChartModel()
//                .chartType(AAChartType.Column)
////                .title("THE HEAT OF PROGRAMMING LANGUAGE")
////                .subtitle("Virtual Data")
////                .backgroundColor("#4b2b7f")
//                .categories(new String[]{"aaa","xxx","ccc","fff","ggg"})
//                .dataLabelsEnabled(false)
//                .yAxisGridLineWidth(0f)
//                .series(new AASeriesElement[]{
//                        new AASeriesElement()
//                                .name("Tokyo")
//                                .data(new Object[]{7.0, 6.9, 9.5}),
//                        new AASeriesElement()
//                                .name("NewYork")
//                                .data(new Object[]{0.2, 0.8, 5.7}),
//                        new AASeriesElement()
//                                .name("London")
//                                .data(new Object[]{0.9, 0.6, 3.5}),
////                        new AASeriesElement()
////                                .name("Berlin")
////                                .data(new Object[]{3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8})
//                });
//
//        aaChartViewVisitReason.aa_drawChartWithChartModel(aaChartReason);

        testBarChart();
    }

    public void testBarChart() {
        barChart = findViewById(R.id.bar_chart);

//        // 设置BarChart的描述文字
//        Description description = new Description();
//        description.setText("Bar Chart Example");
//        barChart.setDescription(description);

        //去除" Description Label"
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);

        // 禁用图例（图例是标识不同数据集的小方块）
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        // 设置x轴的标签和数据
        List<String> xAxisLabels = new ArrayList<>();
        xAxisLabels.add("面试");
        xAxisLabels.add("商务洽谈");
        xAxisLabels.add("入职");
        xAxisLabels.add("驻场供应商");
        xAxisLabels.add("外包驻场");
        BarDataSet barDataSet = new BarDataSet(getData(), "");
        barDataSet.setColor(Color.rgb(0, 133, 255));
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "";
            }
        });
        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(12f);      //图形上方字体大小
        barChart.setData(barData);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        // 将 axisMinimum 属性设置为 0
        leftAxis.setAxisMinimum(0f);
        // 设置x轴的标签的位置、旋转角度和数量
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(0);
        xAxis.setLabelCount(xAxisLabels.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        // 设置BarChart的一般属性
        barChart.setDoubleTapToZoomEnabled(false);//设置为false以禁止通过在其上双击缩放图表
        barChart.animateY(1400);
        barChart.setDrawValueAboveBar(true);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false);//启用/禁用缩放图表上的两个轴
        barChart.setNoDataText("");
        barChart.invalidate();

        //关闭背景参考线
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);

        barChart.getAxisLeft().setEnabled(false);       // 去除左侧纵坐标轴
        barChart.getAxisRight().setEnabled(false);      // 去除右侧纵坐标

    }

    private List<BarEntry> getData() {
        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0f, a == 0 ? 0.02f : a));
        entries.add(new BarEntry(1f, b == 0 ? 0.02f : b));
        entries.add(new BarEntry(2f, c == 0 ? 0.02f : c));
        entries.add(new BarEntry(3f, d == 0 ? 0.02f : d));
        entries.add(new BarEntry(4f, e == 0 ? 0.02f : e));
        return entries;
    }

    private List<BarEntry> getDataNum(int[] XData) {
        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0f, XData[4] == 0 ? 0.000f : XData[4]));
        entries.add(new BarEntry(1f, XData[3] == 0 ? 0.000f : XData[3]));
        entries.add(new BarEntry(2f, XData[2] == 0 ? 0.000f : XData[2]));
        entries.add(new BarEntry(3f, XData[1] == 0 ? 0.000f : XData[1]));
        entries.add(new BarEntry(4f, XData[0] == 0 ? 0.000f : XData[0]));
        return entries;
    }

    private List<BarEntry> getDataMainTime(int[] XData) {
        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0f, XData[2] == 0 ? 0.000f : XData[2]));
        entries.add(new BarEntry(1f, XData[1] == 0 ? 0.000f : XData[1]));
        entries.add(new BarEntry(2f, XData[0] == 0 ? 0.000f : XData[0]));
        return entries;
    }

    public void visitWayChart() {
//        AAChartModel aaChartWay = new AAChartModel()
//                .chartType(AAChartType.Pie)
////                .title("THE HEAT OF PROGRAMMING LANGUAGE")
////                .subtitle("Virtual Data")
////                .backgroundColor("#4b2b7f")
//                .categories(new String[]{"Java", "Swift", "Python", "Ruby", "PHP", "Go", "C", "C#", "C++"})
//                .dataLabelsEnabled(false)
//                .yAxisGridLineWidth(0f)
//                .series(new AASeriesElement[]{
//                        new AASeriesElement()
//                                .name("Tokyo")
//                                .data(new Object[]{7.0, 6.9}),
//                        new AASeriesElement()
//                                .name("NewYork")
//                                .data(new Object[]{0.2, 0.8}),
////                        new AASeriesElement()
////                                .name("London")
////                                .data(new Object[]{0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0}),
////                        new AASeriesElement()
////                                .name("Berlin")
////                                .data(new Object[]{3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8})
//                });
//        aaChartViewVisitWay.aa_drawChartWithChartModel(aaChartWay);

        testPieChart();
    }

    public void testPieChart() {
        pieChart = findViewById(R.id.pie_chart);

        //去除" Description Label"
        Description description = new Description();
        description.setEnabled(false);
        pieChart.setDescription(description);

        // 设置pieChart的属性
        pieChart.setRotationEnabled(true);      // 可旋转
        pieChart.setCenterTextSize(12f);         // 饼图描述字体大小为20
        pieChart.setHoleRadius(60f);          // 饼状图中间空白圆心半径
        pieChart.setTransparentCircleRadius(68f); // 饼状图中间透明圈半径
        pieChart.setDrawCenterText(true);     // 饼状图中间文字可见
        pieChart.setDrawEntryLabels(false);   //设置pieChart是否只显示饼图上百分比不显示文字
        pieChart.animateY(1400);
        pieChart.setHoleColor(ContextCompat.getColor(ShowActivity.this, R.color.white));//中心背景
        pieChart.setNoDataText("");
//        pieChart.setUsePercentValues(true);

        // 设置饼状图中间的文字
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setCenterText(numToday.getText());
        pieChart.setCenterTextSize(24);

//        pieChart.setExtraOffsets(5, 5, 1, 5);//设置四周相隔距离属性

        testPieSetData();
    }

    public void testPieSetData() {
        int centerNumFlag = 0;
        List<Visitor.ContentBean> contentBeans = new ArrayList<>(chartList);
        float valueA = 0, valueB = 0, num;
        for (Visitor.ContentBean contentBean : contentBeans) {
            if (contentBean.getType() == 0 && TimeJudge.judgeTimeIsToday(contentBean.getTime()))
                valueA++;//主动
            else if (contentBean.getType() == 1 && TimeJudge.judgeTimeIsToday(contentBean.getTime()))
                valueB++;//邀请
        }
        num = valueA + valueB;
        int percentA = (int) (valueA / num * 1000);
        int i = percentA % 10;
        if (i > 4) percentA = (int)
                (valueA / num * 100) + 1;
        else percentA = (int) (valueA / num * 100);
        int percentB = 100 - percentA;

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (percentA > 0 && Integer.parseInt("" + numToday.getText()) != 0)
            entries.add(new PieEntry(percentA, "主动来访"));
        if (percentB > 0 && Integer.parseInt("" + numToday.getText()) != 0)
            entries.add(new PieEntry(percentB, "邀请来访"));
        if ((percentA <= 0 && Integer.parseInt("" + numToday.getText()) == 0)
                || (percentB <= 0 && Integer.parseInt("" + numToday.getText()) == 0)) {
            // 禁用图例（图例是标识不同数据集的小方块）
            Legend legend = pieChart.getLegend();
            legend.setEnabled(false);
            entries.add(new PieEntry(100, ""));
            centerNumFlag = 1;//如果进入这里去除图例，并且只获取一种color
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String s = (int) value + "%";
                if ((int) value == 100 && Integer.parseInt("" + numToday.getText()) == 0) return "";
                else return s;

            }
        });
        dataSet.setSliceSpace(3f);              // 每个饼块之间的间隙
        dataSet.setSelectionShift(5f);          // 选中的饼块离饼形图中心的距离

        // 设置每个饼块的颜色
        ArrayList<Integer> colors = new ArrayList<>();
        if (centerNumFlag != 1) {
            colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_chart_one));
        }
        colors.add(ContextCompat.getColor(ShowActivity.this, R.color.pie_chart_two));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
//        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);             // 图上方字体的大小为20

        pieChart.setData(data);
        pieChart.invalidate();                 // refresh
    }

    public void visitChangeChart() {
        AAChart();
//        testLineChart();
    }

    public void AAChart() {
        //近一月，一周，一天数据集
        List<String> monthData = new ArrayList<>();
        List<String> weekData = new ArrayList<>();
        List<String> todayData = new ArrayList<>();
        for (Visitor.ContentBean contentBean : chartList) {
            if (TimeJudge.judgeAmonthRecent(contentBean.getTime())) {
                String date = TimeJudge.getDayDate(contentBean.getTime());
                monthData.add(date);
            }
            if (TimeJudge.judgeAweekRecent(contentBean.getTime())) {
                String date = TimeJudge.getDayDate(contentBean.getTime());
                weekData.add(date);
            }
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime())) {
                String dayHours = TimeJudge.getDayHours(contentBean.getTime());
                todayData.add(dayHours);
            }
        }
        Log.d(TAG, "AAChart: monthData-->" + monthData.size() + monthData + "\n"
                + "weekData-->" + weekData.size() + weekData + "\n"
                + "todayData-->" + todayData.size() + todayData);

        //近一月，一周，一天数据集，各个数据出现的次数
        Map<String, Integer> aMonth = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        Map<String, Integer> aWeek = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        Map<String, Integer> aDay = new HashMap<String, Integer>(); // 存储字符串出现次数的HashMap
        for (String data : monthData) {
            if (aMonth.containsKey(data))
                aMonth.put(data, aMonth.get(data) + 1);
            else aMonth.put(data, 1);
        }
        for (String data : weekData) {
            if (aWeek.containsKey(data))
                aWeek.put(data, aWeek.get(data) + 1);
            else aWeek.put(data, 1);
        }
        for (String data : todayData) {
            if (aDay.containsKey(data + ":00"))
                aDay.put(data + ":00", aDay.get(data + ":00") + 1);
            else aDay.put(data + ":00", 1);
        }
        Log.d(TAG, "AAChart: aMonth-->" + aMonth + "\n" + "aWeek-->" + aWeek + "\n" + "aDay-->" + aDay + aDay.isEmpty());

        switch (spinnerFlag) {
            case 1:
                Map<String, Integer> preDataMonth = new HashMap<String, Integer>();//x轴日期预先赋值
                String[] amonthAgoZeroHour = TimeJudge.getAmonthAgoZeroHour();
                long dateToLong = TimeJudge.getDateToLong(amonthAgoZeroHour[0]);

                for (int i = 0; i <= 30; i++) {
                    String longToDate = TimeJudge.getLongToDate(dateToLong);
                    preDataMonth.put(longToDate, 0);
                    dateToLong = dateToLong + 24 * 60 * 60 * 1000L;
                }
                TreeMap<String, Integer> sortedMap = new TreeMap<>(String::compareTo);
                sortedMap.putAll(preDataMonth);
                sortedMap.putAll(aMonth);

                String[] Xline = new String[sortedMap.size()];
                Object[] YData = new Object[sortedMap.size()];
                Xline = (String[]) sortedMap.keySet().toArray(new String[0]);
                YData = sortedMap.values().toArray(new Object[0]);
                showAAlineChart(Xline, YData);

                break;
            case 2:
                Map<String, Integer> preDataWeek = new HashMap<String, Integer>();//x轴日期预先赋值
                String[] weekAgoZeroHour = TimeJudge.getAweekAgoZeroHour();
                long dateToLong2 = TimeJudge.getDateToLong(weekAgoZeroHour[0]);

                for (int i = 0; i <= 7; i++) {
                    String longToDate = TimeJudge.getLongToDate(dateToLong2);
                    preDataWeek.put(longToDate, 0);
                    dateToLong2 = dateToLong2 + 24 * 60 * 60 * 1000L;
                }
                TreeMap<String, Integer> sortedMap2 = new TreeMap<>(String::compareTo);
                sortedMap2.putAll(preDataWeek);
                sortedMap2.putAll(aWeek);

                String[] Xline2 = new String[sortedMap2.size()];
                Object[] YData2 = new Object[sortedMap2.size()];
                Xline2 = (String[]) sortedMap2.keySet().toArray(new String[0]);
                YData2 = sortedMap2.values().toArray(new Object[0]);
                showAAlineChart(Xline2, YData2);

                break;
            case 3:
                Map<String, Integer> preDataToday = new TreeMap<>();//x轴时间预先赋值
                for (int i = 6; i <= 22; i++) {
                    if (i <= 9) preDataToday.put("0" + i + ":00", 0);
                    else preDataToday.put(i + ":00", 0);
                }
                preDataToday.putAll(aDay);

                String[] Xline3 = new String[preDataToday.size()];
                Object[] YData3 = new Object[preDataToday.size()];
                Xline3 = (String[]) preDataToday.keySet().toArray(new String[0]);
                YData3 = preDataToday.values().toArray(new Object[0]);
                showAAlineChart(Xline3, YData3);

                break;
        }
    }

    public void showAAlineChart(String[] XLine, Object[] YData) {
        // 创建 x 轴 labels 对象，并设置 rotation 属性为 -45
        AALabels labels = new AALabels()
                .rotation(0);
        // 创建 x 轴对象，并设置 labels 属性为之前创建的 labels
        AAXAxis xAxis = new AAXAxis()
                .labels(labels);

        AAChartModel aaChartChange = new AAChartModel()
                .chartType(AAChartType.Areaspline)
                .animationDuration(1200)
//                .title("THE HEAT OF PROGRAMMING LANGUAGE")
//                .subtitle("Virtual Data")
//                .backgroundColor("#4b2b7f")
                .categories(XLine)
                .dataLabelsEnabled(false)
                .legendEnabled(false)
                .yAxisGridLineWidth(0f)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("")
                                .data(YData)
                                .color("#69B7FF")
//                        new AASeriesElement()
//                                .name("NewYork")
//                                .data(new Object[]{0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 2.5}),
//                        new AASeriesElement()
//                                .name("London")
//                                .data(new Object[]{0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0}),
//                        new AASeriesElement()
//                                .name("Berlin")
//                                .data(new Object[]{3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8})
                })
                .markerRadius(2f);//圆点大小
        AAOptions aaOptions = aaChartChange.aa_toAAOptions();
//        aaOptions.xAxis(xAxis);
        aaOptions.yAxis
                .min(0).minRange(1)//使Y轴数据均为0时Y轴起点在X轴
//                .tickAmount(6)
                .allowDecimals(false);
        aaOptions.chart
                .events(new AAChartEvents()
                        .load("function() {\n" +
                                "   const chart = this;\n" +
                                "   Highcharts.addEvent(\n" +
                                "       chart.tooltip,\n" +
                                "       'refresh',\n" +
                                "       function () {\n" +
                                "           chart.tooltip.hide(1000);\n" + //设置 tooltip 自动隐藏的时间，单位为 ms
                                "   });\n" +
                                "}"));

//        aaChartViewVisitChange.aa_drawChartWithChartModel(aaChartChange);
        aaChartViewVisitChange.aa_drawChartWithChartOptions(aaOptions);//旋转角度设置
    }

    public void testLineChart() {
        lineChart = findViewById(R.id.line_chart);
        // 禁用图例（图例是标识不同数据集的小方块）
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
        //去除" Description Label"
        Description description = new Description();
        description.setEnabled(false);
        lineChart.setDescription(description);

        List<Entry> entries = new ArrayList<>();
        List<String> month = new ArrayList<>();
        List<String> week = new ArrayList<>();
        List<String> today = new ArrayList<>();
        for (Visitor.ContentBean contentBean : chartList) {
            if (TimeJudge.judgeAmonthRecent(contentBean.getTime())) {
                String date = TimeJudge.getDayDate(contentBean.getTime());
                month.add(date);
            }
            if (TimeJudge.judgeAweekRecent(contentBean.getTime())) {
                String date = TimeJudge.getDayDate(contentBean.getTime());
                week.add(date);
            }
            if (TimeJudge.judgeTimeIsToday(contentBean.getTime())) {
                String dayHours = TimeJudge.getDayHours(contentBean.getTime());
                today.add(dayHours);
            }
        }
        switch (spinnerFlag) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;

        }
        entries.add(new Entry(1, 10));
        entries.add(new Entry(2, 20));
        entries.add(new Entry(3, 30));


        //设置样式,设置图表右边的y轴禁用
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        //设置图表左边的y轴禁用
//        YAxis leftAxis = lineChart.getAxisLeft();
//        leftAxis.setEnabled(false);
        //设置x轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(1f);
        xAxis.setDrawAxisLine(true);//是否绘制轴线
        xAxis.setDrawGridLines(false);//设置x轴上每个点对应的线
        xAxis.setDrawLabels(true);//绘制标签  指x轴上的对应数值
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.animateX(1400);
        lineChart.invalidate();

        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pieChart != null) pieChart.clear();
        if (lineChart != null) lineChart.clear();
        if (barChart != null) barChart.clear();
        pieChart = null;
        lineChart = null;
        barChart = null;
    }

    @Override
    public void onBackPressed() {
        if (pageFlag == 2 && chartShow.getVisibility() == View.VISIBLE) {
            chartTwo.setVisibility(View.GONE);
            chartOne.setVisibility(View.VISIBLE);
            show_text.setVisibility(View.VISIBLE);
            pageFlag = 1;
            return;
        }
        if (pageFlag == 1 && chartShow.getVisibility() == View.VISIBLE) {
            chartShow.setVisibility(View.GONE);
            allDetail.setVisibility(View.VISIBLE);
            show_text.setVisibility(View.GONE);
            pageFlag = 0;
            return;
        }
        if (showContent.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        }
        if (allDetail.getVisibility() == View.VISIBLE) {
            allDetail.setVisibility(View.GONE);
            showContent.setVisibility(View.VISIBLE);
        }
    }
}