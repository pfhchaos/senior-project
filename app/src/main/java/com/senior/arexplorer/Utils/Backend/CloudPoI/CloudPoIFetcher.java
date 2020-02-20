package com.senior.arexplorer.Utils.Backend.CloudPoI;

import android.os.AsyncTask;
import android.util.Log;

import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDB;
import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDBListener;
//import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.RetriveData;
import com.senior.arexplorer.Utils.Backend.LocalPoI.LocalPoI;
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

public class CloudPoIFetcher extends PoIFetcher implements CloudDBListener, SettingListener {


    private static CloudPoIFetcher CPF;
    private boolean isReady = false;

    public static CloudPoIFetcher getInstance(){
        if(CPF == null) getInstanceSynced();
        return CPF;
    }
    private static synchronized void getInstanceSynced() {
        if(CPF == null) CPF = new CloudPoIFetcher();
        return;
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
        ConnectMySql2 db=new ConnectMySql2(newPoIs); // cloud data retring
        db.execute("");


        synchronized (this.poIs) {
//            RetriveData db = new RetriveData();
//            this.poIs = db.getLocalData();
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
    public void onUpdateCloud() {
        this.fetchData();
    }

    @Override
    public void onSettingChange() {
        Log.d("CloudPoIFetcher", "onSettingChanged");
        if (!Settings.getInstance().getUseCloudBackend()) cleanUp();
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

   private class ConnectMySql2 extends AsyncTask<String, Void, String> {
        String res = "";
        String data="";

        private static final String url2 = "jdbc:mysql://database-1.cmns0dweli3w.us-west-2.rds.amazonaws.com:3306/ar_schema";
        private static final String user2 = "masteruser";
        private static final String pass2 = "Bangladesh88";


        ArrayList<PoI> newPoIs2;
        public ConnectMySql2(ArrayList<PoI> newPoIs2){

            this.newPoIs2=newPoIs2;

        }


        // Method that returns the first word



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //  Toast.makeText(CloudDB.applicationContext , "Please wait...", Toast.LENGTH_SHORT)
            //    .show();

        }

        @Override
        protected String doInBackground(String... params) {
            //  newPoIs2 = new ArrayList<PoI>();
            //  newPoIs2 = new ArrayList<PoI>();
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url2, user2, pass2);
                // System.out.println("Databaseection success");

                String result = "Database Connection Successful\n";
                // Statement st = con.createStatement();
                //  PreparedStatement statement = con.prepareStatement("SELECT * FROM TYPE"); //
                Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs    = statement.executeQuery("SELECT * FROM LOCALDATA");

                while(rs.next()){
                    String userName = "testUser";
                    String locName,locDesc;
                    Double locLat,locLong,locElev;
                    Boolean priv = false;
                    locName= rs.getString("name");
                    locDesc = rs.getString("description");
                    locLat = rs.getDouble("latitude");
                    locLong = rs.getDouble("longitude");
                    locElev = rs.getDouble("elevation");
                    Blob blob = rs.getBlob("image");
                    byte[] theBytes = blob.getBytes(1L, (int)blob.length());

                    saveObj s = new saveObj(userName,locName,locDesc,locLat,locLong,locElev,priv);
                    s.setBLOB(theBytes);

                    System.out.println("try :"+s.getLocationDesc());
                    this.newPoIs2.add(new LocalPoI(s));
                    // Log.d("check3 : ", rs.getString("type"));
                    System.out.println("size ****: "+ this.newPoIs2.size());
                }


                res = result;
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
                res = e.toString();
                Log.d("NOT : ", "Not select statement");
            }

            //  for local datastart *********
      /*  Cursor c = LDB.getAllLocalData();



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
            newPoIs2.add(new LocalPoI(s));
        }

        c.close();
      */
            synchronized (poIs) {
                poIs = newPoIs2;
                System.out.println("cloud size : " + poIs.size());
            }

            for (PoIFetcherHandler handler: poIFetcherHandlers) {
                handler.placeFetchComplete();
            }

            isReady = true;
            return data;
        }

        //  for local datastart *********


        ArrayList<PoI> getArray(){
            return newPoIs2;
        }


        @Override
        protected void onPostExecute(String result) {
            //txtData.setText(result);
            Log.d("Result : ", result);
            System.out.println("sizeOUT ****: "+ newPoIs2.size());
        }

    } // private class dove




}


