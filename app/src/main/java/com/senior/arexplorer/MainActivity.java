package com.senior.arexplorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.google.android.material.navigation.NavigationView;
import com.senior.arexplorer.AR.ARFragment;
import com.senior.arexplorer.Utils.Backend.Backend;
import com.senior.arexplorer.Utils.Backend.CloudPoI.AWS.CloudDB;

import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.IconProvider;
import com.senior.arexplorer.Utils.Settings;
import com.senior.arexplorer.Utils.WebRequester;

//import com.amazonaws.mobile.config.AWSConfiguration;
//import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 10;

    DrawerLayout drawer;

    private Backend backend;
    private CompassAssistant compassAssistant;
    private WebRequester webRequester;
    private AWSAppSyncClient mAWSAppSyncClient;
    private Settings settings;
    private IconProvider iconProvider;

    private String fragmentName;

    //lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ActivityLifecycle","onCreate");
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null){

            Settings.init(this);
            if (Settings.getInstance().getStartInARView()) {
                this.fragmentName = "ARFragment";
            }
            else {
                this.fragmentName = "MapFragment";
            }
        }

        checkPermissions();
        if (savedInstanceState == null) {
            drawer.openDrawer(GravityCompat.START);
        }
        //
        if(savedInstanceState!=null){
            // data to retrieve from previous state

        }

    } // onCreate end

    @Override
    public void onResume() {
       super.onResume();
       Log.d("ActivityLifecycle","onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("ActivityLifecycle","onStart");
        initSingletons();
        CompassAssistant.getInstance().onStart();

        NavigationView navView = findViewById(R.id.nav_view);
        if (this.fragmentName != null) {
            switch (this.fragmentName) {
                case "ARFragment":
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ARFragment()).commit();
                    navView.setCheckedItem(R.id.nav_ar);
                    //onNavigationItemSelected(navView.findViewById(R.id.nav_ar));
                    break;
                case "MapFragment":
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
                    navView.setCheckedItem(R.id.nav_map);
                    //onNavigationItemSelected(navView.findViewById(R.id.nav_map));
                    break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("ActivityLifecycle","onPause");
        //TODO: pause location updates here
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.d("ActivityLifecycle","onSaveInstanceState");
        //savedInstanceState.putInt("key", value);
        // we have to find what need to save
        //
    }

    @Override
    public void onStop() {
        Log.d("ActivityLifecycle","onStop");
        super.onStop();
    }

    @Override
    public void onRestart(){
        Log.v("ActivityLifecycle","onRestart");
        super.onRestart();
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
        Fragment tempFrag = null;
        Menu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu();

        switch(menuItem.getItemId()){

            case R.id.nav_map :
                tempFrag =   new MapFragment();
                break;

            case R.id.nav_ar :
                tempFrag = new ARFragment();
                break;

            case R.id.nav_settings :
                tempFrag =  new SettingsFragment();
                break;

            case R.id.nav_save :
                tempFrag = null;
                SaveLocationFragment saveLocationFragment = new SaveLocationFragment();
                saveLocationFragment.show(getSupportFragmentManager(),"Save Fragment");
                //tempFrag = new SaveLocationFragment();
                break;

            case R.id.nav_filter :
                tempFrag = null;
                FilterFragment filterFragment = new FilterFragment();
                filterFragment.show(getSupportFragmentManager(),"Filter Fragment");
                break;

            default :
                break;
        }
        if(tempFrag != null){
            //tempFrag.loadSettingsUI(menu, drawer, this);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, (Fragment)tempFrag).commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initSingletons() {
        CloudDB.init(this);

        Settings.init(this);
        this.settings = Settings.getInstance();

        WebRequester.init(getApplicationContext());
        this.webRequester = WebRequester.getInstance();

        Backend.init(this);
        this.backend = Backend.getInstance();

        this.compassAssistant = CompassAssistant.getInstance(this);
        compassAssistant.onStart();

        IconProvider.init(this);
        this.iconProvider = IconProvider.getInstance();


    }
}
