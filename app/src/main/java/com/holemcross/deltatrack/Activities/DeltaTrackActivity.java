package com.holemcross.deltatrack.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.data.repository.StationRepository;
import com.holemcross.deltatrack.data.Station;
import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;
import com.holemcross.deltatrack.exceptions.CtaServiceException;
import com.holemcross.deltatrack.fragments.SettingsFragment;
import com.holemcross.deltatrack.fragments.StationFragment;
import com.holemcross.deltatrack.services.CtaService;

import java.util.ArrayList;

import helpers.Constants;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DeltaTrackActivity extends AppCompatActivity
        implements StationFragment.OnStationFragmentInteractionListener,
        SettingsFragment.OnSettingsFragmentInteractionListener {

    private final String LOG_TAG = DeltaTrackActivity.class.getSimpleName();
    private final int DEFAULT_MAP_ID = 40730; // Washington/Wells Station - Expect to remove default station on release
    private final String ARG_NAV_TO_DASHBOARD = "NAV_TO_DASHBOARD";
    private final String ARG_DISPLAY_TOAST = "DISPLAY_TOAST";
    private ArrayList<Station> mStations;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    //private final Runnable mHidePart2Runnable = new Runnable() {
    //    @SuppressLint("InlinedApi")
    //    @Override
    //    public void run() {
    //        // Delayed removal of status and navigation bar
//
    //        // Note that some of these constants are new as of API 16 (Jelly Bean)
    //        // and API 19 (KitKat). It is safe to use them, as they are inlined
    //        // at compile-time and do nothing on earlier devices.
    //        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
    //                | View.SYSTEM_UI_FLAG_FULLSCREEN
    //                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    //                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    //                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    //                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    //    }
    //};
    private View mControlsView;
    //private final Runnable mShowPart2Runnable = new Runnable() {
    //    @Override
    //    public void run() {
    //        // Delayed display of UI elements
    //        ActionBar actionBar = getSupportActionBar();
    //        if (actionBar != null) {
    //            actionBar.show();
    //        }
    //        //mControlsView.setVisibility(View.VISIBLE);
    //    }
    //};
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //if (AUTO_HIDE) {
            //    delayedHide(AUTO_HIDE_DELAY_MILLIS);
            //}
            Log.v(LOG_TAG,"Touch has been detected!");
            return false;
        }
    };

    public void refreshStations(){
        Log.d(LOG_TAG, "Refreshing Stations.");
        FetchStationsTask task = new FetchStationsTask();
        task.execute(ARG_DISPLAY_TOAST);
    }

    public void changeStation(Station station){
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putInt(Constants.StationFragment.STATE_MAPID, station.mapId);
        spEditor.commit();

        // Load Station Fragment

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StationFragment stationFragment = StationFragment.newInstance(station.mapId);
        fragmentTransaction.replace(R.id.fullscreen_content, stationFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delta_track);

        // Hide Action Bar
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();
        if(savedInstanceState != null){
            // Already loaded content. Skip
            return;
        }
        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        show();
        // Database Checks
        DeltaTrackDbHelper dbHelper = new DeltaTrackDbHelper(getApplicationContext());

        // Init DB
        dbHelper.onCreate(dbHelper.getWritableDatabase());
        //dbHelper.dropDatabase(dbHelper.getWritableDatabase());
        Log.d(LOG_TAG, "Stations exist in DB. Loading them into memory.");

        // Get list from DB
        StationRepository stationRepo = new StationRepository(dbHelper);
        mStations = stationRepo.getAllStations();

        boolean hasStations = true;
        if(mStations == null || mStations.size() <= 0)
        {
            hasStations = false;
            // Perform Fetch
            Log.d(LOG_TAG, "Stations do not exist in DB. Fetching.");
            FetchStationsTask task = new FetchStationsTask();
            task.execute(ARG_NAV_TO_DASHBOARD);
        }

        if(hasStations){
            goToDashboard();
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void goToDashboard(){

        show();

        // Get Current MapId
        SharedPreferences pref = this.getPreferences(MODE_PRIVATE);
        int currentMapId = pref.getInt(Constants.StationFragment.STATE_MAPID, DEFAULT_MAP_ID);

        // Create Station Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StationFragment stationFragment = StationFragment.newInstance(currentMapId);
        fragmentTransaction.add(R.id.fullscreen_content, stationFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void toggle() {
        //if (mVisible) {
        //    hide();
        //} else {
        //    show();
        //}
        Log.v(LOG_TAG, "Click Event DETECTED!");
    }

    private void hide() {
        // Hide UI first
        mContentView.setSystemUiVisibility(0);
        ActionBar actionBar = getSupportActionBar();
        //if (actionBar != null) {
        //    actionBar.hide();
        //}
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        //mHideHandler.removeCallbacks(mShowPart2Runnable);
        //mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        //mHideHandler.removeCallbacks(mHidePart2Runnable);
        //mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    public void onStationFragmentTouch() {
        // Do nothing
        // Toggle Menu

        //show();
        hide();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SettingsFragment settingsFragment = SettingsFragment.newInstance(mStations);
        fragmentTransaction.replace(R.id.fullscreen_content, settingsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onSettingsFragmentInteraction() {
        // Stub, Leave blank
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private boolean dbHasStations(){
        // TODO IMPLEMENT
        DeltaTrackDbHelper helper = new DeltaTrackDbHelper(getApplicationContext());

        return true;
    }

    private class FetchStationsTask extends AsyncTask<String, Void, ArrayList<Station>> {

        private ProgressDialog mDialog;
        private boolean mPerformNavigation;
        private boolean mDisplayToast;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = ProgressDialog.show(DeltaTrackActivity.this, "Getting station information", "Please wait...", true);
        }

        @Override
        protected ArrayList<Station> doInBackground(String... args) {
            Log.v(LOG_TAG, "Entered Task.doInBackground()!");
            if(args.length > 0){
                for (String arg: args
                     ) {
                    if(arg == ARG_NAV_TO_DASHBOARD){
                        mPerformNavigation = true;
                    }
                    if(arg == ARG_DISPLAY_TOAST){
                        mDisplayToast = true;
                    }
                }
            }

            CtaService service = new CtaService();
            ArrayList<Station> resultList = null;
            try {
                resultList = service.GetStations();
            } catch (CtaServiceException ex) {
                return null;
            } catch (Exception ex) {
                return null;
            }

            return resultList;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<Station> stations) {
            super.onPostExecute(stations);
            if (stations != null) {
                if(stations.size() > 0){
                    mStations = stations;

                    DeltaTrackDbHelper dbHelper = new DeltaTrackDbHelper(getApplicationContext());
                    // Drop DB
                    dbHelper.dropDatabase(dbHelper.getWritableDatabase());
                    // Rebuild and update DB
                    dbHelper.onCreate(dbHelper.getWritableDatabase());
                    StationRepository stationRepo = new StationRepository(dbHelper);
                    stationRepo.insertStations(mStations);

                    mDialog.dismiss();

                    if(mDisplayToast){
                        Toast toast = Toast.makeText(getApplicationContext(), "Stations refresh complete.", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    if(mPerformNavigation){
                        goToDashboard();
                    }
                }
            }
        }
    }
}
