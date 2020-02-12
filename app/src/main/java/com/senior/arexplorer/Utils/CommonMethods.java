package com.senior.arexplorer.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

//If you put something non static in here you're going to Hell.
public class CommonMethods {

    public static int xMody (int x, int y){
        int result = x % y;
        return result < 0 ? result + y : result;
    }

    public static float xMody (float x, int y){
        float result = x % y;
        return result < 0 ? result + y : result;
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
