package com.example.inspectionreporter.ui;

/**
 * Class that displays a Google map to the user, with hazard symbols to
 * indicate clickable restaurants that show more info about their
 * inspections/hazard reports when clicked on.
 */


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.DBAdapter;
import com.example.inspectionreporter.model.InspectionData;
import com.example.inspectionreporter.model.RenderMapMarkerIcon;
import com.example.inspectionreporter.model.RestaurantData;
import com.example.inspectionreporter.model.RestaurantDataManager;
import com.example.inspectionreporter.model.RestaurantClusterItem;
import com.example.inspectionreporter.model.UpdateHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    //constants
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String POSITION_INDEX = "PositionIndex";
    private final String LOCATION_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String LOCATION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final float DEFAULT_ZOOM = 10f;
    private final LatLng DEFAULT_LOCATION = new LatLng(49.1702, -122.8008);
    private final int MARKER_SIZE = 80;
    private final String INITIAL_START_KEY = "initial start";
    private final String TAG = "MapsActivity";
    public static final int MAPS_CALLING_FUNCTION = 1;

    //class variables
    private GoogleMap map;
    private boolean locationPermissionsGranted = false;
    private FusedLocationProviderClient deviceLocationData;
    private boolean deviceSetUp = false;
    private boolean initialStart = true;
    private boolean alreadyAskForUpdate = false;
    private int restaurantIndex;
    private List<RestaurantClusterItem> RestaurantOnMapList = new ArrayList<>();
    private ClusterManager<RestaurantClusterItem> clusterManager;
    private RestaurantClusterItem itemClicked;
    private RestaurantDataManager manager;
    private DBAdapter db;

    // Shared preference for Search Option
    private final String SHARED_PREF_LAST_SEARCH = "Last Search Options";
    private final String SHARED_PREF_SEARCH = "Search";
    private final String SHARED_PREF_SEARCH_HAZARD = "Hazards";
    private final String SHARED_PREF_SEARCH_OPERATOR = "Operator";
    private final String SHARED_PREF_SEARCH_VIOLATION_COUNT = "Violations in last year";
    private final String SHARED_PREF_SEARCH_FAVOURITE = "Favourite";
    String textEntry;
    String hazardEntry;
    String operatorEntry;
    String violationCountEntry;
    String favouriteEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getLocationPermissions();
        setupSupportMapFragment();
        setupToolbar();
        setupButtonToList();

        manager = RestaurantDataManager.getInstance(MapsActivity.this);
        db = DBAdapter.getInstance(MapsActivity.this);
        alreadyAskForUpdate = manager.alreadyAskToUpdateRestaurants();
        if(!alreadyAskForUpdate) {
            //display update screen when it's been twenty hours since the
            //last update
            SetUpUpdateFragment setUpUpdateFragment = new SetUpUpdateFragment();
            setUpUpdateFragment.execute();
        }

        setupSearchButton();

    }

    public static Intent makeLaunchIntent(Context c, int position) {
        Intent intent = new Intent(c, MapsActivity.class);
        intent.putExtra(POSITION_INDEX, position);
        return intent;
    }

    /**
     * checks if app has access to location data
     * if not, ask user to give access
     */
    private void getLocationPermissions() {
        String[] permissions = new String[]{LOCATION_FINE, LOCATION_COARSE};

        if (ContextCompat.checkSelfPermission(this, LOCATION_FINE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionsGranted = true;
        }
        else {
            //pop up to get permission from user
            Log.d(TAG, "request permissions");
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setupSupportMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Maps_map);
        mapFragment.getMapAsync(this);
    }

    private void setupGetUpdateMessage() {
        FragmentManager manager = getSupportFragmentManager();
        GetUpdateFragment dialog = new GetUpdateFragment();
        dialog.show(manager, "GetUpdateDialog");
    }

    private void setupToolbar() {
        // displays toolbar back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMaps);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    private void setupButtonToList() {
        Button btnList = (Button) findViewById(R.id.Maps_btn_goToList);
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch 'AllRestaurantList' activity
                Intent intent = AllRestaurantListActivity.makeLaunchIntent(MapsActivity.this);
                startActivity(intent);
                finish();
            }
        });
    }

    //citation: https://stackoverflow.com/questions/23786033/dialogfragment-and-ondismiss
    private void setupSearchButton() {
        Button btnSearch = (Button) findViewById(R.id.Maps_btn_save_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragManager = getSupportFragmentManager();
                SearchFragment dialog = new SearchFragment();
                dialog.show(fragManager, "SearchFragment");
                fragManager.executePendingTransactions();
                fragManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        super.onFragmentViewDestroyed(fm, f);
                        if (manager.getSearchFragBtnClicked()) {
                            extractSharedPreferenceSearchOption();
                            updateMap();
                        }
                        if (manager.getResetFragBtnClicked()) {
                            extractSharedPreferenceSearchOption();
                            resetMap();
                        }
                    }
                }, false);
            }
        });
    }

    /**
     * citation:
     * https://stackoverflow.com/questions/3053761/reload-activity-in-android
     * reload activity on initial start up to show user location
     */
    private void reloadActivity() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        restaurantIndex = intent.getIntExtra(POSITION_INDEX, -1);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        setUpCluster();
        getLocationPermissions();
        setUpDeviceLocation();
        setupOnInfoWindowClick();

        // Tapping on GPS coordinates in the single restaurant activity will
        // open it's location in Google maps with an info window open
        extractDataFromIntent();
        if (restaurantIndex >= 0) {
            displayInfoWindow();
        }

        if (manager.getSearchFragBtnClicked()) {
            extractSharedPreferenceSearchOption();
            updateMap();
        }

    }

    private void extractSharedPreferenceSearchOption() {
        SharedPreferences prefs = this.getSharedPreferences(SHARED_PREF_LAST_SEARCH, Context.MODE_PRIVATE);
        textEntry = prefs.getString(SHARED_PREF_SEARCH, "empty");
        hazardEntry = prefs.getString(SHARED_PREF_SEARCH_HAZARD, "empty");
        operatorEntry = prefs.getString(SHARED_PREF_SEARCH_OPERATOR, "empty");
        violationCountEntry = prefs.getString(SHARED_PREF_SEARCH_VIOLATION_COUNT, "empty");
        favouriteEntry = prefs.getString(SHARED_PREF_SEARCH_FAVOURITE, "empty");
    }

    private void updateMap() {

        // clears the map
        clusterManager.clearItems();
        clusterManager.cluster();

        // add new markers
        Cursor c = db.getRestaurantIDs();
        if (c.moveToFirst()) {
            do {
                int i = c.getInt(DBAdapter.COL_ROWID);
                clusterManager.addItem(RestaurantOnMapList.get(i));

            } while(c.moveToNext());
        }

        clusterManager.cluster();
    }

    private void resetMap(){
        // clears the map
        clusterManager.clearItems();
        clusterManager.cluster();

        // add new markers
        Cursor c = db.getAllRows();
        if (c.moveToFirst()) {
            do {
                int i = c.getInt(DBAdapter.COL_ROWID);
                clusterManager.addItem(RestaurantOnMapList.get(i));

            } while(c.moveToNext());
        }

        clusterManager.cluster();
    }

    private void displayInfoWindow() {
        RestaurantClusterItem res = RestaurantOnMapList.get(restaurantIndex);
        LatLng currLoc = res.getPosition();
        moveCamera(currLoc, DEFAULT_ZOOM);

        Marker mark = map.addMarker(new MarkerOptions()
                .position(currLoc)
                .title(res.getRestaurantName())
                .snippet(res.getAddress() + " | " + res.getHazard())
                .alpha(0f)
                .anchor(0.5f,0.5f));

        mark.showInfoWindow();
    }

    /**
     * citation: https://stackoverflow.com/questions/30958224/android-maps-utils-clustering-show-infowindow
     * citation: https://stackoverflow.com/questions/38906000/clustermanager-setoncameraidlelistener
     */
    private void setUpCluster(){
        clusterManager = new ClusterManager<>(this, map);

        final CameraPosition[] mPreviousCameraPosition = {null};
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != map.getCameraPosition().zoom) {
                    mPreviousCameraPosition[0] = map.getCameraPosition();
                    clusterManager.cluster();
                }

            }
        });

        //set up listeners/adapters for the clusters
        map.setOnMarkerClickListener(clusterManager);
        map.setInfoWindowAdapter(clusterManager.getMarkerManager());
        map.setOnInfoWindowClickListener(clusterManager);

        addMarkersToMap();

        //setup image icons and adapters/listeners for the individual restaurants
        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new RestaurantClusterItemAdapter());
        clusterManager.setRenderer(new RenderMapMarkerIcon(this.getApplicationContext(), map, clusterManager));
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<RestaurantClusterItem>() {
            @Override
            public boolean onClusterItemClick(RestaurantClusterItem restaurant) {
                itemClicked = restaurant;
                return false;
            }
        });

    }

    private void addMarkersToMap(){
        Log.d(TAG,"create map");

        RestaurantDataManager manager = RestaurantDataManager.getInstance(this);
        for (int i = 0; i < manager.getSize(); i++) {
            RestaurantData res = manager.getRestaurant(i);
            LatLng resLoc = new LatLng(res.getLatitude(), res.getLongitude());

            InspectionData inspection = res.getMostRecentInsp();
            String hazard;
            String hazardDB;
            BitmapDescriptor icon;
            if (inspection == null) {
                icon = getMarkerIcon(R.drawable.green_check);
                hazard = getString(R.string.hazard_level_not_found);
                hazardDB = getString(R.string.hazard_level_not_found);
            }
            else {
                icon = getMarkerIcon(inspection.getHazardIcon());
                hazard = getString(R.string.hazard_level) + " " + inspection.getHazardRatingStrForUI();
                hazardDB = inspection.getHazardRatingStrForUI();
            }

            RestaurantClusterItem tempRes = new RestaurantClusterItem(resLoc, icon,
                                                res.getRestaurantName(), res.getAddress(), hazard);
            RestaurantOnMapList.add(tempRes);
            clusterManager.addItem(tempRes);
        }
        clusterManager.cluster();
    }

    /**
     * get the icon to display on the map
     * resize it to fight properly on the map
     * referenced: https://stackoverflow.com/questions/14851641/change-marker-size-in-google-maps-api-v2
     */
    private BitmapDescriptor getMarkerIcon(int resource) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), resource);
        Bitmap resize = Bitmap.createScaledBitmap(original, MARKER_SIZE, MARKER_SIZE, false);
        return BitmapDescriptorFactory.fromBitmap(resize);
    }

    /**
     * shows the blue dot of user's location
     * and double checks if the program has permission to access device location
     */
    private void setUpDeviceLocation() {
        if (locationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{LOCATION_FINE, LOCATION_COARSE}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
            map.setMyLocationEnabled(true);
        }
        else if (deviceSetUp && !locationPermissionsGranted){
            Log.d(TAG, "no device location enabled");

            //set default view to Surrey if no device location given
            moveCamera(DEFAULT_LOCATION, DEFAULT_ZOOM);
        }
        else {
            moveCamera(DEFAULT_LOCATION, DEFAULT_ZOOM);
        }
        deviceSetUp = true;
    }

    /**
     * get the device location and center the view on it
     */
    private void getDeviceLocation() {
        deviceLocationData = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(locationPermissionsGranted) {
                Task location = deviceLocationData.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            LatLng currLoc;
                            if (currentLocation != null) {
                                currLoc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            }
                            else {
                                currLoc = DEFAULT_LOCATION;
                            }
                            moveCamera(currLoc, DEFAULT_ZOOM);
                        }
                        else {
                            Toast.makeText(MapsActivity.this, R.string.failed_to_get_current_location, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "failed to get device current location");
                        }
                    }
                });
            }
        }
        catch (SecurityException e) {
            Log.e("MapsActivity", "getDeviceLocation security exception");
        }
        catch (NullPointerException e) {
            Log.e(TAG, "no current location to query coordinates");
            Toast.makeText(MapsActivity.this,R.string.failed_to_get_current_location, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "failed to get device current location");
        }
    }

    private void moveCamera(LatLng ll, float zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoom));
    }

    // clicking on the custom window takes you to the Single Restaurant Activity
    private void setupOnInfoWindowClick() {
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                manager = RestaurantDataManager.getInstance(MapsActivity.this);

                for (int i = 0; i < manager.getSize(); i++) {
                    RestaurantData res = manager.getRestaurant(i);
                    res.setLatLng(res.getLatitude(), res.getLongitude());

                    if(res.getLatLng().equals(marker.getPosition())){
                        Intent intent = SingleRestaurantActivity.makeLaunchIntent(MapsActivity.this, i);
                        intent.putExtra(SingleRestaurantActivity.CALLING_FUNCTION_KEY, MAPS_CALLING_FUNCTION);
                        startActivity(intent);
                        break;
                    }
                }
            }
        });
    }


    /**
     * if location request is granted update the variable that
     * keeps track if the permission has been granted
     * is called when the user clicks on the pop up
     * asking for permission to use user location
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Log.d(TAG, "grant result" + grantResults[i]);
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            locationPermissionsGranted = true;
                            Log.d(TAG, "set locationPermissionGranted to " + locationPermissionsGranted);

                            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                            initialStart = prefs.getBoolean(INITIAL_START_KEY, true);

                            setUpDeviceLocation();
                            if (initialStart) {
                                reloadActivity();
                            }

                            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                            editor.putBoolean(INITIAL_START_KEY, false);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * citation: https://stackoverflow.com/questions/30958224/android-maps-utils-clustering-show-infowindow
     * Class displays a custom info window when an icon marker is clicked
     */
    public class RestaurantClusterItemAdapter implements GoogleMap.InfoWindowAdapter {
        private final View view;

        RestaurantClusterItemAdapter() {
            view = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView restaurantName = (TextView) view.findViewById(R.id.Maps_txt_markerTitle);
            restaurantName.setText(itemClicked.getRestaurantName());

            TextView address = (TextView) view.findViewById(R.id.Maps_txt_markerSnippet);
            address.setText(itemClicked.getAddress() + " | " + itemClicked.getHazard());

            return view;
        }
    }


    //Inner class used to display update message when there is a new update
    private class SetUpUpdateFragment extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void...params) {

            UpdateHandler updateHandler = new UpdateHandler(MapsActivity.this);


            //display update message if there is a new update after 20 hours
            //since the last update
            if(updateHandler.hasBeenTwentyHoursSinceLastAppUpdate()
                    && updateHandler.isNewUpdate()) {
                setupGetUpdateMessage();
            }

            return null;
        }
    }
}

