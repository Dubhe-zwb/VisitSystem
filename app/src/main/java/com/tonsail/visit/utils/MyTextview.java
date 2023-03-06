//package com.tonsail.visit.utils;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.Rect;
//import android.graphics.Typeface;
//import android.util.AttributeSet;
//import android.util.Log;
//
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//
//import com.tonsail.visit.R;
//import com.tonsail.visit.ShowActivity;
//
//import java.util.Timer;
//
//public  class MyTextview extends androidx.appcompat.widget.AppCompatTextView {
//    private static final boolean DBG = true;
//    // never public, so that another class won't be messed up.
//    private final static String TAG = "MyTextview ";
//
//    protected int mBackcolor;
//    protected int mTextColor;
//
//    private String mText;
//    private Paint mPaint;
//
//    private Rect mBound;//绘制时控制文本绘制的范围
//
//    private int width = -1;
//    private int height = -1;
//    private float top = 18;
//
//    private Rect mRect;
//    private Timer mTimer;
//    private Path path;
//
//    public MyTextview (Context context) {
//        super(context);
//    }
//
//    public MyTextview (Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//
//        mPaint = new Paint();//创建画笔
//        mText="n你好，世界，hellow world";// 初始化文本
//
//        mPaint.setColor(ContextCompat.getColor(context,R.color.white));//初始化画笔颜色
//        mBound = new Rect();//显示区域
//        mPaint.getTextBounds(mText, 0, mText.length(), mBound);
//        mPaint.clearShadowLayer();
//        initDisplay();
//        path = new Path();
//        path.lineTo(0,500);
//
//    }
//
//    public MyTextview (Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//
//    protected void initDisplay(ProgramParser.Item item) {
//
//        //RectColor
//        setBackgroundColor(mBackcolor);//初始化显示区域颜色
//
//
//        //Size
//        mPaint.setTextSize(Float.parseFloat(18));//初始化文字大小
//
//
//
//
//        //style
//        int style = Typeface.NORMAL;
//        style = Typeface.ITALIC;
//        mPaint.setTextSkewX(-0.25f);
//
//
//        style |= Typeface.BOLD;
//        mPaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG | getPaint().getFlags());
//
//
//        if ("1".equals( item.logfont.lfUnderline)) {
//            mPaint.setFlags(Paint.UNDERLINE_TEXT_FLAG | getPaint().getFlags());
//        }
//
//        Typeface typeface = AppController.getInstance().getTypeface(item.logfont.lfFaceName);
//        if (typeface == null)
//            typeface = Typeface.defaultFromStyle(style);
//        if (DBG)
//            Log.d(TAG, "style= " + style + ", lfFaceName= " +  item.logfont.lfFaceName +
//                    ", typeface= " + typeface +
//                    ((typeface == null)? "" : ", current style= " + typeface.getStyle()));
//
//        setTypeface(Typeface.SERIF);//风格设置
//    }
//
//    @Override
//    public boolean isFocused() {
//        return true;
//    }
//
//    public boolean isChinese(char str) {
//        Character.UnicodeBlock ub = Character.UnicodeBlock.of(str);
//        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//
//        super.onDraw(canvas);
//        float a = mPaint.measureText("正正", 0, 1);
//        float left = getWidth()/2-a/2;
//        float w;
//        final int len = mText.length();
//        float py = 0 ;
//        for(int i=0; i<len; i ++){
//            char c = mText.charAt(i);
//            w = mPaint.measureText(mText, i, i+1);//获取字符宽度
//            Log.d(TAG,"tongfei ---w= "+w);
//            StringBuffer b = new StringBuffer();
//            b.append(c);
//            if(py > getHeight()){//定义字的范围
//                return;
//            }
//            if(isChinese(c)){
//                py += w;
//                if(py > getHeight()){
//                    return;
//                }
//                canvas.drawText(b.toString(), left, py, mPaint); //中文处理方法
//            }else {
//                canvas.drawTextOnPath(b.toString(), path, py, -left-2, mPaint);//其他文字处理方法
//                py += w;
//            }
//        }
//
//    }
//
//}
