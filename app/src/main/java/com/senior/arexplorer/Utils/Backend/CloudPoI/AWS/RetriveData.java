package com.senior.arexplorer.Utils.Backend.CloudPoI.AWS;

import android.os.AsyncTask;
import android.util.Log;

import com.senior.arexplorer.Utils.Backend.LocalPoI.LocalPoI;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.saveObj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

public class RetriveData {
    private static final String url = "jdbc:mysql://database-1.cmns0dweli3w.us-west-2.rds.amazonaws.com:3306/ar_schema";
    private static final String user = "masteruser";
    private static final String pass = "Bangladesh88";

    ArrayList<PoI> newPoIs = new ArrayList<PoI>();

 public RetriveData(){

     ForLocalDataTable localData = new ForLocalDataTable();
     localData.execute("");
 }




    public ArrayList getLocalData(){


        return newPoIs;
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
           // Toast.makeText(CloudDB.applicationContext , "Please wait...", Toast.LENGTH_SHORT)
           //         .show();

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
                con.close();
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


}
