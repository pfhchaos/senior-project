package com.senior.arexplorer.Utils.Places;

import android.app.Activity;

import java.util.Collection;

public interface PoIFetcher {

    Collection<GooglePoI> getGooglePoIs();

    void fetchData(Activity mActivity);
    void addHandler(PoIFetcherHandler handler);
    void removeHandler(PoIFetcherHandler handler);

}
