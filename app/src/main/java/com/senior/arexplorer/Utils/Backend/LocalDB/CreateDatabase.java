package com.senior.arexplorer.Utils.Backend.LocalDB;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CreateDatabase extends SQLiteOpenHelper {

 private static final String DB_NAME ="ARdatabase";
 private static final int DB_VERSION = 2;

    public CreateDatabase(Context context){
        //check that context has been initalized
        super(context.getApplicationContext(),DB_NAME, null ,DB_VERSION);
        Log.d("CreateDatabase","super called");
    }

    //do not change header
    //create tables here
    @Override
    public void onCreate(SQLiteDatabase db){
       db.execSQL("CREATE TABLE USER("+"_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                     + "fName TEXT,"
                     + "lName TEXT,"
                     + "email TEXT,"
                      + "Password TEXT);");
        //db.insertUsers(  "Md", "kashem", "kasem@yahoo.com", "secret");

        db.execSQL("CREATE TABLE LOCAL_DATA("+"_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "description TEXT,"
                + "latitude TEXT,"
                + "longitude TEXT,"
                + "elevation TEXT,"
                + "image BLOB);");

        db.execSQL("CREATE TABLE TYPE("+"_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "type TEXT);");
    }
    //do not change header
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion ){

        db.execSQL("DROP TABLE IF EXISTS USER");
        db.execSQL("DROP TABLE IF EXISTS LOCAL_DATA");
        db.execSQL("DROP TABLE IF EXISTS TYPE");
        onCreate(db);

    }
}
