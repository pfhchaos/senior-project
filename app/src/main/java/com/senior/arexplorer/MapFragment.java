package com.senior.arexplorer;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.Backend.Backend;
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.FragmentWithSettings;
import com.senior.arexplorer.Utils.Backend.Here.HereListener;
import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.PoIFetcherHandler;
import com.senior.arexplorer.Utils.IconListener;
import com.senior.arexplorer.Utils.IconProvider;
import com.senior.arexplorer.Utils.SettingListener;
import com.senior.arexplorer.Utils.Settings;

import java.util.Collection;

public class MapFragment extends FragmentWithSettings implements OnMapReadyCallback, PoIFetcherHandler, HereListener, CompassAssistant.CompassAssistantListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, IconListener, SettingListener {

    private GoogleMap googleMap;
    private MapView mapView;
    private Marker youAreHere;

    private float zoom = 18;
    private float tilt = 60;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("MapFragment", "onCreateView");
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

        Here.getInstance().addListener(this);
        Backend.getInstance().addHandler(this);
        CompassAssistant.getInstance(getContext()).addCompassListener(this);
        CompassAssistant.getInstance(getContext()).addPitchListener(this);
        Settings.getInstance().addShowBuildingsListener(this);
        IconProvider.getInstance().addIconListener(this);
        IconProvider.getInstance().generateIcon("map_you_are_here_icon", ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star,null));

        return v;
    }

    @Override
    public void onStart() {
        Log.d("MapFragment", "onStart");
        mapView.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("MapFragment", "onResume");
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("MapFragment", "onPause");
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        Log.d("MapFragment", "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MapFragment", "onDestroy");
        Here.getInstance().removeListener(this);
        Backend.getInstance().removeHandler(this);
        CompassAssistant.getInstance(getContext()).removeCompassListener(this);
        IconProvider.getInstance().removeIconListener(this);
        Settings.getInstance().removeShowBuildingsListener(this);
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.d("MapFragment", "onLowMemory");
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        Log.d("MapFragment", "onMapReady");
        googleMap = gMap;

        if (Settings.getInstance().getShowBuildings()) {
            googleMap.setBuildingsEnabled(true);
        }
        else {
            googleMap.setBuildingsEnabled(false);
        }

        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(false);

        CameraPosition newPosition = new CameraPosition(new LatLng(0, 0), zoom, tilt, 0);
        this.googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));

        if (Backend.getInstance().isReady()) {
            placeMarkers();
        }

        if (Here.getInstance().isReady()) {
            changeLocation(Here.getInstance().getLocation());
        }

        changeHeading(CompassAssistant.getInstance().getLastHeading());

        this.googleMap.setOnMarkerClickListener(this);
        this.googleMap.setOnMarkerDragListener(this);
    }

    private void placeMarkers() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Collection<PoI> pois = Backend.getInstance().getPoIs();
                googleMap.clear();

                for (PoI poi: pois) {
                    createMarker(poi);
                }
            }
        });
    }

    private void changeLocation(Location location) {
        Log.v("changeLocation","loc changed to "+location.toString());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));

        createYouAreHereMarker();
    }

    private void changeHeading(float userHeading) {
        CameraPosition currentPosition = googleMap.getCameraPosition();
        CameraPosition newPosition = new CameraPosition(currentPosition.target, currentPosition.zoom, currentPosition.tilt, userHeading);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
    }

    private void changePitch(float pitch) {
        CameraPosition currentPosition = googleMap.getCameraPosition();
        CameraPosition newPosition = new CameraPosition(currentPosition.target, currentPosition.zoom, pitch, currentPosition.bearing);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
    }

    private void createMarker(PoI poi) {
        MarkerOptions newMarkerOptions = new MarkerOptions();
        newMarkerOptions.position(poi.getLatLng());
        newMarkerOptions.title(poi.getName());
        newMarkerOptions.icon(poi.getMapIcon());
        Marker newMarker = this.googleMap.addMarker(newMarkerOptions);
        newMarker.setTag(poi);
        newMarker.setDraggable(true);
    }

    private void createYouAreHereMarker() {
        if (this.youAreHere != null) this.youAreHere.remove();
        MarkerOptions newMarkerOptions = new MarkerOptions();
        newMarkerOptions.position(Here.getInstance().getLatLng());
        newMarkerOptions.title("You Are Here.");
        BitmapDescriptor mapIcon = BitmapDescriptorFactory.fromBitmap(IconProvider.getInstance().getMapIcon("map_you_are_here_icon"));
        newMarkerOptions.icon(mapIcon);
        this.youAreHere = this.googleMap.addMarker(newMarkerOptions);
    }

    @Override
    public void loadSettingsUI(Menu menu, DrawerLayout drawer, Context context) {
        menu.removeGroup(R.id.settings);

        MenuItem showBuildingsMenuItem = menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Show Buildings");
        Switch showBuildingsSwitch = new Switch(getContext());
        showBuildingsSwitch.setChecked(Settings.getInstance().getShowBuildings());

        showBuildingsMenuItem.setActionView(showBuildingsSwitch);

        showBuildingsSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if(isChecked) {
                Settings.getInstance().setShowBuildings(true);
            }
            else {
                Settings.getInstance().setShowBuildings(false);
            }
        });
    }

    @Override
    public void placeFetchComplete() {
        Log.d("MapFragment", "placeFetchComplete");
        if (googleMap != null) {
            placeMarkers();
        }
        else {
            Log.d("MapFragment", "placeFetchComplete update failed googleMap is null");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.v("MapFragment", "onLocationChanged");
        if (googleMap != null) {
            changeLocation(location);
        }
    }

    @Override
    public void onCompassChanged(float userHeading) {
        //Log.v("MapFragment", "onCompassChanged");
        if(googleMap != null) {
            changeHeading(userHeading);
        }
    }

    @Override
    public void onPitchChanged(float pitch) {
        //Log.v("MapFragment", "onPitchChanged");
        if(googleMap != null) {
            if (pitch < 0) pitch = 0;
            pitch = 90 - pitch;
            changePitch(pitch);
        }
    }

    @Override
    public void onCompassAccuracyChange(int compassStatus) {
        Log.v("MapFragment", "onCompassAccuracyChanged");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(this.youAreHere)) {
            return true;
        }
        return ((PoI) marker.getTag()).onShortTouch(getContext());
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        if (marker.equals(this.youAreHere)) {
            return;
        }
        PoI poi = ((PoI) marker.getTag());
        marker.remove();
        poi.onLongTouch(getContext());
        createMarker(poi);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onIconsFetched() {
        Log.v("MapFragment", "onIconsFetched");
        this.placeFetchComplete();
    }

    @Override
    public void onSettingChange() {
        if(googleMap != null) {
            if (Settings.getInstance().getShowBuildings()) {
                googleMap.setBuildingsEnabled(true);
            }
            else {
                googleMap.setBuildingsEnabled(false);
            }
        }
    }
}
