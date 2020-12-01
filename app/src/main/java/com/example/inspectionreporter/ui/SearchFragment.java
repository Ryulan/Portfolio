package com.example.inspectionreporter.ui;

/**
 * Fragment to get the input from the user for their search query
 */

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.InspectionData;
import com.example.inspectionreporter.model.RestaurantDataManager;

public class SearchFragment extends AppCompatDialogFragment {

    // variables
    View view;
    private Context context = getActivity();
    private final String SHARED_PREF_LAST_SEARCH = "Last Search Options";
    private final String SHARED_PREF_SEARCH = "Search";
    private final String SHARED_PREF_SEARCH_HAZARD = "Hazards";
    private final String SHARED_PREF_SEARCH_OPERATOR = "Operator";
    private final String SHARED_PREF_SEARCH_VIOLATION_COUNT = "Violations in last year";
    private final String SHARED_PREF_SEARCH_FAVOURITE = "Favourite";

    Spinner hazardSpinner;
    Spinner violationOperatorSpinner;
    Spinner favouriteSpinner;

    String textEntry;
    String hazard;
    String violationOperator;
    String violationCount;
    String favourite;

    private RestaurantDataManager manager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        manager = RestaurantDataManager.getInstance(getActivity());

        //Create the view to show
        view = LayoutInflater.from(getActivity())
                .inflate(R.layout.save_search_data_fragment_layout, null);

        populateSpinners();

        //Buttons
        Button btnOK = (Button) view.findViewById(R.id.Search_btn_search);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setSearchFragBtnClicked(true);
                manager.setResetFragBtnClicked(false);

                // Saves Restaurant Search
                EditText editTextSearch = (EditText) view.findViewById(R.id.Search_txtIn_restaurantSearch);
                textEntry = editTextSearch.getText().toString();

                // Saves Hazard Level
                hazardSpinner = (Spinner) view.findViewById(R.id.Search_spinner_hazard);
                hazard = hazardSpinner.getSelectedItem().toString();

                // Saves Operator for Violation in last year
                violationOperatorSpinner = (Spinner) view.findViewById(R.id.Search_spinner_violationsYear);
                violationOperator = violationOperatorSpinner.getSelectedItem().toString();

                // Saves number of violations
                EditText editTextNumViolations = (EditText) view.findViewById(R.id.Search_txtIn_numViolations);
                String violationCount = editTextNumViolations.getText().toString();

                // Saves Favourite as True/False
                favouriteSpinner = (Spinner) view.findViewById(R.id.Search_spinner_favourite);
                favourite = favouriteSpinner.getSelectedItem().toString();

                // Saves them as shared preferences
                SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREF_LAST_SEARCH, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SHARED_PREF_SEARCH, textEntry);
                editor.putString(SHARED_PREF_SEARCH_HAZARD, hazard);
                editor.putString(SHARED_PREF_SEARCH_OPERATOR, violationOperator);
                editor.putString(SHARED_PREF_SEARCH_VIOLATION_COUNT, violationCount);
                editor.putString(SHARED_PREF_SEARCH_FAVOURITE, favourite);
                editor.apply();

                dismiss();

            }
        });

        Button btnReset = (Button) view.findViewById(R.id.Search_btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setSearchFragBtnClicked(false);
                manager.setResetFragBtnClicked(true);
                dismiss();
            }
        });

        Button btnCancel = (Button) view.findViewById(R.id.Search_btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

    }

    //citation: https://stackoverflow.com/questions/17311335/how-to-populate-a-spinner-from-string-array
    private void populateSpinners() {
        hazardSpinner = view.findViewById(R.id.Search_spinner_hazard);
        String[] hazardStrings = new String[] {"", getString(R.string.low), getString(R.string.moderate), getString(R.string.high)};
        ArrayAdapter<String> hazardAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, hazardStrings);
        hazardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hazardSpinner.setAdapter(hazardAdapter);

        violationOperatorSpinner = view.findViewById(R.id.Search_spinner_violationsYear);
        String[] violationStrings = new String[] {"", "\u2264", "\u2265", "<", ">", "="};
        ArrayAdapter<String> violationAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, violationStrings);
        violationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        violationOperatorSpinner.setAdapter(violationAdapter);

        favouriteSpinner = view.findViewById(R.id.Search_spinner_favourite);
        String[] favouriteStrings = new String[] {"", getString(R.string.True), getString(R.string.False)};
        ArrayAdapter<String> favouriteAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, favouriteStrings);
        favouriteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        favouriteSpinner.setAdapter(favouriteAdapter);

    }

}
