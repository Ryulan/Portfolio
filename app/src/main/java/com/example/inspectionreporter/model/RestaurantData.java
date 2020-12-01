package com.example.inspectionreporter.model;

/**
 * Class for storing and retrieving info about a single restaurant.
 * This includes all the inspections for the restaurant.
 */

import android.widget.Toast;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.ui.SingleRestaurantActivity;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RestaurantData {

    // restaurant info
    private String trackingNumber;
    private String restaurantName;
    private String address;
    private String city;
    private String facilityType;
    private double latitude;
    private double longitude;
    private int iconID;
    private LatLng lat_lng_coordinates;
    private boolean favourite;
    private boolean favouriteUpdated;


    // inspections info
    private List<InspectionData> inspections = new ArrayList<InspectionData>();
    private InspectionData mostRecentInsp;

    //Constructor
    public RestaurantData() {
        iconID =R.drawable.generic_restaurant_icon;
        favourite=false;
        favouriteUpdated=false;

    }

    public int geticonID() {
        if (restaurantName.contains("7-Eleven")) {
            return (R.drawable.seven_eleven_logo);
        } else if (restaurantName.contains("A&W")) {
            return (R.drawable.aw_logo);
        } else if (restaurantName.contains("Barcelos")) {
            return (R.drawable.barceloslogo);
        } else if (restaurantName.contains("Blenz")) {
            return (R.drawable.blenzlogo);
        } else if (restaurantName.contains("Booster Juice")) {
            return (R.drawable.booster_juice_logo);
        } else if (restaurantName.contains("Boston Pizza")) {
            return (R.drawable.boston_pizza_logo);
        } else if (restaurantName.contains("Browns Socialhouse")) {
            return (R.drawable.brownssocialhouselogo);
        } else if (restaurantName.contains("Bubble")) {
            return (R.drawable.bubble);
        } else if (restaurantName.contains("Burger King")) {
            return (R.drawable.burger_king_logo);
        } else if (restaurantName.contains("Church's Chicken")) {
            return (R.drawable.church_s_chicken_logo);
        } else if (restaurantName.contains("Circle K")) {
            return (R.drawable.circle_k_logo);
        } else if (restaurantName.contains("COBS Bread")) {
            return (R.drawable.cobs_bread);
        } else if (restaurantName.contains("D-Plus Pizza")) {
            return (R.drawable.d_plus);
        } else if (restaurantName.contains("Dairy Queen")) {
            return (R.drawable.dairy_queen);
        } else if (restaurantName.contains("De Dutch")) {
            return (R.drawable.de_dutch);
        } else if (restaurantName.contains("Domino's Pizza")) {
            return (R.drawable.dominos_logo);
        } else if (restaurantName.contains("Elements Casino")) {
            return (R.drawable.element_casino);
        } else if (restaurantName.contains("Fraserview Meats")) {
            return (R.drawable.fraserview_meats);
        } else if (restaurantName.contains("Freshii")) {
            return (R.drawable.freshii);
        } else if (restaurantName.contains("Freshslice Pizza")) {
            return (R.drawable.freshslic_pizza);
        } else if (restaurantName.contains("Garcha Bros Meat")) {
            return (R.drawable.garcha_bros_meat);
        } else if (restaurantName.contains("KFC")) {
            return (R.drawable.kfc);
        } else if (restaurantName.contains("Little Caesars Pizza")) {
            return (R.drawable.little_caesars_pizza);
        } else if (restaurantName.contains("Mac's Convenience Store")) {
            return (R.drawable.macs_convenience_store);
        } else if (restaurantName.contains("McDonald's")) {
            return (R.drawable.mcdonalds);
        } else if (restaurantName.contains("Ocean Park")) {
            return (R.drawable.ocean_park_pizza);
        } else if (restaurantName.contains("Pizza Hut")) {
            return (R.drawable.pizza_hut);
        } else if (restaurantName.contains("Panago")) {
            return (R.drawable.panago);
        } else if (restaurantName.contains("Subway")) {
            return (R.drawable.subwaylogonew);
        } else if (restaurantName.contains("White Spot")) {
            return (R.drawable.white_spot);
        } else if (restaurantName.contains("Wendy's")) {
            return (R.drawable.wendys);
        } else if (restaurantName.contains("Taste of Africa")) {
            return (R.drawable.taste_of_africa);
        } else if (restaurantName.contains("T&T")) {
            return (R.drawable.tt);
        } else if (restaurantName.contains("Surrey Memorial Hospital")) {
            return (R.drawable.surrey_memorial_hospital);
        } else if (restaurantName.contains("Starbucks Coffee")) {
            return (R.drawable.starbucks_coffee);
        } else if (restaurantName.contains("Save On Foods")) {
            return (R.drawable.save_on_foods);
        } else if (restaurantName.contains("Safeway")) {
            return (R.drawable.safeway);
        } else if (restaurantName.contains("Quesada")) {
            return (R.drawable.quesada);
        } else if (restaurantName.contains("Quizno")) {
            return (R.drawable.quizno);
        } else if (restaurantName.contains("Real Canadian Superstore")) {
            return (R.drawable.real_canadian_superstore);
        } else if (restaurantName.contains("Ricky's")) {
            return (R.drawable.rickys);
        } else if (restaurantName.contains("Royal Canadian Legion Branch")) {
            return (R.drawable.royal_canadian_legion_branch);
        } else if (restaurantName.contains("Alebi African Cuisine")) {
            return (R.drawable.alebi_african_cuisine);
        } else {
            return (R.drawable.generic_restaurant_icon);
        }
    }
    // methods

    public void setFavouriteUpdated(boolean favouriteUpdated) {
        this.favouriteUpdated = favouriteUpdated;
    }

    public boolean isFavouriteUpdated() {
        return favouriteUpdated;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }


    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFacilityType() {
        return this.facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRestaurantIconID() {
        iconID=geticonID();
        return iconID;
    }

    public void setRestaurantIconID(int id) {
        iconID = id;
    }

    public void setLatLng(double latitude, double longitude){
        LatLng resLoc = new LatLng(latitude, longitude);
        this.lat_lng_coordinates = resLoc;
    }

    public LatLng getLatLng(){
        return lat_lng_coordinates;
    }

    public void addInspection(InspectionData insp) {
        inspections.add(insp);

        //update most recent inspection
        if(inspections.size() == 1) {
            mostRecentInsp = insp;
        } else {
            //compare year
            if(insp.getYear() > mostRecentInsp.getYear()) {
                mostRecentInsp = insp;
            } else if(insp.getYear() == mostRecentInsp.getYear()) {
                //years same, compare month
                if(insp.getMonth() > mostRecentInsp.getMonth()) {
                    mostRecentInsp = insp;
                } else if(insp.getMonth() == mostRecentInsp.getMonth()) {
                    //years and months same, compare day
                    if(insp.getDay() > mostRecentInsp.getDay()) {
                        mostRecentInsp = insp;
                    }
                }
            }
        }
    }

    public InspectionData getInspection(int i) {
        return inspections.get(i);
    }

    public List<InspectionData> getInspections() {
        return inspections;
    }

    public int getNumInspections() {
        return inspections.size();
    }

    public InspectionData getMostRecentInsp() {
        if(inspections.size() > 0) {
            return mostRecentInsp;
        } else {
            return null;
        }
    }

    public int getCriticalViolationsWithinYear(){
        int total_Critical_Violation = 0;
        boolean withinYear;

        // checks every inspection within 1 year
        for (int i = 0 ; i < inspections.size() ; i ++){
            withinYear = inspections.get(i).checkInspectionDateWithinYear();

            if(withinYear){
                total_Critical_Violation = inspections.get(i).getNumCritical() + total_Critical_Violation;
            }
        }

        return total_Critical_Violation;
    }

    @Override
    public String toString() {
        return "RestaurantData{" +
                "TrackingNumber='" + trackingNumber + '\'' +
                ", RestaurantName='" + restaurantName + '\'' +
                ", Address='" + address + '\'' +
                ", City='" + city + '\'' +
                ", FacilityType='" + facilityType + '\'' +
                ", Latitude=" + latitude +
                ", Longitude=" + longitude +
                '}';
    }

}
