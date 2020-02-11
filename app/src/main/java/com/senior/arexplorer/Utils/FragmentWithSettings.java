package com.senior.arexplorer.Utils;

import android.content.Context;
import android.view.Menu;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.senior.arexplorer.R;

public abstract class FragmentWithSettings extends Fragment {
    public void onStart() {
        super.onStart();

        Menu menu = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu();
        DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
        this.loadSettingsUI(menu,drawer,getContext());
    }

    public abstract void loadSettingsUI(Menu menu, DrawerLayout drawer, Context context);
}
