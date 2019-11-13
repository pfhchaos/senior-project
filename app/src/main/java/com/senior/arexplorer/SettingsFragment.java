package com.senior.arexplorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment implements IFragSettings{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu (true);
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void loadSettings(Menu menu, DrawerLayout drawer){
        menu.removeGroup(R.id.settings);
        MenuItem item = menu.add(R.id.settings, Menu.NONE, Menu.NONE, "This is a test?");
        item.setOnMenuItemClickListener((i) -> {item.setTitle("This was clicked!"); drawer.closeDrawer(GravityCompat.START); return true;});

    }
}
