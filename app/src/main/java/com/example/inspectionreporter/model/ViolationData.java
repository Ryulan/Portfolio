package com.example.inspectionreporter.model;

/**
 * Class for grouping together and encapsulating data for a single violation
 */

import android.content.Context;

import com.example.inspectionreporter.R;

public class ViolationData {
    public static final int NON_CRITICAL_VIOLATION = 0;
    public static final int CRITICAL_VIOLATION = 1;

    //strings to print for one-liners
    private final String DESC_SHORT_TEMPERATURE;
    private final String DESC_SHORT_SANITARY;
    private final String DESC_SHORT_PEST;
    private final String DESC_SHORT_PERMIT;
    private final String DESC_SHORT_BAD_FOOD;
    
    //keywords within long description for finding out what kind of violation it is
    //temperature
    private final String KEYWORD_CELSIUS = "°C";
    private final String KEYWORD_COOL = "cool";
    private final String KEYWORD_THAW = "thaw";
    private final String KEYWORD_THERMOMETER = "thermometer";
    //sanitary
    private final String KEYWORD_SANIT = "sanit";
    private final String KEYWORD_WASH = "wash";
    private final String KEYWORD_CLEAN = "clean";
    //bad food
    private final String KEYWORD_CONTAM = "contam";
    //pest
    private final String KEYWORD_PEST = "pest";
    
    
    //represent what kind of violation it is
    private final int VIOLATION_TEMPERATURE = 0;
    private final int VIOLATION_SANITARY = 1;
    private final int VIOLATION_PEST = 2;
    private final int VIOLATION_PERMIT = 3;
    private final int VIOLATION_BAD_FOOD = 4;

    private String violation;
    private int violationRating;
    private int violationType;

    private Context context;

    //Constructor
    public ViolationData(Context c) { 
        context = c;

        //short descriptions
        DESC_SHORT_TEMPERATURE = context.getString(R.string.temperature_violation);
        DESC_SHORT_SANITARY = context.getString(R.string.sanitary_violation);
        DESC_SHORT_PEST = context.getString(R.string.pest_violation);
        DESC_SHORT_PERMIT = context.getString(R.string.permit_violation);
        DESC_SHORT_BAD_FOOD = context.getString(R.string.bad_food_violation);
    }

    //methods
    
    public String getViolation() {
        return violation;
    }

    public void setViolation(String violation) {
        this.violation = violation;

        if (violation.contains(KEYWORD_CELSIUS) || violation.contains(KEYWORD_COOL) ||
            violation.contains(KEYWORD_THAW) || violation.contains(KEYWORD_THERMOMETER)) {
            this.violationType = VIOLATION_TEMPERATURE;
        }
        else if (violation.contains(KEYWORD_SANIT) || violation.contains(KEYWORD_WASH) || violation.contains(KEYWORD_CLEAN)) {
            this.violationType = VIOLATION_SANITARY;
        }
        else if (violation.contains(KEYWORD_CONTAM)) {
            this.violationType = VIOLATION_BAD_FOOD;
        }
        else if (violation.contains(KEYWORD_PEST)) {
            this.violationType = VIOLATION_PEST;
        }
        else {
            this.violationType = VIOLATION_PERMIT;
        }
    }

    public void setViolationRating(int violationRating) {
        this.violationRating = violationRating;
    }

    public int getViolationRating() {
        return violationRating;
    }

    public int getViolationHazardIcon() {
        if(violationRating == NON_CRITICAL_VIOLATION) {
            return (R.drawable.yellow_warning);
        }
        else {
            return (R.drawable.red_triple_warning);
        }
    }

    public int getViolationTypeIcon() {
        if (violationType == VIOLATION_TEMPERATURE) {
            return (R.drawable.temperature_icon);
        }
        else if (violationType == VIOLATION_SANITARY) {
            return (R.drawable.sanitary_icon);
        }
        else if (violationType == VIOLATION_BAD_FOOD) {
            return (R.drawable.bad_food_icon);
        }
        else if (violationType == VIOLATION_PEST) {
            return (R.drawable.pest_icon);
        }
        else {
            return (R.drawable.permit_icon);
        }
    }

    public String getViolationShortDescription() {
        if (violationType == VIOLATION_TEMPERATURE) {
            return DESC_SHORT_TEMPERATURE;
        }
        else if (violationType == VIOLATION_SANITARY) {
            return DESC_SHORT_SANITARY;
        }
        else if (violationType == VIOLATION_BAD_FOOD) {
            return DESC_SHORT_BAD_FOOD;
        }
        else if (violationType == VIOLATION_PEST) {
            return DESC_SHORT_PEST;
        }
        else {
            return DESC_SHORT_PERMIT;
        }
    }

    @Override
    public String toString() {
        return "ViolationData{" +
                "Violation='" + violation + '\'' +
                ", ViolationRating='" + violationRating + '\'' +
                ", ViolationType='" + violationType + '\'' +
                '}';
    }
}
