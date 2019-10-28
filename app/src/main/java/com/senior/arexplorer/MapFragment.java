package com.senior.arexplorer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapView mapView;
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final String ZOOM_KEY = "zoom_key";
    private static final String MAP_OPTIONS_LIST_KEY = "map_options_list_key";
    private float zoom = 10;
    ArrayList<MarkerOptions> markerOptionsList;
    ArrayList<Marker> markerList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
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
        googleMap = gMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        //googleMap.addMarker(new MarkerOptions().position(/*some location*/));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(/*some location*/, 10));
        moveToLocation(getLocation());

    }
/*
    private void initMap() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            return;
        }
        this.mMap.setMyLocationEnabled(true);
        this.mMap.getUiSettings().setZoomControlsEnabled(true);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Location location = getLocation() ;
                if (location!=null)
                    moveToLocation(location) ;
                else
                    Toast.makeText(getActivity().getApplicationContext(),
                            "No Location, try the zoom to button", Toast.LENGTH_SHORT).show();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 200);
        for (MarkerOptions markerOptions : markerOptionsList) markerList.add(mMap.addMarker(markerOptions));
    }
    */
    private void moveToLocation(Location location) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), zoom);
        this.googleMap.moveCamera(cameraUpdate);
    }

    private String getProvider(LocationManager locMgr, int accuracy, String
            defProvider) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(accuracy);
        // get best provider regardless of whether it is enabled
        String providerName = locMgr.getBestProvider(criteria, false);
        if (providerName == null)
            providerName = defProvider;
        // if neither that nor the default are enabled, prompt user to change settings
        if (!locMgr.isProviderEnabled(providerName)) {
            View parent = getActivity().findViewById(R.id.mapLayout);
            Toast.makeText(getActivity(),
                    "Location Provider Not Enabled: Goto Settings?", Toast.LENGTH_SHORT)
                    .show();
        }

        return providerName;
    }

    private Location getLocation() {
        LocationManager locMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        // location = this.getLocation();

        String provider;
        if (location == null) {
            provider = getProvider(locMgr, Criteria.ACCURACY_FINE, locMgr.GPS_PROVIDER);
            try {
                location = locMgr.getLastKnownLocation(provider);
            } catch(SecurityException e) {
                Log.e("Error", "Security Exception: " + e.getMessage());
            }
        }
        if (location == null) {
            provider = getProvider(locMgr, Criteria.ACCURACY_COARSE, locMgr.NETWORK_PROVIDER);
            try {
                location = locMgr.getLastKnownLocation(provider);
            } catch(SecurityException e) {
                Log.e("Error", "Security Exception: " + e.getMessage());
            }
        }
        if (location == null) Toast.makeText(getActivity(), "Cannot get current location.", Toast.LENGTH_SHORT).show();

        return location;
    }
    /*

    @Override
    public void onRequestPermissionsResult(int rqst, String perms[], int[] res) {
        if (rqst == PERMISSION_REQUEST_LOCATION) {
            // if the request is cancelled, the result arrays are empty.
            if (res.length>0 && res[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted! We can now init the map
                initMap() ;
            } else {
                Toast.makeText(getActivity(), "This app is useless without loc permissions",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    */
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners
     * or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    /*
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
    }
    */
/*
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putFloat(ZOOM_KEY, mMap.getCameraPosition().zoom);
        savedInstanceState.putSerializable(MAP_OPTIONS_LIST_KEY, markerOptionsList);
        super.onSaveInstanceState(savedInstanceState);
    }
    */
}
