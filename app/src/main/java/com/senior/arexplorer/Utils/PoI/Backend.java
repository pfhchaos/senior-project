package com.senior.arexplorer.Utils.PoI;

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

        PoIFetcher googlePoIFetcher = GooglePoIFetcher.getInstance();
        this.sources.add(googlePoIFetcher);
        googlePoIFetcher.addHandler(this);

        PoIFetcher localPoIFetcher = LocalPoIFetcher.getInstance();
        this.sources.add(localPoIFetcher);
        localPoIFetcher.addHandler(this);

        PoIFetcher oneBusAwayPoIFetcher = OneBusAwayPoIFetcher.getInstance();
        this.sources.add(oneBusAwayPoIFetcher);
        oneBusAwayPoIFetcher.addHandler(this);

        Here.getInstance().addListener(this);
    }

    public Collection<PoI> getPoIs() {
        if (this.isReady()) {
            Collection<PoI> ret = new ArrayList<PoI>();
            for (PoIFetcher source : sources) {
                ret.addAll(source.getPoIs());
            }
            return ret;
        }
        else {
            Log.d("Backend", "attempted to retrieve poi's before sources are ready");
            return null;
        }
    }

    void fetchData() {
        Log.v("Backend","fetching data from sources");
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
        Log.v("Backend", "location changed");
        if (this.lastFetched == null || location.distanceTo(this.lastFetched) > 100) {
            this.fetchData();
        }
    }

    @Override
    public void placeFetchComplete() {
        Log.v("Backend","place fetch complete");
        boolean ready = true;
        for (PoIFetcher source : sources) {
            ready &= source.isReady();
        }

        if (ready) {
            Log.v("Backend","all sources ready");
            for (PoIFetcherHandler handler : this.poIFetcherHandlers) {
                handler.placeFetchComplete();
            }
        }
        else {
            Log.v("Backend","sources not ready");
        }
    }
}
