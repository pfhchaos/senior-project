package com.senior.arexplorer.Utils.Backend.LocalPoI;


import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDBListener;
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.Backend.LocalPoI.LocalDB.LocalDB;
import com.senior.arexplorer.Utils.Backend.LocalPoI.LocalDB.LocalDBListener;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.PoIFetcher;
import com.senior.arexplorer.Utils.Backend.PoIFetcherHandler;
import com.senior.arexplorer.Utils.Backend.saveObj;
import com.senior.arexplorer.Utils.SettingListener;
import com.senior.arexplorer.Utils.Settings;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class LocalPoIFetcher extends PoIFetcher implements LocalDBListener, SettingListener {




    private Here here;
    private LocalDB LDB;
    private static LocalPoIFetcher LPF;
    private boolean isReady = false;

    public static LocalPoIFetcher getInstance(){
        if(LPF == null) getInstanceSynced();
        return LPF;
    }

    private static synchronized void getInstanceSynced() {
        if(LPF == null) LPF = new LocalPoIFetcher();
        return;
    }

    private LocalPoIFetcher(){
        Log.d("LocalPoIFetcher", "LocalPoIFetcher is instanciated");
        this.here = Here.getInstance();
        this.LDB = LocalDB.getInstance();
        this.poIFetcherHandlers = new ArrayList<>();
        this.poIs = new ArrayList<>();

        Settings.getInstance().addUseLocalBackendListener(this);
        LocalDB.getInstance().addListener(this);
    }

    @Override
    public Collection<PoI> getPoIs() {
        // sexy
        return this.poIs;
    }

    @Override
    public void fetchData() {

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
        ArrayList<PoI> newPoIs = new ArrayList<PoI>();


        Cursor c = LDB.getAllLocalData();



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
            s.setBLOB(c.getBlob(c.getColumnIndex("image")));

            Log.i("fetched saveObj",s.toString());
            Log.d("LocalPoIFetcher", s.toString());
            newPoIs.add(new LocalPoI(s));
        }

        c.close();

        synchronized (this.poIs) {
            this.poIs = newPoIs;
            System.out.println("local DB size : " + poIs.size());
        }

        for (PoIFetcherHandler handler: poIFetcherHandlers) {
            handler.placeFetchComplete();
        }

        isReady = true;

        // local data end
    }

    public void cleanUp(){
        Log.d("LocalPoIFetcher", "LocalPoIFetcher is cleaned up");
        Settings.getInstance().removeUseLocalBackendListener(this);
        LocalPoIFetcher.LPF = null;
    }

    @Override
    public boolean isReady() {
        return this.isReady;
    }

    @Override
    public void onUpdate() {
        this.fetchData();
    }

    @Override
    public void onSettingChange() {
        Log.d("LocalPoIFetcher", "onSettingChanged");
        if (!Settings.getInstance().getUseLocalBackend()) cleanUp();
    }
}
    // class end
