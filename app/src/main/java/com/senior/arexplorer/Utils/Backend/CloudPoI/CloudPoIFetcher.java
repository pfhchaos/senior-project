package com.senior.arexplorer.Utils.Backend.CloudPoI;

import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDB;
import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDBListener;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.PoIFetcher;
import com.senior.arexplorer.Utils.Backend.PoIFetcherHandler;
import com.senior.arexplorer.Utils.Backend.saveObj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
        ArrayList<PoI> newPoIs = new ArrayList<PoI>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            String result = "Database Connection Successful\n";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from LOCAL_DATA");

            while (rs.next()) {
                String userName = "testUser";
                String locName,locDesc;
                Double locLat,locLong,locElev;
                Boolean priv = false;
                locName= rs.getString(2).toString();
                locDesc = rs.getString(3).toString();
                locLat = rs.getDouble(4);
                locLong = rs.getDouble(5);
                locElev = rs.getDouble(6);
                saveObj s = new saveObj(userName,locName,locDesc,locLat,locLong,locElev,priv);
                newPoIs.add(new CloudPoI(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (PoIFetcherHandler handler: this.poIFetcherHandlers) {
            handler.placeFetchComplete();
        }
        synchronized (this.poIs) {
            this.poIs = newPoIs;
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
