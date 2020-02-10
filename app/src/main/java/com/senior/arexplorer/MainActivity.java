package com.senior.arexplorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.Backend.LocalPoI.LocalDB.LocalDB;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.IFragSettings;
import com.senior.arexplorer.Utils.Settings;
import com.senior.arexplorer.Utils.WebRequester;

//import com.amazonaws.mobile.config.AWSConfiguration;
//import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 10;

    DrawerLayout drawer;
    public SQLiteDatabase db;
    private Cursor favoritesCursor;
    public Cursor cursor;

    private Backend backend;
    private CompassAssistant compassAssistant;
    private WebRequester webRequester;
    private AWSAppSyncClient mAWSAppSyncClient;
    private Settings settings;
    private LocalDB localDB;
    private CloudDB cloudDB;

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
        Here.getInstance().cleanUp();
        Backend.getInstance().cleanUp();
        //TODO: this seems wrong
        CompassAssistant.getInstance().onStop();
        CompassAssistant.getInstance().cleanUp();
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

    public String getName(){
        return "From Activity";
    }

    private void initSingletons() {

        LocalDB.init(this);
        this.localDB = LocalDB.getInstance();
        CloudDB.init(this);
        this.cloudDB=CloudDB.getInstance();

        Settings.init(this);
        this.settings = Settings.getInstance();

        WebRequester.init(getApplicationContext());
        this.webRequester = WebRequester.getInstance();

        Backend.init(this);
        this.backend = Backend.getInstance();

        this.compassAssistant = CompassAssistant.getInstance(this);
        compassAssistant.onStart();
    }
}
