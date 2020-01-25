package com.senior.arexplorer;

import android.app.AlertDialog;
import android.content.res.Resources;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.arexplorer.AR.SaveView;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.PoI.Backend;
import com.senior.arexplorer.Utils.PoI.Here;
import com.senior.arexplorer.Utils.IFragSettings;
import com.senior.arexplorer.Utils.PoI.HereListener;
import com.senior.arexplorer.Utils.PoI.PoI;
import com.senior.arexplorer.Utils.PoI.PoIFetcherHandler;

import java.util.Collection;


public class MapFragment extends Fragment implements OnMapReadyCallback, IFragSettings, PoIFetcherHandler, HereListener, CompassAssistant.CompassAssistantListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap googleMap;
    private MapView mapView;
    private float zoom = 18;
    private float tilt = 60;

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
        Here here = Here.getInstance();
        here.addListener(this);
        if (here == null) {
            Toast.makeText(getActivity(), "here is null. this should not happen", Toast.LENGTH_SHORT).show();
        }
        Backend.getInstance().addHandler(this);

        CompassAssistant.getInstance().addCompassListener(this);

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

        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
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
        Collection<PoI> pois = Backend.getInstance().getPoIs();
        Location here = Here.getInstance().getLocation();

        Log.d("mapFragment","entered callback from place fetcher");

        for (PoI poi: pois) {
            createMarker(poi);
        }
    }

    private void changeLocation(Location location) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
    }

    private void changeHeading(float userHeading) {
        CameraPosition currentPosition = googleMap.getCameraPosition();
        CameraPosition newPosition = new CameraPosition(currentPosition.target, currentPosition.zoom, currentPosition.tilt, userHeading);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
    }

    private void createMarker(PoI poi) {
        MarkerOptions newMarkerOptions = new MarkerOptions();
        newMarkerOptions.position(poi.getLatLng());
        newMarkerOptions.title(poi.getName());
        Marker newMarker = this.googleMap.addMarker(newMarkerOptions);
        newMarker.setTag(poi);
        newMarker.setDraggable(true);
    }

    @Override
    public void loadSettings(Menu menu, DrawerLayout drawer) {
        menu.removeGroup(R.id.settings);

        menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Save Location")
                .setOnMenuItemClickListener((i) ->{
                    AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());

                    View view = new SaveView(getContext(), null);
                            //((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            //.inflate(R.layout.fragment_save, null);

                    //From here we can grab the views with view.getViewByID and assign on clicks to the popup

                    popDialog.setView(view);

                    popDialog.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                    });

                    popDialog.create();

                    int width = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * .97);
                    int height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * .97);

                    popDialog.show().getWindow().setLayout(width,height);
                    return false;
                });
    }

    @Override
    public void placeFetchComplete() {
        if (googleMap != null) {
            placeMarkers();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (googleMap != null) {
            changeLocation(location);
        }
    }

    @Override
    public void onCompassChanged(float userHeading) {
        if(googleMap != null) {
            changeHeading(userHeading);
        }
    }

    @Override
    public void onCompassAccuracyChange(int compassStatus) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return ((PoI) marker.getTag()).onShortTouch(getContext());

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        PoI poi = ((PoI) marker.getTag());
        marker.remove();
        poi.onLongTouch(getContext());
        createMarker(poi);
        return;
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
