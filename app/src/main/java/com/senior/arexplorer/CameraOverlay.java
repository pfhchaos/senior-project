package com.senior.arexplorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;

public class CameraOverlay extends View{
    private Handler clockHandler;
    private Runnable clockTimer;
    private boolean isRunning = true;
    Paint p = new Paint();
    Rect rect = new Rect(), curCompass = new Rect();
    Bitmap compass;
    int bearing = 0;

    private final long TIMER_MSEC = 100;

    public CameraOverlay(Context context){
        super(context);
        init();
    }

    public void init(){

        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        setAlpha(1f);


        clockHandler = new Handler();
        clockTimer = new Runnable() {
            @Override
            public void run(){
                if(isRunning) {
                    bearing += 20;
                    invalidate();
                    clockHandler.postDelayed(this, TIMER_MSEC);
                }
            }
        };
        clockTimer.run();

        compass = BitmapFactory.decodeResource(getResources(),R.drawable.compass);
        toggleTimer();

    }

    public void toggleTimer(){
        isRunning = !isRunning;
        if(isRunning)
            clockHandler.postDelayed(clockTimer, TIMER_MSEC);
        else
            clockHandler.removeCallbacks(clockTimer);
    }

    public void stopTimer(){
        isRunning = false;
        clockHandler.removeCallbacks(clockTimer);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p.setColor(Color.BLUE);
        p.setStyle(Paint.Style.FILL);

        float sx = (float) getWidth() / 10000;
        float sy = (float) getHeight() / 10000;
        canvas.scale(sx,sy);

        rect.set(500,500,9500,1000);
        calcCompass();
        canvas.drawBitmap(compass, curCompass, rect, null);
    }

    private void calcCompass(){
        int width = compass.getWidth();
        int offset = width / 4;
        int mid =  width / 2 + bearing;
        if(mid >= width / 2 + offset) {
            mid = width / 2 - offset;
            bearing = - offset;
        }
        curCompass.set(mid - offset,0,mid + offset, compass.getHeight());
    }
}
