package com.senior.arexplorer;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.arexplorer.Utils.Places.GooglePlaceFetcher;
import com.senior.arexplorer.Utils.Places.Here;
import com.senior.arexplorer.Utils.IFragSettings;
import com.senior.arexplorer.Utils.Places.Place;
import com.senior.arexplorer.Utils.Places.PlaceFetcherHandler;

import java.util.Collection;


public class MapFragment extends Fragment implements OnMapReadyCallback, IFragSettings, PlaceFetcherHandler {

    private GoogleMap googleMap;
    private MapView mapView;
    private GooglePlaceFetcher backend;
    private float zoom = 18;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        return v;
    }

    @Override
    public void onStart() {
        Here here = Here.getHere();
        if (here == null) {
            Toast.makeText(getActivity(), "here is null. this should not happen", Toast.LENGTH_SHORT).show();
        }
        this.backend = GooglePlaceFetcher.getGooglePlaceFetcher(getActivity(), here);
        this.backend.addHandler(this);

        mapView.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        Location location = null;
        googleMap = gMap;
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        while ((location = Here.getHere().getLocation()) == null) {
            try {
                Log.d("map fragment", "location is null. sleeping");
                Thread.sleep(10);
            } catch (Exception ex) {
                Log.e("map fragment", "sleep was interrupted. i blame you");
            }
        }
        moveToLocation(location);
        backend.fetchData(getActivity());
    }

    private void moveToLocation(Location location) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), zoom);
        this.googleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void loadSettings(Menu menu, DrawerLayout drawer) {
        menu.removeGroup(R.id.settings);

        menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Save Location")
                .setOnMenuItemClickListener((i) ->{
                    AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());

                    View view = ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(R.layout.fragment_save, null);

                    //From here we can grab the views with view.getViewByID and assign on clicks to the popup

                    popDialog.setView(view);

                    popDialog.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                    });

                    popDialog.create();
                    popDialog.show();
                    return false;
                });
    }

    @Override
    public void placeFetchComplete() {
        Collection<Place> places = backend.getPlaces();
        Location here = Here.getHere().getLocation();

        Log.d("mapFragment","entered callback from place fetcher");

        for (Place p: places) {
            googleMap.addMarker(new MarkerOptions().position(p.getLatLng()));
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(here.getLatitude(),here.getLongitude()), zoom));
    }
}
