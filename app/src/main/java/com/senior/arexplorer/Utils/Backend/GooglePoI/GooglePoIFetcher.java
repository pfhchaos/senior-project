package com.senior.arexplorer.Utils.Backend.GooglePoI;

import android.location.Location;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.PoIFetcher;
import com.senior.arexplorer.Utils.Backend.PoIFetcherHandler;
import com.senior.arexplorer.Utils.SettingListener;
import com.senior.arexplorer.Utils.Settings;
import com.senior.arexplorer.Utils.WebRequester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class GooglePoIFetcher extends PoIFetcher implements Response.ErrorListener, Response.Listener<String>, SettingListener {
    public final String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    public final int radius = 1000;

    private String lastRequest;
    private boolean isReady;
    private ArrayList<PoI> newPoIs;

    private static PoIFetcher instance;

    private Integer outstandingRequests = 0;

    public static PoIFetcher getInstance() {
        if (instance == null) {
            GooglePoIFetcher.getInstanceSynced();
        }
        return instance;
    }

    private static synchronized void getInstanceSynced() {
        if (instance == null) {
            instance = new GooglePoIFetcher();
        }
        return;
    }

    private GooglePoIFetcher() {
        this.poIFetcherHandlers = new ArrayList<>();

        Settings.getInstance().addUseGoogleBackendListener(this);

        poIs = new ArrayList<>();
        this.isReady = false;
    }

    @Override
    public void fetchData() {
        WebRequester.getInstance().cancelPendingRequests(this);
        Location here = Here.getInstance().getLocation();
        if (here == null) {
            Log.e("GooglePoIFetcher","here is null. this should not happen");
            return; //will try again, don't make null references
        }

        String request = String.format("%s?key=%s&location=%s,%s&radius=%s", url, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", here.getLatitude(),here.getLongitude(), radius);
        StringRequest stringRequest = new StringRequest(request, this, this);
        stringRequest.setTag(this);

        this.lastRequest = request;

        this.newPoIs = new ArrayList<PoI>();

        WebRequester.getInstance().getRequestQueue().add(stringRequest);
        synchronized (this.outstandingRequests) {
            outstandingRequests = 1;
        }
    }


    @Override
    public Collection<PoI> getPoIs() {
        //todo: this is dangerous. clone googlePoIs before returning
        return this.poIs;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        synchronized (this.outstandingRequests) {
            this.outstandingRequests--;
        }
        Log.e("GooglePoiFetcher","No response from google. Fuck you Google!");
    }

    @Override
    public void onResponse(String response) {
        JSONObject googleResp = null;
        String next_page_token = null;
        JSONArray results = null;

        Log.d("GooglePoIFetcher", "Response recieved from Google Places API");
        try {
            googleResp = new JSONObject(response);
            try {
                next_page_token = googleResp.getString("next_page_token");
                Log.d("GooglePoIFetcher", "next_page_token recieved. proceeding to new request.");

                StringRequest stringRequest = new StringRequest(lastRequest + "&pagetoken=" + next_page_token, this, this);
                stringRequest.setTag(this);
                WebRequester.getInstance().getRequestQueue().add(stringRequest);

                synchronized (this.outstandingRequests) {
                    this.outstandingRequests++;
                }
            }
            catch (JSONException ex) {
                Log.d("GooglePoIFetcher", "No next_page_token recieved. request finished.");
            }

            results = googleResp.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject poi = results.getJSONObject(i);

                newPoIs.add(new GooglePoI(poi));
            }

            synchronized (this.outstandingRequests) {
                this.outstandingRequests--;
            }
            if (next_page_token == null) {
                while (this.outstandingRequests > 0) {
                    Thread.sleep(10);
                }
                Log.d("GooglePoIFetcher", "No next_page_token recieved. request finished.");
                synchronized (this.poIs) {
                    this.poIs = newPoIs;
                }
                for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
                    handler.placeFetchComplete();
                }
                this.isReady = true;
            }
        }
        catch (Exception ex) {
            Log.e("GooglePoIFetcher", "Failed to process response from Google!");
            Log.e("GooglePoIFetcher", ex.getStackTrace().toString());
            return;
        }
    }

    public void cleanUp() {
        GooglePoIFetcher.instance = null;
        Settings.getInstance().removeUseGoogleBackendListener(this);
    }

    @Override
    public boolean isReady() {
        return this.isReady;
    }

    @Override
    public void onSettingChange() {
        if (!Settings.getInstance().getUseGoogleBackend()) cleanUp();
    }
}
