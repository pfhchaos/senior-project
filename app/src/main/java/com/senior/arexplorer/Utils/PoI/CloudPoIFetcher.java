package com.senior.arexplorer.Utils.PoI;

import com.senior.arexplorer.Utils.AWS.CloudDB;
import com.senior.arexplorer.Utils.AWS.CloudDBListener;
import com.senior.arexplorer.Utils.AWS.ForLocalDataTable;

import java.util.ArrayList;
import java.util.Collection;

public class CloudPoIFetcher extends PoIFetcher implements CloudDBListener {

    private static CloudPoIFetcher CPF;
    private boolean isReady = false;

    private static final String url = "jdbc:mysql://database-1.cmns0dweli3w.us-west-2.rds.amazonaws.com:3306/ar_schema";
    private static final String user = "masteruser";
    private static final String pass = "Bangladesh88";

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

        for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
            handler.placeFetchComplete();
        }
        synchronized (this.poIs) {
            ForLocalDataTable db = new ForLocalDataTable();
            this.poIs = db.getLocalData();
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
