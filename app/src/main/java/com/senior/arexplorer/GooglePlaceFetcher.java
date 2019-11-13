package com.senior.arexplorer;

import android.app.Activity;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class GooglePlaceFetcher implements PlaceFetcher, Response.ErrorListener, Response.Listener<String> {

    CurrentLocation currentLocation;
    private final String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private RequestQueue requestQueue;
    Collection<Place> places;
    public final int radious = 10;

    public GooglePlaceFetcher(Activity mActivity, CurrentLocation currentLocation) {
        this.currentLocation = currentLocation;

        requestQueue = Volley.newRequestQueue(mActivity);
        places = new ArrayList<>();

        Location here = this.currentLocation.getLocation();
        String request = String.format("%s?key=%s&location=%s,%s&radius=%s", url, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", here.getLatitude(),here.getLongitude(), radious);
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
        String next_page_token = null;
        JSONArray results = null;
        try {
            googleResp = new JSONObject(response);
            next_page_token = googleResp.getString("next_page_token");
            results = googleResp.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                Place place = new Place();

                JSONObject row = results.getJSONObject(i);
                JSONObject location = row.getJSONObject("geometry").getJSONObject("location");
                place.setLatitude(location.getDouble("lat"));
                place.setLongitude(location.getDouble("lng"));
                JSONArray types = row.getJSONArray("types");

                for (int j = 0; j < types.length(); j++) {
                    place.addType(types.getString(j));
                }

                place.setName(row.getString("name"));

                places.add(place);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        System.err.println(results);
    }
}
