package com.senior.arexplorer;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Place implements Serializable {
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private Double elevation;

    private Collection<String> types;

    private long timeRetrieved;

    public Place() {
        this.name = "";
        this.description = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.elevation = 0.0;
        this.types = new ArrayList<>();

        this.timeRetrieved = 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setElevation(Double elevation) {
       this.elevation = elevation;
    }

    //double distanceFrom(Location cur) {
    //    return
    //`}

    public void addType(String type) {
        this.types.add(type);
    }

    public void setTimeRetrieved(long timeRetrieved) {
        this.timeRetrieved = timeRetrieved;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getElevation() {
        return this.elevation;
    }

    public Collection<String> getTypes() {
        //TODO: clone types before returning it
        return this.types;
    }

    public long getTimeRetrieved() {
        return this.timeRetrieved;
    }

    public String toString() {
        String ret = "";
        ret += "name: " + this.name + "\n";
        ret += "description: " + this.description + "\n";
        ret += "latitude: " + this.latitude + "\n";
        ret += "longitude: " + this.longitude + "\n";
        ret += "elevation: " + this.elevation + "\n";

        return ret;
    }
}
