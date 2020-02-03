package com.senior.arexplorer.Utils.PoI;

import com.senior.arexplorer.AR.saveObj;

public class CloudPoI extends PoI{

    public CloudPoI(saveObj s){
        super();
        setName(s.getLocationName());
        setDescription(s.getLocationDesc());
        setElevation(s.getLocationElevation());
        setLatitude(s.getLocationLatitude());
        setLongitude(s.getLocationLongitude());


    }
}
