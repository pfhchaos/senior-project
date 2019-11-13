package com.senior.arexplorer;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

public class LocationManager {

    private static LocationManager locationManager = null;

    private LocationManager() {}

    public static LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = new LocationManager();
        }
        return locationManager;
    }

    private String getProvider(android.location.LocationManager locMgr, int accuracy, String
            defProvider) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(accuracy);
        // get best provider regardless of whether it is enabled
        String providerName = locMgr.getBestProvider(criteria, false);
        if (providerName == null)
            providerName = defProvider;
        // if neither that nor the default are enabled, prompt user to change settings
        if (!locMgr.isProviderEnabled(providerName)) {
            System.err.println("Location Provider Not Enabled: Goto Settings?");
        }

        return providerName;
    }

    public Location getLocation(Context mContext) {
        android.location.LocationManager locMgr = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        // location = this.getLocation();

        String provider;
        if (location == null) {
            provider = getProvider(locMgr, Criteria.ACCURACY_FINE, locMgr.GPS_PROVIDER);
            try {
                location = locMgr.getLastKnownLocation(provider);
            } catch(SecurityException e) {
                Log.e("Error", "Security Exception: " + e.getMessage());
            }
        }
        if (location == null) {
            provider = getProvider(locMgr, Criteria.ACCURACY_COARSE, locMgr.NETWORK_PROVIDER);
            try {
                location = locMgr.getLastKnownLocation(provider);
            } catch(SecurityException e) {
                Log.e("Error", "Security Exception: " + e.getMessage());
            }
        }
        if (location == null) Toast.makeText(mContext, "Cannot get current location.", Toast.LENGTH_SHORT).show();

        return location;
    }

    double getLatitude(Context mContext) {
        return getLocation(mContext).getLatitude();
    }
    double getLongitude(Context mContext) {
        return getLocation(mContext).getLongitude();
    }

}
