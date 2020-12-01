package com.example.inspectionreporter.ui;

/**
 * Class that displays the favourite restaurants after a download
 * and saves it
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.RestaurantData;
import com.example.inspectionreporter.model.RestaurantDataManager;

import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private RestaurantDataManager manager=RestaurantDataManager.getInstance(FavouriteActivity.this);
    private List<RestaurantData> favouriteRestaurant=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        int count=0;

        for(int i=0; i<manager.getSize(); i++){
            if(manager.getRestaurant(i).isFavourite()){
                count++;
                if( manager.getRestaurant(i).isFavouriteUpdated()){

                    favouriteRestaurant.add(manager.getRestaurant(i));
                }
            }
        }

        //favouriteRestaurant=UrlDataFetcher.getFavouriteRestaurants();
        populateListView();
        setupOkButtonAndTextview(count);
        setupToolbar();
    }

    private void setupToolbar() {
        // displays toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarFavourite);
        toolbar.setTitle(R.string.favourite);
        setSupportActionBar(toolbar);
    }

    private void setupOkButtonAndTextview(int count) {
        TextView txt=(TextView) findViewById(R.id.info);
        txt.setText(getString(R.string.of_your_fav_rest_updated,
                favouriteRestaurant.size(),
                "/",
                count));

        Button btn = (Button) findViewById(R.id.fav_btn_ok);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouriteActivity.this, MapsActivity.class));
            }
        });

    }

    private void populateListView() {
        ArrayAdapter<RestaurantData> adapter = new FavListViewAdapter();
        ListView list = (ListView) findViewById(R.id.fav_list_viewAllRestaurants);
        list.setAdapter(adapter);
    }



    //Class for setting up each item in the list view of all the restaurants
    private class FavListViewAdapter extends ArrayAdapter<RestaurantData> {


        // Constructor
        public FavListViewAdapter() {
            super(FavouriteActivity.this, R.layout.layout_favourite, favouriteRestaurant);
        }

        //Sets up and returns the view based on the restaurant's data
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            //Make sure we have a view to work with (in case we were given null)
            View itemView = convertView;
            if(itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.layout_favourite, parent, false);
            }

            //Find restaurant to work with
            final RestaurantData currentRestaurant = favouriteRestaurant.get(position);

            //Fill the view
            if(currentRestaurant.isFavourite() && currentRestaurant.isFavouriteUpdated())
            {
                ImageView imageRestaurantIcon = (ImageView) itemView.findViewById(R.id.fav_img_restaurantIcon);
                imageRestaurantIcon.setImageResource(currentRestaurant.getRestaurantIconID());

                ImageView imageHazardIcon = (ImageView) itemView.findViewById(R.id.fav_img_hazardIcon);
                if (currentRestaurant.getNumInspections() != 0) {
                    imageHazardIcon.setImageResource(currentRestaurant.getMostRecentInsp().getHazardIcon());
                } else { //no inspection, set default icon to green
                    imageHazardIcon.setImageResource(R.drawable.green_check);
                }

                TextView txtRestaurantName = (TextView) itemView.findViewById(R.id.fav_txt_restaurantName);
                txtRestaurantName.setText(currentRestaurant.getRestaurantName());

                TextView txtMostRecentInspInfo = (TextView) itemView.findViewById(R.id.fav_txt_MostRecentInspNumIssuesAndDate);
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
            }




            return itemView;
        }
    }
}
