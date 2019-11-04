package com.senior.arexplorer;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.ContentValues;
public class CreateDatabase extends SQLiteOpenHelper {

 private static final String DB_NAME ="ARdatabase";
 private static final int DB_VERSION = 1;

    protected CreateDatabase(Context context){
        super(context,DB_NAME, null ,DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db){
       db.execSQL("CREATE TABLE USER("+"_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                     + "fName TEXT,"
                     + "lName TEXT,"
                     + "email TEXT,"
                      + "Password TEXT);");
        insertUsers( db, "Md", "kashem", "kasem@yahoo.com", "secret");

        db.execSQL("CREATE TABLE LOCAL_DATA("+"_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "description TEXT,"
                + "latitude TEXT,"
                + "longitude TEXT,"
                + "elevation TEXT,"
                + "image_resource_id INTEGER);");

        insertUsers( db, "Md", "kashem", "kasem@yahoo.com", "secret");


    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion ){



    }

    private static void insertUsers(SQLiteDatabase db, String fName, String lName, String email, String password){
                   ContentValues users = new ContentValues();
                   users.put("fName",fName);
                   users.put("lName",lName);
                   users.put("email", email);
                   users.put("Password",password);
                   db.insert("USER",null, users);


    }


}
