package com.senior.arexplorer;

import android.app.Activity;
import android.location.Location;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class GooglePlaceFetcher implements PlaceFetcher, Response.ErrorListener, Response.Listener<String> {

    LocationManager locationManager;
    private final String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private RequestQueue requestQueue;
    Collection<Place> places;
    public final int radius = 1000;
    String lastRequest;
    PlaceFetcherHandler placeFetcherHandler = null;

    public GooglePlaceFetcher(Activity mActivity, LocationManager locationManager, PlaceFetcherHandler placeFetcherHandler) {
        this.locationManager = locationManager;
        this.placeFetcherHandler = placeFetcherHandler;

        requestQueue = Volley.newRequestQueue(mActivity);
        places = new ArrayList<>();
    }

    public void fetchData(Activity mActivity) {
        Location here = this.locationManager.getLocation(mActivity.getApplicationContext());
        if (locationManager == null) {
            Toast.makeText(mActivity, "here is null. this should not happen", Toast.LENGTH_SHORT).show();
        }
        String request = String.format("%s?key=%s&location=%s,%s&radius=%s", url, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", here.getLatitude(),here.getLongitude(), radius);
        StringRequest stringRequest = new StringRequest(request, this, this);
        this.lastRequest = request;

        requestQueue.add(stringRequest);
    }

    @Override
    public Collection<Place> getPlaces() {
        //todo: this is dangerous. clone places before returning
        return this.places;
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
            try {
                next_page_token = googleResp.getString("next_page_token");
            }
            catch (JSONException ex) {}
            results = googleResp.getJSONArray("results");
            System.out.println(results.length() + " places fetched");
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

            if (next_page_token != null) {
                StringRequest stringRequest = new StringRequest(lastRequest + "&pagetoken=" + next_page_token, this, this);
                requestQueue.add(stringRequest);
            }
            else {
                this.placeFetcherHandler.placeFetchComplete();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        System.err.println(results);
    }
}
