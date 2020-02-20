package com.senior.arexplorer.Utils.Backend.Here;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.senior.arexplorer.Utils.WebRequester;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class Here implements LocationListener, Response.ErrorListener, Response.Listener<String>, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    public final String elevationAPIurl = "https://maps.googleapis.com/maps/api/elevation/json";
    public final int smoothNumLocations = 5;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final long LOCATION_UPDATE_INTERVAL = 5000;
    private static final long LOCATION_FASTEST_INTERVAL = 5000;

    private static Here instance = null;
    private static Context applicationContext;

    private Collection<HereListener> callbacks;
    private Queue<Location> prevLocations;
    private Location currentLocation;
    private GoogleApiClient googleApiClient;
    private boolean isReady;
    private double lastElevation = 0;

    private Here() {
        Log.v("Here", "Here is instantiated.");
        this.currentLocation = new Location("dummy");
        this.currentLocation.setLatitude(0);
        this.currentLocation.setLongitude(0);

        this.callbacks = new ArrayList<HereListener>();
        this.prevLocations = new LinkedList<Location>();
        this.isReady = false;

        if (!checkPlayServices()) {
            Log.e("Here ","You need to install Google Play Services to use the App properly");
        }

        this.googleApiClient = new GoogleApiClient.Builder(applicationContext).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        if (this.googleApiClient != null) {
            this.googleApiClient.connect();
        }
        else {
            Log.e("Here", "googleApiClient is null!");
        }
    }

    public static Here getInstance() {
        if (Here.applicationContext == null) {
            Log.e("Here", "Attempted to instantianate without initializing!");
            return null;
        }
        if (Here.instance == null) Here.getInstanceSynced();
        return Here.instance;
    }

    private static synchronized Here getInstanceSynced() {
        if (Here.applicationContext == null) {
            Log.e("Here", "Attempted to instantionate without initalizing!");
            return null;
        }
        if (Here.instance == null) Here.instance = new Here();
        return Here.instance;
    }

    public static synchronized void init(Context context) {
        Log.d("Here", "here is initialized.");
        if (Here.applicationContext == null) {
            Here.applicationContext = context.getApplicationContext();
        }
        else {
            if (Here.applicationContext == context.getApplicationContext()) {
                Log.d("Here", "Here initalized twice, same context proceding");
            }
            else {
                Log.e("Here", "Attempted to initialize Here twice!");
                Log.e("Here", new Exception("Stack trace").getStackTrace().toString());
                throw new Error("Shit and die!");
            }
        }
    }

    public Location getLocation() {
        return this.currentLocation;
    }

    public double getLatitude() {
        return this.currentLocation.getLatitude();
    }
    public double getLongitude() {
        return this.currentLocation.getLongitude();
    }

    public LatLng getLatLng() {
        return new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude());
    }

    public void setElevation(Double elevation) {
        this.currentLocation.setAltitude(elevation);
    }

    public double getElevation() {
        double tempElevation = this.currentLocation.getAltitude();
        if(tempElevation != 0) lastElevation = tempElevation;
        return lastElevation;
    }

    public boolean isReady() {
        return this.isReady;
    }
    public void cleanUp() {
        Here.instance = null;
    }

    public void addListener(HereListener listener) {
        this.callbacks.add(listener);
    }

    public void removeListener(HereListener listener) {
        this.callbacks.remove(listener);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.prevLocations.add(location);
            while (this.prevLocations.size() > this.smoothNumLocations) {
                this.prevLocations.remove();
            }

            Location newLocation = new Location("dummy");
            Double lat = 0.0;
            Double lon = 0.0;
            for (Location loc : prevLocations) {
                lat += loc.getLatitude();
                lon += loc.getLongitude();
            }
            lat /= prevLocations.size();
            lon /= prevLocations.size();

            newLocation.setLatitude(lat);
            newLocation.setLongitude(lon);

            synchronized (this.currentLocation) {
                this.currentLocation = newLocation;
            }

            for (HereListener listener: callbacks) {
                listener.onLocationChanged(this.currentLocation);
            }

            String request = String.format("%s?key=%s&locations=%s,%s", elevationAPIurl, "AIzaSyCh8fjtEu9nC2j9Khxv6CDbAtlll2Dd-w4", location.getLatitude(), location.getLongitude());
            StringRequest stringRequest = new StringRequest(request, this, this);
            WebRequester.getInstance().getRequestQueue().add(stringRequest);
            this.isReady = true;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("Here","No response from Google Elevation API");
        Log.e("Here",error.toString());
    }

    @Override
    public void onResponse(String response) {
        Log.v("Here", "Response recieved from Google Elevation API");
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

    @NonNull
    @Override
    public String toString() {
        String result = "Here: "+super.toString()+"\n";
        result += "currentLocation null?\t\t"+(currentLocation==null);
        result += "\n# of callbacks listening:\t\t"+callbacks.size()+"\n";
        return result;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Here", "Connection to Google Location Services connected!");
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        Log.d("Here","current location is " + location);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, Here.getInstance(), null);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("googleApiClient", "Connection to Google Location Services suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("googleApiClient", "Connection to Google Location Services failed!");
    }

    //google location services
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this.applicationContext);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                //TODO: print useful debugging info
                //apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }
}
