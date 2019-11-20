package com.senior.arexplorer.AR;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.senior.arexplorer.AR.Assistant.CompassAssistant;
import com.senior.arexplorer.R;

public class CameraOverlay extends View implements CompassAssistant.CompassAssistantListener {
    private boolean isRunning = true;
    Paint p = new Paint();
    Rect rect = new Rect(), curCompass = new Rect();
    Bitmap compass;
    float heading = 0;
    float scale;
    int fov = 90;
    int drawDistance = 1000;
    CompassAssistant assistant;


    public CameraOverlay(Context context){
        super(context);
        init(context);
    }

    public void init(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        assistant = new CompassAssistant(windowManager, sensorManager);
        assistant.addCompassListener(this);

        assistant.onStart();


        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        setAlpha(1f);

        compass = BitmapFactory.decodeResource(getResources(), R.drawable.compass);
        scale = (float) compass.getWidth() / 720;
        //System.out.println(scale * 360 + " " + compass.getWidth());

    }

    public void toggleTimer(){
        if(!isRunning){
            assistant.onStart();
        }
        else {
            assistant.onStop();
        }
        isRunning = !isRunning;
    }

    public void kill(){
        isRunning = false;

        assistant.onStop();
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
        int offset = (int) (fov * scale);
        int mid = width / 2 +  (int) ((heading) * scale);

        curCompass.set(mid - offset,0,mid + offset, compass.getHeight());
    }

    void setFoV(int newFoV){
        fov = newFoV;
    }

    void setDD(int newDrawDistance){
        drawDistance = newDrawDistance;
    }

    private float previousCompassBearing = -1f;

    @Override
    public void onCompassChanged(float userHeading) {
        if (previousCompassBearing < 0) {
            previousCompassBearing = userHeading;
        }
        float normalizedBearing = CompassAssistant.shortestRotation(userHeading, previousCompassBearing);
        previousCompassBearing = userHeading;
        heading = normalizedBearing;

        String status = "NO_CONTACT";
        switch (assistant.getLastAccuracySensorStatus()) {
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
                status = "NO_CONTACT";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                status = "UNRELIABLE";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                status = "ACCURACY_LOW";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                status = "ACCURACY_MEDIUM";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                status = "ACCURACY_HIGH";
                break;
        }

        //System.out.printf("CompassBearing: %f\nAccuracySensorStatus: %s\n", normalizedBearing, status);
        invalidate();
    }

    @Override
    public void onCompassAccuracyChange(int compassStatus) {}
}
