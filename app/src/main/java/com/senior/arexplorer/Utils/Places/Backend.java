package com.senior.arexplorer.Utils.Places;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class Backend implements HereListener{
    private static Backend instance = null;
    private Location lastFetched;

    private Collection<PoIFetcher> sources;

    public static Backend getInstance() {
        if (Backend.instance == null) Backend.getInstanceSynced();
        return Backend.instance;
    }

    private static synchronized Backend getInstanceSynced() {
        if (Backend.instance == null) Backend.instance = new Backend();
        return Backend.instance;
    }

    private Backend() {
        this.sources = new ArrayList<PoIFetcher>();

        this.sources.add(GooglePoIFetcher.getInstance());
    }

    public Collection<PoI> getPoIs() {
        Collection<PoI> ret = new ArrayList<PoI>();
        for (PoIFetcher source: sources) {
            ret.addAll(source.getPoIs());
        }
        return ret;
    }

    private void fetchData() {
        this.lastFetched = Here.getInstance().getLocation();
        for (PoIFetcher source: sources) {
            source.fetchData();
        }
    }

    public void addHandler(PoIFetcherHandler handler) {
        for (PoIFetcher source : sources) {
            source.addHandler(handler);
        }
    }

    public void removeHandler(PoIFetcherHandler handler) {
        for (PoIFetcher source: sources) {
            source.removeHandler(handler);
        }
    }

    public void cleanUp() {
        //TODO: do something
        for (PoIFetcher source: sources) {
            source.cleanUp();
        }
    }

    public boolean isReady() {
        boolean ret = true;
        for (PoIFetcher source : sources) {
            ret &= source.isReady();
        }
        return ret;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (this.lastFetched == null || location.distanceTo(this.lastFetched) > 100) {
            this.fetchData();
        }
    }
}
