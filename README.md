## 大致算法

### 数据结构

定义一个 8x8 的二维数组，数组取值表示不同种类的图案

>其中FlashBitmap为自定义数据结构

```java
private FlashBitmap[][] bitmaps = new FlashBitmap[row][col];
```

假设有6种不同的图案: 土豆 霜瓜 甜瓜 杨桃 蓝莓 草莓，分别用int变量 0-5表示

### 初始化数组

数据采用Java生成随机数的方法

```java
// 随机产生 0 - map.size() 之间的数
int index = (int) (Math.random() * map.size());
```

### 随机填充数据
```Java
bitmaps = new FlashBitmap[row][col];
for (int i = 0; i < row; ++i) {  
    for (int j = 0; j < col; ++j) {  
        do {  
            // 为bitmaps填充随机值
        } while (StageUtil.checkClearPoint(bitmaps));  
    }  
}
```

### 添加到画布
```java
for (int i = 0; i < row; ++i) {  
    for (int j = 0; j < col; ++j) {  
        bitmap = bitmaps[i][j];  
        if (bitmap != null && 并且坐标点要进入舞台 ) {  
            canvas.drawBitmap(bitmap相关属性);  
        }  
    }  
}
```