package edu.hitsz.application;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

/** 按屏幕像素缓存和绘制滚动背景。 */
final class ScrollingBackground {
    private final int width;
    private final int height;
    private final BitmapShader shader;
    private final Paint paint = new Paint();
    private final Matrix translation = new Matrix();

    ScrollingBackground(Bitmap source, int width, int height) {
        this.width = width;
        this.height = height;
        Bitmap bitmap = Bitmap.createScaledBitmap(source, width, height, true);
        // 原图上下边缘不连续；交替正向/倒向采样，拼接处重复同一条边缘。
        // Shader 使用像素坐标，不会再次按 Bitmap/Canvas 的 density 比例缩放。
        shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.MIRROR);
        paint.setShader(shader);
    }

    boolean hasSize(int width, int height) {
        return this.width == width && this.height == height;
    }

    void draw(Canvas canvas, int offset) {
        translation.setTranslate(0, offset);
        shader.setLocalMatrix(translation);
        canvas.drawRect(0, 0, width, height, paint);
    }

    int advance(int offset, int pixels) {
        // 镜像纹理的周期是两倍高度，保留越界余量，奇数高度也不跳帧。
        return (int) Math.floorMod((long) offset + pixels, 2L * height);
    }
}
