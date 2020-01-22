package com.senior.arexplorer.Utils.LocalDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LocalDB {
 private static SQLiteDatabase localDB=null;
 private static Context applicationContext = null;
 private static LocalDB instance = null;

 public static void init(Context context) {
     Log.d("LocalDB", "LocalDB is initialized.");
     LocalDB.applicationContext = context.getApplicationContext();
 }
    //private constructor

    public static LocalDB getInstance() {
     if(LocalDB.applicationContext == null) {
         // log error and return null

         return null;
     }
     if (LocalDB.instance == null) LocalDB.getInstanceSynced();
     return LocalDB.instance;
    }

    private static synchronized LocalDB getInstanceSynced() {
     Log.d("LocalDB", "LocalDB is instanciated.");
     if (LocalDB.instance == null) LocalDB.instance = new LocalDB();
     return LocalDB.instance;
    }
    //private getInstanceSynced

    /*
    in constructor connect to database and save refrence
     */

    private LocalDB(){
        localDB = new CreateDatabase(applicationContext).getWritableDatabase();
        if (localDB == null) {
            Log.e("LocalDB", "failed to get writable database");
        }
    }

    private static void insertUsers( String fName, String lName, String email, String password){
                   ContentValues users = new ContentValues();
                   users.put("fName",fName);
                   users.put("lName",lName);
                   users.put("email", email);
                   users.put("Password",password);
        localDB.insert("USER",null, users);
    }

    private static void insertLocalData( String name, String description, String latitude, String longitude, String elevation, int image_resource_id){
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("description",description);
        values.put("latitude", latitude);
        values.put("longitude",longitude);
        values.put("elevation",elevation);
        values.put("image_resource_id",image_resource_id);

        localDB.insert("LOCAL_DATA",null, values);
    }

    public Cursor getData(int id) {
        //TODO: different shit here
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  localDB.rawQuery( "select * from USER where _id="+id+"", null );
        return res;
    }

    public Cursor getLocalData(int id) {
        //TODO: different shit here
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  localDB.rawQuery( "select * from LOCAL_DATA where _id="+id+"", null );
        return res;
    }

}
