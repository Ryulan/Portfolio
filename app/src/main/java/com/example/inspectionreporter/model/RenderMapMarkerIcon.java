package com.example.inspectionreporter.model;

/**
 * Class used to enable the clustering of icons, while giving them
 * appropriate icons
 */

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class RenderMapMarkerIcon extends DefaultClusterRenderer<RestaurantClusterItem> {

    public RenderMapMarkerIcon(Context context, GoogleMap map, ClusterManager<RestaurantClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(RestaurantClusterItem item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(item.getIcon());
    }
}
