package com.senior.arexplorer.AR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Location;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.senior.arexplorer.R;

import com.senior.arexplorer.Utils.Backend.Here.HereListener;
import com.senior.arexplorer.Utils.CommonMethods;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.Backend.Backend;
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.PopupBox;
import com.senior.arexplorer.Utils.Settings;

import java.util.NavigableSet;
import java.util.TreeSet;

import androidx.appcompat.widget.AppCompatDrawableManager;

public class CameraOverlay extends View implements CompassAssistant.CompassAssistantListener, HereListener {
    private Paint p = new Paint();
    private Rect rect = new Rect(), curCompass = new Rect();
    private Bitmap compass;
    private float heading = 0;
    private float scale;
    private float sx = (float) getWidth() / 10000;
    private float sy = (float) getHeight() / 10000;
    private float previousCompassBearing = -1f;
    private float camHorizViewingAngle, camVertViewingAngle;
    private float pitch;
    private Location curLoc;
    private PoI lastTouchedLocation;
    private NavigableSet<PoI> nearby;
    private long lastTouchTime;
    final GestureDetector gestureDetector;

    public CameraOverlay(Context context){
        super(context);

        Here.getInstance().addListener(this);
        CompassAssistant.getInstance().addCompassListener(this);
        CompassAssistant.getInstance().addPitchListener(this);

        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        setAlpha(1f);
        setViewingAngles();
        //Log.d("CameraVA", String.format("Horizontal : %f\nVertical : %f", camHorizViewingAngle, camVertViewingAngle));

        p.setAntiAlias(true);
        p.setFilterBitmap(true);

        @SuppressLint("RestrictedApi") Drawable drawable = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.compassvector);
        compass = getBitmap(drawable);

        scale = (float) compass.getWidth() / 720;

        if(Here.getInstance().isReady()) {
            curLoc = Here.getInstance().getLocation();
        }
        else
            curLoc = new Location("dummyProvider") {{
                setLatitude(0);
                setLongitude(-0);
            }};

        nearby = new TreeSet<>();


        Backend.getInstance().addHandler(() -> {
            CommonMethods.getActivity(getContext()).runOnUiThread(() -> {
                nearby.clear();
                nearby.addAll(Backend.getInstance().getPoIs());
                });
            });
        if(Backend.getInstance().isReady())
            nearby.addAll(Backend.getInstance().getPoIs());

