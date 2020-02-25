package com.senior.arexplorer.Utils.Backend.OneBusAwayPoI;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.PopupBox;
import com.senior.arexplorer.Utils.WebRequester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

class OneBusAwayPoi extends PoI implements Serializable, Response.ErrorListener, Response.Listener<String> {
    private final String elevationAPIurl = "https://maps.googleapis.com/maps/api/elevation/json";
    private final String arvDepURL = "http://52.88.188.196:8080/api/api/where/arrivals-and-departures-for-stop/%s.json?key=TEST";
    private String id = "",longDescription="";
    private StopListener stopListener = new StopListener();



    public OneBusAwayPoi(JSONObject poi) {
        super();
        String direction = " ",name,toTrim,description="";
        Double lat,lon;

        iconURL = "https://mayorguthrie.files.wordpress.com/2015/11/img_4464.png";

        try {
            direction += poi.getString("direction");
            name = poi.getString("name");
            name += direction;
            id = poi.getString("id");

            setName(name);
            setLatitude(poi.getDouble("lat"));
            setLongitude(poi.getDouble("lon"));
            JSONArray r = poi.getJSONArray("routeIds");

            Log.v("OneBusAway routes","r.length() "+r.length()+" r = "+r.toString());

            for(int i = 0; i < r.length(); i++){
                toTrim = r.getString(i);
                description += toTrim+", ";
            }
            int j = description.lastIndexOf(",");
            description = description.substring(0,j);

            setDescription(description);

            Log.v("OneBusAwayPoI","added routes "+description+" to "+name);

            this.fetchElevation();
            this.fetchToFrom();

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

    private void fetchToFrom(){
        String request = String.format(arvDepURL,id);
        StringRequest sr = new StringRequest(request,stopListener,stopListener);
        WebRequester.getInstance().getRequestQueue().add(sr);
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

    @Override
    public boolean onShortTouch(Context context) {
        String toastText;
        if(!(Here.getInstance().isReady()))
            toastText = "Cannot get current location\nAs such cannot display distance to " + getName();
        else{
            toastText = toShortString()+"\nRoutes: "+getDescription();
        }
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onLongTouch(Context context) {
        fetchToFrom();
        PopupBox popup = new PopupBox(context, getName());
        popup.setView(getDetailsView(context));
        popup.show();
        return true;
    }

    @Override
    public View getDetailsView(Context context){
        TextView retView = new TextView(context);
        retView.setPadding(10,5,10,5);
        retView.setGravity(Gravity.CENTER);
        retView.setText(longDescription);
        retView.setTextSize(18);
        return retView;
    }



    private class StopListener implements Response.ErrorListener, Response.Listener<String> {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.v("OneBusAway arv/dep","ERROR getting arrivals and departures for: "+id+"\n"+error);
        }

        @Override
        public void onResponse(String response) {
            Log.v("OneBusAway arv/dep","Response getting arrivals and departures for: "+id+"\n"+response);
            try {
                JSONArray jsonArray;
                JSONObject jsonObject = new JSONObject(response).getJSONObject("data").getJSONObject("entry");
                jsonArray = jsonObject.getJSONArray("arrivalsAndDepartures");
                String results="";
                long dep;

                Log.v("OneBus dep","array size: "+jsonArray.length());

                for(int i = 0; i < jsonArray.length(); i++){
                    results += jsonArray.getJSONObject(i).getString("routeId")+" "+jsonArray.getJSONObject(i).getString("tripHeadsign");
                    //todo if predicted==0 then getLong("scheduledArrivalTime")
                    dep = jsonArray.getJSONObject(i).getLong("predictedDepartureTime");
                    if(dep==0){
                        dep = jsonArray.getJSONObject(i).getLong("scheduledArrivalTime");
                    }

                    results += "\n\tNext departure in: " + formatTime(dep)+"\n";
                }

                longDescription = results;
                Log.v("OneBus arvs",longDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String formatTime(long durationInMillis){
            long now = System.currentTimeMillis();
            durationInMillis = durationInMillis - now;
            Log.v("OneBus formatTime","durationInMillis "+durationInMillis);
            long second = (durationInMillis / 1000) % 60;
            long minute = (durationInMillis / (1000 * 60)) % 60;
            long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

            if(hour!=0)minute+=(hour*60);

            return String.format("%02dm:%02ds", minute, second);
        }
    }
}
