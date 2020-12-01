package com.example.inspectionreporter.model;

/**
 * Class used to fetch raw data from urls (related to City of Surrey's data set
 * for restaurant inspections). Updates RestaurantDataManger with data from urls.
 */

/**
 * Code Citations:
 *  Used course textbook "Android Programming: The Big Nerd Ranch Guide, 3rd edition"
 *  to figure out how to get data from urls.
 */


import android.content.Context;
import android.util.Log;

import com.example.inspectionreporter.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UrlDataFetcher {

    private final String STR_CRITICAL = "Critical";
    private final String STR_NOT_CRITICAL = "Not Critical";


    private UpdateHandler updateHandler;


    private RestaurantDataManager manager;
    private List<RestaurantData> restaurants = new ArrayList<RestaurantData>();
    private final String URL_RESTAURANT_CSV_FILE = "https://data.surrey.ca/dataset/3c8cb648-0e80-4659-9078-ef4917b90ffb/resource/0e5d04a2-be9b-40fe-8de2-e88362ea916b/download/restaurants.csv";
    private final String URL_INSPECTIONS_CSV_FILE = "https://data.surrey.ca/dataset/948e994d-74f5-41a2-b3cb-33fa6a98aa96/resource/30b38b66-649f-4507-a632-d5f6f5fe87f1/download/fraserhealthrestaurantinspectionreports.csv";
    private Context context;



    //Constructor
    public UrlDataFetcher(Context c) {
        context = c;
        manager = RestaurantDataManager.getInstance(context);
        updateHandler = new UpdateHandler(context);
    }


    public List<RestaurantData> getRestaurants() {
        return restaurants;
    }

    public void loadRestaurants() {
        try {
            loadRestaurantDataFromUrl();
            loadInspectionDataFromUrl();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sortRestaurantsAlphabetically();
        sortRestaurantInspectionListBydate();
    }



    public void saveUpdatedRestaurants() {
        manager.setRestaurants(restaurants);
        updateHandler.saveUseInitialRestaurantsInSharedPrefs(false);
        manager.saveRestaurantDataInSharedPref();
    }




    //Get data from url as an array of bytes
    //Note: function must run on a separate thread from the main one
    public void loadRestaurantDataFromUrl() throws IOException {

        //reference to url connection
        URL url = new URL(URL_RESTAURANT_CSV_FILE);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();


        try {
            //connect to url
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in =  connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage()
                        + ": with "
                        + url);
            }

            //parse data into RestaurantData objects
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, Charset.forName("UTF-8"))
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

                    //restaurant name has quotes, and a comma within it
                    if(tokens[1].substring(0,1).equals("\"")) {
                        sample.setRestaurantName(tokens[1].substring(1) + tokens[2].substring(0,tokens[2].length()-1));
                        sample.setAddress(tokens[3]);
                        sample.setCity(tokens[4]);
                        sample.setFacilityType(tokens[5]);
                        if(tokens[6].length() > 0){
                            // Since token[6] is a string, if it's length is more than 0, parse it to a double
                            sample.setLatitude(Double.parseDouble(tokens[6]));
                        } else {
                            // when token[6] is empty, sets the latitude to 0 as a default
                            sample.setLatitude(0);
                        }
                        if(tokens.length >= 8 && tokens[7].length() > 0){
                            sample.setLongitude(Double.parseDouble(tokens[7]));
                        } else {
                            sample.setLongitude(0);
                        }

                    } else { //restaurant name not in quotes, and doesn't have a comma within it

                        sample.setRestaurantName(tokens[1]);
                        sample.setAddress(tokens[2]);
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

                    }


                    restaurants.add(sample);
                }
            } catch (IOException e){
                // catch error for readLine
                Log.wtf("UrlDataFetcher", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }


            out.close();

        } finally {
            connection.disconnect();
        }
    }




    //Note: function must run on a separate thread from the main one
    public void loadInspectionDataFromUrl() throws IOException {

        //reference to url connection
        URL url = new URL(URL_INSPECTIONS_CSV_FILE);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();


        try {
            //connect to url
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in =  connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage()
                        + ": with "
                        + url);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, Charset.forName("UTF-8"))
            );

            String line = "";
            try{
                // Step over headers
                reader.readLine();

                while ((line = reader.readLine()) != null){
                    // Split by ","
                    String[] tokens = line.split(",");
                    InspectionData sample = new InspectionData(context);

                    if(tokens.length == 0) {
                        //no data, don't do anything
                    } else {
                        //date of inspection
                        String inspDate = tokens[1];
                        String inspYear = inspDate.substring(0,4);
                        String inspMonth = inspDate.substring(4,6);
                        String inspDay = inspDate.substring(6,8);
                        sample.setYear(Integer.parseInt(inspYear));
                        sample.setMonth(Integer.parseInt(inspMonth));
                        sample.setDay(Integer.parseInt(inspDay));

                        //other inspection info
                        sample.setInspType(tokens[2]);
                        sample.setNumCritical(Integer.parseInt(tokens[3]));
                        sample.setNumNonCritical(Integer.parseInt(tokens[4]));

                        if(tokens.length > 5) {
                            sample.setHazardRating(tokens[tokens.length-1]);
                        }

                    }


                    //violations
                    if(tokens.length >= 6) {
                        List<ViolationData> violations = new ArrayList<>();
                        int i = 0; //iterate until "Critical" is hit
                        int j = 0; //iterate through split string after critical
                        int v = 0; //use to index list of added violations
                        String element = "";
                        while (i < tokens.length) {
                            if (tokens[i].contains(STR_CRITICAL)) {
                                violations.add(new ViolationData(context));
                                if (tokens[i].equals(STR_NOT_CRITICAL)) {
                                    violations.get(v).setViolationRating(ViolationData.NON_CRITICAL_VIOLATION);
                                } else {
                                    violations.get(v).setViolationRating(ViolationData.CRITICAL_VIOLATION);
                                }

                                String[] split = tokens[i + 1].split(" ");
                                j = 0;
                                while (j < split.length) {
                                    if (split[j].contains("[")) {
                                        break;
                                    } else {
                                        element = element + " " + split[j];
                                    }
                                    j++;
                                }
                                violations.get(v).setViolation(element.substring(1));

                                element = "";
                                v++;
                            }
                            i++;

                        }
                        sample.setViolations(violations);

                        //save inspection to restaurant according to tracking number
                        String trackingNum = tokens[0];

                        for(RestaurantData restaurant : restaurants) {
                            if(restaurant.getTrackingNumber().equals(trackingNum)) {
                                restaurant.addInspection(sample);
                                break;
                            }
                        }
                    }


                }
            } catch (IOException e){
                // catch error for readLine
                Log.wtf("UrlDataFetcher", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }


            out.close();

        } finally {
            connection.disconnect();
        }
    }

    private void sortRestaurantInspectionListBydate()
    {

        for(int i=0; i<restaurants.size(); i++) {

            Collections.sort(restaurants.get(i).getInspections(), new Comparator<InspectionData>() {
                //TODO: put numbers into constants
                @Override
                public int compare(InspectionData current, InspectionData next) {
                    int inspDate1 = (current.getYear() * 10000) + (current.getMonth() * 100) + current.getDay();
                    int inspDate2 = (next.getYear() * 10000) + (next.getMonth() * 100) + next.getDay();
                    return inspDate2 - inspDate1;
                }


            });
        }
        for(int i=0; i<manager.getSize(); i++) {

            Collections.sort(manager.getRestaurant(i).getInspections(), new Comparator<InspectionData>() {
                //TODO: put numbers into constants
                @Override
                public int compare(InspectionData current, InspectionData next) {
                    int inspDate1 = (current.getYear() * 10000) + (current.getMonth() * 100) + current.getDay();
                    int inspDate2 = (next.getYear() * 10000) + (next.getMonth() * 100) + next.getDay();
                    return inspDate2 - inspDate1;
                }


            });
        }



        for (RestaurantData  res : manager.getRestaurants()){
            int i=0;
            for (RestaurantData  restaurant : restaurants) {
                manager.saveRestaurantFavouriteInSharedPref(i, restaurant.isFavourite());
                if(res.isFavourite()
                    && res.getTrackingNumber().contains(restaurant.getTrackingNumber())
                    &&res.getInspections().size()>0
                    &&restaurant.getInspections().size()>0)
                {

                    restaurant.setFavourite(true);

                    InspectionData inspection=res.getInspection(0);
                    InspectionData inspection2=restaurant.getInspection(0);

                    if(((inspection.getYear() * 10000) + (inspection.getMonth() * 100) + inspection.getDay())<((inspection2.getYear() * 10000) + (inspection2.getMonth() * 100) + inspection2.getDay())) {

                        restaurant.setFavouriteUpdated(true);

                        //favouriteRestaurants.add(restaurant);

                        if (!manager.isAnyFavouriteUpdate()){
                            manager.setAnyFavouriteUpdate(true);
                        }
                        break;

                    }

                }
            }
            i++;

        }

    }

    // Sorts the Restaurant List Alphabetically
    private void sortRestaurantsAlphabetically() {
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


}
