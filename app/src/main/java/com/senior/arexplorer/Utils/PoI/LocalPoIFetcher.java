package com.senior.arexplorer.Utils.PoI;


import android.database.Cursor;
import android.util.Log;

import com.senior.arexplorer.AR.saveObj;
import com.senior.arexplorer.Utils.LocalDB.LocalDB;
import com.senior.arexplorer.Utils.LocalDB.LocalDBListener;

import java.util.ArrayList;
import java.util.Collection;

public class LocalPoIFetcher extends PoIFetcher implements LocalDBListener {

    private Here here;
    private LocalDB LDB;
    private static LocalPoIFetcher LPF;
    private boolean isReady = false;

    public static LocalPoIFetcher getInstance(){
        if(LPF == null) LPF = new LocalPoIFetcher();
        return LPF;
    }

    private LocalPoIFetcher(){
        this.here = Here.getInstance();
        this.LDB = LocalDB.getInstance();
        this.poIFetcherHandlers = new ArrayList<>();
        this.poIs = new ArrayList<>();

        LocalDB.getInstance().addListener(this);
    }

    @Override
    Collection<PoI> getPoIs() {
        // sexy
        return this.poIs;
    }

    @Override
    void fetchData() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                fetchDataAsync();
            }
        });

        thread.start();

    }

    private synchronized void fetchDataAsync(){
        isReady = false;
        Cursor c = this.LDB.getAllLocalData();
        ArrayList<PoI> newPoIs = new ArrayList<PoI>();

        while(c.moveToNext()){
            String userName = "testUser";
            String locName,locDesc;
            Double locLat,locLong,locElev;
            Boolean priv = false;

            locName = c.getString(c.getColumnIndex("name"));
            locDesc = c.getString(c.getColumnIndex("description"));
            locLat = new Double(c.getString(c.getColumnIndex("latitude")));
            locLong = new Double(c.getString(c.getColumnIndex("longitude")));
            locElev = new Double(c.getString(c.getColumnIndex("elevation")));

            saveObj s = new saveObj(userName,locName,locDesc,locLat,locLong,locElev,priv);
            Log.i("fetched saveObj",s.toString());
            newPoIs.add(new LocalPoI(s));
        }
        for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
            handler.placeFetchComplete();
        }
        synchronized (this.poIs) {
            this.poIs = newPoIs;
        }
        isReady = true;
    }

    public void cleanUp(){
        LocalPoIFetcher.LPF = null;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void onUpdate() {
        this.fetchData();
    }
}
