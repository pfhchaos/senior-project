package com.senior.arexplorer;


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
        Cursor cursor = LocalDB.getInstance().getData(2);
        if(cursor.moveToFirst()) {
            String st = cursor.getString(2);

            name.setText(st);
            cursor.close();
        }
        return view;

    }

    @Override
    public void loadSettings(Menu menu, DrawerLayout drawer) {

    }

    public static class Place {
    }
}