        gestureDetector = new GestureDetector(getContext(), new CameraOverlayGestureListener());
    }

    private static Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        sx = (float) getWidth() / 10000;
        sy = (float) getHeight() / 10000;
        canvas.scale(sx,sy);

        if(curLoc.getProvider().equalsIgnoreCase("dummyProvider") || curLoc == null){
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(300);


            p.setColor(Color.parseColor("black"));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(10);
            canvas.drawText("CURRENT LOCATION CANNOT BE RETRIEVED!", 5000, 5000, p);

            p.setColor(Color.parseColor("red"));
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(7);
            canvas.drawText("CURRENT LOCATION CANNOT BE RETRIEVED!", 5000, 5000, p);
        }
        else {
            for (PoI poi : nearby.descendingSet()) {
                calcARRect(poi);
                if (poi.arMarkerRender) {
                    canvas.drawBitmap(poi.getRoundIcon(), null, poi.getARRect(), p);
                    //Log.d("CamOver", "Rendering ARMarker at " + poi.getARRect());
                }
            }

            calcCompass();
            canvas.drawBitmap(compass, curCompass, rect, null);

            for (PoI poi : nearby.descendingSet()) {
                calcCompassRect(poi);
                if(poi.compassRender)
                    canvas.drawBitmap(poi.getPointyIcon(), null, poi.getCompassRect(), p);
            }
        }
    }

    private void calcCompass(){
        int height = compass.getHeight();
        int width = compass.getWidth();
        int fov = Settings.getInstance().getCompassFOV();
        int offset = (int) (fov/2 * scale);
        int mid = width / 2 +  (int) (heading * scale);

        curCompass.set(mid - offset,0,mid + offset, height);
        rect.set(500,500,9500,1000);
    }

    private void calcCompassRect(PoI poi){
        int markerHeight = 700;
        float markerRatio = 1f; //this is unused as of now but left in in case we need it
        float markerWidth = (float)(markerHeight) * markerRatio;
        int halfMarkerWidth = Math.round(markerWidth / 2f);
        int offsetFromTop = 50;

        int fov = Settings.getInstance().getCompassFOV();
        int drawDistance = Settings.getInstance().getDrawDistance();

        Location destLoc = poi.getLocation();
        float relativeHeading = curLoc.bearingTo(destLoc) - heading;
        //this next bit just takes us from [0,360] to [-180,180]
        relativeHeading = CommonMethods.xMody((relativeHeading + 180), 360) - 180;

        double dist = poi.getDistanceTo();

        boolean shouldRender;
        shouldRender = dist <= drawDistance && dist >= 5;
        shouldRender &= relativeHeading >= -(double) fov / 2;
        shouldRender &= relativeHeading <= (double) fov / 2;
        //if our heading is within our FoV
        if(shouldRender){
            //Why is this magic number here? Why 9000? Was I drunk when I did this?
            int newScale = 9000 / fov; //I think 9000 equates to 90% of the screen, AKA 5% margin on either side
            int center = (int)(5000 + relativeHeading * newScale); //which would make this at the 50% point on the screen + our offset

            poi.getCompassRect().set(center - halfMarkerWidth, offsetFromTop, center + halfMarkerWidth, markerHeight + offsetFromTop);


            //Log.d("CompassMarker", poi.getCompassRect().toString());

            int alpha =(int)( (1 - dist / drawDistance) * 255) + 50;
            if(alpha > 255) alpha = 255;
            p.setAlpha(alpha);

            poi.compassRender = true;
        }
        else
            poi.compassRender = false;
    }

    public void calcARRect(PoI poi){
        int baseMarkerLength = 1500; // 2500 is 25% of the screen, so at MIN_DIST the marker will take up a quarter of your screen
        int minRenderDistance = 5, maxRenderDistance = 100; //in meters, a city block is ABOUT 100m.


        double dist = poi.getDistanceTo();

        float relativeHorizHeading = curLoc.bearingTo(poi.getLocation()) - heading;
        relativeHorizHeading = CommonMethods.xMody((relativeHorizHeading + 180), 360) - 180;

        boolean useElevation = Settings.getInstance().getUseElevation();
        int relativeElevation = (useElevation) ? (int)(curLoc.getAltitude() - poi.getElevation()) : 0;
        float relativeVertHeading = (float) Math.toDegrees(Math.atan(relativeElevation / dist)) + pitch;

        int offScreen = 10; //variable for number of degrees offscreen to continue rendering (for smooth disappearance)
        boolean shouldRender;
        shouldRender = dist <= maxRenderDistance && dist >= minRenderDistance;
        shouldRender &= relativeHorizHeading >= -(double) camHorizViewingAngle / 2 - offScreen;
        shouldRender &= relativeHorizHeading <= (double) camHorizViewingAngle / 2 + offScreen;
        shouldRender &= relativeVertHeading >= -(double) camVertViewingAngle / 2 - offScreen;
        shouldRender &= relativeVertHeading <= (double) camVertViewingAngle / 2 + offScreen;

        if(shouldRender) {
            float newHorizScale = 10000 / camHorizViewingAngle;
            float newVertScale = 10000 / camVertViewingAngle;
            int centerHoriz = (int) (5000 + relativeHorizHeading * newHorizScale);
            int centerVert = (int) (5000 + relativeVertHeading * newVertScale);

            float scalingFactor = 1 - ((float)(dist - minRenderDistance) / (float)(maxRenderDistance - minRenderDistance));
            if(scalingFactor < .1) {
                float remainingDist = (float) (dist - maxRenderDistance * .9);
                scalingFactor = (float) (maxRenderDistance - minRenderDistance) / 10;
                float scaledRemainingDist = remainingDist / scalingFactor;
                p.setAlpha((int)(scaledRemainingDist * 255));
                scalingFactor = .1f;
            }
            else
                p.setAlpha(255);
            int scaledMarkerLength = (int) (baseMarkerLength * scalingFactor / 2);

            poi.getARRect().set(centerHoriz - scaledMarkerLength, centerVert - scaledMarkerLength,
                    centerHoriz + scaledMarkerLength, centerVert + scaledMarkerLength);

            poi.arMarkerRender = true;

        }
        else
            poi.arMarkerRender = false;
    }

    public void setViewingAngles(){
        float tempHoriz, tempVert;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("cameraFoV", Context.MODE_PRIVATE);
        tempHoriz = sharedPreferences.getFloat("horiz", 0);
        tempVert = sharedPreferences.getFloat("vert", 0);
        if(tempHoriz == 0 || tempVert == 0){
            Log.d("CamOver", "Camera Viewing angles unknown, loading now!");

            Camera c = Camera.open();
            if(c != null) {
                Camera.Parameters params = c.getParameters();
                tempHoriz = params.getHorizontalViewAngle();
                tempVert = params.getVerticalViewAngle();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("horiz", tempHoriz);
            editor.putFloat("vert", tempVert);
            editor.apply();
        }
        camVertViewingAngle = tempVert;
        camHorizViewingAngle = tempHoriz;
    }

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
    public void onPitchChanged(float pitch){
        this.pitch = pitch * -1;
        //Log.d("PitchTest", "Pitch : " + userPitch);
    }

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

        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class CameraOverlayGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public void onLongPress(MotionEvent e) {
            Log.d("CamOver", "Long Touch detected!");
            PoI closest = null;
            TreeSet<PoI> touched = new TreeSet<>();
            for (PoI poi : nearby) {
                if (poi.wasTouched(e)) {
                    touched.add(poi);
                    if(touched.first() == poi)
                        closest = poi;
                }
            }
            if(closest != null) {
                if (touched.size() == 1)
                    closest.onLongTouch(getContext());
                else {
                    PopupBox popup = new PopupBox(getContext(), "Which would you like to view?");

                    LinearLayout popView = new LinearLayout(getContext());
                    popView.setOrientation(LinearLayout.VERTICAL);
                    for (PoI poi : touched) {
                        TextView poiView = new TextView(getContext());
                        poiView.setPadding(10, 5, 10, 5);
                        poiView.setGravity(Gravity.END);
                        poiView.setText(poi.toShortString());
                        poiView.setTextSize(18);
                        poiView.setOnClickListener((i) -> {
                            poi.onLongTouch(getContext());
                            popup.dismiss();
                        });
                        popView.addView(poiView);
                    }

                    popup.setView(popView);
                    popup.show();
                }
            }

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e){
            Log.d("CamOver", "Tap detected!");
            PoI closest = null;
            TreeSet<PoI> touched = new TreeSet<>();
            for (PoI poi : nearby) {
                if (poi.wasTouched(e)) {
                    touched.add(poi);
                    if(touched.first() == poi)
                        closest = poi;
                }
            }
            if(closest != null) {
                return closest.onShortTouch(getContext());
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e){
            Log.d("CamOver", "Down detected!");
            return true;
        }
    }
}
