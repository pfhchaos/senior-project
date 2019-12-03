package com.senior.arexplorer.Utils.Places;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

import java.util.ArrayList;
import java.util.Collection;

public class Here implements LocationListener {

    private static Here here = null;
    private Collection<LocationListener> callbacks;

    private Location currentLocation;

    private Here() {
        Log.d("location manager", "here is instanciated");
        this.callbacks = new ArrayList<LocationListener>();
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

    public void addListener(LocationListener listener) {
        this.callbacks.add(listener);
    }

    public void removeListener(LocationListener listener) {
        this.callbacks.remove(listener);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.currentLocation = location;
            for (LocationListener listener: callbacks) {
                listener.onLocationChanged(this.currentLocation);
            }
        }
    }

}
