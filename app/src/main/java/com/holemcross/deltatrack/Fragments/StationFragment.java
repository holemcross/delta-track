package com.holemcross.deltatrack.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.holemcross.deltatrack.data.TrainArrival;
import com.holemcross.deltatrack.exceptions.CtaServiceException;
import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.services.CtaService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import helpers.Constants;
import helpers.KeyManager;
import helpers.Serializer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String Log_TAG = StationFragment.class.getSimpleName();
    private static final String ARG_PARAM_MAPID = "mapId";
    private final int DEFAULT_MAP_ID = 40730; // Washington/Wells Station - Expect to remove default station on release
    private static final int MAX_TASK_RETRIES = 4;

    private int mStationMapId;
    private Date mLastArrivalsRefresh;
    private ArrayList<TrainArrival> mArrivals;
    private int mFetchTaskRetryCount;

    private OnFragmentInteractionListener mListener;

    public StationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mapId
     * @return A new instance of fragment StationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StationFragment newInstance(int mapId) {
        StationFragment fragment = new StationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_MAPID, mapId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFetchTaskRetryCount = 0;

        if(savedInstanceState != null){
            // restore state data
            mStationMapId = savedInstanceState.getInt(Constants.StationFragment.STATE_MAPID, DEFAULT_MAP_ID);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dow mon dd hh:mm:ss zzz yyyy", Locale.ENGLISH);
            Date restoredDate = new Date();
            try{
                restoredDate = dateFormat.parse(savedInstanceState.getString(Constants.StationFragment.STATE_LAST_REFRESH));
            }catch(ParseException ex){
                Log.e(Log_TAG, ex.getMessage());
            }
            mLastArrivalsRefresh = restoredDate;

            mArrivals = (ArrayList<TrainArrival>) Serializer.deserializeObject(savedInstanceState.getByteArray(Constants.StationFragment.STATE_ARRIVALS));

            Log.v(Log_TAG, "Restored Station Fragment from saved state.");
            return;
        }

        if (getArguments() != null) {
            mStationMapId = getArguments().getInt(ARG_PARAM_MAPID, DEFAULT_MAP_ID);
        }else{
            SharedPreferences prefs = getContext().getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME, Context.MODE_PRIVATE);
            mStationMapId = prefs.getInt(Constants.StationFragment.STATE_MAPID, DEFAULT_MAP_ID);
        }

        mArrivals = new ArrayList<TrainArrival>();
        FetchArrivalsTask task = new FetchArrivalsTask();
        task.execute(mStationMapId);
        Log.v(Log_TAG, "Task has executed!");

        saveStateToPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_station, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.StationFragment.STATE_MAPID, mStationMapId);
        outState.putString(Constants.StationFragment.STATE_LAST_REFRESH, mLastArrivalsRefresh.toString());
        outState.putByteArray(Constants.StationFragment.STATE_ARRIVALS, Serializer.serializeObject(mArrivals));

        super.onSaveInstanceState(outState);
    }

    public void stationChanged(int newMapId){
        mStationMapId = newMapId;
        FetchArrivalsTask task = new FetchArrivalsTask();
        task.execute(mStationMapId);
    }

    private void updateArrivalsUi(){
        TextView arrivalTextView = (TextView) getActivity().findViewById(R.id.arrival_text);

        String arrivalsText = "";

        for (TrainArrival arrival: mArrivals
                ) {
            arrivalsText += "Train:"+ arrival.runNumber + ", ";
        }
        arrivalTextView.setText(arrivalsText);
    }

    private void saveStateToPreferences(){
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.StationFragment.STATE_MAPID, mStationMapId);
        editor.commit();
    }

    private class FetchArrivalsTask extends AsyncTask<Integer, Void, ArrayList<TrainArrival>> {

        @Override
        protected ArrayList<TrainArrival> doInBackground(Integer...mapIds){
            Log.v(Log_TAG, "Entered Task.doInBackground()!");
            if(mapIds.length == 0){
                // No Map Ids
                return new ArrayList<TrainArrival>();
            }
            int mapId = mapIds[0];
            String apiKey = KeyManager.GetCtaApiKey(getContext());

            CtaService service = new CtaService();
            ArrayList<TrainArrival> resultList = null;
            try{
                resultList = service.GetTrainArrivalsForMapId(mapId, apiKey);
            }catch(CtaServiceException ex){
                return null;
            }catch(Exception ex){
                return null;
            }

            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<TrainArrival> trainArrivals) {
            super.onPostExecute(trainArrivals);
            if(trainArrivals != null){

                for ( TrainArrival train: trainArrivals
                     ) {
                    Log.v(Log_TAG, "Train " + train.route.toString() + " Arrival Time: " + train.arrivalTime);
                }
                mLastArrivalsRefresh = new Date();
                mArrivals = trainArrivals;
                updateArrivalsUi();
            }else{
                // Failed to fetch results
                mFetchTaskRetryCount++;

                if(mFetchTaskRetryCount < MAX_TASK_RETRIES){
                    // Perform Retry
                    FetchArrivalsTask task = new FetchArrivalsTask();
                    task.execute(mStationMapId);
                }
            }
        }
    }
}
