package com.senior.arexplorer.Utils.PoI;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.senior.arexplorer.Utils.WebRequester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

class OneBusAwayPoi extends PoI implements Serializable, Response.ErrorListener, Response.Listener<String> {
    private final String elevationAPIurl = "https://maps.googleapis.com/maps/api/elevation/json";



    public OneBusAwayPoi(JSONObject poi) {
        super();
        String direction = " ",name,toTrim,description="";
        Double lat,lon;

        try {
            direction += poi.getString("direction");
            name = poi.getString("name");
            name += direction;
            setName(name);
            setLatitude(poi.getDouble("lat"));
            setLongitude(poi.getDouble("lon"));
            JSONArray r = poi.getJSONArray("routeIds");

            Log.v("OneBusAway routes","r.length() "+r.length());

            for(int i = 0; i < r.length(); i++){
                toTrim = r.getString(i);
                toTrim.replaceAll("\"|[|]|,","");
                toTrim.replaceAll("_"," ");
                description += toTrim+", ";
            }
            int j = description.lastIndexOf(",");
            description = description.substring(0,j);

            setDescription(description);

            Log.v("OneBusAwayPoI","added routes "+description+" to "+name);

            this.fetchElevation();

        } catch (JSONException e) {
            Log.e("JSONexception",""+e.toString());
            e.printStackTrace();
        }
    }


    private void fetchElevation() {
        //TODO: google elevation API call
        String request = String.format("%s?key=%s&locations=%s,%s", elevationAPIurl, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", this.getLatitude(),this.getLongitude());
        StringRequest stringRequest = new StringRequest(request, this, this);
        WebRequester.getInstance().getRequestQueue().add(stringRequest);
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("OneBusAwayPoI","No response from Google Elevation API");
        Log.e("OneBusAwayPoI", error.toString());
    }

    @Override
    public void onResponse(String response) {
        Log.d("OneBusAwayPoI", "Response received from Google Elevation API");
        Log.v("OneBusAwayPoI", response);

        JSONObject elevationResp = null;
        JSONArray results = null;
        try {
            elevationResp = new JSONObject(response);

            results = elevationResp.getJSONArray("results");
            this.setElevation(results.getJSONObject(0).getDouble("elevation"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

}
