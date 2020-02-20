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

    public Bitmap getPointyIcon(String url){
        return getIcon(url, pointyIconMap);
    }

    public Bitmap getMapIcon(String url){
        return getIcon(url, mapIconMap);
    }

    public Bitmap getRoundIcon(String url){
        return getIcon(url, roundIconMap);
    }

    private Bitmap getIcon(String url, Map<String, Bitmap> map){
        Bitmap retBitmap;

        boolean mapContains = map.containsKey(url);
        if(mapContains && map.get(url) != null) {
            return map.get(url);
        }

        if(!mapContains) {
            synchronized (this.outstandingRequests) {
                this.outstandingRequests++;
                Log.d("IconProvider", "Retrieving icon for URL " + url);
                Log.d("IconProvider","After increment outstandingRequests is " + this.outstandingRequests);
                loadBitmapFromURL(url);
            }
        }

        retBitmap = map.get("default");

        return retBitmap;
    }

    private void loadBitmapFromURL(String url){
        pointyIconMap.put(url, null);
        roundIconMap.put(url, null);

        WebRequester.getInstance().getImageLoader().get(url, new ImageLoader.ImageListener() {

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Log.d("IconProvider", "onResponse");
                Canvas canvas;
                Bitmap bitmap;
                Drawable d;
                Rect iconLocation;
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setFilterBitmap(true);
                int offset;

                //pointyIconMap calculations
                d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.compassmapmarkerbackground);
                bitmap = CommonMethods.getBitmapFromDrawable(d);
                canvas = new Canvas(bitmap);

                offset = canvas.getWidth() / 5;
                iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, (int) (canvas.getHeight() * (2f / 3f) - offset));

                if (response.getBitmap() != null)
                    canvas.drawBitmap(response.getBitmap(), null, iconLocation, p);

                pointyIconMap.put(url, bitmap.copy(bitmap.getConfig(), false));

                Bitmap temp = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * mapIconRatio), (int) (bitmap.getHeight() * mapIconRatio), true);
                mapIconMap.put(url, temp.copy(temp.getConfig(), false));


                //roundIconMap Calculations
                d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.armarkerbackground);
                bitmap = CommonMethods.getBitmapFromDrawable(d);
                canvas = new Canvas(bitmap);

                offset = canvas.getWidth() / 5; //offset 20% in both directions
                iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, canvas.getHeight() - offset);

                if (response.getBitmap() != null)
                    canvas.drawBitmap(response.getBitmap(), null, iconLocation, p);

                roundIconMap.put(url, bitmap.copy(bitmap.getConfig(), false));

                IconProvider.this.outstandingRequests--;
                Log.d("IconProvider", "After decrement outstandingRequests is " + IconProvider.this.outstandingRequests);
                if (IconProvider.this.outstandingRequests == 0) {
                    notifyListeners();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                    /*
                    Log.d("IconProvider", "onErrorResponse");
                    synchronized (IconProvider.this.outstandingRequests) {
                        IconProvider.this.outstandingRequests--;
                    }
                    Log.d("IconProvider", "outstandingRequests is " + IconProvider.this.outstandingRequests);
                    if (IconProvider.this.outstandingRequests == 0) {
                        notifyListeners();
                    }
                    */
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
