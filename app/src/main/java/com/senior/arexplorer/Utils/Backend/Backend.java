package com.senior.arexplorer.Utils.Backend;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDB;
import com.senior.arexplorer.Utils.Backend.CloudPoI.CloudPoIFetcher;
import com.senior.arexplorer.Utils.Backend.GooglePoI.GooglePoIFetcher;
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.Backend.Here.HereListener;
import com.senior.arexplorer.Utils.Backend.LocalPoI.LocalDB.LocalDB;
import com.senior.arexplorer.Utils.Backend.LocalPoI.LocalPoIFetcher;
import com.senior.arexplorer.Utils.Backend.OneBusAwayPoI.OneBusAwayPoIFetcher;
import com.senior.arexplorer.Utils.SettingListener;
import com.senior.arexplorer.Utils.Settings;

import java.util.ArrayList;
import java.util.Collection;

public class Backend extends PoIFetcher implements HereListener, PoIFetcherHandler, SettingListener {
    private static Backend instance = null;
    private static Context applicationContext;
    private Location lastFetched = null;

    private Collection<PoIFetcher> sources;

    private SettingListener filterListener;

    public static synchronized void init(Context context) {
        Log.d("Backend", "Backend is initialized.");
        if (Backend.applicationContext == null) {
            Backend.applicationContext = context.getApplicationContext();
        }
        else {
            if (Backend.applicationContext == context.getApplicationContext()) {
                Log.d("Backend", "Backend initialized twice, same context proceeding");
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


        Here.init(this.applicationContext);
        Here.getInstance().addListener(this);

        Settings.getInstance().addUseGoogleBackendListener(this);
        Settings.getInstance().addUseLocalBackendListener(this);
        Settings.getInstance().addUseOneBusAwayBackendListener(this);
        Settings.getInstance().addUseCloudBackendListener(this);

        this.filterListener = new SettingListener() {
            @Override
            public void onSettingChange() {
                Log.d("Backend", "Filter changed");
                buildPoIs();
                notifyHandlers();
            }
        };
        Settings.getInstance().addFilterListener(filterListener);

        buildSources();

        fetchData();
    }

    private void buildSources() {
        this.sources = new ArrayList<PoIFetcher>();

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

        if(Settings.getInstance().getUseCloudBackend()){
            Log.d("Backend","CloudDB backend is enabled, starting");
            CloudDB.init(this.applicationContext);
            PoIFetcher cloudPoIFetcher = CloudPoIFetcher.getInstance();
            this.sources.add(cloudPoIFetcher);
            cloudPoIFetcher.addHandler(this);
        }
        else{
            Log.d("Backend","CloudDB backend is disabled, skipping");
        }

        this.isReady = true;
    }

    private void buildPoIs() {
        Log.d("Backend","Building PoI list");
        if (this.isReady()) {
            Collection<PoI> newPoIs = new ArrayList<PoI>();
            for (PoIFetcher source : sources) {
                for (PoI poi : source.getPoIs()) {
                    if (matchesFilter(poi)) {
                        newPoIs.add(poi);
                    }
                }
            }
            this.poIs = newPoIs;
        }
        else {
            Log.d("Backend", "attempted to build poi's before sources are ready");
        }
    }

    private boolean matchesFilter(PoI poi) {
        String filter = Settings.getInstance().getFilter();
        /*take filter and transform into a regex. the types have underscors instead of spaces
        so the filter needs to treat those the same. it should also ignore case and repeated whitespace.
        possibly also ignore word order though that will be harder
         */
        String result = filter.replaceAll("\\s+"," ").toLowerCase();
        String typeString = filter.replaceAll("\\s+", "_").toLowerCase();
        boolean matches = false;
        matches |= poi.getName().toLowerCase().matches(".*"+result+".*");
        matches |= poi.getDescription().toLowerCase().matches(".*"+result+".*");
        for (String type : poi.getTypes()) {
            matches |= typeString.matches(type);
        }
        return matches;
    }

    private void notifyHandlers() {
        for (PoIFetcherHandler handler : this.poIFetcherHandlers) {
            handler.placeFetchComplete();
        }
    }

    public Collection<PoI> getPoIs() {
        //TODO: clone pois
        return this.poIs;
    }

    public void fetchData() {
        Log.v("Backend","fetching data from sources");
        this.lastFetched = Here.getInstance().getLocation();
        for (PoIFetcher source: sources) {
            source.fetchData();
        }
    }

    public void cleanUp() {
        //TODO: do something
        Log.d("Backend", "cleanUp");
        this.sources = null;
        this.isReady = false;
        Backend.instance = null;
        Settings.getInstance().removeUseGoogleBackendListener(this);
        Settings.getInstance().removeUseLocalBackendListener(this);
        Settings.getInstance().removeUseOneBusAwayBackendListener(this);
        Settings.getInstance().removeUseCloudBackendListener(this);
        Settings.getInstance().removeFilterListener(this.filterListener);
    }

    public boolean isReady() {
        boolean ret = false;
        if (this.isReady) {
            for (PoIFetcher source : sources) {
                ret |= source.isReady();
            }
        }
        return ret;
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.v("Backend", "location changed");
        if (this.lastFetched == null || location.distanceTo(this.lastFetched) > 100) {
            this.fetchData();
        }
    }

    @Override
    public void placeFetchComplete() {
        Log.v("Backend","place fetch complete");

        if (isReady()) {
            Log.v("Backend","all sources ready");
            buildPoIs();
            notifyHandlers();
        }
        else {
            Log.v("Backend","sources not ready");
        }
    }

    @Override
    public void onSettingChange() {
        Log.d("Backend", "onSettingChanged");
        buildSources();
        fetchData();
    }
}
