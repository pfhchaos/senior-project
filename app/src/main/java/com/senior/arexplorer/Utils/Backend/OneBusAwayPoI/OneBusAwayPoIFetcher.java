package com.senior.arexplorer.Utils.Backend.OneBusAwayPoI;

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

public class OneBusAwayPoIFetcher extends PoIFetcher implements Response.ErrorListener, Response.Listener<String>, SettingListener {

    private final String stopsUrl = "http://52.88.188.196:8080/api/api/where/stops-for-location.json?key=TEST";
    private final int radius = 500;
    private String lastRequest = "";
    private boolean isReady = false;

    private static OneBusAwayPoIFetcher instance;

    public static OneBusAwayPoIFetcher getInstance(){
        if(instance == null) instance = new OneBusAwayPoIFetcher();
        Log.v("OneBusAway","Instance returned");
        return instance;
    }

    public OneBusAwayPoIFetcher() {
        super();

        Settings.getInstance().addUseOneBusAwayBackendListener(this);
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("OneBusAwayPoIFetcher","No response from OneBusAway. onErrorResponse");
    }

    @Override
    public void onResponse(String response) {
        JSONObject OBAresponse = null;
        String nextPageToken = "";
        JSONArray results = null;

        ArrayList<PoI> newPoIs = new ArrayList<PoI>();

        Log.v("OneBusAway","Response received from OneBusAway");

        try {
            OBAresponse = new JSONObject(response);

            //nextPageToken = OBAresponse.getString("data");
            Log.v("OneBusAway",""+OBAresponse.toString());

            OBAresponse = OBAresponse.getJSONObject("data");
            results = OBAresponse.getJSONArray("list");

            Log.v("OneBusAway","getJSONArray returned: "+results.toString());

            for(int i = 0; i < results.length(); i++){
                JSONObject poi = results.getJSONObject(i);
                newPoIs.add(new OneBusAwayPoi(poi));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        synchronized (this.poIs) {
            this.poIs = newPoIs;
        }
        for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
            handler.placeFetchComplete();
        }

        this.isReady = true;
    }

    @Override
    public Collection<PoI> getPoIs() {
        return this.poIs;
    }

    @Override
    public void fetchData() {
        Location hereNow = Here.getInstance().getLocation();
        if(hereNow == null){
            Log.e("OneBusAwayPoIFetcher","here is null. this should not happen");
            return;
        }
        String request = String.format("%s&lat=%s&lon=%s&radius=%s",stopsUrl,hereNow.getLatitude(),hereNow.getLongitude(),radius);
        Log.v("OneBusAway","Request: "+request);
        StringRequest stringRequest = new StringRequest(request, this, this);
        this.lastRequest = request;
        WebRequester.getInstance().getRequestQueue().add(stringRequest);

    }

    @Override
    public void cleanUp() {
        OneBusAwayPoIFetcher.instance = null;
        Settings.getInstance().removeUseOneBusAwayBackendListener(this);
    }

    @Override
    public boolean isReady() {
        return this.isReady;
    }

    @Override
    public void onSettingChange() {
        Log.d("OneBusAwayFetcher", "onSettingChanged");
        if (!Settings.getInstance().getUseOneBusAwayBackend()) cleanUp();
    }
}
