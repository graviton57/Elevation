package com.havrylyuk.elevation.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Igor Havrylyuk on 24.03.2017.
 */

public class ElevationEvent {

    private String elevation;
    private LatLng latLng;
    private String location;

    public ElevationEvent(LatLng latLng, String elevation, String location) {
        this.elevation = elevation;
        this.latLng = latLng;
        this.location= location;
    }

    public String getElevation() {
        return elevation;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getLocation() {
        return location;
    }
}
