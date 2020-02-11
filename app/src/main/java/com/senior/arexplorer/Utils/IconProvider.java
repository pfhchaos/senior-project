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

    private IconProvider(){

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
        Log.d("location manager", "here is initialized.");
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
            loadBitmapFromURL(url);
        }

        retBitmap = map.get("default");

        return retBitmap;
    }

    private void loadBitmapFromURL(String url){
        pointyIconMap.put(url, null);
        roundIconMap.put(url, null);


        Log.d("IconProvider", "Retrieving icon for URL " + url);

        WebRequester.getInstance().getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
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
                iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, (int) (canvas.getHeight() * (2f/3f) - offset));

                if(response.getBitmap() != null)
                    canvas.drawBitmap(response.getBitmap(), null, iconLocation, p);

                pointyIconMap.put(url, bitmap.copy(bitmap.getConfig(), false));

                Bitmap temp = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()  * mapIconRatio), (int)(bitmap.getHeight()  * mapIconRatio), true);
                mapIconMap.put(url, temp);


                //roundIconMap Calculations
                d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.armarkerbackground);
                bitmap = CommonMethods.getBitmapFromDrawable(d);
                canvas = new Canvas(bitmap);

                offset = canvas.getWidth() / 5; //offset 20% in both directions
                iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, canvas.getHeight() - offset);

                if(response.getBitmap() != null)
                    canvas.drawBitmap(response.getBitmap(), null, iconLocation, p);

                roundIconMap.put(url, bitmap.copy(bitmap.getConfig(), false));
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("getImage", "No response from ImageLoader!\n" + error);
            }
        });
    }

}
