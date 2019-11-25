package com.senior.arexplorer;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

public class Here implements LocationListener {

    private static Here here = null;

    private Location currentLocation;

    private Here() {
        Log.d("location manager", "here is instanciated");
    }

    public static Here getHere() {
        if (Here.here == null) Here.here = new Here();
        return Here.here;
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

    public void cleanUp() {
        Here.here = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) this.currentLocation = location;
    }

}
