package com.senior.arexplorer;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class DataBaseFragment extends Fragment {

    SQLiteOpenHelper databaseHelper = new CreateDatabase(getContext());
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_base, container, false);





    }

    //CreateDatabase db;
    private void LoadData(){
        // for database

        try{
            SQLiteDatabase db =databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("USER",
                    new String[]{"fName", "lName","email", "Password"},
                    "_id = ?",null,null,null,null);

            // move to the first record in the cursor
            if(cursor.moveToFirst()){
                String fNameText = cursor.getString(0);
                String lNameText = cursor.getString(1);
                String emailText = cursor.getString(2);
                String passwordText = cursor.getString(3);

            }
        }catch(SQLiteException e){
            Toast toast = Toast.makeText(getContext(), "Database unavailable",Toast.LENGTH_SHORT);
            toast.show();

        }

        //database end

    }

    public static class Place {
    }
}