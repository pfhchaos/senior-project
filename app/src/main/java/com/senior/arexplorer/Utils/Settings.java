package com.senior.arexplorer.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class Settings {
    private static Settings instance;
    private static Context applicationContext;

    private Integer drawDistance;
    private Integer compassFOV;

    private Boolean showBuildings;

    private Boolean useGoogleBackend;
    private Boolean useLocalBackend;
    private Boolean useOneBusAwayBackend;
    private Boolean useCloudBackend;

    private Boolean startInARView;

    private Collection<SettingListener> drawDistanceListeners;
    private Collection<SettingListener> compassFOVListeners;

    private Collection<SettingListener> showBuildingsListeners;

    private Collection<SettingListener> useGoogleBackendListeners;
    private Collection<SettingListener> useLocalBackendListeners;
    private Collection<SettingListener> useOneBusAwayBackendListeners;
    private Collection<SettingListener> useCloudBackendListeners;

    private Collection<SettingListener> startInARViewListeners;

    public static synchronized void init(Context context) {
        Log.d("Settings", "Settings is initialized.");
        if (Settings.applicationContext == null) {
            Settings.applicationContext = context.getApplicationContext();
        }
        else {
            if (Settings.applicationContext == context.getApplicationContext()) {
                Log.d("Settings", "Settings initalized twice, same context proceding");
            }
            else {
                Log.e("Settings", "Attempted to initialize Settings twice!");
                throw new Error("Shit and die!");
            }
        }
    }

    public static Settings getInstance() {
        if (applicationContext == null) {
            Log.e("Settings", "attemped to instanciate Settings without initalizing");
            throw new Error("attemped to instanciate Settings without initalizing");
        }
        if (Settings.instance == null) Settings.getInstanceSynced();
        return Settings.instance;
    }

    private static synchronized void getInstanceSynced() {
        if (Settings.instance == null) Settings.instance = new Settings();
        return;
    }

    private Settings() {
        SharedPreferences sharedPreferences = Settings.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE);

        this.compassFOV  = Integer.valueOf(sharedPreferences.getString("Pref_AR_Compass_FOV","180"));
        this.drawDistance  = Integer.valueOf(sharedPreferences.getString("Pref_AR_Compass_DrawDistance","1000"));

        this.showBuildings  = Boolean.valueOf(sharedPreferences.getString("Pref_Map_Show_Buildings","true"));

        this.useGoogleBackend  = Boolean.valueOf(sharedPreferences.getString("Pref_Backend_Use_Google","true"));
        this.useLocalBackend  = Boolean.valueOf(sharedPreferences.getString("Pref_Backend_Use_Local","true"));
        this.useOneBusAwayBackend  = Boolean.valueOf(sharedPreferences.getString("Pref_Backend_Use_One_Bus_Away","true"));
        this.useCloudBackend  = Boolean.valueOf(sharedPreferences.getString("Pref_Backend_Use_Cloud","true"));

        this.startInARView  = Boolean.valueOf(sharedPreferences.getString("Pref_Start_In_AR_View","true"));

        this.compassFOVListeners = new ArrayList<SettingListener>();
        this.drawDistanceListeners = new ArrayList<SettingListener>();

        this.showBuildingsListeners = new ArrayList<SettingListener>();

        this.useGoogleBackendListeners = new ArrayList<SettingListener>();
        this.useLocalBackendListeners = new ArrayList<SettingListener>();
        this.useOneBusAwayBackendListeners = new ArrayList<SettingListener>();
        this.useCloudBackendListeners = new ArrayList<SettingListener>();

        this.startInARViewListeners = new ArrayList<SettingListener>();
    }

    public int getDrawDistance() {
        return this.drawDistance;
    }

    public void setDrawDistance(int drawDistance) {
        set("drawDistance", new Integer(drawDistance),"Pref_AR_Compass_DrawDistance");
    }

    public void addDrawDistanceListener(SettingListener listener) {
        addListener("drawDistanceListeners",listener);
    }

    public void removeDrawDistanceListener(SettingListener listener) {
        removeListener("drawDistanceListeners", listener);
    }

    public int getCompassFOV() {
        return this.compassFOV;
    }

    public void setCompassFOV(int compassFOV) {
        this.set("compassFOV", new Integer(compassFOV), "Pref_AR_Compass_FOV");
    }

    public void addCompassFOVListener(SettingListener listener) {
        addListener("compassFOVListeners",listener);
    }

    public void removeCompassFOVListener(SettingListener listener) {
        removeListener("compassFOVListeners", listener);
    }

    public boolean getShowBuildings() {
        return this.showBuildings;
    }

    public void setShowBuildings(boolean showBuildings) {
        this.set("showBuildings", new Boolean(showBuildings), "Pref_Map_Show_Building");
    }

    public void addShowBuildingsListener(SettingListener listener) {
        addListener("showBuildingsListeners",listener);
    }

    public void removeShowBuildingsListener(SettingListener listener) {
        removeListener("showBuildingsListeners", listener);
    }

    public boolean getUseGoogleBackend() {
        return this.useGoogleBackend;
    }

    public void setUseGoogleBackend(boolean useGoogleBackend) {
        this.set("useGoogleBackend", new Boolean(useGoogleBackend), "Pref_Backend_Use_Google");
    }

    public void addUseGoogleBackendListener(SettingListener listener) {
        addListener("useGoogleBackendListeners",listener);
    }

    public void removeUseGoogleBackendListener(SettingListener listener) {
        removeListener("useGoogleBackendListeners", listener);
    }

    public boolean getUseLocalBackend() {
        return this.useLocalBackend;
    }

    public void setUseLocalBackend(boolean useLocalBackend) {
        this.set("useLocalBackend", new Boolean(useLocalBackend), "Pref_Backend_Use_Local");
    }

    public void addUseLocalBackendListener(SettingListener listener) {
        addListener("useLocalBackendListeners",listener);
    }

    public void removeUseLocalBackendListener(SettingListener listener) {
        removeListener("useLocalBackendListeners", listener);
    }

    public boolean getUseOneBusAwayBackend() {
        return this.useOneBusAwayBackend;
    }

    public void setUseOneBusAwayBackend(boolean useOneBusAwayBackend) {
        this.set("useOneBusAwayBackend", new Boolean(useOneBusAwayBackend), "Pref_Backend_Use_One_Bus_Away");
    }

    public void addUseOneBusAwayBackendListener(SettingListener listener) {
        addListener("useOneBusAwayBackendListeners",listener);
    }

    public void removeUseOneBusAwayBackendListener(SettingListener listener) {
        removeListener("useOneBusAwayBackendListeners", listener);
    }

    public boolean getUseCloudBackend() {
        return this.useCloudBackend;
    }

    public void setUseCloudBackend(boolean useCloudBackend) {
        this.set("useCloudBackend", new Boolean(useCloudBackend), "Pref_Backend_Cloud");
    }

    public void addUseCloudBackendListener(SettingListener listener) {
        addListener("useCloudBackendListeners",listener);
    }

    public void removeUseCloudBackendListener(SettingListener listener) {
        removeListener("useCloudBackendListeners", listener);
    }

    public boolean getStartInARView() {
        return this.startInARView;
    }

    public void setStartInARView(boolean startInARView) {
        this.set("startInARView", new Boolean(startInARView), "Pref_Start_In_AR_View");
    }

    public void addStartInARViewListener(SettingListener listener) {
        addListener("startInARViewListeners",listener);
    }

    public void removeStartInARViewListener(SettingListener listener) {
        removeListener("startInARViewListeners", listener);
    }

    //NEVER DO THIS
    private void set(String local, Object toSave, String key) {
        Class aClass = this.getClass();
        Field field;
        Object syncOn;

        try {
            field = aClass.getDeclaredField(local);
            syncOn = field.get(this);
            Log.d("Settings", "local: " + local);
            Log.d("Settings", "syncOn: " + syncOn.toString());

            synchronized (syncOn) {
                field.set(this, toSave);

                SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key, "" + toSave);
                editor.commit();
            }

            Collection<SettingListener> listeners = (Collection<SettingListener>) aClass.getDeclaredField(local + "Listeners").get(this);
            for (SettingListener listener : listeners) listener.onSettingChange();
        }
        catch (Exception ex) {
            Log.e("Settings","THIS IS WHY YOU SHOULD NEVER DO THIS!!!");
            Log.e("Settings",ex.toString());
            Log.e("Settings",ex.getStackTrace().toString());
            ex.printStackTrace();
        }
    }

    private void addListener(String local, SettingListener listener) {
        Class aClass = this.getClass();
        Field field;
        Object syncOn;

        try {
            field = aClass.getDeclaredField(local);
            syncOn = field.get(this);
            Log.d("Settings", "syncOn: " + syncOn.toString());

            synchronized (syncOn) {
                Log.d("Settings", "addListener: " + listener.toString() + " from " + local);
                Collection<SettingListener> addTo = (Collection<SettingListener>) syncOn;
                addTo.add(listener);
            }
        }
        catch (Exception ex) {
            Log.e("Settings","THIS IS WHY YOU SHOULD NEVER DO THIS!!!");
            Log.e("Settings",ex.toString());
            Log.e("Settings",ex.getStackTrace().toString());
            ex.printStackTrace();
        }
    }

    private void removeListener(String local, SettingListener listener) {
        Class aClass = this.getClass();
        Field field;
        Object syncOn;

        try {
            field = aClass.getDeclaredField(local);
            syncOn = field.get(this);
            Log.d("Settings", "syncOn: " + syncOn.toString());

            synchronized (syncOn) {
                Log.d("Settings", "removeListener: " + listener.toString() + " from " + local);
                Collection<SettingListener> removeFrom = (Collection<SettingListener>) syncOn;
                removeFrom.remove(listener);
            }
        }
        catch (Exception ex) {
            Log.e("Settings","THIS IS WHY YOU SHOULD NEVER DO THIS!!!");
            Log.e("Settings",ex.toString());
            Log.e("Settings",ex.getStackTrace().toString());
            ex.printStackTrace();
        }
    }
}
