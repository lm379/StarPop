package cn.lm379.starpop.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;


import cn.lm379.starpop.R;
import cn.lm379.starpop.model.FlashBitmap;

public class StageUtil {

    public static final int row = 8; // 行、列
    public static final int col = 8;
    private static final int point = 3; // 最少消除数量

    private static final FlashBitmap stage = new FlashBitmap();

    /**
     * 初始化舞台相关信息
     * @param x x
     * @param y y
     * @param width 宽
     * @param height 高
     */
    public static void initStage(int x, int y, int width, int height) {
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(width);
        stage.setHeight(height);
    }

    public static FlashBitmap getStage() {
        return stage;
    }

    /**
     * 判断是否在舞台内
     *
     * @param x x
     * @param y y
     * @return boolean
     */
    public static boolean inStage(float x, float y) {
        return x >= stage.getX() && x <= stage.getWidth() && y >= stage.getY() && y <= stage.getHeight();
    }

    /**
     * 检查两个点是否符合交换
     * 1.约定只有相邻的两个点可以交换，若距离超过两个点计算出相邻的两个点
     * 2.约定不能斜边的点可以交换
     * 3.约定一个点无法完成交换，计为无效
     *
     * @param point 只有返回true，此数组才有值
     * @return true:可以交换；false 不可以交换
     */
    public static boolean checkTwoPoint(FlashBitmap p1, FlashBitmap p2, int[] point) {
        final int width = ImageUtil.getImageWidth();
        final int height = ImageUtil.getImageHeight();
        // 计算鼠标按下时的真实坐标
        int px1 = (int) (p1.getX() - stage.getX()) / width;
        int py1 = (int) (p1.getY() - stage.getY()) / height;
        // 注意鼠标抬起时并不是真正的可以跟px1, py1的坐标，
        // 如：鼠标抬起时跟第一个点中间相差三个头像
        // 所以这里通过计算得出相邻的第一个点的头像
        int px2 = (int) (p2.getX() - stage.getX()) / width;
        int py2 = (int) (p2.getY() - stage.getY()) / height;
        // 1.约定只有相邻的两个点可以交换，若距离超过两个点计算出相邻的两个点
        if (Math.abs(py1 - py2) > 1) {
            py2 = (py2 > py1) ? py1 + 1 : py1 - 1;
        }
        if (Math.abs(px1 - px2) > 1) {
            px2 = (px2 > px1) ? px1 + 1 : px1 - 1;
        }
        // 3.约定一个点无法完成交换，计为无效
        if (px1 == px2 && py1 == py2) {
            return false;
        }
        // 2.约定不能斜边的点可以交换
        // 将正常交换的可能性判断完毕后其它的都为斜边的点
        boolean flag = Math.abs(px1 - px2) > 0 && Math.abs(py1 - py2) == 0;
        if (Math.abs(px1 - px2) == 0 && Math.abs(py1 - py2) > 0) {
            flag = true;
        }
        if (!flag) {
            return false;
        }
        point[0] = px1;
        point[1] = py1;
        point[2] = px2;
        point[3] = py2;
        return true;
    }

