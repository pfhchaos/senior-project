package com.senior.arexplorer.Utils.Places;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.senior.arexplorer.Utils.WebRequester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class GooglePoIFetcher extends PoIFetcher implements Response.ErrorListener, Response.Listener<String> {
    public final String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    public final int radius = 1000;

    private Here here;
    private String lastRequest;
    private long lastUpdated;

    private static GooglePoIFetcher googlePlaceFetcher;

    public static GooglePoIFetcher getGooglePlaceFetcher(Activity mActivity) {
        if (googlePlaceFetcher == null) {
            googlePlaceFetcher = new GooglePoIFetcher();
        }
        return googlePlaceFetcher;
    }

    private GooglePoIFetcher() {
        this.here = Here.getInstance();
        this.poIFetcherHandlers = new ArrayList<>();

        poIs = new ArrayList<>();
    }

    public void fetchData(Activity mActivity) {
        Location here = this.here.getLocation();
        if (this.here == null) {
            Toast.makeText(mActivity, "here is null. this should not happen", Toast.LENGTH_SHORT).show();
        }
        String request = String.format("%s?key=%s&location=%s,%s&radius=%s", url, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", here.getLatitude(),here.getLongitude(), radius);
        StringRequest stringRequest = new StringRequest(request, this, this);
        this.lastRequest = request;

        WebRequester.getInstance().getRequestQueue().add(stringRequest);
    }

    @Override
    public void addHandler(PoIFetcherHandler handler) {
        this.poIFetcherHandlers.add(handler);
    }

    @Override
    public void removeHandler(PoIFetcherHandler handler) {
        this.poIFetcherHandlers.remove(handler);
    }

    @Override
    public Collection<PoI> getPoIs() {
        //todo: this is dangerous. clone googlePoIs before returning
        return this.poIs;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("GooglePoiFetcher","No response from google. Fuck you Google!");
    }

    @Override
    public void onResponse(String response) {
        JSONObject googleResp = null;
        String next_page_token = null;
        JSONArray results = null;

        Log.d("GooglePoIFetcher", "Response recieved from Google Places API");
        Log.d("GooglePoIFetcher", response);
        try {
            googleResp = new JSONObject(response);
            try {
                next_page_token = googleResp.getString("next_page_token");
            }
            catch (JSONException ex) {
                Log.d("GooglePoIFetcher", "No next_page_token recieved. request finished.");
            }

            results = googleResp.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject poi = results.getJSONObject(i);

                poIs.add(new GooglePoI(poi));
            }

            if (next_page_token != null) {
                StringRequest stringRequest = new StringRequest(lastRequest + "&pagetoken=" + next_page_token, this, this);
                WebRequester.getInstance().getRequestQueue().add(stringRequest);
            }
            else {
                for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
                    this.lastUpdated = System.currentTimeMillis();
                    handler.placeFetchComplete();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        Log.d("GooglePoIFetcher", results.toString());
    }

    public void cleanUp() {
        GooglePoIFetcher.googlePlaceFetcher = null;
    }
}
