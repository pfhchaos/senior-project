package com.senior.arexplorer.Utils.PoI;

import com.senior.arexplorer.Utils.AWS.CloudDB;
import com.senior.arexplorer.Utils.AWS.CloudDBListener;

import java.util.ArrayList;
import java.util.Collection;

public class CloudPoIFetcher extends PoIFetcher implements CloudDBListener {


    private Here here;
    private CloudDB CDB;
    private static CloudPoIFetcher CPF;
    private boolean isReady = false;

    public static CloudPoIFetcher getInstance(){
        if(CPF == null) CPF = new CloudPoIFetcher();
        return CPF;
    }

    @Override
    Collection<PoI> getPoIs() {
        return this.poIs;
    }

    @Override
    void fetchData() {
        Thread thread = new Thread(new Runnable(){

            @Override
            public void run() {
                fetchDataAsync();
            }

        });
        thread.start();


    }

    private synchronized void fetchDataAsync(){
        isReady = false;
       /* Cursor c = this.CDB.getAllLocalData();
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
        }*/
        for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
            handler.placeFetchComplete();
        }
        synchronized (this.poIs) {
            this.poIs = CloudDB.getInstance().getLocalData();
        }
        isReady = true;


    }



    private CloudPoIFetcher(){
        this.here = Here.getInstance();
        this.CDB = CloudDB.getInstance();
        this.poIFetcherHandlers = new ArrayList<>();
        this.poIs = new ArrayList<>();

        CloudDB.getInstance().addListener(this);
    }

    @Override
    public void cleanUp() {
        CloudPoIFetcher.CPF = null;
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
