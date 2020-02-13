package com.senior.arexplorer.Utils.Backend.CloudPoI;

import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDB;
import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDBListener;
import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.RetriveData;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.PoIFetcher;
import com.senior.arexplorer.Utils.Backend.PoIFetcherHandler;

import java.util.ArrayList;
import java.util.Collection;

public class CloudPoIFetcher extends PoIFetcher implements CloudDBListener {

    private static CloudPoIFetcher CPF;
    private boolean isReady = false;
    public static CloudPoIFetcher getInstance(){
        if(CPF == null) CPF = new CloudPoIFetcher();
        return CPF;
    }

    @Override
    public Collection<PoI> getPoIs() {
        return this.poIs;
    }

    @Override
    public void fetchData() {
        Thread thread = new Thread(new Runnable(){

            @Override
            public void run() {
                fetchDataAsync();
            }

        });
        thread.start();
    }

    private synchronized void fetchDataAsync(){
       // ArrayList<PoI> newPoIs = new ArrayList<PoI>();



        synchronized (this.poIs) {
            RetriveData db = new RetriveData();
            this.poIs = db.getLocalData();
        }

        for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
            handler.placeFetchComplete();
        }
        isReady = true;
    }

    private CloudPoIFetcher(){
        super();
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