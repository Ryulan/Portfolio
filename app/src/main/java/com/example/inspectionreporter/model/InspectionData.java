package com.example.inspectionreporter.model;

/**
 * Class for storing and retrieving info about a single inspection.
 */

import android.content.Context;

import com.example.inspectionreporter.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InspectionData {
    private static final int RESULT_CODE_WITHIN_30_DAYS = 1;
    private static final int RESULT_CODE_WITHIN_1_YEAR = 2;
    private static final int RESULT_CODE_AFTER_1_YEAR = 3;
    
    public static final String STR_LOW = "Low";
    public static final String STR_MEDIUM = "Moderate";
    public static final String STR_FOLLOW_UP = "Follow-Up";
    public static final String STR_HIGH = "High";

    private final int LOW_HAZARD_COL;
    private final int MODER_HAZARD_COL;
    private final int HIGH_HAZARD_COL;
    private int hazardCol;

    // date when inspection occurred
    private int year;
    private int month;
    private int day;
    
    private String inspType;
    private String inspTypeStrForUI;
    private int numCritical;
    private int numNonCritical;
    private String hazardRating;
    private String hazardRatingStrForUI;
    private List<ViolationData> violations;

    private Context context;

    // Constructor
    public InspectionData(Context c) {
        context = c;
        LOW_HAZARD_COL = context.getResources().getColor(R.color.colorLowHazard, null);
        MODER_HAZARD_COL = context.getResources().getColor(R.color.colorModerHazard, null);
        HIGH_HAZARD_COL = context.getResources().getColor(R.color.colorHighHazard, null);
        hazardCol = LOW_HAZARD_COL;
    }
    
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getInspType() {
        return inspTypeStrForUI;
    }

    public void setInspType(String inspType) {
        this.inspType = inspType;

        if(inspType.contains(STR_FOLLOW_UP)) {
            inspTypeStrForUI = context.getString(R.string.follow_up);
        } else {
            inspTypeStrForUI = context.getString(R.string.routine);
        }
    }

    public int getNumCritical() {
        return numCritical;
    }

    public void setNumCritical(int numCritical) {
        this.numCritical = numCritical;
    }

    public int getNumNonCritical() {
        return numNonCritical;
    }

    public void setNumNonCritical(int numNonCritical) {
        this.numNonCritical = numNonCritical;
    }

    public int getTotalNumIssues() {
        return (numNonCritical + numCritical);
    }

    public int getNumViolations() {
        return violations.size();
    }

    public int getHazardCol() {
        return hazardCol;
    }

    public String getHazardRatingStrForUI() {
        return hazardRatingStrForUI;
    }

    public void setHazardRating(String hazardRating) {
        this.hazardRating = hazardRating;

        //set hazard color
        if(hazardRating.contains(STR_LOW) || hazardRating.contains(context.getString(R.string.low))) {
            hazardCol = LOW_HAZARD_COL;
            this.hazardRatingStrForUI = context.getString(R.string.low);
        } else if(hazardRating.contains(STR_MEDIUM) || hazardRating.contains(context.getString(R.string.moderate))) {
            hazardCol = MODER_HAZARD_COL;
            this.hazardRatingStrForUI = context.getString(R.string.moderate);
        } else {
            hazardCol = HIGH_HAZARD_COL;
            this.hazardRatingStrForUI = context.getString(R.string.high);
        }
    }

    public int getHazardIcon(){
        if(hazardRating.contains(STR_LOW) || hazardRating.contains(context.getString(R.string.low))){
            return (R.drawable.green_check);
        } else if(hazardRating.contains(STR_MEDIUM) || hazardRating.contains(context.getString(R.string.moderate))){
            return (R.drawable.yellow_warning);
        } else {
            return (R.drawable.red_triple_warning);
        }
    }

    public String getMonthName(int month){
        String monthName = context.getString(R.string.month);
        switch(month){
            case 1:
                monthName= context.getString(R.string.jan);
                break;
            case 2:
                monthName= context.getString(R.string.feb);
                break;
            case 3:
                monthName= context.getString(R.string.march);
                break;
            case 4:
                monthName= context.getString(R.string.april);
                break;
            case 5:
                monthName= context.getString(R.string.may);
                break;
            case 6:
                monthName= context.getString(R.string.june);
                break;
            case 7:
                monthName= context.getString(R.string.july);
                break;
            case 8:
                monthName= context.getString(R.string.aug);
                break;
            case 9:
                monthName= context.getString(R.string.sept);
                break;
            case 10:
                monthName= context.getString(R.string.oct);
                break;
            case 11:
                monthName= context.getString(R.string.nov);
                break;
            case 12:
                monthName= context.getString(R.string.dec);
                break;
        }
        return monthName;
    }
    
    public String getDateRecentFormat() {
        int requestCode = checkDates(day, month, year);

        switch (requestCode){
            case RESULT_CODE_WITHIN_30_DAYS:
                return context.getString(R.string.date_within_30_days, getDaysAgo(day, month, year));

            case RESULT_CODE_WITHIN_1_YEAR:
                return context.getString(R.string.date_after_30_days, getMonthName(month), day);

            default:
                return context.getString(R.string.date_year_or_more, getMonthName(month), year);
        }
    }

    private int checkDates(int day, int month, int year){
        Calendar calender = Calendar.getInstance();
        Calendar inspectionCalender = Calendar.getInstance();
        Date startDate = new Date();

        calender.setTime(startDate); // sets startDate to today's date
        calender.add(Calendar.DATE, -30);
        Date within30DaysDate = calender.getTime(); // sets withing30DaysDate to today's date minus 30 days

        calender.add(Calendar.DATE, 30); // resets the calender date back to today's date
        calender.add(Calendar.YEAR, -1);
        Date within1YearDate = calender.getTime(); // sets within1YearDate to today's date minus 1 year

        inspectionCalender.set(Calendar.DAY_OF_MONTH, day);
        inspectionCalender.set(Calendar.MONTH, month - 1); // 0-JAN, 1-FEB, 2-MARCH , etc, so we have to subtract 1
        inspectionCalender.set(Calendar.YEAR, year);
        Date inspectionDate = inspectionCalender.getTime();

        if(inspectionDate.before(startDate) && inspectionDate.after(within30DaysDate)){ // within 30 days of current date
            return RESULT_CODE_WITHIN_30_DAYS;
        } else if(inspectionDate.before(startDate) && inspectionDate.after(within1YearDate)){ //withing 1 year of current date
            return RESULT_CODE_WITHIN_1_YEAR;
        } else{ // over 1 year old
            return RESULT_CODE_AFTER_1_YEAR;
        }
    }

    public boolean checkInspectionDateWithinYear(){
        Calendar calender = Calendar.getInstance();
        Calendar inspectionCalender = Calendar.getInstance();
        Date startDate = new Date();

        calender.setTime(startDate); // sets startDate to today's date
        calender.add(Calendar.YEAR, -1);
        Date within1YearDate = calender.getTime(); // sets within1YearDate to today's date minus 1 year

        inspectionCalender.set(Calendar.DAY_OF_MONTH, day);
        inspectionCalender.set(Calendar.MONTH, month - 1); // 0-JAN, 1-FEB, 2-MARCH , etc, so we have to subtract 1
        inspectionCalender.set(Calendar.YEAR, year);
        Date inspectionDate = inspectionCalender.getTime();

        if(inspectionDate.after(within1YearDate)) { //within 1 year of current date
            return true;
        }else{
            return false;
        }

    }

    private int getDaysAgo(int day, int month, int year){
        Calendar calender = Calendar.getInstance();
        Calendar inspectionCalender = Calendar.getInstance();
        Date startDate = new Date();
        int daysAgoCounter = 0;

        calender.setTime(startDate); // current date

        inspectionCalender.set(Calendar.DAY_OF_MONTH, day);
        inspectionCalender.set(Calendar.MONTH, month - 1); // 0-JAN, 1-FEB, 2-MARCH , etc, so we have to subtract 1
        inspectionCalender.set(Calendar.YEAR, year);

        Date inspectionDate = inspectionCalender.getTime();

        while(inspectionDate.before(startDate)){
            inspectionCalender.add(Calendar.DAY_OF_MONTH, 1);
            inspectionDate = inspectionCalender.getTime();
            daysAgoCounter++;
        }
        return daysAgoCounter;
    }

    public ViolationData getViolation(int i) {
        return violations.get(i);
    }

    public List<ViolationData> getViolations() {
        return violations;
    }

    public void setViolations(List<ViolationData> violations) {
        this.violations = violations;
    }

    @Override
    public String toString() {
        return "InspectionData{" +
                "Year='" + year + '\'' +
                ", Month='" + month + '\'' +
                ", Day='" + day + '\'' +
                ", InspType='" + inspType + '\'' +
                ", NumCritical='" + numCritical + '\'' +
                ", NumNonCritical=" + numNonCritical +
                ", HazardRating=" + hazardRating +
                ", VioLump=" + violations +
                '}';
    }
}
