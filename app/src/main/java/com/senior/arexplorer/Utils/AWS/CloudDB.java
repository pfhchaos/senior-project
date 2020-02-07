package com.senior.arexplorer.Utils.AWS;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.senior.arexplorer.AR.saveObj;
import com.senior.arexplorer.Utils.PoI.LocalPoI;
import com.senior.arexplorer.Utils.PoI.PoI;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class CloudDB {
    private static Context applicationContext = null;
    private static CloudDB instance = null;

    private static final String url = "jdbc:mysql://database-1.cmns0dweli3w.us-west-2.rds.amazonaws.com:3306/ar_schema";
    private static final String user = "masteruser";
    private static final String pass = "Bangladesh88";
    public  String query;
    private Collection<CloudDBListener> callbacks;

    ArrayList<PoI> newPoIs = new ArrayList<PoI>();

    public static void init(Context context) {
        Log.d("CloudDB", "CloudDB is initialized.");
        CloudDB.applicationContext = context.getApplicationContext();


    }

    //private constructor

    public static CloudDB getInstance() {
        if(CloudDB.applicationContext == null) {
            // log error and return null

            return null;
        }
        if (CloudDB.instance == null) CloudDB.getInstanceSynced();
        return CloudDB.instance;
    }

    private static synchronized CloudDB getInstanceSynced() {
        Log.d("CloudDB", "CloudDB is instanciated.");
        if (CloudDB.instance == null) CloudDB.instance = new CloudDB();
        return CloudDB.instance;
    }
    //private getInstanceSynced

    private CloudDB(){
       // ForLocalDataTable localData = new ForLocalDataTable();
      //  localData.execute("");

    }
    private void notifyListeners() {
        for (CloudDBListener listener : this.callbacks) {
            listener.onUpdate();
        }
    }
    public void addListener(CloudDBListener listener) {
        this.callbacks.add(listener);
    }

    public void removeListener(CloudDBListener listener) {
        this.callbacks.remove(listener);
    }

    /*
     * the ExecurQuery(String command) function can be used to execute any mysql query
     *  and accessible from any fragment
     * */

    public void ExecurQuery( saveObj s){
       // this.query=command;
        ConnectMySql connectMySql = new ConnectMySql(s);
        connectMySql.execute("");
    }
    public ArrayList getLocalData(){


        return newPoIs;
    }

    public void insertCloudData(saveObj s) {
        this.ExecurQuery(s);
    }

    // for local data
    private class ForLocalDataTable extends AsyncTask<String, Void, String> {
        String res = "";
        // Method that returns the first word
        public  String checkCommand(String input) {
            return input.split(" ")[0]; // Create array of words and return the 0th word
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(CloudDB.applicationContext , "Please wait...", Toast.LENGTH_SHORT)
                    .show();

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from LOCAL_DATA");
                ResultSetMetaData rsmd = rs.getMetaData();
                while (rs.next()) {
                    // result += rs.getString(2).toString() + "\n";
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
                    newPoIs.add(new LocalPoI(s));
                    // Log.d("check3 : ", rs.getString("type"));
                }

                res = result;
            } catch (Exception e) {
                e.printStackTrace();
                res = e.toString();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            //txtData.setText(result);
            Log.d("Result : ", result);
        }
    }

    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res = "";
        String data="";
        saveObj s;
       public ConnectMySql(saveObj s){

           this.s=s;
          }

        // Method that returns the first word


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(CloudDB.applicationContext , "Please wait...", Toast.LENGTH_SHORT)
                    .show();

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                // System.out.println("Databaseection success");

                String result = "Database Connection Successful\n";
               // Statement st = con.createStatement();
                PreparedStatement statement = con.prepareStatement("INSERT INTO LOCALDATA (name, description, latitude, longitude, elevation,image) VALUES (?, ?, ?, ?, ?,?)"); //


                System.out.println("kkk"+this.s.getLocationName());
                statement.setString(1,this.s.getLocationName());
                statement.setString(2,this.s.getLocationDesc());
                System.out.println("aaa :"+this.s.getLocationLatitude());
                statement.setDouble(3,this.s.getLocationLatitude());
                statement.setDouble(4,this.s.getLocationLongitude());
                statement.setDouble(5,this.s.getLocationElevation());
                statement.setBinaryStream(6,new ByteArrayInputStream(s.getBlob()),s.getBlob().length);

                statement.execute();

                  //  st.execute(query);


                res = result;
            } catch (Exception e) {
                e.printStackTrace();
                res = e.toString();
                Log.d("NOT : ", "Not select statement");
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            //txtData.setText(result);
            Log.d("Result : ", result);
        }
    }

}