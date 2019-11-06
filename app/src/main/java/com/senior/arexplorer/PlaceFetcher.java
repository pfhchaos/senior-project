package com.senior.arexplorer;

import android.app.Activity;

import java.util.Collection;

public interface PlaceFetcher {
    public Collection<Place> getPlaces();
    public void onStart(Activity activity, CurrentLocation currentLocation);
    public void onStop();
}
