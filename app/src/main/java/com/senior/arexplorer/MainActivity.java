package com.senior.arexplorer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.AdapterView;
import android.view.View;
import android.widget.ListView;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.navigation.NavigationView;
import com.senior.arexplorer.AR.ARFragment;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 10;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final long LOCATION_UPDATE_INTERVAL = 5000;
    private static final long LOCATION_FASTEST_INTERVAL = 5000;

    DrawerLayout drawer;
    public SQLiteDatabase db;
    private Cursor favoritesCursor;
    public Cursor cursor;
    SQLiteOpenHelper databaseHelper = new CreateDatabase(this);

    private Here here;
    private GoogleApiClient googleApiClient;
    private GooglePlaceFetcher googlePlaceFetcher;


    //lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("main activity lifecycle","onCreate");
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navView.setCheckedItem(R.id.nav_home);
        }

        checkPermissions();
        if (savedInstanceState == null) {
            drawer.openDrawer(GravityCompat.START);
        }
        //
        if(savedInstanceState!=null){
            // data to retrieve from previous state

        }

       // LoadData(); //create database and load

       // loadPlace();

        this.here = here.getHere();
        this.googlePlaceFetcher = GooglePlaceFetcher.getGooglePlaceFetcher(this, this.here);

        this.googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

    }

    @Override
    public void onResume() {
       super.onResume();
        Log.v("main activity lifecycle","onResume");

       if (!checkPlayServices()) {
           Toast.makeText(this, "You need to install Google Play Services to use the App properly", Toast.LENGTH_SHORT);
       }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v("main activity lifecycle","onStart");

        if (this.googleApiClient != null) {
            this.googleApiClient.connect();
        }
        else {
            Log.e("googleApiClient", "googleApiClient is null!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("main activity lifecycle","onPause");
        //TODO: pause location updates here

        /*
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
          LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
          googleApiClient.disconnect();
        }
         */
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.v("main activity lifecycle","onSaveInstanceState");
        //savedInstanceState.putInt("key", value);
        // we have to find what need to save
        //
    }

    @Override
    public void onStop() {
        Log.v("main activity lifecycle","onStop");
        this.here.cleanUp();
        this.googlePlaceFetcher.cleanUp();
        super.onStop();
    }

    //permission methods
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int rqst, String perms[], int[] res) {
        switch(rqst){
            case PERMISSION_REQUEST_CAMERA :
            case PERMISSION_REQUEST_LOCATION :
                // if the request is cancelled, the result arrays are empty.
                if (res.length>0 && res[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted! We can now init the map
                } else {
                    Toast.makeText(this, "This app is useless without loc and camera permissions",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    System.exit(0);
                }
                break;
        }

    }

    //navigation methods
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        IFragSettings tempFrag = null;
        Menu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu();

        switch(menuItem.getItemId()){
            case R.id.nav_home :
                tempFrag = new HomeFragment();
                break;

            case R.id.nav_map :
                tempFrag =   new MapFragment();
                break;

            case R.id.nav_ar :
                tempFrag = new ARFragment();
                break;
            case R.id.nav_login :
                tempFrag =  new DataBaseFragment();
                break;

            case R.id.nav_settings :
                tempFrag =  new SettingsFragment();
                break;

            case R.id.nav_save :
                tempFrag = new SaveLocationFragment();
                break;

            default :
                break;
        }
        if(tempFrag != null){
            tempFrag.loadSettings(menu, drawer);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, (Fragment)tempFrag).commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //database methods
    private void LoadData(){
        // for database

        try{
            SQLiteDatabase db =databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("USER",
                    new String[]{"fName", "lName","email", "Password"},
                    "_id = ?",null,null,null,null);

            // move to the first record in the cursor
            if(cursor.moveToFirst()){
                String fNameText = cursor.getString(0);
                String lNameText = cursor.getString(1);
                String emailText = cursor.getString(2);
                String passwordText = cursor.getString(3);
                /*
                //display data
                TextView fName =(TextView)findViewById(R.id.textViewFname);
                fName.setText(fNameText);
                TextView lName =(TextView)findViewById(R.id.textViewLname);
                lName.setText(lNameText);
                TextView email =(TextView)findViewById(R.id.textViewEmail);
                email.setText(emailText);
                TextView pass =(TextView)findViewById(R.id.textViewPassword);
                pass.setText(passwordText);
                */

            }
        }catch(SQLiteException e){
            Toast toast = Toast.makeText(this, "Database unavailable",Toast.LENGTH_SHORT);
            toast.show();

        }

        //database end

    }

    public void loadPlace() {

        // SQLiteOpenHelper starbuzzDatabaseHelper = new StarbuzzDatabaseHelper(this);
        ListView listUser = (ListView) findViewById(R.id.list_users);
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("USER",
                    new String[]{"_id", "lName"},
                    null, null, null, null, null);
            SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[]{"lName"},
                    new int[]{android.R.id.text1},
                    0);
            listUser.setAdapter(listAdapter);
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //google location services
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                //finish();

            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("googleApiClient", "Connection to Google Location Services connected!");
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        Log.v("googleApiClient","current location is " + location);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (LocationListener)here, null);
    }

<<<<<<< HEAD
    public String getName(){
        return "From Activity";
    }


=======
    @Override
    public void onConnectionSuspended(int i) {
        Log.v("googleApiClient", "Connection to Google Location Services suspended!");
    }
>>>>>>> 201cabbc10c0bd3bc710dd322a69ad4091494343

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("googleApiClient", "Connection to Google Location Services failed!");
    }
}
