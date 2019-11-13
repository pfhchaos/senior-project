package com.senior.arexplorer;

import android.app.Activity;

import java.util.Collection;

public interface PlaceFetcher {
    Collection<Place> getPlaces();

    void fetchData(Activity mActivity);
}
