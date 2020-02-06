package com.senior.arexplorer.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Settings {
    private static Settings instance;
    private static Context applicationContext;

    private Integer drawDistance;
    private Integer compassFOV;

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
        synchronized (this.drawDistance) {
            this.drawDistance = drawDistance;
        }

    }

    public int getCompassFOV() {
        return this.compassFOV;
    }

    public void setCompassFOV(int compassFOV) {
        synchronized (this.compassFOV) {
            this.compassFOV = compassFOV;

            SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Pref_AR_Compass_FOV","" + compassFOV);
            editor.commit();
        }

    }
}
