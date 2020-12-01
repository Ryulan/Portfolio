package com.example.inspectionreporter.ui;

/**
 * Fragment used to inform user that the application is currently downloading
 * updated data. Contains a button that cancels data download when clicked on.
 */

/**
 * Code Citation:
 * Used https://stackoverflow.com/questions/15021372/repeat-android-animation
 * to figure out how to play an animation indefinitely using this line of code:
 * animation.setRepeatCount(Animation.INFINITE);
 *
 * Used https://stackoverflow.com/questions/1634252/how-to-make-a-smooth-image-rotation-in-android
 * to figure out how to remove rotation animation's default acceleration using this line of code:
 * animation.setInterpolator(new LinearInterpolator());
 *
 * Used course textbook "Android Programming: The Big Nerd Ranch Guide, 3rd edition"
 * to learn how to use AsyncTask.
 *
 * Icon Citation:
 * Refresh icon made by Roundicons from http://www.flaticon.com.
 * https://roundicons.com/
 * https://www.flaticon.com/
 */

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.inspectionreporter.R;
import com.example.inspectionreporter.model.DBAdapter;
import com.example.inspectionreporter.model.RestaurantDataManager;
import com.example.inspectionreporter.model.UpdateHandler;
import com.example.inspectionreporter.model.UrlDataFetcher;


public class DownloadFragment extends AppCompatDialogFragment {

    //animation
    private final String ROTATION = "rotation";
    private final int ROTATION_DURAION = 1000;


    //update data
    private UpdateData updateData;
    private UrlDataFetcher urlDataFetcher;
    private UpdateHandler updateHandler;
    private RestaurantDataManager manager;
    private boolean returnFlag = false;



    public Dialog onCreateDialog(Bundle savedInstanceState) {
        manager = RestaurantDataManager.getInstance(getActivity());
        urlDataFetcher = new UrlDataFetcher(getActivity());
        updateHandler = new UpdateHandler(getActivity());


        //download and update restaurant data while dialogue is open
        updateData = new UpdateData();
        updateData.execute();


        //create view
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.downloading_fragment_layout, null);

        setupCancelButton(view);
        playLoadingAnimation(view);


        // Build the alert dialog
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();


    }


    private void setupCancelButton(View view) {
        Button btnCancel = (Button) view.findViewById(R.id.DownloadFrag_btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel downloading and updating data
                updateData.cancel(true);

                // sets flag to true
                returnFlag = true;

                //end dialog
                dismiss();
            }
        });
    }

    private void playLoadingAnimation(View view) {
        //get image
        View downloadIconView = view.findViewById(R.id.DownloadFrag_image_download);

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


    //Inner class used to update restaurant data
    private class UpdateData extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void...params) {
            //download updated data
            urlDataFetcher.loadRestaurants();
            updateHandler.loadUpdateDateFromUrl();
            dismiss();


            if(returnFlag){
                Log.d("Download Fragment", "Return");
                return null;
            }

            // creates saving alert dialog
            FragmentManager fragmentManager = getFragmentManager();
            SavingFragment dialog = new SavingFragment();
            dialog.show(fragmentManager, "SavingFragment");

            //save update data only when updated has been successfully downloaded
            urlDataFetcher.saveUpdatedRestaurants();
            manager.updateDatabase();
            updateHandler.saveUpdateDate();

            dialog.dismiss();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
