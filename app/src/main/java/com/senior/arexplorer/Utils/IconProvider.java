package com.senior.arexplorer.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.senior.arexplorer.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.AppCompatDrawableManager;

public class IconProvider {
    private static IconProvider instance;
    private static Context applicationContext;
    private HashMap<String, Bitmap> pointyIconMap = new HashMap<>();
    private HashMap<String, Bitmap> roundIconMap = new HashMap<>();
    private HashMap<String, Bitmap> mapIconMap = new HashMap<>();
    private float mapIconRatio = 1f/ 4f;
    private Integer outstandingRequests;
    private Collection<IconListener> iconListeners;

    private IconProvider(){
        this.outstandingRequests = 0;
        this.iconListeners = new ArrayList<IconListener>();

        Drawable d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.compassmapmarkerbackground);
        Bitmap temp = CommonMethods.getBitmapFromDrawable(d);
        pointyIconMap.put("default", temp);

        temp = Bitmap.createScaledBitmap(temp, (int)(temp.getWidth() * mapIconRatio), (int)(temp.getHeight() * mapIconRatio), true);
        mapIconMap.put("default", temp);

        d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.armarkerbackground);
        roundIconMap.put("default", CommonMethods.getBitmapFromDrawable(d));
    }

    public static IconProvider getInstance() {
        if (applicationContext == null) {
            Log.e("IconProvider", "attempted to instantiate IconProvider without initializing");
            throw new Error("attempted to instantiate IconProvider without initializing");
        }
        if (IconProvider.instance == null) IconProvider.getInstanceSynced();
        return IconProvider.instance;
    }

    private static synchronized void getInstanceSynced() {
        if (IconProvider.instance == null) IconProvider.instance = new IconProvider();
    }

    public static synchronized void init(Context context) {
        Log.d("IconProvider", "here is initialized.");
        if (IconProvider.applicationContext == null) {
            IconProvider.applicationContext = context.getApplicationContext();
        }
        else {
            if (IconProvider.applicationContext == context.getApplicationContext()) {
                Log.d("IconProvider", "IconProvider initialized twice, same context proceeding");
            }
            else {
                Log.e("IconProvider", "Attempted to initialize IconProvider twice!");
                throw new Error("Shit and die!");
            }
        }
    }

    public void addIconListener(IconListener listener) {
        this.iconListeners.add(listener);
    }

    public void removeIconListener(IconListener listener) {
        this.iconListeners.remove(listener);
    }

    public Bitmap getPointyIcon(String key){
        return getIcon(key, pointyIconMap);
    }

    public Bitmap getMapIcon(String key){
        return getIcon(key, mapIconMap);
    }

    public Bitmap getRoundIcon(String key){
        return getIcon(key, roundIconMap);
    }

    private Bitmap getIcon(String key, Map<String, Bitmap> map){
        Bitmap retBitmap;

        boolean mapContains = map.containsKey(key);
        if(mapContains && map.get(key) != null) {
            return map.get(key);
        }

        if(!mapContains) {
            synchronized (this.outstandingRequests) {
                this.outstandingRequests++;
                Log.d("IconProvider", "Retrieving icon for URL " + key);
                Log.d("IconProvider","After increment outstandingRequests is " + this.outstandingRequests);
                loadBitmapFromURL(key);
            }
        }

        retBitmap = map.get("default");

        return retBitmap;
    }

    public void generateIcon(String key, Bitmap bitmapIn){
        Canvas canvas;
        Bitmap tempBitmap;
        Drawable d;
        Rect iconLocation;
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        int offset;

        //pointyIconMap calculations
        d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.compassmapmarkerbackground);
        tempBitmap = CommonMethods.getBitmapFromDrawable(d);
        canvas = new Canvas(tempBitmap);

        offset = canvas.getWidth() / 5;
        iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, (int) (canvas.getHeight() * (2f / 3f) - offset));

        if (bitmapIn != null)
            canvas.drawBitmap(bitmapIn, null, iconLocation, p);

        pointyIconMap.put(key, tempBitmap.copy(tempBitmap.getConfig(), false));

        Bitmap temp = Bitmap.createScaledBitmap(tempBitmap, (int) (tempBitmap.getWidth() * mapIconRatio), (int) (tempBitmap.getHeight() * mapIconRatio), true);
        mapIconMap.put(key, temp.copy(temp.getConfig(), false));


        //roundIconMap Calculations
        d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.armarkerbackground);
        tempBitmap = CommonMethods.getBitmapFromDrawable(d);
        //Log.d("IconProvider", String.format("Circle Bitmap Dimensions : %d x %d", bitmap.getWidth(), bitmap.getHeight()));
        canvas = new Canvas(tempBitmap);

        offset = canvas.getWidth() / 5; //offset 20% in both directions
        iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, canvas.getHeight() - offset);

        if (bitmapIn != null)
            canvas.drawBitmap(bitmapIn, null, iconLocation, p);

        roundIconMap.put(key, tempBitmap.copy(tempBitmap.getConfig(), false));

    }

    public void generateIcon(String key, Drawable d){
        generateIcon(key, CommonMethods.getBitmapFromDrawable(d));
    }

    private void loadBitmapFromURL(String url){
        pointyIconMap.put(url, null);
        roundIconMap.put(url, null);

        WebRequester.getInstance().getImageLoader().get(url, new ImageLoader.ImageListener() {

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Log.d("IconProvider", "onResponse");

                generateIcon(url, response.getBitmap());

                if (isImmediate) {
                    IconProvider.this.outstandingRequests--;
                    Log.d("IconProvider", "After decrement outstandingRequests is " + IconProvider.this.outstandingRequests);
                    if (IconProvider.this.outstandingRequests == 0) {
                        notifyListeners();
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("IconProvider", "onErrorResponse");
                synchronized (IconProvider.this.outstandingRequests) {
                    IconProvider.this.outstandingRequests--;
                }
                Log.d("IconProvider", "outstandingRequests is " + IconProvider.this.outstandingRequests);
                if (IconProvider.this.outstandingRequests == 0) {
                    notifyListeners();
                }
                Log.e("IconProvider", "No response from ImageLoader!\n" + error);
            }
        });
    }

    private void notifyListeners() {
        for (IconListener listener : this.iconListeners) {
            listener.onIconsFetched();
        }
    }

}
