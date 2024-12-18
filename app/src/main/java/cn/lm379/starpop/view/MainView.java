package cn.lm379.starpop.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import cn.lm379.starpop.R;
import cn.lm379.starpop.interfaces.MainViewInterface;
import cn.lm379.starpop.utils.DisplayUtil;

public class MainView extends View {

    private final Bitmap startBtn, exitBtn;
    private final TextView appName;
    private final Bitmap background; // 背景图片
    private final Paint paint; // 画笔
    private final int screenWidth;
    private final int screenHeight;
    private final int StardewValleyColor;
    private String ExitGame;
    private String StartGame;
    private int x, y, w, h; // 开始按钮显示的区域
    private int exitX, exitY, exitW, exitH; // 退出按钮显示的区域
    private MainViewInterface listener; // 事件监听接口

    @SuppressLint("ResourceType")
    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = this.getResources();

        // 获取颜色
        StardewValleyColor = ContextCompat.getColor(context, R.color.StardewValleyColor);

        ExitGame = context.getString(R.string.exit_game);
        StartGame = context.getString(R.string.start_game);

        // 实例化标题
        appName = new TextView(context);
        appName.setText(R.string.app_name);
        appName.setTextColor(StardewValleyColor);
        appName.setTextSize(50);
        appName.setGravity(1);

        // 设置自定义字体
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/HYPixel11pxJ-2.ttf");
        appName.setTypeface(typeface);

        // 加载开始按钮图片
        startBtn = BitmapFactory.decodeResource(res, R.drawable.background_btn);
        exitBtn = BitmapFactory.decodeResource(res, R.drawable.background_btn);

        // 加载背景图片
        background = BitmapFactory.decodeResource(res, R.drawable.background);
        screenHeight = DisplayUtil.getScreenHeight(context);
        screenWidth = DisplayUtil.getScreenWidth(context);

        // 实例化画笔
        paint = new Paint();
        paint.setAntiAlias(false); // 消除锯齿
        paint.setTypeface(typeface); // 设置字体
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bgBitmap, btnBitmap;
        // 绘制背景图片

        bgBitmap = DisplayUtil.resizeBitmap(background, screenWidth, screenHeight);
        canvas.drawBitmap(bgBitmap, 0, 0, paint);

        // 绘制标题
        int titleY = (int) (screenHeight * 0.1);
        appName.layout(0, titleY, screenWidth, screenHeight);
        appName.draw(canvas);

        // 计算开始按钮显示的坐标
        w = (int) (screenWidth * 0.5);
        h = (int) (screenHeight * 0.08);
        x = screenWidth / 2 - w / 2;
        y = screenHeight - (int) (screenHeight * 0.40);

        // 绘制开始按钮
        btnBitmap = DisplayUtil.resizeBitmap(startBtn, w, h);
        canvas.drawBitmap(btnBitmap, x, y, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(StartGame, x + (float) w / 2, y + (float) h / 2 - ((paint.descent() + paint.ascent()) / 2), paint);

        // 计算退出按钮显示的坐标
        exitW = (int) (screenWidth * 0.5);
        exitH = (int) (screenHeight * 0.08);
        exitX = screenWidth / 2 - exitW / 2;
        exitY = screenHeight - (int) (screenHeight * 0.28);

        // 绘制退出按钮
        btnBitmap = DisplayUtil.resizeBitmap(exitBtn, exitW, exitH);
        canvas.drawBitmap(btnBitmap, exitX, exitY, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(ExitGame, exitX + (float) exitW / 2, exitY + (float) exitH / 2 - ((paint.descent() + paint.ascent()) / 2), paint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 获取当前触控位置
        int ex = (int) event.getX();
        int ey = (int) event.getY();
        switch (event.getAction()) {
            // 按下
            case MotionEvent.ACTION_DOWN:
                if (ex > x && ex < (x + w)
                        && ey > y && ey < (y + h)) {
                    listener.startGame();
                } else if (ex > exitX && ex < (exitX + exitW)
                        && ey > exitY && ey < (exitY + exitH)) {
                    listener.exitGame();
                }
                break;
            // 移动
            case MotionEvent.ACTION_MOVE:
                break;
            // 抬起
            case MotionEvent.ACTION_UP:
                break;
        }
        // 刷新界面
        invalidate();
        // 使系统响应事件，返回true
        return true;
    }

    /**
     * 此方法交由MainActivity调用
     * 目的是获取公共接口对象以实现需要功能
     */
    public void setMainViewInterface(MainViewInterface mvi) {
        this.listener = mvi;
    }
}
