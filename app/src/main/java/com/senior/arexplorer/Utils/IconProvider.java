package com.senior.arexplorer.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.senior.arexplorer.R;

import androidx.appcompat.widget.AppCompatDrawableManager;

public class IconProvider {
    private static IconProvider instance;
    private static Context applicationContext;

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

    public Bitmap getPointy(){
        Drawable d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.compassmapmarkerbackground);
        return CommonMethods.getBitmapFromDrawable(d);
    }

    public Bitmap getRound(){
        Drawable d = AppCompatDrawableManager.get().getDrawable(applicationContext, R.drawable.armarkerbackground);
        return CommonMethods.getBitmapFromDrawable(d);
    }

}
