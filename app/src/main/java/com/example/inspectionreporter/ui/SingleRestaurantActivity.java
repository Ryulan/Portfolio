package com.example.inspectionreporter.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.DBAdapter;
import com.example.inspectionreporter.model.InspectionData;
import com.example.inspectionreporter.model.RestaurantData;
import com.example.inspectionreporter.model.RestaurantDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An activity that displays details about a selected restaurant, and lists all
 * of that restaurant's inspection reports to the user to choose from. Clicking
 * on an inspection will launch the "SingleInspection" activity.
 */

/**
 * STAR ICON CITATIONS:
 * Icons made by Pixel perfect from www.flaticon.com
 * https://www.flaticon.com/authors/pixel-perfect
 * https://www.flaticon.com/
 * https://icon54.com/
 * 
 */

public class SingleRestaurantActivity extends AppCompatActivity
{
    public static final String EXTRA_RESTAURANT_INDEX = "com.example.inspectionreporter.SingleRestaurant - restaurantIndex";
    public static final String CALLING_FUNCTION_KEY = "calling function";
    private final String SHARED_PREF_RESTAURANTS_INFO = "Info For All Restaurants";
    private final String SHARED_PREF_ITEM_FAVOURITE = "Favourite";


    private int restaurantIndex;
    private RestaurantData restaurant;
    private RestaurantDataManager manager;
    private List<InspectionData> inspections = new ArrayList<>();
    private int callingFunction;
    private DBAdapter db;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_restaurant);

        manager = RestaurantDataManager.getInstance(SingleRestaurantActivity.this);
        db = DBAdapter.getInstance(this);

        extractDataFromIntent();

        restaurant = manager.getRestaurant(restaurantIndex);
        inspections = restaurant.getInspections();

        setupTextNotInList();
        setupFavouriteButton();
        setupGPSButton();
        sortRestaurantInspectionListBydate();
        populateListView();
        setupToolBar();
    }

    private void setupToolBar() {
        // displays toolbar back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSingleRestaurant);
        toolbar.setTitle(R.string.restaurant_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setupTextNotInList()
    {
        //for activity
        TextView name = (TextView)findViewById(R.id.SingleRest_txt_RestaurantName);
        name.setText(getString(R.string.name)+ restaurant.getRestaurantName());

        TextView address = (TextView)findViewById(R.id.SingleRest_txt_restaurantAddress);
        address.setText(getString(R.string.address)+ restaurant.getAddress());

        Button btnGSP = (Button) findViewById(R.id.SingleRest_btn_restaurantGPSCoordinates);
        btnGSP.setPadding(0,0,0,0);
        btnGSP.setText(getString(R.string.gpss_coordinate)
                + restaurant.getLatitude()
                +  getString(R.string.comma)
                + restaurant.getLongitude()
                + ")");
    }

    private void setupFavouriteButton() {
        //get button
        final ImageButton btnFav = (ImageButton) findViewById(R.id.SingleRest_btn_favourite);

        //change image of button depending on whether the restaurant is a favourite or not
        if(restaurant.isFavourite()) {
            btnFav.setImageResource(R.drawable.star_gold);
        } else {
            btnFav.setImageResource(R.drawable.star_blank);
        }

        //favourite or unfavourite restaurant when button is clicked
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_RESTAURANTS_INFO, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();


        //favourite or unfavourite restaurant when button is clicked
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(restaurant.isFavourite()) {
                    btnFav.setImageResource(R.drawable.star_blank);
                    restaurant.setFavourite(false);
                    manager.getRestaurant(restaurantIndex).setFavourite(false);
                    manager.saveRestaurantFavouriteInSharedPref(restaurantIndex, restaurant.isFavourite());
                    db.updateFavourite(restaurantIndex, false);

                } else {

                    btnFav.setImageResource(R.drawable.star_gold);
                    restaurant.setFavourite(true);
                    manager.getRestaurant(restaurantIndex).setFavourite(true);
                    manager.saveRestaurantFavouriteInSharedPref(restaurantIndex, restaurant.isFavourite());
                    db.updateFavourite(restaurantIndex, true);
                }

            }
        });

    }

    private void setupGPSButton(){
        Button btnGSP = (Button) findViewById(R.id.SingleRest_btn_restaurantGPSCoordinates);
        btnGSP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sets restaurantIndex to the proper index
                extractDataFromIntent();

                //launch 'AllRestaurantList' activity
                if (callingFunction == MapsActivity.MAPS_CALLING_FUNCTION) {
                    finish();
                }
                else {
                    Intent intent = MapsActivity.makeLaunchIntent(SingleRestaurantActivity.this, restaurantIndex);
                    startActivity(intent);
                }
            }
        });

    }

    public static Intent makeLaunchIntent(Context context, int position) {
        Intent intent = new Intent(context, SingleRestaurantActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_INDEX, position);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        restaurantIndex = intent.getIntExtra(EXTRA_RESTAURANT_INDEX, 0);
        callingFunction = intent.getIntExtra(CALLING_FUNCTION_KEY,
                AllRestaurantListActivity.ALL_RESTAURANT_LIST_CALLING_FUNCTION);
    }


    private void sortRestaurantInspectionListBydate()
    {

        Collections.sort(inspections, new Comparator<InspectionData>() {
            //TODO: put numbers into constants
            @Override
            public int compare(InspectionData current,  InspectionData next) {
                int inspDate1 = (current.getYear()*10000)+(current.getMonth()*100)+current.getDay();
                int inspDate2 = (next.getYear()*10000)+(next.getMonth()*100)+next.getDay();
                return inspDate2-inspDate1;
            }

        });
    }

    private void populateListView() {
        ArrayAdapter<InspectionData> adapter = new SingleRestListViewAdapter();
        ListView list = (ListView) findViewById(R.id.SingleRest_list_inspections);
        list.setAdapter(adapter);

        //launch the 'SingleInspection' activity
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = SingleInspectionActivity.makeLaunchIntent(SingleRestaurantActivity.this);
                intent.putExtra(SingleInspectionActivity.EXTRA_RESTAURANT_INDEX, restaurantIndex);
                intent.putExtra(SingleInspectionActivity.EXTRA_INSPECTION_INDEX, position);
                startActivity(intent);
            }
        });
    }


    private class SingleRestListViewAdapter extends ArrayAdapter<InspectionData> {
        public SingleRestListViewAdapter() {
            super(SingleRestaurantActivity.this, R.layout.layout_single_restaurant_list_item, inspections);

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            //make sure we have a view to work with (in case we were given null)
            View itemView = convertView;
            if(itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.layout_single_restaurant_list_item, parent, false);
            }

            //fill in view
            ImageView hazard_rating = (ImageView) itemView.findViewById(R.id.hazard_rating);
            hazard_rating.setImageResource(inspections.get(position).getHazardIcon());

            TextView date = (TextView) itemView.findViewById(R.id.SingleRest_txt_date);
            date.setText(inspections.get(position).getDateRecentFormat());

            TextView critical = (TextView) itemView.findViewById(R.id.SingleRest_txt_numCriticalIssues);
            critical.setText(getString(
                    R.string.critical_issues,
                    inspections.get(position).getNumCritical()));

            TextView non_critical = (TextView) itemView.findViewById(R.id.SingleRest_txt_numNonCriticalIssues);
            non_critical.setText(getString(
                    R.string.non_critical_issues,
                    inspections.get(position).getNumNonCritical()));


            return itemView;
        }
    }
}



























