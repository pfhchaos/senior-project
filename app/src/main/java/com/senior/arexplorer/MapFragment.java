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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final String ZOOM_KEY = "zoom_key";
    private static final String MAP_OPTIONS_LIST_KEY = "map_options_list_key";
    private float zoom = 10;
    ArrayList<MarkerOptions> markerOptionsList;
    ArrayList<Marker> markerList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        getChildFragmentManager().beginTransaction().replace(R.id.mapView, mapFragment).commit();

        final Button mark = getActivity().findViewById(R.id.button_mark);

        if (savedInstanceState != null) {
            markerOptionsList = (ArrayList<MarkerOptions>) savedInstanceState.getSerializable(MAP_OPTIONS_LIST_KEY);
            zoom = savedInstanceState.getFloat(ZOOM_KEY);
        }
        else markerOptionsList = new ArrayList<MarkerOptions>();

        markerList = new ArrayList<Marker>();
/*
        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = getLocation();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title("Mark" + markerOptionsList.size() + 1);
                markerOptions.position(new LatLng(location.getLatitude(),location.getLongitude()));
                markerOptionsList.add(markerOptions);
                markerList.add(mMap.addMarker(markerOptions));
            }
        });

        mark.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (markerList.size() != 0) {
                    Marker lastAdded = markerList.remove(markerList.size() - 1);
                    markerOptionsList.remove(markerOptionsList.size() - 1);
                    lastAdded.remove();
                }
                return true;
            }
        });
*/
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

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
    private void moveToLocation(Location location) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), zoom);
        this.mMap.moveCamera(cameraUpdate);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setContentView(R.layout.fragment_map);

    }


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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putFloat(ZOOM_KEY, mMap.getCameraPosition().zoom);
        savedInstanceState.putSerializable(MAP_OPTIONS_LIST_KEY, markerOptionsList);
        super.onSaveInstanceState(savedInstanceState);
    }

}
