package com.senior.arexplorer.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
//import android.graphics.Bitmap;
//import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class WebRequester {
    private static WebRequester instance = null;
    private static Context applicationContext;

    private RequestQueue requestQueue = null;
    private ImageLoader mImageLoader;


    private WebRequester() {
        Log.d("WebRequester", "WebRequester is instantiated.");
        this.requestQueue = getRequestQueue();


        mImageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

    }

    public static void init(Context context) {
        Log.d("WebRequester", "WebRequester is initialized.");

        if (WebRequester.applicationContext == null) {
            WebRequester.applicationContext = context.getApplicationContext();
        }
        else {
            if (context.getApplicationContext() == WebRequester.applicationContext) {
                Log.d("WebRequester","Attempted to initialize WebRequester twice! Same context proceeding.");
            }
            else {
                Log.e("WebRequester", "Attempted to initialize WebRequester twice!");
                Log.e("WebRequester", new Exception("Stack trace").getStackTrace().toString());
            }
        }
    }

    public static WebRequester getInstance() {
        if (WebRequester.applicationContext == null) {
            Log.e("WebRequester", "Attempted to instantionate WebRequester without initalizing!");
            return null;
        }
        if (WebRequester.instance == null) WebRequester.getInstanceSynced();
        return WebRequester.instance;
    }

    private static synchronized WebRequester getInstanceSynced() {
        if (WebRequester.applicationContext == null) {
            Log.e("WebRequester", "Attempted to instantionate without initalizing!");
            return null;
        }
        if (WebRequester.instance == null) {
            WebRequester.instance = new WebRequester();
        }
        return WebRequester.instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(WebRequester.applicationContext);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }


    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
