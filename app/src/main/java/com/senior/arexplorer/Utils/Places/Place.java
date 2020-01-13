package com.senior.arexplorer.Utils.Places;

import android.content.Context;
import android.graphics.Rect;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import static androidx.camera.core.CameraX.getContext;

public class Place implements Serializable {
    private String name;
    private String description;
    private Location loc;
    private Rect compassRect;

    private Collection<String> types;

    public Place() { // need to modify for  Arguments based
        this(new Location("dummy"));
    }

    public Place(Location curr){
        this.loc = new Location(curr);
        this.loc.setLatitude(0);
        this.loc.setLongitude(0);
        this.loc.setAltitude(0);

        this.name = "";
        this.description = "";
        this.types = new ArrayList<>();
        this.compassRect = new Rect();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLatitude(Double latitude) {
        this.loc.setLatitude(latitude);
    }

    public void setLongitude(Double longitude) {
        this.loc.setLongitude(longitude);
    }

    public void setElevation(Double elevation) {
       this.loc.setAltitude(elevation);
    }

    double distanceTo(Location cur) {
        return loc.distanceTo(cur);
    }

    public void addType(String type) {
        this.types.add(type);
    }

    public void removeType(String type) {
        this.types.remove(type);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public double getLatitude() {
        return loc.getLatitude();
    }

    public double getLongitude() {
        return loc.getLongitude();
    }

    public Location getLocation() {
        return loc;
    }

    public LatLng getLatLng() {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public double getElevation() {
        return loc.getAltitude();
    }

    public Collection<String> getTypes() {
        //TODO: clone types before returning it
        return this.types;
    }

    public Rect getCompassRect(){ return compassRect; }

    public String toString() {
        String ret = "";
        ret += "name: " + this.name + "\n";
        ret += "description: " + this.description + "\n";
        ret += "latitude: " + loc.getLatitude() + "\n";
        ret += "longitude: " + loc.getLongitude() + "\n";
        ret += "elevation: " + loc.getAltitude() + "\n";

        return ret;
    }

    public void onShortTouch(Context context){
        Toast.makeText(context, getName() + " is " +
                                    new DecimalFormat("#.##").format(Here.getHere().getLocation().distanceTo(getLocation())) +
                                    "m away.",
                Toast.LENGTH_SHORT).show();
    }

    //context dependent handlers
    //transient boolean onClick(Event event);
    //transient boolean onLongClick(Event event);
}
