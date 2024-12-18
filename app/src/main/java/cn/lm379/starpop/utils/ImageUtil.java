package cn.lm379.starpop.utils;

import android.content.res.Resources;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import cn.lm379.starpop.R;
import cn.lm379.starpop.model.FlashBitmap;

public class ImageUtil {
    // 图像的宽和高
    private static int imageWidth = 0;
    private static int imageHeight = 0;
    private static final Map<Integer, FlashBitmap> map = new HashMap<>();
    private static final FlashBitmap blueberry = new FlashBitmap(); // 蓝莓
    private static final FlashBitmap melon = new FlashBitmap(); // 甜瓜
    private static final FlashBitmap potato = new FlashBitmap(); // 土豆
    private static final FlashBitmap starfruit = new FlashBitmap(); // 杨桃
    private static final FlashBitmap strawberry = new FlashBitmap(); // 草莓
    private static final FlashBitmap powdermelon = new FlashBitmap(); // 霜瓜

    /**
     * 初始化图像数据
     * @param width w
     * @param height h
     * @param res res
     */
    public static void initImageData(int width, int height, Resources res) {
        imageWidth = width;
        imageHeight = height;
        // 初始化图像
        powdermelon.setId(0);
        powdermelon.setSize(width, height);
        powdermelon.setBitmap(BitmapFactory.decodeResource(res, R.drawable.powdermelon));

        blueberry.setId(1);
        blueberry.setSize(width, height);
        blueberry.setBitmap(BitmapFactory.decodeResource(res, R.drawable.blueberry));

        melon.setId(2);
        melon.setSize(width, height);
        melon.setBitmap(BitmapFactory.decodeResource(res, R.drawable.melon));

        potato.setId(3);
        potato.setSize(width, height);
        potato.setBitmap(BitmapFactory.decodeResource(res, R.drawable.potato));

        starfruit.setId(4);
        starfruit.setSize(width, height);
        starfruit.setBitmap(BitmapFactory.decodeResource(res, R.drawable.starfruit));

        strawberry.setId(5);
        strawberry.setSize(width, height);
        strawberry.setBitmap(BitmapFactory.decodeResource(res, R.drawable.strawberry));

        // 添加到 map
        map.put(powdermelon.getId(), powdermelon);
        map.put(blueberry.getId(), blueberry);
        map.put(melon.getId(), melon);
        map.put(potato.getId(), potato);
        map.put(starfruit.getId(), starfruit);
        map.put(strawberry.getId(), strawberry);
    }

    /**
     * 随机获取图像
     */
    private static final List<Integer> prepIndex = new ArrayList<>();
    private static final int overflow = 3; //  0 <= 该值 <= map.size()

    public static FlashBitmap getImage() {
        while (true) {
            // 随机产生 0 - map.size() 之间的数
            int index = (int) (Math.random() * map.size());
            // 判断是否相同
            if (prepIndex.contains(index)) {
                continue;
            }
            prepIndex.add(index);
            // 只要不与上 overflow 次的相同就行了
            if (prepIndex.size() > overflow) {
                prepIndex.remove(0);
            }
            return Objects.requireNonNull(map.get(index)).clone();
        }
    }

    public static int getImageWidth() {
        return imageWidth;
    }

    public static int getImageHeight() {
        return imageHeight;
    }
}
