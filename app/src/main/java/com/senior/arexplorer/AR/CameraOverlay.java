package com.senior.arexplorer.AR;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.view.View;


import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.R;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatDrawableManager;

public class CameraOverlay extends View implements CompassAssistant.CompassAssistantListener {
    private boolean isRunning = true;
    Paint p = new Paint();
    Rect rect = new Rect(), curCompass = new Rect();
    Bitmap compass, compassMarker;
    float heading = 0;
    float scale;
    int fov = 180;
    int drawDistance = 1000;
    CompassAssistant assistant;

    Location curLoc;
    List<Location> nearby;


    public CameraOverlay(Context context){
        super(context);
        init(context);
    }

    public void init(Context context){
        CompassAssistant.getInstance(context).addCompassListener(this);

        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        setAlpha(1f);

        VectorDrawable drawable = (VectorDrawable) AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.compassvector);
        compass = getBitmap(drawable);

        drawable = (VectorDrawable) AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.compassmarker);
        compassMarker = getBitmap(drawable);

        scale = (float) compass.getWidth() / 720;


        //todo : everything after this is for testing purposes, remove later
        curLoc = new Location("dummyprovider");
        //pub
//        curLoc.setLatitude(47.49218543922342);
//        curLoc.setLongitude(-117.5838589668274);
        //CSE
        curLoc.setLatitude(47.4899634586667);
        curLoc.setLongitude(-117.58538246154787);
        //fountain
//        curLoc.setLatitude(47.49133725545527);
//        curLoc.setLongitude(-117.58288800716402);


        nearby = new ArrayList<>();


        nearby.add(new Location("dummyProvider"){{
            //ewu fountain
            setLatitude(47.49133725545527);
            setLongitude(-117.58288800716402);
        }});
        nearby.add(new Location("dummyProvider"){{
            //pub
        setLatitude(47.49218543922342);
        setLongitude(-117.5838589668274);
        }});

    }

    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public void toggleTimer(){
//        if(!isRunning){
//            assistant.onStart();
//        }
//        else {
//            assistant.onStop();
//        }
        isRunning = !isRunning;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p.setColor(Color.parseColor("fuchsia"));
        p.setStyle(Paint.Style.FILL);

        float sx = (float) getWidth() / 10000;
        float sy = (float) getHeight() / 10000;
        canvas.scale(sx,sy);

        rect.set(500,500,9500,1000);
        calcCompass(canvas);
        canvas.drawBitmap(compass, curCompass, rect, null);


        for(Location l : nearby){
            drawNearbyRect(l, canvas);
//            canvas.drawRect(rect, p);
//            canvas.drawBitmap(compassMarker, null, rect, p);

            //Log.d("rect", "Rect location is " + rect);
        }
    }

    private void calcCompass(Canvas canvas){
        int height = compass.getHeight();
        int width = compass.getWidth();
        int offset = (int) (fov/2 * scale);
        int mid = width / 2 +  (int) (heading * scale);

        //Log.d("CamOver", "Width : " + width + " Height : " + height);

        curCompass.set(mid - offset,0,mid + offset, height);
    }

    private void drawNearbyRect(Location destLoc, Canvas canvas){
        double headingTo = curLoc.bearingTo(destLoc);
        double relativeHeading = (headingTo - heading);
        //this next bit just takes us from [0,360] to [-180,180]
        relativeHeading = (relativeHeading + 180) % 360 - 180;

        double dist = curLoc.distanceTo(destLoc);

        //if our heading is within our FoV
        if(relativeHeading >= -(double)fov/2 && relativeHeading <= (double)fov/2 && dist <= drawDistance){
            int newScale = 9000 / fov;
            int center = (int)(5000 + relativeHeading * newScale);

            rect.set(center - 250, 250 , center + 250, 750);

            int alpha =(int)( (1 - dist / drawDistance) * 255);
            p.setAlpha(alpha);

            canvas.drawBitmap(compassMarker, null, rect, p);
        }

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

        invalidate();
    }

    @Override
    public void onCompassAccuracyChange(int compassStatus) {}
}
