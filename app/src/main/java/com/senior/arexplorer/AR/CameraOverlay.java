package com.senior.arexplorer.AR;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;

import com.senior.arexplorer.R;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.Places.HereListener;
import com.senior.arexplorer.Utils.Places.Place;

import java.util.TreeSet;

import androidx.appcompat.widget.AppCompatDrawableManager;

public class CameraOverlay extends View implements CompassAssistant.CompassAssistantListener, HereListener {
    private boolean isRunning = true;
    private Paint p = new Paint();
    private Rect rect = new Rect(), curCompass = new Rect();
    private Bitmap compass, compassMarker;
    private float heading = 0;
    private float scale;
    private int fov = 180;
    private int drawDistance = 1000;
    private float sx = (float) getWidth() / 10000;
    private float sy = (float) getHeight() / 10000;

    private Location curLoc;
    private Place lastTouchedLocation;
    private TreeSet<Place> nearby;
    private long lastTouchTime;

    public CameraOverlay(Context context){
        super(context);
        init();
    }

    public void init(){

        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        setAlpha(1f);

        VectorDrawable drawable = (VectorDrawable) AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.compassvector);
        compass = getBitmap(drawable);

        drawable = (VectorDrawable) AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.compassmarker);
        compassMarker = getBitmap(drawable);

        scale = (float) compass.getWidth() / 720;

        curLoc = new Location("dummyProvider") {{
            //ewu fountain
            setLatitude(47.49133725545527);
            setLongitude(-117.58288800716402);
        }};

        nearby = new TreeSet<>();


        nearby.add(new Place(){{
            //ewu fountain
            setName("Fountain");
            setLatitude(47.49133725545527);
            setLongitude(-117.58288800716402);
        }});
        nearby.add(new Place(){{
            //pub
            setName("PUB");
            setLatitude(47.49218543922342);
            setLongitude(-117.5838589668274);
        }});
        nearby.add(new Place(){{
            //CSE
            setName("CSE");
            setLatitude(47.4899634586667);
            setLongitude(-117.58538246154787);
        }});
        nearby.add(new Place(){{
            //google HQish
            setName("GooglePlex");
            setLatitude(37.4225);
            setLongitude(-122.0845);
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
        isRunning = !isRunning;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        sx = (float) getWidth() / 10000;
        sy = (float) getHeight() / 10000;
        canvas.scale(sx,sy);

        rect.set(500,500,9500,1000);
        drawCompass(canvas);

        if(curLoc.getProvider().equalsIgnoreCase("dummyProvider") || curLoc == null){
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(300);


            p.setColor(Color.parseColor("black"));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawText("CURRENT LOCATION CANNOT BE RETREIVED!", 5000, 5000, p);

            p.setColor(Color.parseColor("red"));
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(7);
            canvas.drawText("CURRENT LOCATION CANNOT BE RETREIVED!", 5000, 5000, p);
        }
        else {
            for (Place poi : nearby.descendingSet()) {
                drawNearbyRect(poi, canvas);
                //Log.d("rect", "Rect location is " + rect);
            }
        }
    }

    private void drawCompass(Canvas canvas){
        int height = compass.getHeight();
        int width = compass.getWidth();
        int offset = (int) (fov/2 * scale);
        int mid = width / 2 +  (int) (heading * scale);

        //Log.d("CamOver", "Width : " + width + " Height : " + height);

        curCompass.set(mid - offset,0,mid + offset, height);

        canvas.drawBitmap(compass, curCompass, rect, null);
    }

    private void drawNearbyRect(Place poi, Canvas canvas){
        Location destLoc = poi.getLocation();
        double headingTo = curLoc.bearingTo(destLoc);
        double relativeHeading = (headingTo - heading);
        //this next bit just takes us from [0,360] to [-180,180]
        relativeHeading = (relativeHeading + 180) % 360 - 180;

        double dist = curLoc.distanceTo(destLoc);

        //if our heading is within our FoV
        if(relativeHeading >= -(double)fov/2 && relativeHeading <= (double)fov/2 && dist <= drawDistance){
            int newScale = 9000 / fov;
            int center = (int)(5000 + relativeHeading * newScale);

            poi.getCompassRect().set(center - 250, 250 , center + 250, 750);

            int alpha =(int)( (1 - dist / drawDistance) * 255) + 50;
            if(alpha > 255) alpha = 255;
            p.setAlpha(alpha);

            canvas.drawBitmap(compassMarker, null, poi.getCompassRect(), p);
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

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) curLoc = location;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float[] mClickCoords = new float[2];
        mClickCoords[0] = event.getX();
        mClickCoords[1] = event.getY();
        Matrix matrix = new Matrix();
        matrix.set(getMatrix());
        matrix.preTranslate(0, 0);
        matrix.preScale(sx, sy, 0, 0);
        matrix.invert(matrix);
        matrix.mapPoints(mClickCoords);
        event.setLocation(mClickCoords[0], mClickCoords[1]);

        boolean handled = false;
        Place closest = null;
        TreeSet<Place> touched = new TreeSet<>();
        for (Place poi : nearby) {
            if (poi.getCompassRect().contains((int) event.getX(), (int) event.getY())) {
                touched.add(poi);
                if(touched.first() == poi)
                    closest = poi;
            }
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                lastTouchedLocation = closest;
                lastTouchTime = System.currentTimeMillis();
                handled = true;
                break;
            case MotionEvent.ACTION_UP :
                if (closest != null && closest == lastTouchedLocation)
                    if (System.currentTimeMillis() - lastTouchTime <= 1000)
                        handled = closest.onShortTouch(getContext());
                    else
                        handled = closest.onLongTouch(getContext());
                lastTouchTime = 0;
                lastTouchedLocation = null;
                break;
        }
        return handled;
    }
}
