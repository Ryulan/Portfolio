package com.example.inspectionreporter.ui;

/**
 * Fragment used to inform user that already downloaded data
 * is being saved to the app.
 */


import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.inspectionreporter.R;

public class SavingFragment extends AppCompatDialogFragment {

    //animation
    private final String ROTATION = "rotation";
    private final int ROTATION_DURAION = 1000;

    public Dialog onCreateDialog(Bundle savedInstanceState) {


        //create view
        final View viewSave = LayoutInflater.from(getActivity()).inflate(R.layout.saving_fragment_layout, null);

        playLoadingAnimation(viewSave);

        // Build the alert dialog
        return new AlertDialog.Builder(getActivity())
                .setView(viewSave)
                .create();


    }

    private void playLoadingAnimation(View view) {
        //get image
        View downloadIconView = view.findViewById(R.id.SavingFrag_image_download);

        //set image to default position
        downloadIconView.setRotation(0);

        //animation for rotation
        ObjectAnimator rotateDownloadIcon = ObjectAnimator
                .ofFloat(downloadIconView, ROTATION, 0, 360)
                .setDuration(ROTATION_DURAION);

        //remove default acceleration for smoother
        //rotation animation
        rotateDownloadIcon.setInterpolator(new LinearInterpolator());

        //play animation indefinitely
        rotateDownloadIcon.setRepeatCount(Animation.INFINITE);
        rotateDownloadIcon.start();
    }
}
