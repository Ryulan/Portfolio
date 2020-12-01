package com.example.inspectionreporter.model;

/**
 * Class used to check whether there are new updates or not.
 * Fetches update dates from urls (related to City of Surrey's data set
 * for restaurant inspections) and saves date of last update using SharedPreferences.
 */

/**
 * Citation:
 * Used the course textbook: "Android Programming: The Big Nerd Ranch Guide, Third Edition"
 * to figure out how to access data from a url.
 *
 * Used https://stackoverflow.com/questions/9243578/java-util-date-and-getyear
 * and https://stackoverflow.com/questions/10161637/getting-current-year-and-month-resulting-strange-results/10161805
 * and https://stackoverflow.com/questions/40167572/how-to-get-current-hour-in-android
 * to learn how to get current date and time using Calendar.
 *
 * Used https://stackoverflow.com/questions/5677470/java-why-is-the-date-constructor-deprecated-and-what-do-i-use-instead
 * to figure out how to set the date of a Calendar object.
 *
 * Used  https://stackoverflow.com/questions/37568616/what-is-the-difference-between-gettimeinmilis-and-gettime-gettime/37568960#:~:text=getTime()%20uses%20getTimeInMillis(),getTime()%20.
 * to figure out the difference between the "getTime" and "getTimeInMillis" methods of a Calendar object.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.inspectionreporter.ui.FavouriteActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;



public class UpdateHandler {

    private final String URL_REST_PACKAGE ="https://data.surrey.ca/api/3/action/package_show?id=restaurants";
    private final String URL_INSP_REPORT_PACKAGE = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

    //for reading JSON files from urls
    private final String UTF_8 = "UTF-8";
    private final String RESULT = "result";
    private final String RESOURCES = "resources";
    private final String LAST_MODIFIED = "last_modified";

    private final long NUM_MILLIS_IN_TWENTY_HOURS = 72000000;

    //date for when data was last updated on City of Surrey's urls
    private final String SHARED_PREF_LAST_URL_UPDATE_SET = "Last Data Update Date And Time";

    //date for when data was last updated on this app
    private final String SHARED_PREF_LAST_APP_UPDATE_SET = "Last App Update Date And Time";

    private final String SHARED_PREF_ITEM_YEAR = "Year";
    private final String SHARED_PREF_ITEM_MONTH = "Month";
    private final String SHARED_PREF_ITEM_DAY = "Day";
    private final String SHARED_PREF_ITEM_HOUR = "Hour";
    private final String SHARED_PREF_ITEM_MINUTE = "Minute";
    private final String SHARED_PREF_ITEM_SECOND = "Second";


    //saved data for general info on updates
    private final String SHARED_PREF_RESTAURANT_UPDATE_INFO = "Restauarant Update Info";
    private final String SHARED_PREF_ITEM_USE_INITIAL_RESTAURANTS = "Use Initial Restaurants";

    //array: year, month, day, hours, minute, second
    private int[] currentDate = new int[6];
    private int[] dataUpdateDate = new int[6];


    private Context context;

    //Constructor
    public UpdateHandler(Context c) {
        context = c;
    }


    public boolean hasBeenTwentyHoursSinceLastAppUpdate() {
        refreshCurrentDate();

        //get last app update date and time from shared preferences
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_LAST_APP_UPDATE_SET, Context.MODE_PRIVATE);
        int lastUpdateYear = prefs.getInt(SHARED_PREF_ITEM_YEAR, -1);
        int lastUpdateMonth = prefs.getInt(SHARED_PREF_ITEM_MONTH, -1);
        int lastUpdateDay = prefs.getInt(SHARED_PREF_ITEM_DAY, -1);
        int lastUpdateHour = prefs.getInt(SHARED_PREF_ITEM_HOUR, -1);
        int lastUpdateMinute = prefs.getInt(SHARED_PREF_ITEM_MINUTE, -1);
        int lastUpdateSecond = prefs.getInt(SHARED_PREF_ITEM_SECOND, -1);

        int[] lastAppUpdateDate = {lastUpdateYear, lastUpdateMonth, lastUpdateDay, lastUpdateHour, lastUpdateMinute, lastUpdateSecond};

        long lastAppUpdateDateInMillis = dateAsMillis(lastAppUpdateDate);
        long currentDateInMillis = dateAsMillis(currentDate);


        //compare dates to see if it's been twenty hours since the last update
        if((currentDateInMillis - lastAppUpdateDateInMillis) >= NUM_MILLIS_IN_TWENTY_HOURS) {
            return true;
        } else {
            return false;
        }

    }



    //Returns true if an update has occurred since the last url data update,
    //false otherwise
    public boolean isNewUpdate() {
        //get last url update date from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_LAST_URL_UPDATE_SET, Context.MODE_PRIVATE);
        int lastUpdateYear = prefs.getInt(SHARED_PREF_ITEM_YEAR, -1);
        int lastUpdateMonth = prefs.getInt(SHARED_PREF_ITEM_MONTH, -1);
        int lastUpdateDay = prefs.getInt(SHARED_PREF_ITEM_DAY, -1);
        int lastUpdateHour = prefs.getInt(SHARED_PREF_ITEM_HOUR, -1);
        int lastUpdateMinute = prefs.getInt(SHARED_PREF_ITEM_MINUTE, -1);
        int lastUpdateSecond = prefs.getInt(SHARED_PREF_ITEM_SECOND, -1);

        int[] lastUpdateDate = {lastUpdateYear, lastUpdateMonth, lastUpdateDay, lastUpdateHour, lastUpdateMinute, lastUpdateSecond};


        if(isDate1GreaterThanDate2(dataUpdateDate, lastUpdateDate)) {
            return true;
        } else {
            return false;
        }

    }


    public boolean getUseInitialRestaurantsFromSharedPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_RESTAURANT_UPDATE_INFO, Context.MODE_PRIVATE);
        return prefs.getBoolean(SHARED_PREF_ITEM_USE_INITIAL_RESTAURANTS, true);
    }

    public void saveUseInitialRestaurantsInSharedPrefs(boolean b) {
        //restaurants saved, no need to use default list
        //of initial restaurants
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_RESTAURANT_UPDATE_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SHARED_PREF_ITEM_USE_INITIAL_RESTAURANTS, b);
        editor.commit();
    }


    //Save current update dates
    public void saveUpdateDate() {
        storeUrlUpdateTimeToSharedPreferences();
        storeAppUpdateTimeToSharedPreferences();
        if(RestaurantDataManager.getInstance(context).isAnyFavouriteUpdate()) {
            context.startActivity(new Intent(context, FavouriteActivity.class));
        }
    }


    //Refreshes the current date to this moment
    private void refreshCurrentDate() {

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; //+1 to compensate for Jan starting at 0 rather than 1
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); //24 hour clock
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentSecond = calendar.get(Calendar.SECOND);

        currentDate = new int[]{currentYear, currentMonth, currentDay, currentHour, currentMinute, currentSecond};

    }


    private void storeUrlUpdateTimeToSharedPreferences() {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_LAST_URL_UPDATE_SET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SHARED_PREF_ITEM_YEAR, dataUpdateDate[0]);
        editor.putInt(SHARED_PREF_ITEM_MONTH, dataUpdateDate[1]);
        editor.putInt(SHARED_PREF_ITEM_DAY, dataUpdateDate[2]);
        editor.putInt(SHARED_PREF_ITEM_HOUR, dataUpdateDate[3]);
        editor.putInt(SHARED_PREF_ITEM_MINUTE, dataUpdateDate[4]);
        editor.putInt(SHARED_PREF_ITEM_SECOND, dataUpdateDate[5]);
        editor.commit();
    }


    private void storeAppUpdateTimeToSharedPreferences() {
        //update current date
        refreshCurrentDate();

        //save current date using shared preferences
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_LAST_APP_UPDATE_SET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SHARED_PREF_ITEM_YEAR, currentDate[0]);
        editor.putInt(SHARED_PREF_ITEM_MONTH, currentDate[1]);
        editor.putInt(SHARED_PREF_ITEM_DAY, currentDate[2]);
        editor.putInt(SHARED_PREF_ITEM_HOUR, currentDate[3]);
        editor.putInt(SHARED_PREF_ITEM_MINUTE, currentDate[4]);
        editor.putInt(SHARED_PREF_ITEM_SECOND, currentDate[5]);
        editor.commit();
    }



    //Note: function must run on a separate thread from the main one
    public void loadUpdateDateFromUrl() {
        loadRestUpdateDateFromUrl();
        loadInspUpdateDateFromUrl();
    }


    //Note: function must run on a separate thread from the main one
    private void loadRestUpdateDateFromUrl() {

        try {
            URL url=new URL(URL_REST_PACKAGE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            InputStream in = connection.getInputStream();

            //read in data
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, Charset.forName(UTF_8))
            );

            String line="";
            String restaurantPackageData = "";

            while((line = reader.readLine()) != null){
                restaurantPackageData = restaurantPackageData + line;
            }

            JSONObject restaurantPackage = new JSONObject(restaurantPackageData);

            //get date for when restaurant data was last updated
            String dateLastModifiedRest = "" + restaurantPackage.getJSONObject(RESULT)
                    .getJSONArray(RESOURCES)
                    .getJSONObject(0)
                    .get(LAST_MODIFIED);

            int restUpdateYear = Integer.parseInt(dateLastModifiedRest.substring(0,4));
            int restUpdateMonth = Integer.parseInt(dateLastModifiedRest.substring(5,7));
            int restUpdateDay = Integer.parseInt(dateLastModifiedRest.substring(8,10));
            int restUpdateHour = Integer.parseInt(dateLastModifiedRest.substring(11,13));
            int restUpdateMinute = Integer.parseInt(dateLastModifiedRest.substring(14,16));
            int restUpdateSecond = Integer.parseInt(dateLastModifiedRest.substring(17,19));
            int[] restUpdateDate = {restUpdateYear, restUpdateMonth, restUpdateDay, restUpdateHour, restUpdateMinute, restUpdateSecond};

            //save date if the restaurant package's update date is more recent than
            //the inspection report package's update date
            if(isDate1GreaterThanDate2(restUpdateDate, dataUpdateDate)) {
                dataUpdateDate = new int[]{restUpdateYear, restUpdateMonth, restUpdateDay, restUpdateHour, restUpdateMinute, restUpdateSecond};
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //Note: function must run on a separate thread from the main one
    private void loadInspUpdateDateFromUrl() {

        try {
            URL url = new URL(URL_INSP_REPORT_PACKAGE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            InputStream in = connection.getInputStream();

            //read in data
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, Charset.forName(UTF_8))
            );

            String line = "";
            String inspReportPackageData = "";

            while((line = reader.readLine()) != null){
                inspReportPackageData = inspReportPackageData + line;
            }

            JSONObject inspReportPackage = new JSONObject(inspReportPackageData);

            //get date for when inspection data was last updated
            String dateLastModifiedInsp = "" + inspReportPackage.getJSONObject(RESULT)
                    .getJSONArray(RESOURCES)
                    .getJSONObject(0)
                    .get(LAST_MODIFIED);


            int inspUpdateYear = Integer.parseInt(dateLastModifiedInsp.substring(0,4));
            int inspUpdateMonth = Integer.parseInt(dateLastModifiedInsp.substring(5,7));
            int inspUpdateDay = Integer.parseInt(dateLastModifiedInsp.substring(8,10));
            int inspUpdateHour = Integer.parseInt(dateLastModifiedInsp.substring(11,13));
            int inspUpdateMinute = Integer.parseInt(dateLastModifiedInsp.substring(14,16));
            int inspUpdateSecond = Integer.parseInt(dateLastModifiedInsp.substring(17,19));
            int[] inspUpdateDate = {inspUpdateYear, inspUpdateMonth, inspUpdateDay, inspUpdateHour, inspUpdateMinute, inspUpdateMinute};


            //save date if the inspection report package's update date is more recent than
            //the restaurant package's update date
            if(isDate1GreaterThanDate2(inspUpdateDate, dataUpdateDate)) {
                dataUpdateDate = new int[]{inspUpdateYear, inspUpdateMonth, inspUpdateDay, inspUpdateHour, inspUpdateMinute, inspUpdateSecond};
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //Returns the given date in milliseconds
    private long dateAsMillis(int[] date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date[0]);
        calendar.set(Calendar.MONTH, date[1]);
        calendar.set(Calendar.DAY_OF_MONTH, date[2]);
        calendar.set(Calendar.HOUR_OF_DAY, date[3]);
        calendar.set(Calendar.MINUTE, date[4]);
        calendar.set(Calendar.SECOND, date[5]);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();

    }


    //Returns true if date1 is greater than date2; false otherwise
    //(Date array: year, month, day, hour, minute, second)
    private boolean isDate1GreaterThanDate2(int[] date1, int[] date2) {
        //compare years
        if(date1[0] > date2[0]) {
            return true;
        } else {
            //compare months
            if(date1[1] > date2[1]) {
                return true;
            } else {
                //compare days
                if(date1[2] > date2[2]) {
                    return true;
                } else {
                    //compare hours
                    if(date1[3] > date2[3]) {
                        return true;
                    } else {
                        //compare minutes
                        if(date1[4] > date2[4]) {
                            return true;
                        } else {
                            //compare seconds
                            if(date1[5] > date2[5]) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
    }




}
