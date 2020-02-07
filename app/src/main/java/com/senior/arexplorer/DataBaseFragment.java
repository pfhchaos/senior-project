package com.senior.arexplorer;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.senior.arexplorer.Utils.IFragSettings;
import com.senior.arexplorer.Utils.LocalDB.LocalDB;


public class DataBaseFragment extends Fragment implements IFragSettings {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_data_base, container, false);
        MainActivity activity = new MainActivity();

        LocalDB.getInstance().insertUsers("m1","lastname","kasem@gmail.com", "secret");
        TextView name = (TextView)view.findViewById(R.id.TextViewName);
        Cursor cursor = LocalDB.getInstance().getUserData(2);
        if(cursor.moveToFirst()) {
            String st = cursor.getString(2);

            name.setText(st);
            cursor.close();
        }
       //CloudDB.getInstance().ExecurQuery("INSERT INTO TYPE ( type) VALUES ( 'tt5' )");   //INSERT INTO TYPE ( type) VALUES ( 'type4' );
     // ArrayList<PoI> arr= CloudDB.getInstance().getLocalData();
       //int s = arr.size();
       // System.out.println("************************ ::: "+s);
       //Log.d("datafrag : ", arr.get(0).getName());

       // CloudDB.getInstance().ExecurQuery( " INSERT INTO LOCAL_DATA (name, description, latitude, longitude, elevation ) VALUES ('nn', 'des', '123', '120', '25')");

        return view;

    }

    @Override
    public void loadSettingsUI(Menu menu, DrawerLayout drawer, Context context) {

    }

    public static class Place {
    }
}