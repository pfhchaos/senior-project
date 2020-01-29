package com.senior.arexplorer.AWS;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class CloudDB {


    private static Context applicationContext = null;
    private static CloudDB instance = null;

    private static final String url = "jdbc:mysql://database-1.cmns0dweli3w.us-west-2.rds.amazonaws.com:3306/ar_schema";
    private static final String user = "masteruser";
    private static final String pass = "Bangladesh88";
    public  String query;

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


    }

/*
* the ExecurQuery(String command) function can be used to execute any mysql query
*  and accessible from any fragment
* */

    public void ExecurQuery(String command){
        this.query=command;
        ConnectMySql connectMySql = new ConnectMySql();
        connectMySql.execute("");
    }


    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res = "";

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
                System.out.println("Databaseection success");

                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                st.execute(query);
                //ResultSet rs = st.executeQuery(query);
               // ResultSetMetaData rsmd = rs.getMetaData();

               // while (rs.next()) {
                  //  result += rs.getString(2).toString() + "\n";
              //  }
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

//************************* for test end
}
