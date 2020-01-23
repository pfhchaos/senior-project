package com.senior.arexplorer.Utils.Places;
import com.senior.arexplorer.AR.saveObj;


public class LocalPoI extends PoI {
    public LocalPoI(saveObj s) {
        super();
        setName(s.getLocationName());
        setDescription(s.getLocationDesc());
        setElevation(s.getLocationElevation());
        setLatitude(s.getLocationLatitude());
        setLongitude(s.getLocationLongitude());
    }
}
