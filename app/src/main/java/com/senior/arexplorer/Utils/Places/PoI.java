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
import androidx.annotation.NonNull;

public abstract class PoI implements Serializable, Comparable<PoI> {

    private String name;
    private String description;
    private Location loc;
    private double elevation;
    private Rect compassRect;
    public boolean compassRender = false;

    private Collection<String> types;

    public PoI() {
        this.loc = new Location("dummy");

        this.name = "";
        this.description = "";
        this.types = new ArrayList<String>();
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

    public String toShortString() {
        if(Here.getInstance().isReady()){
            float dist = Here.getInstance().getLocation().distanceTo(getLocation());
            return getName() + " : " + new DecimalFormat("#.00").format(dist) + " m";
        }
        else
            return getName();
    }

    @NonNull
    @Override
    public String toString() {
        String ret = "";
        ret += "name: " + this.name + "\n";
        ret += "description: " + this.description + "\n";
        ret += "latitude: " + loc.getLatitude() + "\n";
        ret += "longitude: " + loc.getLongitude() + "\n";
        ret += "elevation: " + loc.getAltitude() + "\n";

        return ret;
    }

    public boolean onShortTouch(Context context){
        String toastText;
        if(!(Here.getInstance().isReady()))
            toastText = "Cannot get current location\nAs such cannot display distance to " + getName();
        else{
            toastText = toShortString();
        }
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
        return true;
    }

    public boolean onLongTouch(Context context){
        Toast.makeText(context, "Long touch detected but not yet implemented for this item!", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public int compareTo(PoI place) {
        Location here = Here.getInstance().getLocation();
        if(here == null)
            here = place.getLocation();
        int retInt = (int) (here.distanceTo(getLocation()) - here.distanceTo(place.getLocation()));
        if(retInt == 0){
            retInt = (here.equals(place.getLocation())) ? 0 : 1;
        }
        return retInt;
    }

    public boolean save() {
        return true;
    }
    //context dependent handlers
    //transient boolean onClick(Event event);
    //transient boolean onLongClick(Event event);
}