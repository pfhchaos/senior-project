package com.senior.arexplorer;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.senior.arexplorer.Utils.IFragSettings;
import com.senior.arexplorer.Utils.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment implements IFragSettings {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu (true);
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch aSwitch;

        aSwitch = v.findViewById(R.id.google_settings_switch);
        aSwitch.setChecked(Settings.getInstance().getUseGoogleBackend());
        aSwitch.setOnClickListener(i -> {
            if(Settings.getInstance().getUseGoogleBackend()) {
                Settings.getInstance().setUseGoogleBackend(false);
            }
            else {
                Settings.getInstance().setUseGoogleBackend(true);
            }
        } );

        aSwitch = v.findViewById(R.id.one_bus_away_settings_switch);
        aSwitch.setChecked(Settings.getInstance().getUseOneBusAwayBackend());
        aSwitch.setOnClickListener(i -> {
            if(Settings.getInstance().getUseOneBusAwayBackend()) {
                Settings.getInstance().setUseOneBusAwayBackend(false);
            }
            else {
                Settings.getInstance().setUseOneBusAwayBackend(true);
            }
        } );

        aSwitch = v.findViewById(R.id.local_db_settings_switch);
        aSwitch.setChecked(Settings.getInstance().getUseLocalBackend());
        aSwitch.setOnClickListener(i -> {
            if(Settings.getInstance().getUseLocalBackend()) {
                Settings.getInstance().setUseLocalBackend(false);
            }
            else {
                Settings.getInstance().setUseLocalBackend(true);
            }
        } );


        aSwitch = v.findViewById(R.id.cloud_db_settings_switch);
        aSwitch.setChecked(Settings.getInstance().getUseCloudBackend());
        aSwitch.setOnClickListener(i -> {
            if(Settings.getInstance().getUseCloudBackend()) {
                Settings.getInstance().setUseCloudBackend(false);
            }
            else {
                Settings.getInstance().setUseCloudBackend(true);
            }
        } );

        aSwitch = v.findViewById(R.id.start_in_ar_view_settings_switch);
        aSwitch.setChecked(Settings.getInstance().getStartInARView());
        aSwitch.setOnClickListener(i -> {
            if(Settings.getInstance().getStartInARView()) {
                Settings.getInstance().setStartInARView(false);
            }
            else {
                Settings.getInstance().setStartInARView(true);
            }
        } );

        return v;
    }

    /*
     * You guys can use this as a template for loadSettings in other fragments.
     * Two ways are shown. if you need the item for something after use the first, otherwise the second is less verbose
     * The variable passed in is the same as the MenuItem item, it is what was clicked.
     * The return boolean is just saying you handled the onclick.
     * If you return false the MainActivity will close the drawer, otherwise you will need to close it as shown below.
     */
    public void loadSettingsUI(Menu menu, DrawerLayout drawer, Context context){
        menu.removeGroup(R.id.settings);

        MenuItem item = menu.add(R.id.settings, Menu.NONE, Menu.NONE, "This is a test?");
        item.setOnMenuItemClickListener((i) -> {
            i.setTitle("This was clicked!");
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Save Location")
            .setOnMenuItemClickListener((i) ->{
                AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());

                View view = ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(R.layout.fragment_save, null);

                popDialog.setView(view);

                popDialog.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                });

                popDialog.create();
                popDialog.show();
                return false;
            });
    }
}
