package com.example.inspectionreporter.ui;

/**
 * An activity that displays a list of all restaurants to the user, as well
 * as the number of issues and date of each restaurant's most recent inspection.
 * Clicking on a restaurant will launch the "SingleRestaurant" activity.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.DBAdapter;
import com.example.inspectionreporter.model.RestaurantData;
import com.example.inspectionreporter.model.RestaurantDataManager;

import java.util.ArrayList;
import java.util.List;

public class AllRestaurantListActivity extends AppCompatActivity {
    private RestaurantDataManager manager;
    private List<RestaurantData> restaurants = new ArrayList<>();
    public static final int ALL_RESTAURANT_LIST_CALLING_FUNCTION = 0;
    private DBAdapter db;
    private boolean refresh = false;

    // Shared Preference
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
        setContentView(R.layout.activity_all_restaurant_list);

        manager = RestaurantDataManager.getInstance(AllRestaurantListActivity.this);
        db = DBAdapter.getInstance(AllRestaurantListActivity.this);

        populateRestaurantList();
        populateListView();
        registerClickCallBack();
        setupToolbar();
        setupButtonToMap();
        setupSearchButton();

        // Search Options
        if(manager.getSearchFragBtnClicked()){
            extractSharedPreferenceSearchOption();
            updateList();
        }
    }

    /**
     * Used https://stackoverflow.com/questions/46587880/how-to-refresh-the-activitys-data-when-we-return-from-another-activity
     * to figure out how refresh an activity using "onResume()" and a boolean
     */
    @Override
    protected void onResume() {
        super.onResume();
        //refresh the list of restaurants
        if(refresh) {
            populateListView();
        }
    }

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, AllRestaurantListActivity.class);
    }

    private void setupToolbar() {
        // displays toolbar back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarRestaurantList);
        toolbar.setTitle(R.string.list_of_all_restaurants);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }


    //Populates the restaurants list
    private void populateRestaurantList() {
        for (int i = 0; i < manager.getSize() ; i++){
            RestaurantData sample = manager.getRestaurant(i);
            restaurants.add(sample);
        }
    }


    //Populates the ListView
    private void populateListView() {
        ArrayAdapter<RestaurantData> adapter = new AllRestListViewAdapter();
        ListView list = (ListView) findViewById(R.id.AllRest_list_viewAllRestaurants);
        list.setAdapter(adapter);
    }


    private void registerClickCallBack() {
        ListView list = (ListView) findViewById(R.id.AllRest_list_viewAllRestaurants);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                for(int i=0; i<manager.getSize(); i++)
                    if(restaurants.get(position).getTrackingNumber().contains(manager.getRestaurant(i).getTrackingNumber()))
                    {
                        Intent intent = SingleRestaurantActivity.makeLaunchIntent(AllRestaurantListActivity.this, i);
                        intent.putExtra(SingleRestaurantActivity.CALLING_FUNCTION_KEY, ALL_RESTAURANT_LIST_CALLING_FUNCTION);
                        startActivity(intent);
                        refresh = true;
                        break;
                    }
            }

        });
    }

    private void setupButtonToMap() {
        Button btn = (Button) findViewById(R.id.List_btn_goToMap);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch 'AllRestaurantList' activity, -1 so no info window is shown
                Intent intent = MapsActivity.makeLaunchIntent(AllRestaurantListActivity.this, -1);
                startActivity(intent);
                finish();
            }
        });
    }

    //citation: https://stackoverflow.com/questions/23786033/dialogfragment-and-ondismiss
    private void setupSearchButton(){
        Button btnSearch = (Button) findViewById(R.id.List_btn_save_search);
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

                        if(manager.getSearchFragBtnClicked()){
                            extractSharedPreferenceSearchOption();
                            updateList();
                        }
                        if(manager.getResetFragBtnClicked()){
                            extractSharedPreferenceSearchOption();
                            resetList();
                        }
                    }
                }, false);

            }
        });
    }

    private void extractSharedPreferenceSearchOption() {
        SharedPreferences prefs = this.getSharedPreferences(SHARED_PREF_LAST_SEARCH, Context.MODE_PRIVATE);
        textEntry = prefs.getString(SHARED_PREF_SEARCH, "empty");
        hazardEntry = prefs.getString(SHARED_PREF_SEARCH_HAZARD, "empty");
        operatorEntry = prefs.getString(SHARED_PREF_SEARCH_OPERATOR, "empty");
        violationCountEntry = prefs.getString(SHARED_PREF_SEARCH_VIOLATION_COUNT, "empty");
        favouriteEntry = prefs.getString(SHARED_PREF_SEARCH_FAVOURITE, "empty");
    }

    private void updateList() {
        // clears the restaurant array list
        restaurants.clear();

        Cursor c = db.getRestaurantIDs();
        if (c.moveToFirst()) {
            do {
                int i = c.getInt(DBAdapter.COL_ROWID);
                restaurants.add(manager.getRestaurant(i));
            } while(c.moveToNext());
        }
        populateListView();
    }
    private void resetList() {
        // clears the restaurant array list
        restaurants.clear();

        Cursor c = db.getAllRows();
        if (c.moveToFirst()) {
            do {
                int i = c.getInt(DBAdapter.COL_ROWID);
                restaurants.add(manager.getRestaurant(i));
            } while(c.moveToNext());
        }
        populateListView();
    }

    //Class for setting up each item in the list view of all the restaurants
    private class AllRestListViewAdapter extends ArrayAdapter<RestaurantData> {
         // Constructor
        public AllRestListViewAdapter() {

            super(AllRestaurantListActivity.this, R.layout.layout_all_restaurant_list_item, restaurants);

        }

        //Sets up and returns the view based on the restaurant's data
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            //Make sure we have a view to work with (in case we were given null)
            View itemView = convertView;
            if(itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.layout_all_restaurant_list_item, parent, false);
            }

            //Find restaurant to work with
            RestaurantData currentRestaurant = restaurants.get(position);

            //Fill the view

            ImageView imageRestaurantIcon = (ImageView) itemView.findViewById(R.id.AllRest_img_restaurantIcon);
            imageRestaurantIcon.setImageResource(currentRestaurant.getRestaurantIconID());

            ImageView imageHazardIcon = (ImageView) itemView.findViewById(R.id.AllRest_img_hazardIcon);
            if (currentRestaurant.getNumInspections() != 0) {
                imageHazardIcon.setImageResource(currentRestaurant.getMostRecentInsp().getHazardIcon());
            } else { //no inspection, set default icon to green
                imageHazardIcon.setImageResource(R.drawable.green_check);
            }

            TextView txtRestaurantName = (TextView) itemView.findViewById(R.id.AllRest_txt_restaurantName);
            txtRestaurantName.setText(currentRestaurant.getRestaurantName());

            TextView txtMostRecentInspInfo = (TextView) itemView.findViewById(R.id.AllRest_txt_MostRecentInspNumIssuesAndDate);
            if (currentRestaurant.getNumInspections() != 0) {

                txtMostRecentInspInfo.setTextColor(currentRestaurant.getMostRecentInsp().getHazardCol());
                //Japanese language support - different strings based on number of issues
                int totalNumIssues = currentRestaurant.getMostRecentInsp().getTotalNumIssues();
                if((totalNumIssues > 0) && (totalNumIssues < 10)) {
                    txtMostRecentInspInfo.setText(getString(
                            R.string.num_issues_and_date_less_than_ten_issues,
                            totalNumIssues,
                            currentRestaurant.getMostRecentInsp().getDateRecentFormat()));
                } else if((totalNumIssues > 0) && (totalNumIssues < 100)) {
                    txtMostRecentInspInfo.setText(getString(
                            R.string.num_issues_and_date_less_than_hundred_issues,
                            totalNumIssues,
                            currentRestaurant.getMostRecentInsp().getDateRecentFormat()));
                } else {
                    txtMostRecentInspInfo.setText(getString(
                            R.string.num_issues_and_date,
                            totalNumIssues,
                            currentRestaurant.getMostRecentInsp().getDateRecentFormat()));
                }

            } else { //no inspections
                txtMostRecentInspInfo.setTextColor(Color.DKGRAY);
                txtMostRecentInspInfo.setText(getString(R.string.no_inspections_message));
            }

            //change background color based on whether the restaurant is a favourite or not
            if(currentRestaurant.isFavourite()) {
                itemView.setBackgroundColor(0xFFFFFBC2); //light yellow
            } else {
                itemView.setBackgroundColor(Color.WHITE);
            }

            return itemView;

        }
    }
}