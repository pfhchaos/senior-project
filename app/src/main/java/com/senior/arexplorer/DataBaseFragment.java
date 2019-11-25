package com.senior.arexplorer;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class DataBaseFragment extends Fragment implements IFragSettings{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_data_base, container, false);
        MainActivity activity = new MainActivity();
        String Name = activity.getName();

        TextView name = (TextView)view.findViewById(R.id.TextViewName);
        name.setText(Name);
        return view;

    }

    @Override
    public void loadSettings(Menu menu, DrawerLayout drawer) {

    }

    public static class Place {
    }
}