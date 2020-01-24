package com.senior.arexplorer.Utils.Places;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class Backend extends PoIFetcher implements HereListener, PoIFetcherHandler{
    private static Backend instance = null;
    private Location lastFetched = null;

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
        super();

        this.sources = new ArrayList<PoIFetcher>();
        this.sources.add(GooglePoIFetcher.getInstance());
        this.sources.add(LocalPoIFetcher.getInstance());

        Here.getInstance().addListener(this);
    }

    public Collection<PoI> getPoIs() {
        Collection<PoI> ret = new ArrayList<PoI>();
        for (PoIFetcher source: sources) {
            ret.addAll(source.getPoIs());
        }
        return ret;
    }

    void fetchData() {
        this.lastFetched = Here.getInstance().getLocation();
        for (PoIFetcher source: sources) {
            source.fetchData();
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

    @Override
    public void placeFetchComplete() {
        boolean ready = true;
        for (PoIFetcher source : sources) {
            ready &= source.isReady();
        }

        if (ready) {
            for (PoIFetcherHandler handler : this.poIFetcherHandlers) {
                handler.placeFetchComplete();
            }
        }
    }
}
