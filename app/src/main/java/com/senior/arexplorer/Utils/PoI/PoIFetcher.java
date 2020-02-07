package com.senior.arexplorer.Utils.PoI;

import java.util.ArrayList;
import java.util.Collection;

public abstract class PoIFetcher {

    protected Collection<PoIFetcherHandler> poIFetcherHandlers = null;
    protected Collection<PoI> poIs;
    protected boolean isReady;

    abstract Collection<PoI> getPoIs();
    abstract void fetchData();

    public PoIFetcher() {
        this.poIFetcherHandlers = new ArrayList<PoIFetcherHandler>();
        this.poIs = new ArrayList<PoI>();
        this.isReady = false;
    }
    public void addHandler(PoIFetcherHandler handler) {
        this.poIFetcherHandlers.add(handler);
    }

    public void removeHandler(PoIFetcherHandler handler) {
        this.poIFetcherHandlers.remove(handler);
    }

    public abstract void cleanUp();
    public abstract boolean isReady();
}