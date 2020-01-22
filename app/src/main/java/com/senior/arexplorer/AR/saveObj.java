package com.senior.arexplorer.AR;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class saveObj implements Serializable {

    private String userID,locationName,locationDesc;
    private double locationLatitude,locationLongitude,locationElevation;
    private boolean priv;
    private byte[] blob;

    public saveObj(String userID, String locationName, String locationDesc, double locationLatitude, double locationLongitude, double locationElevation, boolean priv) {
        this.userID = userID;
        this.locationName = locationName;
        this.locationDesc = locationDesc;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.locationElevation = locationElevation;
        this.priv = priv;
        this.blob = null;
    }

    public void setBLOB(Bitmap bm){
        if(bm != null){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG,100,bos);
            this.blob = bos.toByteArray();
        }

    }


    public String getUserID() {
        return userID;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLocationDesc() {
        return locationDesc;
    }

    public double getLocationLatitude() {
        return locationLatitude;
    }

    public double getLocationLongitude() {
        return locationLongitude;
    }

    public double getLocationElevation() {
        return locationElevation;
    }

    public boolean isPriv() {
        return priv;
    }

    public byte[] getBlob() {
        return blob;
    }
}
