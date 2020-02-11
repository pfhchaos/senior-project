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

import androidx.appcompat.widget.AppCompatDrawableManager;

public class IconProvider {
    private static IconProvider instance;
    private static Context applicationContext;
    private HashMap<String, Bitmap> pointyMap = new HashMap<>();
    private HashMap<String, Bitmap> roundMap = new HashMap<>();

    private IconProvider(){

        Drawable d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.compassmapmarkerbackground);
        pointyMap.put("default", CommonMethods.getBitmapFromDrawable(d));

        d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.armarkerbackground);
        roundMap.put("default", CommonMethods.getBitmapFromDrawable(d));
    }

    public static IconProvider getInstance() {
        if (applicationContext == null) {
            Log.e("IconProvider", "attempted to instantiate Settings without initializing");
            throw new Error("attempted to instantiate Settings without initializing");
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

    public Bitmap getPointy(String url){
        Bitmap retBitmap;

        boolean containsPointyMap = pointyMap.containsKey(url);
        if(containsPointyMap && pointyMap.get(url) != null) {
            return pointyMap.get(url);
        }

        if(!containsPointyMap) {
            loadBitmapFromURL(url);
        }

        retBitmap = roundMap.get("default");

        return retBitmap;
    }

    public Bitmap getRound(String url){
        Bitmap retBitmap;

        boolean containsRoundMap = roundMap.containsKey(url);
        if(containsRoundMap && roundMap.get(url) != null) {
            return roundMap.get(url);
        }

        if(!containsRoundMap) {
            loadBitmapFromURL(url);
        }

        retBitmap = roundMap.get("default");

        return retBitmap;
    }

    private void loadBitmapFromURL(String url){
        pointyMap.put(url, null);
        roundMap.put(url, null);


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

                //pointyMap calculations
                d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.compassmapmarkerbackground);
                bitmap = CommonMethods.getBitmapFromDrawable(d);
                canvas = new Canvas(bitmap);

                offset = canvas.getWidth() / 5;
                iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, (int) (canvas.getHeight() * (2f/3f) - offset));

                if(response.getBitmap() != null)
                    canvas.drawBitmap(response.getBitmap(), null, iconLocation, p);

                pointyMap.put(url, bitmap);


                //roundMap Calculations
                d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.armarkerbackground);
                bitmap = CommonMethods.getBitmapFromDrawable(d);
                canvas = new Canvas(bitmap);

                offset = canvas.getWidth() / 5; //offset 20% in both directions
                iconLocation = new Rect(offset, offset, canvas.getWidth() - offset, canvas.getHeight() - offset);

                if(response.getBitmap() != null)
                    canvas.drawBitmap(response.getBitmap(), null, iconLocation, p);

                roundMap.put(url, bitmap);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("getImage", "No response from ImageLoader!\n" + error);
            }
        });
    }

}
