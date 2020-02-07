package com.senior.arexplorer.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Field;

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

    public static synchronized void init(Context context) {
        Log.d("location manager", "here is initialized.");
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

    private static synchronized Settings getInstanceSynced() {
        if (Settings.instance == null) Settings.instance = new Settings();
        return Settings.instance;
    }

    private Settings() {
        SharedPreferences sharedPreferences = Settings.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE);

        this.compassFOV  = Integer.valueOf(sharedPreferences.getString("Pref_AR_Compass_FOV","180"));
        this.drawDistance  = Integer.valueOf(sharedPreferences.getString("Pref_AR_Compass_DrawDistance","1000"));

    }

    public int getDrawDistance() {
        return this.drawDistance;
    }

    public void setDrawDistance(int drawDistance) {
        set("drawDistance", new Integer(drawDistance),"Pref_AR_Compass_DrawDistance");
    }

    public int getCompassFOV() {
        return this.compassFOV;
    }

    public void setCompassFOV(int compassFOV) {
        this.set("compassFOV", new Integer(compassFOV), "Pref_AR_Compass_FOV");
    }

    public boolean getShowBuildings() {
        return this.showBuildings;
    }

    public void setShowBuildings(boolean showBuildings) {
        this.set("showBuildings", new Boolean(showBuildings), "Pref_Map_Show_Building");
    }

    public boolean getUseGoogleBackend() {
        return this.useGoogleBackend;
    }

    public void setUseGoogleBackend(boolean useGoogleBackend) {
        this.set("useGoogleBackend", new Boolean(useGoogleBackend), "Pref_Backend_Use_Google_Backend");
    }

    public boolean getUseLocalBackend() {
        return this.useLocalBackend;
    }

    public void setUseLocalBackend(boolean useLocalBackend) {
        this.set("useLocalBackend", new Boolean(useLocalBackend), "Pref_Backend_Use_Local_Backend");
    }

    public boolean getUseOneBusAwayBackend() {
        return this.useOneBusAwayBackend;
    }

    public void setUseOneBusAwayBackend(boolean useOneBusAwayBackend) {
        this.set("useOneBusAwayBackend", new Boolean(useOneBusAwayBackend), "Pref_Backend_Use_One_Bus_Away_Backend");
    }

    public boolean getUseCloudBackend() {
        return this.useCloudBackend;
    }

    public void setCloudBackend(boolean useCloudBackend) {
        this.set("useCloudBackend", new Boolean(useCloudBackend), "Pref_Backend_Cloud_Backend");
    }

    public boolean getStartInARView() {
        return this.useCloudBackend;
    }

    public void setStartInARView(boolean startInARView) {
        this.set("startInARView", new Boolean(startInARView), "Pref_Start_In_AR_View");
    }

    //NEVER DO THIS
    private void set(String local, Object toSave, String key) {
        synchronized (local) {
            try {
                Class aClass = this.getClass();
                Field field = aClass.getDeclaredField(local);
                field.set(this, toSave);

                SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key, "" + toSave);
                editor.commit();
            }
            catch (Exception ex) {
                Log.e("Settings","THIS IS WHY YOU SHOULD NEVER DO THIS!!!");
                ex.printStackTrace();
            }
        }
    }
}
