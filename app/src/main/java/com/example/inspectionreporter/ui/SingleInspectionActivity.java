package com.example.inspectionreporter.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.InspectionData;
import com.example.inspectionreporter.model.RestaurantDataManager;
import com.example.inspectionreporter.model.ViolationData;

import java.util.List;

/**
 * Activity to show all relevant details about a single violation
 * that a restaurant gets to the user.
 */

public class SingleInspectionActivity extends AppCompatActivity {
    public static final String EXTRA_RESTAURANT_INDEX = "com.example.inspectionreporter.SingleInspection - restaurant";
    public static final String EXTRA_INSPECTION_INDEX = "com.example.inspectionreporter.SingleInspection - inspection";
    private int resIndex;
    private int inIndex;
    private List<InspectionData> inspectionList;
    private RestaurantDataManager manager;
    private List<ViolationData> violations;
    private boolean noViolations = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_inspection);

        setupToolBar();
        extractDataFromIntent();
        initVariables();
        populateText();
        if (!noViolations) {
            populateList();
            registerListClick();
        }
    }

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, SingleInspectionActivity.class);
    }

    private void setupToolBar() {
        // displays toolbar back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSingleInspection);
        toolbar.setTitle(R.string.inspection_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void extractDataFromIntent() {
        Intent i = getIntent();
        resIndex = i.getIntExtra(EXTRA_RESTAURANT_INDEX, 0);
        inIndex = i.getIntExtra(EXTRA_INSPECTION_INDEX, 0);
    }

    private void initVariables() {
        manager = RestaurantDataManager.getInstance(SingleInspectionActivity.this);
        inspectionList = manager.getRestaurant(resIndex).getInspections();
        violations = inspectionList.get(inIndex).getViolations();
        if (violations == null) {
            noViolations = true;
        }
    }

    private void populateText() {
        InspectionData id = inspectionList.get(inIndex);

        TextView txt = findViewById(R.id.SingleInsp_txt_numCritIssues);
        txt.setText(getString(
                R.string.critical_issues,
                id.getNumCritical()));

        txt = findViewById(R.id.SingleInsp_txt_numNonCritIssues);
        txt.setText(getString(
                R.string.non_critical_issues,
                id.getNumNonCritical()));

        txt = findViewById(R.id.SingleInsp_txt_val_HazardLevel);
        txt.setText(id.getHazardRatingStrForUI());

        txt = findViewById(R.id.SingleInsp_txt_val_inspectionType);
        txt.setText(id.getInspType());

        String strFormat = getString(R.string.inspection_date);
        txt = findViewById(R.id.SingleInsp_txt_inspectionDate);
        txt.setText(String.format(strFormat, id.getMonthName(id.getMonth()), id.getDay(), id.getYear()));

        ImageView img = findViewById(R.id.SingleInsp_img_hazardIcon);
        img.setImageResource(id.getHazardIcon());

        if (noViolations) {
            txt = findViewById(R.id.SingleInsp_txt_noViolations);
            txt.setText(R.string.no_violations);
        }

    }

    private void populateList() {
        ArrayAdapter<ViolationData> adapter = new MyListAdapter();
        ListView list = findViewById(R.id.SingleInsp_list_violations);
        list.setAdapter(adapter);
    }

    public void registerListClick()  {
        ListView list = findViewById(R.id.SingleInsp_list_violations);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InspectionData id = inspectionList.get(inIndex);
                Toast.makeText(SingleInspectionActivity.this, violations.get(i).getViolation(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<ViolationData> {
        public MyListAdapter() {
            super(SingleInspectionActivity.this, R.layout.layout_single_inspection_list_item, violations);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.layout_single_inspection_list_item, parent, false);
            }
            ViolationData violation = violations.get(position);

            TextView text = itemView.findViewById(R.id.SingleInsp_txt_violationShortDesc);
            text.setText(violation.getViolationShortDescription());

            ImageView img = itemView.findViewById(R.id.SingleInsp_img_violationSeverity);
            img.setImageResource(violation.getViolationHazardIcon());

            img = itemView.findViewById(R.id.SingleInsp_img_typeOfViolation);
            img.setImageResource(violation.getViolationTypeIcon());

            return itemView;
        }
    }
}