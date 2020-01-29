package com.senior.arexplorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.senior.arexplorer.AR.ARFragment;
import com.senior.arexplorer.AWS.CloudDB;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.IFragSettings;
import com.senior.arexplorer.Utils.LocalDB.LocalDB;
import com.senior.arexplorer.Utils.PoI.Backend;
import com.senior.arexplorer.Utils.PoI.Here;
import com.senior.arexplorer.Utils.WebRequester;

//import com.amazonaws.mobile.config.AWSConfiguration;
//import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;

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
    //SQLiteOpenHelper databaseHelper = new CreateDatabase(this);
    private LocalDB localDB;
    private CloudDB cloudDB;

    private Here here;
    private GoogleApiClient googleApiClient;
    private Backend backend;
    private CompassAssistant compassAssistant;
    private WebRequester webRequester;
    private AWSAppSyncClient mAWSAppSyncClient;



    //lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("ActivityLifecycle","onCreate");
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

        /**************************************************************
         * aws code start
         */
        /*
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                // If you are using complex objects (S3) then uncomment
                //.s3ObjectManager(new S3ObjectManagerImplementation(new AmazonS3Client(AWSMobileClient.getInstance())))
                .build();


*/

        /**************************************************************
         * aws code end
         */


        LocalDB.init(this);
        this.localDB = LocalDB.getInstance();

        // for cloudDB
        CloudDB.init(this);
        this.cloudDB=CloudDB.getInstance();

        Here.init(this);
        this.here = Here.getInstance();
        this.backend = Backend.getInstance();

        this.googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        this.compassAssistant = CompassAssistant.getInstance(this);
        WebRequester.init(getApplicationContext());
        this.webRequester = WebRequester.getInstance();
        compassAssistant.onStart();
    } // onCreate end


    @Override
    public void onResume() {
       super.onResume();
        Log.v("ActivityLifecycle","onResume");

        compassAssistant.onStart();

       if (!checkPlayServices()) {
           Toast.makeText(this, "You need to install Google Play Services to use the App properly", Toast.LENGTH_SHORT);
       }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v("ActivityLifecycle","onStart");

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
        Log.v("ActivityLifecycle","onPause");
        //TODO: pause location updates here

        compassAssistant.onStop();

        /*
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
          LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
          googleApiClient.disconnect();
        }
         */
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.v("ActivityLifecycle","onSaveInstanceState");
        //savedInstanceState.putInt("key", value);
        // we have to find what need to save
        //
    }

    @Override
    public void onStop() {
        Log.v("ActivityLifecycle","onStop");
        this.here.cleanUp();
        this.backend.cleanUp();
        this.compassAssistant.cleanUp();
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
    public void onRequestPermissionsResult(int rqst,@NonNull String[] perms,@NonNull int[] res) {
        switch(rqst){
            case PERMISSION_REQUEST_CAMERA :
            case PERMISSION_REQUEST_LOCATION :
                // if the request is cancelled, the result arrays are empty.
                if (res.length>0 && res[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted! We can now init the map
                    break;
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
            tempFrag.loadSettingsUI(menu, drawer, this);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, (Fragment)tempFrag).commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public String getName(){
        return "From Activity";
    }



    @Override
    public void onConnectionSuspended(int i) {
        Log.v("googleApiClient", "Connection to Google Location Services suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("googleApiClient", "Connection to Google Location Services failed!");
    }
}
