package com.senior.arexplorer.AR;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
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
import com.senior.arexplorer.Utils.IconProvider;
import com.senior.arexplorer.Utils.PopupBox;
import com.senior.arexplorer.Utils.Settings;

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

    private Location curLoc;
    private PoI lastTouchedLocation;
    private TreeSet<PoI> nearby;
    private long lastTouchTime;

    public CameraOverlay(Context context){
        super(context);

        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        setAlpha(1f);

        p.setAntiAlias(true);
        p.setFilterBitmap(true);

        Drawable drawable = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.compassvector);
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

        Backend.getInstance().addHandler(() -> nearby.addAll(Backend.getInstance().getPoIs()));
        if(Backend.getInstance().isReady())
            nearby.addAll(Backend.getInstance().getPoIs());
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

        rect.set(500,500,9500,1000);
        drawCompass(canvas);

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
                calcNearbyRect(poi);
                if(poi.compassRender)
                    canvas.drawBitmap(IconProvider.getInstance().getPointy(poi.getIconURL()), null, poi.getCompassRect(), p);
            }
        }
    }

    private void drawCompass(Canvas canvas){
        int height = compass.getHeight();
        int width = compass.getWidth();
        int fov = Settings.getInstance().getCompassFOV();
        int offset = (int) (fov/2 * scale);
        int mid = width / 2 +  (int) (heading * scale);

        curCompass.set(mid - offset,0,mid + offset, height);

        canvas.drawBitmap(compass, curCompass, rect, null);
    }

    private void calcNearbyRect(PoI poi){
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

        //if our heading is within our FoV
        if(relativeHeading >= -(double)fov/2 && relativeHeading <= (double)fov/2 && dist <= drawDistance && dist >= 10){
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
        PoI closest = null;
        TreeSet<PoI> touched = new TreeSet<>();
        for (PoI poi : nearby) {
            if (poi.getCompassRect().contains((int) event.getX(), (int) event.getY()) && poi.compassRender) {
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
                if (closest != null && closest == lastTouchedLocation) {
                    if (System.currentTimeMillis() - lastTouchTime <= 1000)
                        handled = closest.onShortTouch(getContext());
                    else{
                        if(touched.size() == 1)
                            handled = closest.onLongTouch(getContext());
                        else{
                            PopupBox popup = new PopupBox(getContext(), "Which would you like to view?");

                            LinearLayout popView = new LinearLayout(getContext());
                            popView.setOrientation(LinearLayout.VERTICAL);
                            for(PoI poi : touched){
                                TextView poiView = new TextView(getContext());
                                poiView.setPadding(10,5,10,5);
                                poiView.setGravity(Gravity.END);
                                poiView.setText(poi.toShortString());
                                poiView.setTextSize(18);
                                poiView.setOnClickListener( (i) -> {
                                    poi.onLongTouch(getContext());
                                    popup.dismiss();
                                });
                                popView.addView(poiView);
                            }

                            popup.setView(popView);
                            popup.show();
                        }
                    }

                    //Log.d("CamOver", "Bearing to " + curLoc.bearingTo(lastTouchedLocation.getLocation()));
                }
                lastTouchTime = 0;
                lastTouchedLocation = null;
                break;
        }

        return handled;
    }

}
