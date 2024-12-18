package cn.lm379.starpop.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import cn.lm379.starpop.R;
import cn.lm379.starpop.model.FlashBitmap;
import cn.lm379.starpop.utils.DisplayUtil;
import cn.lm379.starpop.utils.StageUtil;
import cn.lm379.starpop.utils.ImageUtil;

public class GameView extends View {

    private final Bitmap background; // 游戏背景
    private final Paint paint;
    private final int screenWidth;
    private final int screenHeight;
    private final Context context;
    private int consecutiveClear = 0; // 连续消除的次数
    // 资源统一管理
    private final int row = StageUtil.row;
    private final int col = StageUtil.col;
    private final Typeface typeface;
    // 两个方块的交换状态
    private boolean swapState = false;
    // 载入头像动画是否结束状态
    private boolean loadImageState = false;
    // 是否要加载舞台消除动画(程序运行时立即加载)
    private boolean load = true;
    private boolean clearLoad = true;
    // 头像以及游戏坐标
    private FlashBitmap[][] bitmaps = new FlashBitmap[row][col];
    // 背景音乐
    private final MediaPlayer bgMedia;
    private final SoundPool soundPool = new SoundPool.Builder().setMaxStreams(10).build();
    private final Map<Integer, Integer> soundPoolMap = new HashMap<>();
    // 线程池
    ExecutorService pool = Executors.newFixedThreadPool(5);


    // 游戏相关数据
    private int level = 1;
    private int currScore = 0;
    private final int[] accessScore = {800, 1200};  // 过关分数

