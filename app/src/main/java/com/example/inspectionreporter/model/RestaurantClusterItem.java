package com.example.inspectionreporter.model;

/**
 * Class that allows objects to be added to the
 * cluster manager (thus letting them be clustered).
 */

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class RestaurantClusterItem implements ClusterItem {
    private LatLng position;
    private BitmapDescriptor icon;
    private String address;
    private String hazard;
    private String resName;

    public RestaurantClusterItem(LatLng position, BitmapDescriptor icon, String resName, String address, String hazard) {
        this.position = position;
        this.icon = icon;
        this.address = address;
        this.hazard = hazard;
        this.resName = resName;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public String getAddress() {
        return address;
    }

    public String getHazard() {
        return hazard;
    }

    public String getRestaurantName() {
        return resName;
    }
}
