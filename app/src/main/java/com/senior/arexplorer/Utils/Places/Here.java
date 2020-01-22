package com.senior.arexplorer.Utils.Places;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.LocationListener;
import com.senior.arexplorer.Utils.WebRequester;

import java.util.ArrayList;
import java.util.Collection;

public class Here implements LocationListener, Response.ErrorListener, Response.Listener<String> {

    public final String elevationAPIurl = "https://maps.googleapis.com/maps/elevation/json";

    private static Here instance = null;
    private static Context applicationContext;

    private Collection<HereListener> callbacks;
    private Location currentLocation;
    private double elevation;

    private Here() {
        Log.d("location manager", "here is instantiated.");
        this.callbacks = new ArrayList<HereListener>();
    }

    public static Here getInstance() {
        if (Here.applicationContext == null) {
            Log.e("Here", "Attempted to instantionate without initalizing!");
            return null;
        }
        if (Here.instance == null) Here.getInstanceSynced();
        return Here.instance;
    }

    private static synchronized Here getInstanceSynced() {
        if (Here.applicationContext == null) {
            Log.e("Here", "Attempted to instantionate without initalizing!");
            return null;
        }
        if (Here.instance == null) Here.instance = new Here();
        return Here.instance;
    }

    public static synchronized void init(Context context) {
        Log.d("location manager", "here is initialized.");
        if (Here.applicationContext == null) {
            Here.applicationContext = context.getApplicationContext();
        }
        else {
            Log.e("Here","Attempted to initialize Here twice!");
        }
    }

    public Location getLocation() {
        return this.currentLocation;
    }

    public double getLatitude() {
        return this.currentLocation.getLatitude();
    }
    public double getLongitude() {
        return this.currentLocation.getLongitude();
    }

    public void cleanUp() {
        Here.instance = null;
    }

    public void addListener(HereListener listener) {
        this.callbacks.add(listener);
    }

    public void removeListener(HereListener listener) {
        this.callbacks.remove(listener);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.currentLocation = location;
            for (HereListener listener: callbacks) {
                listener.onLocationChanged(this.currentLocation);
            }

            //TODO: google elevation API call
            Here here = Here.getInstance();
            String request = String.format("%s?key=%s&locations=%s,%s", elevationAPIurl, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", here.getLatitude(),here.getLongitude());
            StringRequest stringRequest = new StringRequest(request, this, this);
            WebRequester.getInstance().getRequestQueue().add(stringRequest);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
