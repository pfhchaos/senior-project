package com.senior.arexplorer.Utils.Backend;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.senior.arexplorer.Utils.Backend.LocalDB.LocalDB;
import com.senior.arexplorer.Utils.Settings;

import java.util.ArrayList;
import java.util.Collection;

public class Backend extends PoIFetcher implements HereListener, PoIFetcherHandler{
    private static Backend instance = null;
    private static Context applicationContext;
    private Location lastFetched = null;

    private Collection<PoIFetcher> sources;

    public static synchronized void init(Context context) {
        Log.d("Backend", "Backend is initialized.");
        if (Backend.applicationContext == null) {
            Backend.applicationContext = context.getApplicationContext();
        }
        else {
            if (Backend.applicationContext == context.getApplicationContext()) {
                Log.d("Backend", "Backend initalized twice, same context proceding");
            }
            else {
                Log.e("Backend", "Attempted to initialize Backend twice! from different contexts");
                throw new Error("Shit and die!");
            }
        }
    }

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

        Here.init(this.applicationContext);
        Here.getInstance().addListener(this);

        if (Settings.getInstance().getUseGoogleBackend()) {
            Log.d("Backend","Google Places backend is enabled, starting");
            PoIFetcher googlePoIFetcher = GooglePoIFetcher.getInstance();
            this.sources.add(googlePoIFetcher);
            googlePoIFetcher.addHandler(this);
        }
        else {
            Log.d("Backend","Google Places backend is disabled, skipping");
        }

        if (Settings.getInstance().getUseLocalBackend()) {
            Log.d("Backend","LocalDB backend is enabled, starting");
            LocalDB.init(this.applicationContext);
            PoIFetcher localPoIFetcher = LocalPoIFetcher.getInstance();
            this.sources.add(localPoIFetcher);
            localPoIFetcher.addHandler(this);
        }
        else {
            Log.d("Backend","LocalDB backend is disabled, skipping");
        }

        if (Settings.getInstance().getUseOneBusAwayBackend()) {
            Log.d("Backend","OneBusAway backend is enabled, starting");
            PoIFetcher oneBusAwayPoIFetcher = OneBusAwayPoIFetcher.getInstance();
            this.sources.add(oneBusAwayPoIFetcher);
            oneBusAwayPoIFetcher.addHandler(this);
        }
        else {
            Log.d("Backend","OneBusAway backend is disabled, skipping");
        }

        fetchData();
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
        boolean ret = false;
        for (PoIFetcher source : sources) {
            ret |= source.isReady();
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

        if (isReady()) {
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
