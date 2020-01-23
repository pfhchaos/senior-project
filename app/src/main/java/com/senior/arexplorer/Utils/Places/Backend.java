package com.senior.arexplorer.Utils.Places;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class Backend {
    private static Backend instance = null;

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

    public void fetchData() {
        //TODO: is this nessicarry?
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
}