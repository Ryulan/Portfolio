package com.example.inspectionreporter.ui;

/**
 * Fragment used to ask user whether to download the latest
 * update or not.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.RestaurantDataManager;

public class GetUpdateFragment extends AppCompatDialogFragment {

    private RestaurantDataManager manager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        manager = RestaurantDataManager.getInstance(getActivity());

        //Create the view to show
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.get_update_fragment_layout, null);


        //Buttons
        Button btnYes = (Button) view.findViewById(R.id.GetUpdate_btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setAlreadyAskToUpdateRestaurants(true);
                setupDownloadFragmentMessage();
                dismiss();
            }
        });

        Button btnNo = (Button) view.findViewById(R.id.GetUpdate_btn_no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setAlreadyAskToUpdateRestaurants(true);

                //end dialog
                dismiss();
            }
        });


        //Build the alert dialog
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    private void setupDownloadFragmentMessage() {
        FragmentManager manager = getFragmentManager();
        DownloadFragment dialog = new DownloadFragment();
        dialog.show(manager, "DownloadFragment");
    }
}
