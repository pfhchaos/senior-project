package com.senior.arexplorer.Utils.AWS;


import android.os.AsyncTask;
import android.util.Log;

import com.senior.arexplorer.AR.saveObj;
import com.senior.arexplorer.Utils.PoI.LocalPoI;
import com.senior.arexplorer.Utils.PoI.PoI;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ForLocalDataTable

{

    private static final String url = "jdbc:mysql://database-1.cmns0dweli3w.us-west-2.rds.amazonaws.com:3306/ar_schema";
    private static final String user = "masteruser";
    private static final String pass = "Bangladesh88";
    ArrayList<PoI> newPoIs = new ArrayList<PoI>();




    public ArrayList getLocalData(){
        ForFetchData connectMySql = new ForFetchData();
        connectMySql.execute("");

        return newPoIs;
    }

    // for local data
    private class ForFetchData extends AsyncTask<String, Void, String> {
        String res = "";
        // Method that returns the first word
        public  String checkCommand(String input) {
            return input.split(" ")[0]; // Create array of words and return the 0th word
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                String result = "Database Connection Successful\n";
                // Statement st = con.createStatement();
                // ResultSet rs = st.executeQuery("select * from LOCALDATA");
                // ResultSetMetaData rsmd = rs.getMetaData();
                Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs    = statement.executeQuery("select * from LOCALDATA");
                while (rs.next()) {
                    // result += rs.getString(2).toString() + "\n";
                    String userName = "testUser";
                    String locName,locDesc;
                    Double locLat,locLong,locElev;
                    Boolean priv = false;
                    locName= rs.getString(rs.findColumn("name"));

                    locDesc = rs.getString(rs.findColumn("description"));
                    locLat = rs.getDouble(rs.findColumn("latitude"));
                    locLong = rs.getDouble(rs.findColumn("longitude"));
                    locElev = rs.getDouble(rs.findColumn("elevation"));
                    saveObj s = new saveObj(userName,locName,locDesc,locLat,locLong,locElev,priv);
                    Blob blob = rs.getBlob(rs.findColumn("image"));
                    byte[] theBytes = blob.getBytes(1L, (int)blob.length());
                    s.setBLOB( theBytes);
                    newPoIs.add(new LocalPoI(s));
                    System.out.println("locName "+locName+" arr size "+newPoIs.size());
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
}