    public GameView(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
        screenHeight = DisplayUtil.getScreenHeight(context);
        screenWidth = DisplayUtil.getScreenWidth(context);

        // 设置字体
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/HYPixel11pxJ-2.ttf");

        // 音乐相关初始化
        bgMedia = MediaPlayer.create(context, R.raw.stardew_valley_overture);
        bgMedia.start();

        // 循环播放监听
        bgMedia.setOnCompletionListener(mp -> {
            bgMedia.start();
            bgMedia.setLooping(true);
        });
        soundPoolMap.put(2, soundPool.load(this.getContext(), R.raw.swap_one, 1));
        soundPoolMap.put(3, soundPool.load(this.getContext(), R.raw.swap_two, 1));

        // int ave = screenWidth / (row + 2); // 将屏幕宽度分为 row + 2 等份
        int ave = 0;
        int size = (screenWidth - ave * 2) / row; // 其它两分为：舞台距离屏幕左右边的像素
        ImageUtil.initImageData(size, size, this.getResources()); // 初始化头像数据
        // 背景图片
        background = BitmapFactory.decodeResource(this.getResources(), R.drawable.background);

        /*
         * 计算出舞台距离左边屏幕的距离
         * 计算方式为：
         *   (屏幕总宽度 - 人物头像的宽 * 总行数) / 2
         */
        int leftSpan = (screenWidth - ImageUtil.getImageWidth() * row) / 2;
        int topSpan = (screenHeight - ImageUtil.getImageHeight() * col) / 3;
        // 将游戏舞台的坐标、高宽保存起来
        StageUtil.initStage(leftSpan, topSpan,
                leftSpan + ImageUtil.getImageWidth() * row,
                topSpan + ImageUtil.getImageHeight() * col);
        // 实例化画笔
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true); // 消除锯齿
        paint.setTypeface(typeface); // 设置字体
        initGamePoint();
    }

    /**
     * 生成游戏坐标
     */
    private void initGamePoint() {
        currScore = 0; // 清空当前得分
        bitmaps = new FlashBitmap[row][col];
        // 生成背景图片的坐标
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                do {
                    // 计算头像坐标
                    FlashBitmap bitmap = ImageUtil.getImage();
                    bitmap.setX(StageUtil.getStage().getX() + i * ImageUtil.getImageWidth());
                    bitmap.setY(StageUtil.getStage().getY() + j * ImageUtil.getImageHeight());
                    bitmap.setY(0); // 在顶部慢慢下落
                    bitmaps[i][j] = bitmap;
                } while (StageUtil.checkClearPoint(bitmaps));
            }
        }
        load = true;
        clearLoad = true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景图片
        Bitmap bgBitmap = DisplayUtil.resizeBitmap(background, screenWidth, screenHeight);
        canvas.drawBitmap(bgBitmap, 0, 0, paint);
        // 舞台中的所有头像
        FlashBitmap bitmap;
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                bitmap = bitmaps[i][j];
                if (bitmap != null // 并且坐标点要进入舞台
                        && StageUtil.inStage(bitmap.getX(), bitmap.getY() + (float) bitmap.getHeight() / 2)) {
                    canvas.drawBitmap(bitmap.getBitmap(), bitmap.getX(), bitmap.getY(), paint);
                }
            }
        }
        // 是否需要加载消除
        if (clearLoad && load) {
            clearBitmap();
            load = false;
        }
        int StardewValleyColor = ContextCompat.getColor(context, R.color.StardewValleyColor);
        paint.setColor(StardewValleyColor); // 设置颜色
        paint.setTextSize(60); // 设置字体大小
        paint.setTypeface(Typeface.create(typeface, Typeface.BOLD)); // 设置字体样式
        canvas.drawText(context.getString(R.string.currentLevel) + level, 10, StageUtil.getStage().getHeight() + 70, paint);
        canvas.drawText(context.getString(R.string.currentScore) + currScore, 10, StageUtil.getStage().getHeight() + 150, paint);
        canvas.drawText(context.getString(R.string.needScore) + accessScore[level - 1], 10, StageUtil.getStage().getHeight() + 230, paint);
        // 刷新屏幕的频率(理论上小于25，人就会感觉物体是在移动)
        postInvalidateDelayed(1);
    }

    // 用来保存鼠标按下的两个坐标值
    FlashBitmap p1 = new FlashBitmap();
    FlashBitmap p2 = new FlashBitmap();
    boolean isDown = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 判断交换状态是否完毕
        if (swapState) {
            return false;
        }
        // 如果正在做下落动画不允许操作
        if (loadImageState) {
            return false;
        }
        // 获取当前触控位置
        float ex = event.getX();
        float ey = event.getY();
        switch (event.getAction()) {
            // 按下
            case MotionEvent.ACTION_DOWN:
                // 判断是否该点是按在舞台上
                if (!isDown && StageUtil.inStage(ex, ey)) {
                    p1.setX(ex);
                    p1.setY(ey);
                    isDown = true;
                    Integer soundId = soundPoolMap.get(2);
                    if (soundId != null) {
                        soundPool.play(soundId, 1, 1, 0, 0, 1);
                    }
                }
                break;
            // 移动
            case MotionEvent.ACTION_MOVE:
                // 判断是否该点是按在舞台上
                if (!isDown && StageUtil.inStage(ex, ey)) {
                    p1.setX(ex);
                    p1.setY(ey);
                    isDown = true;
                }
                break;
            // 抬起
            case MotionEvent.ACTION_UP:
                if (isDown) {
                    p2.setX(ex);
                    p2.setY(ey);
                    isDown = false;
                    prepSwap();
                }
                break;
        }
        // 使系统响应事件，返回true
        return true;
    }


    /**
     * 交换预处理
     */
    private void prepSwap() {
        pool.execute(() -> {
            // 保存真实比例的四个点坐标
            swapState = true;
            int[] point = new int[4];
            if (StageUtil.checkTwoPoint(p1, p2, point)) {
                // 尝试交换
                Integer soundId = soundPoolMap.get(3);
                if (soundId != null) {
                    soundPool.play(soundId, 1, 1, 0, 0, 1);
                }
                swap(point[0], point[1], point[2], point[3]);
                // 判断交换后能不能消除，如果能消除则消除点
                if (StageUtil.checkClearPoint(bitmaps)) {
                    load = true; // 告诉程序可以更新
                } else {
                    // 如果不能消除继续交换回来
                    swap(point[0], point[1], point[2], point[3]);
                }
            }
            // 交换完成改回状态
            swapState = false;
        });
    }

    /**
     * 交换
     */
    final int time = 1; // 交换间隔时间
    float speed = 1; // 交换的速度
    Thread thread1 = null;
    Thread thread2 = null;

    private void swap(int x1, int y1, int x2, int y2) {
        speed = screenWidth / 560.0f; // 根据分辨率计算出不同的下落速度
        // 判断是横着交换还是竖的交换
        final int px1 = (int) StageUtil.getStage().getX() + x1 * ImageUtil.getImageWidth();
        final int py1 = (int) StageUtil.getStage().getY() + y1 * ImageUtil.getImageHeight();
        final int px2 = (int) StageUtil.getStage().getX() + x2 * ImageUtil.getImageWidth();
        final int py2 = (int) StageUtil.getStage().getY() + y2 * ImageUtil.getImageHeight();

        final FlashBitmap one = bitmaps[x1][y1];
        final FlashBitmap two = bitmaps[x2][y2];
        // 先进行真实坐标点互换
        FlashBitmap temp = bitmaps[x1][y1];
        bitmaps[x1][y1] = bitmaps[x2][y2];
        bitmaps[x2][y2] = temp;

        /*
         * 计算交换方式
         */
        // 判断是x轴交换还是y轴交换
        if (Math.abs(x1 - x2) == 1) {
            if (px1 < px2) {
                // 横着交换
                thread1 = new Thread(() -> {
                    for (float i = px1; i <= px2; i += speed) {
                        one.setX(i);
                        DisplayUtil.sleep(time);
                    }
                });
                thread2 = new Thread(() -> {
                    for (float i = px2; i >= px1; i -= speed) {
                        two.setX(i);
                        DisplayUtil.sleep(time);
                    }
                });
            } else {
                // 横着交换
                thread1 = new Thread(() -> {
                    for (float i = px2; i <= px1; i += speed) {
                        two.setX(i);
                        DisplayUtil.sleep(time);
                    }
                });
                thread2 = new Thread(() -> {
                    for (float i = px1; i >= px2; i -= speed) {
                        one.setX(i);
                        DisplayUtil.sleep(time);
                    }
                });
            }
        } else {
            if (y1 < y2) {
                // 横着交换
                thread1 = new Thread(() -> {
                    for (float i = py1; i <= py2; i += speed) {
                        one.setY(i);
                        DisplayUtil.sleep(time);
                    }
                });
                thread2 = new Thread(() -> {
                    for (float i = py2; i >= py1; i -= speed) {
                        two.setY(i);
                        DisplayUtil.sleep(time);
                    }
                });
            } else {
                // 横着交换
                thread1 = new Thread(() -> {
                    for (float i = py2; i <= py1; i += speed) {
                        two.setY(i);
                        DisplayUtil.sleep(time);
                    }
                });
                thread2 = new Thread(() -> {
                    for (float i = py1; i >= py2; i -= speed) {
                        one.setY(i);
                        DisplayUtil.sleep(time);
                    }
                });
            }
        }
        pool.execute(thread1);
        pool.execute(thread2);
        try {
            thread1.join();
            thread2.join();
        } catch (Exception e) {
            Log.e("GameView", "swap thread has an error: " + e);
        }
        DisplayUtil.sleep(100);
    }

    /**
     * 清除头像
     */
    private synchronized void clearBitmap() {
        clearLoad = false;
        Thread thread = new Thread(() -> {
            loadImageState = true; // 开始加载
            int size = 0;
            boolean cleared = false;
            while (StageUtil.checkClearPoint(bitmaps)) {
                List<FlashBitmap> clearList = StageUtil.getOneGroupClearPoint(bitmaps);
                for (FlashBitmap point : clearList) {
                    bitmaps[(int) point.getX()][(int) point.getY()] = null;
                    ++size;
                }
                cleared = true;
                DisplayUtil.sleep(200);
            }

            if (cleared) {
                if (consecutiveClear >= 8) {
                    consecutiveClear *= 3;
                } else if (consecutiveClear >= 4) {
                    consecutiveClear *= 2;
                } else {
                    consecutiveClear++;
                }
            } else {
                consecutiveClear = 0;
            }

            // 移动其它头像
            boolean updateFlag = false;
            boolean canDoWhile;
            int[] index = new int[col];
            do {
                canDoWhile = false;
                // 开始进行下落处理
                for (int j = col - 1; j >= 0; --j) {
                    for (int i = 0; i < row; ++i) {
                        if (bitmaps[i][j] != null) {
                            if (!moveStageImage(i, j)) {
                                canDoWhile = true;
                            }
                        } else {
                            // 只要检测到有任意一个空位就要进行更新
                            updateFlag = true;
                            // 如果是空，检测从当前位置往上是否还有其它头像
                            boolean hasPoint = false;
                            for (int k = j - 1; k >= 0; --k) {
                                // 如果检测到有一个点就停止
                                if (bitmaps[i][k] != null) {
                                    hasPoint = true;
                                    break;
                                }
                            }
                            // 如果没有就直接生成一个新点在这个位置
                            // 真实坐标是这个位置，但显示在地图上的坐标要给个到 (x, 0)
                            if (!hasPoint) {
                                FlashBitmap bitmap = ImageUtil.getImage();
                                bitmap.setX(StageUtil.getStage().getX() + i * ImageUtil.getImageWidth());
                                bitmap.setY(StageUtil.getStage().getY() - (index[i] + 1) * ImageUtil.getImageHeight());
                                bitmaps[i][j] = bitmap;
                                index[i]++;
                            }
                        }
                    }
                }
                // 动画停留间隔
                DisplayUtil.sleep(time);
            } while (canDoWhile);
            loadImageState = false; // 加载头像完毕
            // 提示系统进行可消除检测，停0.15秒再载入动画
            if (updateFlag || StageUtil.checkClearPoint(bitmaps)) {
                DisplayUtil.sleep(150);
                load = true;
            }
            // 计算得分
            if (size > 0 && size <= 6) {
                currScore = currScore + size * consecutiveClear;
            } else if (size > 6 && size <= 9) {
                currScore = currScore + size * 3 * consecutiveClear;
            } else {
                currScore = currScore + size * 5 * consecutiveClear;
            }
            if (level > accessScore.length) {
                showMsg("没有更多关卡了!");
                DisplayUtil.sleep(3000);
                return;
            }
            // 判断分数是否符合要求
            if (currScore >= accessScore[level - 1]) {
                ++level;
                showMsg("恭喜您通关啦!");
                DisplayUtil.sleep(3000);
                initGamePoint();
            }
        });
        pool.execute(thread);
        try {
            thread.join();
        } catch (Exception e) {
            Log.e("GameView", "clearBitmap thread has an error: " + e);
        }
        clearLoad = true;
    }

    /**
     * 移动舞台中的图片
     * @param x x
     * @param y y
     * @return 成功true 失败false
     */
    private boolean moveStageImage(int x, int y) {
        // 记录当前的点
        int j;
        float currY = bitmaps[x][y].getY();
        // 寻找最佳底部的空位
        for (j = col - 1; j >= 0; --j) {
            // 不能小于以前的位置
            if (j <= y) {
                break;
            }
            // 从底往上找，找到的第一个空位就为要到的位置
            if (bitmaps[x][j] == null) {
                break;
            }
        }
        // 有最新点时才进行交换
        if (j != y) {
            FlashBitmap temp = bitmaps[x][y];
            bitmaps[x][y] = null;
            bitmaps[x][j] = temp;
        }
        // 不允许在这之上的方块提前下落到下一个方块后面
        if (j < col - 1 && bitmaps[x][j + 1] != null) {
            if (currY + ImageUtil.getImageHeight() >= bitmaps[x][j + 1].getY()) {
                return true;
            }
        }
        // 到达指定高度停止
        if (currY >= j * ImageUtil.getImageHeight() + StageUtil.getStage().getY()) {
            return true;
        }
        // 大于舞台高度直接停止
        if (currY + bitmaps[x][j].getHeight() >= StageUtil.getStage().getHeight()) {
            return true;
        }
        // 自增
        currY += speed;
        bitmaps[x][j].setY(currY);
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bgMedia.isPlaying()) {
            bgMedia.stop();
        }
        soundPool.release();
        pool.shutdown();
        while (!pool.isTerminated()) {
            DisplayUtil.sleep(100);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_LONG).show();
        }
    };

    public void showMsg(String msg) {
        Message message = new Message();
        message.obj = msg;
        handler.sendMessage(message);
    }

    public void pauseMedia() {
        if (bgMedia != null && bgMedia.isPlaying()) {
            bgMedia.pause();
        }
    }

    public void resumeMedia() {
        if (bgMedia != null && !bgMedia.isPlaying()) {
            bgMedia.start();
        }
    }

    public void stopMedia() {
        if (bgMedia != null && bgMedia.isPlaying()) {
            bgMedia.stop();
            bgMedia.release();
        }
    }
}
