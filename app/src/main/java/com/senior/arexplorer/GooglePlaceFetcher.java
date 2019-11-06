package com.senior.arexplorer;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Collection;

public class GooglePlaceFetcher implements PlaceFetcher, Response.ErrorListener, Response.Listener<String> {

    Context mContext;
    CurrentLocation currentLocation;
    private final String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private RequestQueue requestQueue;

    public void onStart(Context mContext, CurrentLocation currentLocation) {
        this.mContext = mContext;
        this.currentLocation = currentLocation;
    }

    public void onStop() {
        this.mContext = null;
        this.currentLocation = null;
    }

    public GooglePlaceFetcher(Context mContext, CurrentLocation currentLocation) {

        this.onStart(mContext, currentLocation);

        requestQueue = Volley.newRequestQueue(this.mContext);

        Location here = this.currentLocation.getLocation();
        String request = String.format("%s?key=%s&location=%s,%s&radius=%s", url, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", here.getLatitude(),here.getLongitude(), 1000);
        System.err.println(request);
        StringRequest stringRequest = new StringRequest(request, this, this);

        requestQueue.add(stringRequest);
    }
    @Override
    public Collection<Place> getPlaces() {
        return null;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        System.err.println("No response from google. Fuck you Google!");
    }

    @Override
    public void onResponse(String response) {
        JSONObject googleResp = null;
        try {
            googleResp = new JSONObject(response);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(googleResp);
    }
}
