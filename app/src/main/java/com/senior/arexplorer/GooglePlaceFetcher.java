package com.senior.arexplorer;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Collection;

public class GooglePlaceFetcher implements PlaceFetcher, Response.ErrorListener, Response.Listener<String> {

    CurrentLocation currentLocation;
    private final String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private RequestQueue requestQueue;

    public GooglePlaceFetcher(Activity mActivity, CurrentLocation currentLocation) {
        this.currentLocation = currentLocation;

        requestQueue = Volley.newRequestQueue(mActivity);

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
