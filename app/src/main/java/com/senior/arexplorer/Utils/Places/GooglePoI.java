package com.senior.arexplorer.Utils.Places;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.senior.arexplorer.Utils.WebRequester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class GooglePoI extends PoI implements Serializable, Response.ErrorListener, Response.Listener<String>{
    public final String elevationAPIurl = "https://maps.googleapis.com/maps/api/elevation/json";

    public GooglePoI(JSONObject poi) {
        super();
        Log.d("GooglePoI", poi.toString());

        try {
            JSONObject location = poi.getJSONObject("geometry").getJSONObject("location");
            Log.d("GooglePoI", location.toString());
            super.setLatitude(location.getDouble("lat"));
            super.setLongitude(location.getDouble("lng"));
            JSONArray types = poi.getJSONArray("types");

            for (int j = 0; j < types.length(); j++) {
                super.addType(types.getString(j));
            }

            super.setName(poi.getString("name"));
        }
        catch (JSONException ex) {
            Log.e("GooglePoI",ex.toString());
        }

        Log.d("GooglePoI", "created GooglePoI " + this.toString());
        this.fetchElevation();
    }

    private void fetchElevation() {
        //TODO: google elevation API call
        String request = String.format("%s?key=%s&locations=%s,%s", elevationAPIurl, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", this.getLatitude(),this.getLongitude());
        StringRequest stringRequest = new StringRequest(request, this, this);
        WebRequester.getInstance().getRequestQueue().add(stringRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("GooglePoI","No response from Google Elevation API");
        Log.e("GooglePoI", error.toString());
    }

    @Override
    public void onResponse(String response) {
        Log.d("Here", "Response recieved from Google Elevation API");
        Log.v("Here", response);

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
