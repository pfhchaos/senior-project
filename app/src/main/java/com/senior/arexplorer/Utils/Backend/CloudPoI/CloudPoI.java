package com.senior.arexplorer.Utils.Backend.CloudPoI;

import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.saveObj;

public class CloudPoI extends PoI {

    public CloudPoI(saveObj s){
        super();
        setName(s.getLocationName());
        setDescription(s.getLocationDesc());
        setElevation(s.getLocationElevation());
        setLatitude(s.getLocationLatitude());
        setLongitude(s.getLocationLongitude());


    }
}
