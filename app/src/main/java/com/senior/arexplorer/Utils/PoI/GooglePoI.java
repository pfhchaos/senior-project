package com.senior.arexplorer.Utils.PoI;

import android.content.Context;
import android.telecom.Call;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;
import com.senior.arexplorer.Utils.CommonMethods;
import com.senior.arexplorer.Utils.PopupBox;
import com.senior.arexplorer.Utils.WebRequester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.arch.core.util.Function;

public class GooglePoI extends PoI implements Serializable, Response.ErrorListener, Response.Listener<String>{
    private final String elevationAPIurl = "https://maps.googleapis.com/maps/api/elevation/json";
    private final String placeDetailsAPIurl = "https://maps.googleapis.com/maps/api/place/details/json";
    private final String KEY = "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4";
    private String placeID;
    private JSONObject details = null;

    public GooglePoI(JSONObject poi) {
        super();
        Log.d("GooglePoI", poi.toString());

        try {
            JSONObject location = poi.getJSONObject("geometry").getJSONObject("location");
            Log.d("GooglePoI", location.toString());
            super.setLatitude(location.getDouble("lat"));
            super.setLongitude(location.getDouble("lng"));
            JSONArray types = poi.getJSONArray("types");
            this.placeID = poi.getString("place_id");
            Log.d("GooglePoI", "PlaceID : " + placeID);

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
        String request = String.format("%s?key=%s&locations=%s,%s", elevationAPIurl, KEY, this.getLatitude(),this.getLongitude());
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

        JSONObject elevationResp;
        JSONArray results;
        try {
            elevationResp = new JSONObject(response);

            results = elevationResp.getJSONArray("results");
            this.setElevation(results.getJSONObject(0).getDouble("elevation"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onLongTouch(Context context){
        if(details == null){
            String request = String.format("%s?key=%s&placeid=%s", placeDetailsAPIurl, KEY, this.placeID);
            Log.d("PlacesRequest", "Sending following getRequest : " + request);
            StringRequest stringRequest = new StringRequest(request,
                    (response) -> {
                        JSONObject detailsResp;
                        try {
                            detailsResp = new JSONObject(response);
                            details = detailsResp.getJSONObject("result");
                            Log.d("PlacesRequest", details.toString());

                            PopupBox popup = new PopupBox(context, getName());
                            popup.setView(getDetailsView(context));
                            popup.show();

                        } catch (JSONException ex) {
                            Log.d("PlacesRequest", ex.toString());
                        }
                    },
                    (error) -> Log.e("GooglePoI", "No response from Google Place Detail API!\n" + error));
            WebRequester.getInstance().getRequestQueue().add(stringRequest);

            Toast.makeText(context, "Details are not yet available, fetching now...", Toast.LENGTH_LONG).show();
        }
        else{
            PopupBox popup = new PopupBox(context, getName());
            popup.setView(getDetailsView(context));
            popup.show();
        }
        return true;
    }

    @Override
    View getDetailsView(Context context){

        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        Function<String, TextView> getTextView = (stringIn) -> {
              TextView textView = new TextView(context);
              textView.setLayoutParams(params);
              textView.setPadding(10,5,10,5);
              textView.setGravity(Gravity.CENTER);
              textView.setTextSize(18);
              textView.setText(stringIn);
          return textView;
        };

        LinearLayout retView = new LinearLayout(context);
        retView.setOrientation(LinearLayout.VERTICAL);
        retView.setLayoutParams(params);
        retView.setGravity(Gravity.CENTER_VERTICAL);

        try{
            TextView tempView;
            tempView = getTextView.apply(details.getString("formatted_phone_number"));
            retView.addView(tempView);

            tempView = getTextView.apply(details.getString("formatted_address"));
            retView.addView(tempView);

            retView.addView(getTextView.apply(new DecimalFormat("#.00").format(getDistanceTo()) + "m away."));

            String openNow = details.getJSONObject("opening_hours").getString("open_now");
            openNow = Boolean.parseBoolean(openNow) ? "Currently Open" : "Currently Closed";
            retView.addView(getTextView.apply(openNow));

            /*
             * AngryRant.start();
             * FOR SOME GODAWFUL REASON THERES INCON-FUCKING-SISTENCIES IN THE WAY JAVA HANDLES DAYS OF THE WEEK AND THE WAY GOOGLE DOES
             * JAVA DECIDED THAT THE WEEK STARTS ON SUNDAY, THATS FINE BUT FOR SOME FUCKING REASON ITS NOT FUCKING ZERO INDEXED. SUNDAY IS 1!
             * MEANWHILE GOOGLE IS AT LEAST CORRECTLY ZERO INDEXED BUT THEY HAVE THE BRIGHT IDEA TO START THE WEEK ON MONDAY BECAUSE FUCK CONSISTENCY
             * THIS IS MY STUPID FUCKING SOLUTION TO GET THIS SHIT TO LINE UP CORRECTLY, MINUS 2 MOD 7. BUT WAIT THERES MORE!
             * JAVA DOESNT CORRECTLY DEFINE THE MODULUS FUNCTION, THEY DONT HANDLE NEGATIVES AND JUST DO INTEGER DIVISION
             * SO THAT MEANS THAT I HAVE TO HAVE A CUSTOM DEFINED FUCKING MOD FUNCTION
             * AngryRant.end();
             */
            int dayOfWeek = CommonMethods.xMody(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2, 7);
            JSONArray hoursArray = details.getJSONObject("opening_hours").getJSONArray("weekday_text");
            retView.addView(getTextView.apply(hoursArray.getString(dayOfWeek)));

            tempView = getTextView.apply(details.getString("website"));
            retView.addView(tempView);

            tempView = getTextView.apply(details.getString("url"));
            tempView.setTextSize(1);
            tempView.setOnClickListener((i) ->
                Toast.makeText(context, "I'm contractually obligated to include this by google, fuck off.", Toast.LENGTH_LONG).show());
            retView.addView(tempView);

        } catch(JSONException ex){
            Log.d("GooglePoI", ex.toString());
        }

        return retView;
    }
}
