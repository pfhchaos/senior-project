package com.senior.arexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;

import java.io.IOException;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraView(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e){
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        canvas.translate(50, 50);
        canvas.drawPoint(150,150, p);
    }

    public CameraOverlay getOverlay(Context context){
        return new CameraOverlay(context);
    }

    protected class CameraOverlay extends View
    {
        private Handler clockHandler;
        private Runnable clockTimer;
        private boolean isRunning = true;

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
                        invalidate();
                        clockHandler.postDelayed(this, TIMER_MSEC);
                    }
                }
            };
            clockTimer.run();
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
            Paint p = new Paint();
            p.setColor(Color.BLUE);
            canvas.translate(100,100);
            canvas.drawRect(100,100,100,100, p);
        }

    }
}
