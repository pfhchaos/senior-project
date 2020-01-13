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
import android.widget.Toast;

import com.senior.arexplorer.R;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.Places.HereListener;
import com.senior.arexplorer.Utils.Places.Place;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatDrawableManager;

public class CameraOverlay extends View implements CompassAssistant.CompassAssistantListener, HereListener {
    private boolean isRunning = true;
    Paint p = new Paint();
    Rect rect = new Rect(), curCompass = new Rect();
    Bitmap compass, compassMarker;
    float heading = 0;
    float scale;
    int fov = 180;
    int drawDistance = 1000;
    float sx = (float) getWidth() / 10000;
    float sy = (float) getHeight() / 10000;

    Location curLoc;
    List<Place> nearby;


    public CameraOverlay(Context context){
        super(context);
        init(context);
    }

    public void init(Context context){

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

        nearby = new ArrayList<>();


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

        if(curLoc.getProvider() == "dummyProvider" || curLoc == null){
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
            for (Place poi : nearby) {
                calcNearbyRect(poi);
                canvas.drawBitmap(compassMarker, null, poi.getCompassRect(), p);
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

    private void calcNearbyRect(Place poi){
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

            int alpha =(int)( (1 - dist / drawDistance) * 255);
            p.setAlpha(alpha);

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
//        Stream.of(nearby).filter( (poi) -> ((Place)poi).getCompassRect().contains((int)event.getX(), (int)event.getY()))
//                .sorted((o1,o2) -> (int)(curLoc.distanceTo(((Place)o1).getLocation()) - curLoc.distanceTo(((Place)o2).getLocation())))
//                .findFirst();

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

        if(event.getAction() == MotionEvent.ACTION_DOWN) {

//            Log.d("CamOver", event.getX() + ", " + event.getY());

            Place closest = null;
            float minDist = Integer.MAX_VALUE;
            for (Place poi : nearby) {
//                Log.d("CamOver", poi.getCompassRect().toShortString());
                if (poi.getCompassRect().contains((int) event.getX(), (int) event.getY())) {
                    float curDist = curLoc.distanceTo(poi.getLocation());
                    if (closest == null || curDist < minDist) {
                        closest = poi;
                        minDist = curDist;
//                        Log.d("CamOver", "Setting closest to " + poi.getName());
                    }
                }
            }

            if(closest != null) {
//                Log.d("CamOver", closest + "");

                Toast.makeText(getContext(), closest.getName() + " is " + new DecimalFormat("#.##").format(minDist) + "m away.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
}
