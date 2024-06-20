package com.example.tomnjerry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;


import android.graphics.Canvas;
import android.graphics.Paint;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;





import android.graphics.Canvas;
import android.graphics.Paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Obstacle {
    private int x;
    private int y;
    private int speed;
    private Bitmap bitmap1;
    private Bitmap bitmap2;
    private Bitmap currentBitmap;
    private int width;
    private int height;
    private long lastSwitchTime;
    private int lane;
    private static final long SWITCH_INTERVAL = 100;

    public Obstacle(Context context, int track, int yPosition) {
        if(track==2) {
            x = track * 400 + 100;
        } else if (track==1) {
            x = track * 400 + 140;
        } else{
            x = track * 400 + 180;
        }
        y = yPosition;
        speed = 20;
        lane=track;

        Bitmap originalBitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.aisawblade1);
        Bitmap originalBitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.aisawblade2);

        width = 200;
        height = 200;

        bitmap1 = Bitmap.createScaledBitmap(originalBitmap1, width, height, false);
        bitmap2 = Bitmap.createScaledBitmap(originalBitmap2, width, height, false);

        currentBitmap = bitmap1;
        lastSwitchTime = System.currentTimeMillis();
    }
    public int getLane() {
        return lane;
    }

    public void update(float v) {
        y += (int) (speed* v);

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSwitchTime >= SWITCH_INTERVAL) {
            currentBitmap = (currentBitmap == bitmap1) ? bitmap2 : bitmap1;
            lastSwitchTime = currentTime;
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(currentBitmap, x - width / 2, y - height / 2, paint);
    }

    public boolean isColliding(Jerry jerry) {
        int jerryX = jerry.getX();
        int jerryY = jerry.getY();
        int jerryRadius = jerry.getRadius();

        return (Math.abs(x - jerryX) < width / 2 + jerryRadius) && (Math.abs(y - jerryY) < height / 2 + jerryRadius);
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return 100;
    }
    public int getHeight() {
        return 100;
    }

}
