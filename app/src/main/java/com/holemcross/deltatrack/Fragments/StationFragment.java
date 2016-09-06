package com.holemcross.deltatrack.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.holemcross.deltatrack.data.Station;
import com.holemcross.deltatrack.data.TrainArrival;
import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;
import com.holemcross.deltatrack.data.repository.StationRepository;
import com.holemcross.deltatrack.exceptions.CtaServiceException;
import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.services.CtaService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.Locale;

import helpers.Constants;
import helpers.KeyManager;
import helpers.Serializer;
import helpers.UiHelper;

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
    private static final int MAX_ARRIVALS_PER_PAGE = 5;
    private static final long VALID_ARRIVED_TRAIN_TIME_BUFFER = 60000; //Time in ms to consider arrival valid after arrival time occurs

    private int mStationMapId;
    private Date mLastArrivalsRefresh;
    private ArrayList<TrainArrival> mArrivals;
    private ArrivalsListAdapter mArrivalsAdapter;
    private ListView mArrivalsListView;
    private int mRefreshArrivalsDelay; // In Seconds
    private int mIteratePageDelay; // In Seconds
    private int mClockUpdateDelay; // In Seconds
    private int mFetchTaskRetryCount;
    private int mCurrentPage = 0;

    private final Handler mRefreshHandler = new Handler();
    private OnFragmentInteractionListener mListener;


    /////////////////////////////////////////
    //      Runnables

    private final Runnable refreshArrivals = new Runnable() {
        @Override
        public void run() {
            mLastArrivalsRefresh = new Date();
            mRefreshHandler.postDelayed(this, mRefreshArrivalsDelay * 1000);
            Log.v(Log_TAG,"Refreshing arrival times.");
            FetchArrivalsTask task = new FetchArrivalsTask();
            task.execute(mStationMapId);
        }
    };

    private final Runnable progressToNextPage = new Runnable() {
        @Override
        public void run() {
            mRefreshHandler.postDelayed(this, mIteratePageDelay * 1000);
            Log.v(Log_TAG,"Iterating Page.");
            iteratePage();
        }
    };

    private final Runnable updatesClock = new Runnable() {
        @Override
        public void run() {
            updateClockTime();
            mRefreshHandler.postDelayed(this, mClockUpdateDelay * 1000);
        }
    };

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
        mRefreshArrivalsDelay = 60;
        mIteratePageDelay = 10;
        mClockUpdateDelay = 5;
        mArrivals = new ArrayList<TrainArrival>();

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

        }else{
            if (getArguments() != null) {
                mStationMapId = getArguments().getInt(ARG_PARAM_MAPID, DEFAULT_MAP_ID);
            }else{
                SharedPreferences prefs = getContext().getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME, Context.MODE_PRIVATE);
                mStationMapId = prefs.getInt(Constants.StationFragment.STATE_MAPID, DEFAULT_MAP_ID);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Station Name
        DeltaTrackDbHelper helper = new DeltaTrackDbHelper(getContext());
        StationRepository stationRepository = new StationRepository(helper);
        Station currentStation = stationRepository.getStationByMapId(mStationMapId);
        String stationNameText = "Station Name";
        if(currentStation != null){
            stationNameText = currentStation.stationName;
        }
        TextView stationNameTextView = (TextView)getActivity().findViewById(R.id.dashHeaderStationNameLabel);
        if(stationNameTextView != null){
            stationNameTextView.setText(stationNameText);
        }

        // Set Last Update
        updateLastUpdateTime();

        // Set Clock
        updateClockTime();

        if(mArrivalsListView == null){
            mArrivalsListView = (ListView)getView().findViewById(R.id.dashArrivalsListView);
        }
        if(mArrivalsAdapter == null){
            mArrivalsAdapter = new ArrivalsListAdapter(mArrivals);
        }
        mArrivalsListView.setAdapter(mArrivalsAdapter);
        // Activate Arrival Refresh Timer
        mRefreshHandler.post(refreshArrivals);
        mRefreshHandler.post(progressToNextPage);
        mRefreshHandler.post(updatesClock);

        // Update UI
        updateArrivalsUi();
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
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveStateToPreferences();
        mRefreshHandler.removeCallbacks(refreshArrivals);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Add back handlers
        mRefreshHandler.post(refreshArrivals);
        mRefreshHandler.post(progressToNextPage);
        mRefreshHandler.post(updatesClock);
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
        Log.v(Log_TAG, "Refreshing Arrivals UI");
        mArrivalsAdapter.updateArrayList(getArrivalsForPage(mCurrentPage));

        updateDisplayMessages();
    }

    private void updateDisplayMessages(){

        // Check if No Arrivals in list
        TextView noArrivalsTextView = (TextView)getActivity().findViewById(R.id.dash_no_arrivals_display_label);
        if(noArrivalsTextView != null){
            if(getValidTrainsList().size() > 0){
                if(noArrivalsTextView.getVisibility() == View.VISIBLE){
                    noArrivalsTextView.setVisibility(View.GONE);
                }
            }else{
                noArrivalsTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveStateToPreferences(){
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.StationFragment.STATE_MAPID, mStationMapId);
        editor.commit();
    }

    private void updateLastUpdateTime(){
        TextView lastUpdateTextView = (TextView) getActivity().findViewById(R.id.dashHeaderUpdateLabel);
        if(lastUpdateTextView != null){

            if(mLastArrivalsRefresh == null){
                lastUpdateTextView.setText("Last Updated Never");
            }else{
                mLastArrivalsRefresh = new Date();
                SimpleDateFormat formatter =  new SimpleDateFormat("h:mmaa");
                String lastUpdateText = "Last Updated " + formatter.format(mLastArrivalsRefresh);
                lastUpdateTextView.setText(lastUpdateText);
            }
        }

    }

    private void updateClockTime(){
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, h:mmaa");
        TextView clockTextView = (TextView) getActivity().findViewById(R.id.dashHeaderClockLabel);
        if(clockTextView != null){
            clockTextView.setText(formatter.format(now));
        }
    }

    private void iteratePage(){
        if(getArrivalsForPage(mCurrentPage+1).size() > 0){
            mCurrentPage++;
        }else{
            mCurrentPage = 0;
        }
        updateArrivalsUi();
    }

    private ArrayList<TrainArrival> getArrivalsForPage(int pageNumber){

        if(pageNumber >= getNumberPages()){

            return new ArrayList<TrainArrival>();
        }
        ArrayList<TrainArrival> arrivalsList = getValidTrainsList();
        int startIndex = pageNumber * MAX_ARRIVALS_PER_PAGE;
        int endIndex = startIndex + MAX_ARRIVALS_PER_PAGE > arrivalsList.size() ? arrivalsList.size() : startIndex + MAX_ARRIVALS_PER_PAGE;
        ArrayList<TrainArrival> resultList = new ArrayList<TrainArrival>(arrivalsList.subList(startIndex, endIndex));
        return resultList;
    }

    private int getNumberPages(){
        return (int)(Math.ceil(getValidTrainsList().size() / MAX_ARRIVALS_PER_PAGE));
    }

    private ArrayList<TrainArrival> getValidTrainsList(){
        ArrayList<TrainArrival> validList = new ArrayList<TrainArrival>();
        Date now = new Date();
        now.setTime( now.getTime() + VALID_ARRIVED_TRAIN_TIME_BUFFER); // 1 min in ms
        for (TrainArrival train: mArrivals
             ) {
            if(train.arrivalTime.after(now)){
                validList.add(train);
            }
        }

        return validList;
    }

    ///////////////////////////////////
    //      Fetch Arrivals Task

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
                mFetchTaskRetryCount = 0;
                mLastArrivalsRefresh = new Date();
                mArrivals = trainArrivals;

                // Update Last Update Label
                updateLastUpdateTime();

                // Update No Arrivals Display
                updateDisplayMessages();

            }else{
                // Failed to fetch results
                if(mFetchTaskRetryCount < MAX_TASK_RETRIES){
                    Log.v(Log_TAG, "Failed to get Arrivals. Checking attempting retry");
                    // Perform Retry
                    mFetchTaskRetryCount++;
                    FetchArrivalsTask task = new FetchArrivalsTask();
                    task.execute(mStationMapId);
                }else{
                    Log.d(Log_TAG, "Failed to get Arrivals. Timed Out!");
                }
            }
        }
    }

    ///////////////////////////////////
    //      FArray Adapter

    private class ArrivalsListAdapter extends BaseAdapter {
        private ArrayList<TrainArrival> mArrivalsList;

        public ArrivalsListAdapter() {
            super();
            mArrivalsList = new ArrayList<TrainArrival>();
        }
        public ArrivalsListAdapter(ArrayList<TrainArrival> newList) {
            super();
            mArrivalsList = newList;
        }

        public void updateArrayList(ArrayList<TrainArrival> newArrivalsList){
            mArrivalsList = newArrivalsList;
            mArrivalsAdapter.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return getArrivalsForPage(mCurrentPage).size();
        }

        @Override
        public Object getItem(int i) {
            return getArrivalsForPage(mCurrentPage).get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {

            // Get Arrival
            TrainArrival arrival = getArrivalsForPage(mCurrentPage).get(index);

            LayoutInflater inflator = getActivity().getLayoutInflater();

            View row = inflator.inflate(R.layout.list_arrival_row, null);

            TextView indexTextView = (TextView)row.findViewById(R.id.list_arrival_index_label);
            TextView destinationTextView = (TextView)row.findViewById(R.id.list_arrival_destination_label);
            TextView displayTextView = (TextView)row.findViewById(R.id.list_arrival_display);

            indexTextView.setText(Integer.toString(arrival.index+1));
            indexTextView.setBackgroundResource(R.color.dashRowBackground);
            indexTextView.setTextColor( ContextCompat.getColor(getContext(),R.color.dashRowText));

            int backgroundColor = UiHelper.Colors.getRouteBackgroundColorByCtaRoute(arrival.route);
            int textColor = ContextCompat.getColor(getContext(), UiHelper.Colors.getTextColorByCtaRoute(arrival.route));

            destinationTextView.setText(arrival.destinationName);
            destinationTextView.setBackgroundResource(backgroundColor);
            destinationTextView.setTextColor(textColor);

            displayTextView.setText(arrival.getArrivalDisplay());
            displayTextView.setBackgroundResource(backgroundColor);
            displayTextView.setTextColor(textColor);

            return row;
        }
    }
}
