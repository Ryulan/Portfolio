package com.example.inspectionreporter.model;

/**
 * Class for managing a collection of restaurants.
 */

/**
 * Code Citations:
 * Used https://stackoverflow.com/questions/23438530/meaning-of-double-doubletolongbitsx
 * and https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
 * to figure out how to change from double to long and long to double using these two bits
 * of code:
 * Double.doubleToRawLongBits(num)
 * Double.longBitsToDouble(num)
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.inspectionreporter.R;
import com.google.android.gms.maps.model.BitmapDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class RestaurantDataManager implements Iterable<RestaurantData>{

    private final String STR_CRITICAL = "Critical";
    private final String STR_NOT_CRITICAL = "Not Critical";

    private final String SHARED_PREF_RESTAURANTS_INFO = "Info For All Restaurants";
    private final String SHARED_PREF_ITEM_NUM_RESTAURANTS = "Number of Restaurants";


    //saved data for a single restaurant
    private String sharedPrefRestaurantDataSet = "Data Set for Restaurant#"; //must append number on end to indicate
    //which restaurant data is being saved for
    private final String SHARED_PREF_ITEM_TRACKING_NUM = "Tracking Number";
    private final String SHARED_PREF_ITEM_NAME = "Name";
    private final String SHARED_PREF_ITEM_ADDRESS = "Address";
    private final String SHARED_PREF_ITEM_CITY = "City";
    private final String SHARED_PREF_ITEM_FACILITY_TYPE = "Facility Type";
    private final String SHARED_PREF_ITEM_LATITUDE = "Latitude";
    private final String SHARED_PREF_ITEM_LONGTITUDE = "Longtitude";
    private final String SHARED_PREF_ITEM_NUM_INSPECTIONS = "Num Inspections";
    private final String SHARED_PREF_ITEM_FAVOURITE = "Favourite";


    //saved data for a single inspection - must append a number to the end to indicate
    //which inspection data is being saved for
    private String sharedPrefInspectionDataSet = "Data Set for Restaurant# Inspection#"; //must append "number number"
    //to indicate which inspection
    //data is being saved for
    private final String SHARED_PREF_ITEM_YEAR = "Year";
    private final String SHARED_PREF_ITEM_MONTH = "Month";
    private final String SHARED_PREF_ITEM_DAY = "Day";
    private final String SHARED_PREF_ITEM_INSP_TYPE = "Inspection Type";
    private final String SHARED_PREF_ITEM_NUM_CRITICAL = "Num Critical";
    private final String SHARED_PREF_ITEM_NUM_NON_CRITICAL = "Num NonCritical";
    private final String SHARED_PREF_ITEM_HAZARD_RATING = "Hazard Rating";
    private final String SHARED_PREF_ITEM_NUM_VIOLATIONS = "Num Violations";


    //saved data for a single violation -
    private String sharedPrefViolationDataSet = "Data Set for Restaurant# Inspection# Violation#"; //must append "number number number"
    //to indicate which violation data
    //is being saved for

    private final String SHARED_PREF_ITEM_VIO_RATING = "Violation Rating";
    private final String SHARED_PREF_ITEM_VIOLATION = "Violation";

    private UpdateHandler updateHandler;
    private boolean useInitialRestaurants;
    private boolean anyFavouriteUpdate=false;

    private boolean alreadyAskToUpdateRestaurants;
    private List<RestaurantData> restaurants = new ArrayList<RestaurantData>();
    private Context context;

    // Search Option Variables
    private boolean SearchFragBtnClicked;
    private boolean ResetFragBtnClicked;

    private DBAdapter db;

    // Singleton

    private static RestaurantDataManager instance;

    // Constructor - private to prevent anyone else from instantiating
    private RestaurantDataManager(Context c){
        context = c;
        db = DBAdapter.getInstance(context);
        db.open();

        updateHandler = new UpdateHandler(context);
        useInitialRestaurants = updateHandler.getUseInitialRestaurantsFromSharedPrefs();

        if(useInitialRestaurants) {
            loadInitialRestaurantList();
            loadInitialInspections();
            Log.d("RestaurantDataManager", "Initial List");
        } else {
            loadRestaurantsFromSharedPreferences();
            Log.d("RestaurantDataManager", "Saved List");
        }

        sortRestaurantList();
        loadRestaurantFavouriteInSharedPref();
        updateDatabase();
        alreadyAskToUpdateRestaurants = false;
    }

    public static RestaurantDataManager getInstance(Context c){
        if (instance == null){
            instance = new RestaurantDataManager(c);
        }

        return instance;
    }


    // Normal Object Code

    public boolean isAnyFavouriteUpdate() {
        return anyFavouriteUpdate;
    }

    public void setAnyFavouriteUpdate(boolean anyFavouriteUpdate) {
        this.anyFavouriteUpdate = anyFavouriteUpdate;
    }


    public void setAlreadyAskToUpdateRestaurants(boolean b) {
        alreadyAskToUpdateRestaurants = b;
    }

    public boolean alreadyAskToUpdateRestaurants() {
        return alreadyAskToUpdateRestaurants;
    }

    public void setSearchFragBtnClicked(boolean b) {
        SearchFragBtnClicked = b;
    }

    public boolean getSearchFragBtnClicked() {
        return SearchFragBtnClicked;
    }

    public void setResetFragBtnClicked(boolean b) {
        ResetFragBtnClicked = b;
    }

    public boolean getResetFragBtnClicked() {
        return ResetFragBtnClicked;
    }


    public List<RestaurantData> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<RestaurantData> restaurants) {
        this.restaurants = restaurants;
    }

    public void add(RestaurantData info){
        restaurants.add(info);
    }

    public int getSize(){
        return restaurants.size();
    }

    public RestaurantData getRestaurant(int position){
        return restaurants.get(position);
    }

    // Loads the restaurant list from csv file into the restaurants list
    private void loadInitialRestaurantList() {

        InputStream is = context.getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        try{
            // Step over headers
            reader.readLine();

            while ((line = reader.readLine()) != null){
                // Split by ","
                String[] tokens = line.split(",");
                RestaurantData sample = new RestaurantData();
                sample.setTrackingNumber(tokens[0]);

                sample.setRestaurantName(tokens[1].substring(1, tokens[1].length()-1));
                sample.setAddress(tokens[2].substring(1,tokens[2].length()-1));
                sample.setCity(tokens[3]);
                sample.setFacilityType(tokens[4]);
                if(tokens[5].length() > 0){
                    // Since token[5] is a string, if it's length is more than 0, parse it to a double
                    sample.setLatitude(Double.parseDouble(tokens[5]));
                } else {
                    // when token[5] is empty, sets the latitude to 0 as a default
                    sample.setLatitude(0);
                }
                if(tokens.length >= 7 && tokens[6].length() > 0){
                    sample.setLongitude(Double.parseDouble(tokens[6]));
                } else {
                    sample.setLongitude(0);
                }

                restaurants.add(sample);
                Log.d("AllRestaurantListActivity", "Just Created: " + sample);
            }
        } catch (IOException e){
            // catch error for readLine
            Log.wtf("AllRestaurantListActivity", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }


    // Loads the inspection data from csv file into corresponding restaurants
    // within the restauarants list, based on the tracking number
    private void loadInitialInspections() {
        InputStream is = context.getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        try{
            // Step over headers
            reader.readLine();

            while ((line = reader.readLine()) != null){
                // Split by ","
                String[] tokens = line.split(",");
                InspectionData sample = new InspectionData(context);

                //date of inspection
                String inspDate = tokens[1];
                String inspYear = inspDate.substring(0,4);
                String inspMonth = inspDate.substring(4,6);
                String inspDay = inspDate.substring(6,8);
                sample.setYear(Integer.parseInt(inspYear));
                sample.setMonth(Integer.parseInt(inspMonth));
                sample.setDay(Integer.parseInt(inspDay));

                //other inspection info
                sample.setInspType(tokens[2].substring(1, tokens[2].length()-1));
                sample.setNumCritical(Integer.parseInt(tokens[3]));
                sample.setNumNonCritical(Integer.parseInt(tokens[4]));
                sample.setHazardRating(tokens[5].substring(1, tokens[5].length()-1));

                //violations
                if(tokens.length >= 7) {
                    List<ViolationData> violations = new ArrayList<>();
                    int i = 0; //iterate until "Critical" is hit
                    int j = 0; //iterate through split string after critical
                    int v = 0; //use to index list of added violations
                    String element = "";
                    while(i < tokens.length) {
                        if (tokens[i].contains(STR_CRITICAL)) {
                            violations.add(new ViolationData(context));
                            if (tokens[i].equals(STR_NOT_CRITICAL)) {
                                violations.get(v).setViolationRating(ViolationData.NON_CRITICAL_VIOLATION);
                            }
                            else {
                                violations.get(v).setViolationRating(ViolationData.CRITICAL_VIOLATION);
                            }

                            String[] split = tokens[i + 1].split(" ");
                            j = 0;
                            while(j < split.length) {
                                if (split[j].contains("[")) {
                                    break;
                                }
                                else {
                                    element = element + " " + split[j];
                                }
                                j++;
                            }
                            Log.d("Added violation", "added: " + element);
                            violations.get(v).setViolation(element.substring(1));
                            element = "";
                            v++;
                        }
                        i++;

                    }
                    sample.setViolations(violations);
                }

                //save inspection to restaurant according to tracking number
                String trackingNum = tokens[0];

                for(RestaurantData restaurant : restaurants) {
                    if(restaurant.getTrackingNumber().equals(trackingNum)) {
                        restaurant.addInspection(sample);
                        break;
                    }
                }

                Log.d("AllRestaurantListActivity", "Just Created Inspection: " + sample);
            }
        } catch (IOException e){
            // catch error for readLine
            Log.wtf("AlRestaurantListActivity", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }

    public void saveRestaurantFavouriteInSharedPref(int position, boolean b)
    {
        SharedPreferences prefs;
        SharedPreferences.Editor editor;




        //save data for current restaurant
        prefs = context.getSharedPreferences(
                sharedPrefRestaurantDataSet + position,
                Context.MODE_PRIVATE);

        editor = prefs.edit();
        editor.putBoolean(SHARED_PREF_ITEM_FAVOURITE, b);
        editor.commit();


    }


    public void saveRestaurantDataInSharedPref() {

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_RESTAURANTS_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SHARED_PREF_ITEM_NUM_RESTAURANTS, restaurants.size());
        editor.commit();

        //save data for all restaurants in manager
        for(int i = 0; i < restaurants.size(); i++) {
            RestaurantData restaurant = restaurants.get(i);

            //save data for current restaurant
            prefs = context.getSharedPreferences(
                    sharedPrefRestaurantDataSet + i,
                    Context.MODE_PRIVATE);

            editor = prefs.edit();
            editor.putString(SHARED_PREF_ITEM_TRACKING_NUM, restaurant.getTrackingNumber());
            editor.putString(SHARED_PREF_ITEM_NAME, restaurant.getRestaurantName());
            editor.putString(SHARED_PREF_ITEM_ADDRESS, restaurant.getAddress());
            editor.putString(SHARED_PREF_ITEM_CITY, restaurant.getCity());
            editor.putString(SHARED_PREF_ITEM_FACILITY_TYPE, restaurant.getFacilityType());
            editor.putLong(SHARED_PREF_ITEM_LATITUDE, Double.doubleToRawLongBits(restaurant.getLatitude()));
            editor.putLong(SHARED_PREF_ITEM_LONGTITUDE, Double.doubleToRawLongBits(restaurant.getLongitude()));
            editor.putInt(SHARED_PREF_ITEM_NUM_INSPECTIONS, restaurant.getNumInspections());
            editor.putBoolean(SHARED_PREF_ITEM_FAVOURITE, restaurant.isFavourite());
            editor.commit();


            //save data for all inspections within restaurant
            for(int j = 0; j < restaurant.getNumInspections(); j++) {
                InspectionData insp = restaurant.getInspection(j);

                //save data for current inspection
                prefs = context.getSharedPreferences(
                        sharedPrefInspectionDataSet + i + " " + j,
                        Context.MODE_PRIVATE);

                editor = prefs.edit();
                editor.putInt(SHARED_PREF_ITEM_YEAR, insp.getYear());
                editor.putInt(SHARED_PREF_ITEM_MONTH, insp.getMonth());
                editor.putInt(SHARED_PREF_ITEM_DAY, insp.getDay());
                editor.putString(SHARED_PREF_ITEM_INSP_TYPE, insp.getInspType());
                editor.putInt(SHARED_PREF_ITEM_NUM_CRITICAL, insp.getNumCritical());
                editor.putInt(SHARED_PREF_ITEM_NUM_NON_CRITICAL, insp.getNumNonCritical());
                editor.putString(SHARED_PREF_ITEM_HAZARD_RATING, insp.getHazardRatingStrForUI());
                editor.putInt(SHARED_PREF_ITEM_NUM_VIOLATIONS, insp.getNumViolations());
                editor.commit();


                //save data for all violations within inspection
                for(int k = 0; k < insp.getNumViolations(); k++) {
                    ViolationData vio = insp.getViolation(k);

                    //save data for current violation
                    prefs = context.getSharedPreferences(
                            sharedPrefViolationDataSet + i + " " + j + " " + k,
                            Context.MODE_PRIVATE);

                    editor = prefs.edit();
                    editor.putInt(SHARED_PREF_ITEM_VIO_RATING, vio.getViolationRating());
                    editor.putString(SHARED_PREF_ITEM_VIOLATION, vio.getViolation());
                    editor.commit();
                }
            }
        }
    }

    public void loadRestaurantFavouriteInSharedPref()
    {
        SharedPreferences prefs;
        if(restaurants.size() > 0) {

            //get all restaurants
            for (int i = 0; i < restaurants.size(); i++) {


                prefs = context.getSharedPreferences(
                        sharedPrefRestaurantDataSet + i,
                        Context.MODE_PRIVATE);
                restaurants.get(i).setFavourite(prefs.getBoolean(SHARED_PREF_ITEM_FAVOURITE, false));
            }
        }
    }


    public void loadRestaurantsFromSharedPreferences() {
        //clear old list
        restaurants = new ArrayList<RestaurantData>();

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_RESTAURANTS_INFO, Context.MODE_PRIVATE);
        int numRestaurants = prefs.getInt(SHARED_PREF_ITEM_NUM_RESTAURANTS, 0);

        if(numRestaurants > 0) {

            //get all restaurants
            for(int i = 0; i < numRestaurants; i++) {
                RestaurantData rest = new RestaurantData();

                prefs = context.getSharedPreferences(
                        sharedPrefRestaurantDataSet + i,
                        Context.MODE_PRIVATE);

                rest.setTrackingNumber(prefs.getString(SHARED_PREF_ITEM_TRACKING_NUM, ""));
                rest.setRestaurantName(prefs.getString(SHARED_PREF_ITEM_NAME, ""));
                rest.setAddress(prefs.getString(SHARED_PREF_ITEM_ADDRESS, ""));
                rest.setCity(prefs.getString(SHARED_PREF_ITEM_CITY, ""));
                rest.setFacilityType(prefs.getString(SHARED_PREF_ITEM_FACILITY_TYPE, ""));
                rest.setLatitude(Double.longBitsToDouble(prefs.getLong(SHARED_PREF_ITEM_LATITUDE, 0)));
                rest.setLongitude(Double.longBitsToDouble(prefs.getLong(SHARED_PREF_ITEM_LONGTITUDE, 0)));
                rest.setFavourite(prefs.getBoolean(SHARED_PREF_ITEM_FAVOURITE, false));
                


                //get all inspections
                int numInsps = prefs.getInt(SHARED_PREF_ITEM_NUM_INSPECTIONS, 0);
                for(int j = 0; j < numInsps; j++) {
                    InspectionData insp = new InspectionData(context);

                    prefs = context.getSharedPreferences(
                            sharedPrefInspectionDataSet + i + " " + j,
                            Context.MODE_PRIVATE);

                    insp.setYear(prefs.getInt(SHARED_PREF_ITEM_YEAR, 0));
                    insp.setMonth(prefs.getInt(SHARED_PREF_ITEM_MONTH, 0));
                    insp.setDay(prefs.getInt(SHARED_PREF_ITEM_DAY, 0));
                    insp.setInspType(prefs.getString(SHARED_PREF_ITEM_INSP_TYPE, ""));
                    insp.setNumCritical(prefs.getInt(SHARED_PREF_ITEM_NUM_CRITICAL, 0));
                    insp.setNumNonCritical(prefs.getInt(SHARED_PREF_ITEM_NUM_NON_CRITICAL, 0));
                    insp.setHazardRating(prefs.getString(SHARED_PREF_ITEM_HAZARD_RATING, ""));

                    //get all violations
                    int numVios = prefs.getInt(SHARED_PREF_ITEM_NUM_VIOLATIONS, 0);
                    List<ViolationData> violations = new ArrayList<>();
                    for(int k = 0; k < numVios; k++) {
                        ViolationData vio = new ViolationData(context);

                        prefs = context.getSharedPreferences(
                                sharedPrefViolationDataSet + i + " " + j + " " + k,
                                Context.MODE_PRIVATE);
                        vio.setViolationRating(prefs.getInt(SHARED_PREF_ITEM_VIO_RATING, 0));
                        vio.setViolation(prefs.getString(SHARED_PREF_ITEM_VIOLATION, ""));


                        violations.add(vio);
                    }

                    insp.setViolations(violations);
                    rest.addInspection(insp);
                }
                restaurants.add(rest);
            }
        }
    }


    // Sorts the Restaurant List Alphabetically
    private void sortRestaurantList() {
        // Referenced: https://stackoverflow.com/questions/5815423/sorting-arraylist-in-alphabetical-order-case-insensitive
        Collections.sort(restaurants, new Comparator<RestaurantData>() {
            @Override
            public int compare(RestaurantData current, RestaurantData next) {
                String s1 = current.getRestaurantName();
                String s2 = next.getRestaurantName();
                return s1.compareToIgnoreCase(s2);
            }
        });
    }

    public void updateDatabase() {
            for (int i = 0; i < restaurants.size(); i++) {
                RestaurantData res = restaurants.get(i);

                InspectionData inspection = res.getMostRecentInsp();
                String hazard;
                if (inspection == null) {
                    hazard = context.getString(R.string.hazard_level_not_found);
                }
                else {
                    hazard = inspection.getHazardRatingStrForUI();
                }

                if (i >= db.getPrimaryKeys()) {
                    db.insertRow(i, res.getRestaurantName(), hazard,
                            res.getCriticalViolationsWithinYear(), res.isFavourite());
                    Log.d("DATABASE INSERT", "DATABASE insert into db, row: " + i);
                } else {
                    db.updateRow(i, res.getRestaurantName(), hazard,
                            res.getCriticalViolationsWithinYear(), res.isFavourite());
                    Log.d("DATABASE UPDATE", "DATABASE update db row: " + i);
                }
            }
            db.savePrimaryKey();
    }

    @Override
    public Iterator<RestaurantData> iterator(){
        return restaurants.iterator();
    }

}
