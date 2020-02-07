package com.senior.arexplorer.Utils.Backend.LocalDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.senior.arexplorer.Utils.Backend.saveObj;

import java.util.ArrayList;
import java.util.Collection;

public class LocalDB {
 private static SQLiteDatabase localDB = null;
 private static Context applicationContext = null;
 private static LocalDB instance = null;

 private Collection<LocalDBListener> callbacks;

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
        this.callbacks = new ArrayList<LocalDBListener>();
    }

    public void insertUsers( String fName, String lName, String email, String password){
                   ContentValues users = new ContentValues();
                   users.put("fName",fName);
                   users.put("lName",lName);
                   users.put("email", email);
                   users.put("Password",password);
        localDB.insert("USER",null, users);
    }
    public void insertType( String type){
        ContentValues TypeValue = new ContentValues();
        TypeValue.put("type",type);
        localDB.insert("TYPE",null, TypeValue);
    }

    private void insertLocalData( String name, String description, String latitude, String longitude, String elevation, byte[] image){
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("description",description);
        values.put("latitude", latitude);
        values.put("longitude",longitude);
        values.put("elevation",elevation);
        values.put("image",image);

        localDB.insert("LOCAL_DATA",null, values);
        notifyListeners();
    }

    private void nuke(){
        if(localDB != null) {
            localDB.delete("LOCAL_DATA",null,null);
            notifyListeners();
        }
    }
    public void deleteAllCustomLoc(){
        nuke();
    }

    public void insertLocalData(saveObj s){
        this.insertLocalData(s.getLocationName(),s.getLocationDesc(),s.getLocationLatitude()+"",s.getLocationLongitude()+"",s.getLocationElevation()+"",s.getBlob());
        long count = DatabaseUtils.queryNumEntries(localDB,"LOCAL_DATA");
        Log.i("local db","number of rows:\t\t"+count);
        notifyListeners();
    }

    public Cursor getUserData(int id) {
        //TODO: different shit here
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  localDB.rawQuery( "select * from USER where _id="+id+"", null );
        return res;
    }

    public Cursor getType(String type) {

        Cursor res =  localDB.rawQuery( "select type from USER where type LIKE '%type%'", null );
        return res;
    }
    public Cursor getAllLocalData(){
        Cursor result = localDB.rawQuery("select * from LOCAL_DATA",null);
        return result;
    }

    private void notifyListeners() {
        for (LocalDBListener listener : this.callbacks) {
            listener.onUpdate();
        }
    }
    public void addListener(LocalDBListener listener) {
        this.callbacks.add(listener);
    }

    public void removeListener(LocalDBListener listener) {
        this.callbacks.remove(listener);
    }


}