    /**
     * 检查整个舞台是否有可以消除的头像
     * 只要检测到有一个就会立刻返回true
     *
     * @return boolean
     */
    public static boolean checkClearPoint(FlashBitmap[][] bitmaps) {
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                // 检查该点是否有周边的点能达到消除，如能没记录
                if (checkPoint(bitmaps, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * todo 获取整个舞台能消除的点
     *
     * @param bitmaps bitmap
     * @return list
     */
    public static List<FlashBitmap> getClearPointData(FlashBitmap[][] bitmaps) {
        List<FlashBitmap> clearResult = new ArrayList<>();
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                // 检查该点是否有周边的点能达到消除，如能则记录
                if (checkPoint(bitmaps, i, j)) {
                    FlashBitmap point = new FlashBitmap();
                    point.setX(i);
                    point.setY(j);
                    clearResult.add(point);
                }
            }
        }
        return clearResult;
    }

    /**
     * 获取一组能交换的所有点
     *
     * @param bitmaps bitmap
     * @return list
     */
    public synchronized static List<FlashBitmap> getOneGroupClearPoint(FlashBitmap[][] bitmaps) {
        List<FlashBitmap> list = new ArrayList<>();
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                if (checkPoint(bitmaps, i, j, list)) {
                    // 注意当前点也要添加进消除集合里面
                    FlashBitmap point = new FlashBitmap();
                    point.setX(i);
                    point.setY(j);
                    list.add(point);
                    return list;
                }
            }
        }
        return list;
    }

    /**
     * 根据坐标检查该点的四周能否消除
     *
     * @param bitmaps bitmap
     * @param x x
     * @param y y
     * @return value
     */
    private static boolean checkPoint(FlashBitmap[][] bitmaps, int x, int y) {
        return checkPoint(bitmaps, x, y, null);
    }

    /**
     * 方法重载
     *
     * @param clearPoint 返回能消除的一组点
     */
    private static boolean checkPoint(FlashBitmap[][] bitmaps, int x, int y, List<FlashBitmap> clearPoint) {
        int countPoint = 1;
        FlashBitmap temp;
        FlashBitmap clearTemp;
        boolean clearPointIsNull = false;
        FlashBitmap bitmap = bitmaps[x][y];
        // 优先检测要查的这个点是否为空
        if (bitmap == null) {
            return false;
        }
        if (clearPoint == null) {
            clearPointIsNull = true;
            clearPoint = new ArrayList<>();
        }
        // 检查横边往右
        for (int i = x + 1; ; ++i) {
            if (i >= row) {
                break;
            }
            temp = bitmaps[i][y];
            if (temp != null && temp.getId() == bitmap.getId()) {
                clearTemp = temp.clone();
                clearTemp.setX(i);
                clearTemp.setY(y);
                clearPoint.add(clearTemp);
                countPoint++;
            } else {
                break;
            }
        }
        // 检查横边往左
        for (int i = x - 1; i >= 0; --i) {
            temp = bitmaps[i][y];
            if (temp != null && temp.getId() == bitmap.getId()) {
                clearTemp = temp.clone();
                clearTemp.setX(i);
                clearTemp.setY(y);
                clearPoint.add(clearTemp);
                countPoint++;
            } else {
                break;
            }
        }
        // 判断是否达到要求
        if (countPoint >= point) {
            // 这里如果是获取一组能消除的点就要判断y轴
            if (!clearPointIsNull) {
                return true;
            }
        } else {
            countPoint = 1;
            clearPoint.clear();
        }

        // 检查竖边往下
        for (int i = y + 1; ; ++i) {
            if (i >= col) {
                break;
            }
            temp = bitmaps[x][i];
            if (temp != null && temp.getId() == bitmap.getId()) {
                clearTemp = temp.clone();
                clearTemp.setX(x);
                clearTemp.setY(i);
                clearPoint.add(clearTemp);
                countPoint++;
            } else {
                break;
            }
        }
        // 检查横边往上
        for (int i = y - 1; i >= 0; --i) {
            temp = bitmaps[x][i];
            if (temp != null && temp.getId() == bitmap.getId()) {
                clearTemp = temp.clone();
                clearTemp.setX(x);
                clearTemp.setY(i);
                clearPoint.add(clearTemp);
                countPoint++;
            } else {
                break;
            }
        }
        // 判断是否达到要求
        if (countPoint >= point) {
            return true;
        } else {
            clearPoint.clear();
        }
        return false;
    }


    /**
     * todo 爆炸效果图片加载
     *
     * @param width w
     * @param height h
     */
    public static Bitmap[] getHiddenData(Resources res, int width, int height) {
        Bitmap bitmap;
        Bitmap[] boomBitmap = new Bitmap[15];
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_0);
        boomBitmap[0] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_1);
        boomBitmap[1] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_2);
        boomBitmap[2] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_3);
        boomBitmap[3] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_4);
        boomBitmap[4] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_5);
        boomBitmap[5] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_6);
        boomBitmap[6] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_7);
        boomBitmap[7] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_8);
        boomBitmap[8] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_9);
        boomBitmap[9] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_10);
        boomBitmap[10] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_11);
        boomBitmap[11] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_12);
        boomBitmap[12] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_13);
        boomBitmap[13] = DisplayUtil.resizeBitmap(bitmap, width, height);
        bitmap = BitmapFactory.decodeResource(res, R.mipmap.boom_14);
        boomBitmap[14] = DisplayUtil.resizeBitmap(bitmap, width, height);
        // 适应屏幕大小
        for (int i = 0; i < boomBitmap.length; ++i) {
            boomBitmap[i] = DisplayUtil.resizeBitmap(boomBitmap[i], width * 2, height * 2);
        }
        return boomBitmap;
    }

}
