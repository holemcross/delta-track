package com.holemcross.deltatrack.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;

import helpers.Constants;
import helpers.KeyManager;
import helpers.Serializer;
import helpers.UiHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnStationFragmentInteractionListener} interface
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
    private static final long VALID_ARRIVED_TRAIN_TIME_BUFFER = 180000; //Time in ms to consider arrival valid after arrival time occurs

    private int mStationMapId;
    private Date mLastArrivalsRefresh;
    private ArrayList<TrainArrival> mArrivals;
    private ArrivalsListAdapter mArrivalsAdapter;
    private ListView mArrivalsListView;
    private int mRefreshArrivalsDelay; // In Seconds
    private int mIteratePageDelay; // In Seconds
    private int mClockUpdateDelay; // In Seconds
    private int mFetchTaskRetryCount;

    private final Handler mRefreshHandler = new Handler();
    private OnStationFragmentInteractionListener mListener;


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
            Log.v(Log_TAG,"Iterating Page.");

            if(mArrivalsAdapter != null){
                mArrivalsAdapter.iteratePage();
            }
            mRefreshHandler.postDelayed(this, mIteratePageDelay * 1000);
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
        mIteratePageDelay = 5;
        mClockUpdateDelay = 5;
        mArrivals = new ArrayList<TrainArrival>();

        if(savedInstanceState != null){
            restoreStateFromSavedState(savedInstanceState);
        }else{
            if (getArguments() != null) {
                restoreStateFromSavedState(getArguments());
            }else{
                SharedPreferences prefs = getContext().getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME, Context.MODE_PRIVATE);
                mStationMapId = prefs.getInt(Constants.StationFragment.STATE_MAPID, DEFAULT_MAP_ID);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null){
            restoreStateFromSavedState(savedInstanceState);
        }

        // Set Station Name
        DeltaTrackDbHelper helper = new DeltaTrackDbHelper(getContext());
        StationRepository stationRepository = new StationRepository(helper);
        Station currentStation = stationRepository.getStationByMapId(mStationMapId);

        if(currentStation != null){
            mStationMapId = currentStation.mapId;
            updateStationUi(currentStation);
        }

        mArrivalsListView = (ListView)getView().findViewById(R.id.dashArrivalsListView);
        if(mArrivalsAdapter == null){
            mArrivalsAdapter = new ArrivalsListAdapter(mArrivals);
        }else{
            mArrivalsAdapter.updateArrayList(mArrivals);
        }
        mArrivalsListView.setAdapter(mArrivalsAdapter);

        // Update UI
        updateLastUpdateTime();
        updateClockTime();
        updateArrivalsUi();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_station, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStationFragmentInteractionListener) {
            mListener = (OnStationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStationFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveStateToPreferences();
        mRefreshHandler.removeCallbacks(refreshArrivals);
        mRefreshHandler.removeCallbacks(progressToNextPage);
        mRefreshHandler.removeCallbacks(updatesClock);
        detatchClickListener();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if MapId has changed
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME,Context.MODE_PRIVATE);
        int sharedPrefMapId = sharedPreferences.getInt(Constants.StationFragment.STATE_MAPID, -1);
        if(sharedPrefMapId > -1 && sharedPrefMapId != mStationMapId){
            // Get Station from DB
            changeStationByMapId(sharedPrefMapId);
        }

        // Check if refresh rate has changed
        int sharedPrefRefreshRate = sharedPreferences.getInt(Constants.SystemSettings.STATE_ARRIVALS_REFRESH_RATE, Constants.DEFAULT_ARRIVALS_REFRESH_RATE);
        mRefreshArrivalsDelay = sharedPrefRefreshRate;

        // Set Full Screen
        getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        // Hide AutoBar
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Add back handlers
        mRefreshHandler.post(refreshArrivals);
        mRefreshHandler.post(progressToNextPage);
        mRefreshHandler.post(updatesClock);

        attachClickListener();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.StationFragment.STATE_MAPID, mStationMapId);
        outState.putLong(Constants.StationFragment.STATE_LAST_REFRESH, mLastArrivalsRefresh.getTime());
        outState.putByteArray(Constants.StationFragment.STATE_ARRIVALS, Serializer.serializeObject(mArrivals));
        super.onSaveInstanceState(outState);
    }

    public void changeStationByMapId(int mapId){
        DeltaTrackDbHelper dbHelper = new DeltaTrackDbHelper(getContext());
        StationRepository repo = new StationRepository(dbHelper);
        Station dbStation = repo.getStationByMapId(mapId);
        if(dbStation != null){
            changeStation(dbStation);
        }
    }

    // Public method for changing station from activity
    public void changeStation(Station newStation){
        if(newStation != null){

            mStationMapId = newStation.mapId;
            mLastArrivalsRefresh = null;
            mArrivals = null;
            saveStateToPreferences();
            updateStationUi(newStation);

            if(mArrivalsAdapter != null){
                //mArrivalsAdapter.clearList();
            }

            FetchArrivalsTask task = new FetchArrivalsTask();
            task.execute(mStationMapId);
        }
    }

    private void restoreStateFromSavedState(Bundle savedState){
        // restore state data

        // Get MapId
        mStationMapId = savedState.getInt(Constants.StationFragment.STATE_MAPID, DEFAULT_MAP_ID);
        long restoreDateValue = savedState.getLong(Constants.StationFragment.STATE_LAST_REFRESH, 0);
        if(restoreDateValue > 0){
            mLastArrivalsRefresh = new Date(restoreDateValue);
        }else{
            mLastArrivalsRefresh = new Date();
        }

        byte[] arrivalsByteArray = savedState.getByteArray(Constants.StationFragment.STATE_ARRIVALS);

        if(arrivalsByteArray != null && arrivalsByteArray.length > 0){
            mArrivals = (ArrayList<TrainArrival>) Serializer.deserializeObject(savedState.getByteArray(Constants.StationFragment.STATE_ARRIVALS));
        }else{
            changeStationByMapId(mStationMapId);
        }

        Log.v(Log_TAG, "Restored Station Fragment from saved state.");
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
    public interface OnStationFragmentInteractionListener {
        void onStationFragmentTouch();
    }

    private void updateArrivalsUi(){
        Log.v(Log_TAG, "Refreshing Arrivals UI");
        mArrivalsAdapter.updateArrayList(mArrivals);
        updateDisplayMessages();
    }

    private void updateDisplayMessages(){

        // Check if No Arrivals in list
        TextView noArrivalsTextView = (TextView)getActivity().findViewById(R.id.dash_no_arrivals_display_label);
        if(noArrivalsTextView != null && mArrivalsAdapter != null){
            if(mArrivalsAdapter.getValidTrainsList().size() > 0){
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
        editor.putLong(Constants.StationFragment.STATE_LAST_REFRESH, mLastArrivalsRefresh == null ? -1 : mLastArrivalsRefresh.getTime());
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

    private void updateStationUi( Station station){
        String stationNameText = "Station Name";
        stationNameText = station.stationName;

        TextView stationNameTextView = (TextView)getActivity().findViewById(R.id.dashHeaderStationNameLabel);
        if(stationNameTextView != null){
            stationNameTextView.setText(stationNameText);
        }
    }



    private void attachClickListener(){
        View view = getView();
        view.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mListener.onStationFragmentTouch();
            }
        });
    }

    private void detatchClickListener(){
        View view = getView();
        view.setOnClickListener(null);
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
            String apiKey = KeyManager.getCtaApiKey(getContext());

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

                if(mArrivalsAdapter != null){
                    mArrivalsAdapter.updateArrayList(mArrivals);
                }

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
    //      Arrivals Array Adapter

    private class ArrivalsListAdapter extends BaseAdapter {
        private ArrayList<TrainArrival> mArrivalsList;
        private ArrayList<TrainArrival> mPaginatedList;
        private int mCurrentPage = 0;
        private int mLastPage = 0;

        public ArrivalsListAdapter() {
            super();
            mArrivalsList = new ArrayList<TrainArrival>();
        }
        public ArrivalsListAdapter(ArrayList<TrainArrival> newList) {
            super();
            mArrivalsList = newList;
            mCurrentPage = 0;
            mPaginatedList = getArrivalsForPage(mCurrentPage);
            mLastPage = 0;
            this.notifyDataSetChanged();
        }

        public void updateArrayList(ArrayList<TrainArrival> newArrivalsList){

            mArrivalsList = newArrivalsList;
            mCurrentPage = 0;
            mPaginatedList = getArrivalsForPage(mCurrentPage);
            mLastPage = 0;
            this.notifyDataSetChanged();
        }

        public void updatePaginationList(ArrayList<TrainArrival> newPaginationList){
            mPaginatedList = newPaginationList;
            mLastPage = mCurrentPage;
            this.notifyDataSetChanged();
        }

        public void clearList(){
            mArrivalsList = new ArrayList<>();
            mCurrentPage = 0;
            this.notifyDataSetChanged();
        }

        public void iteratePage(){
            int numPages = getNumberPages();
            int lookupPage = 0;
            if(mCurrentPage + 1 < numPages)
            {
                lookupPage = mCurrentPage + 1;
            }else{
                lookupPage = 0;
            }

            if(getArrivalsForPage(lookupPage).size() > 0){
                mCurrentPage = lookupPage;
            }else{
                mCurrentPage = 0;
            }
            if(mCurrentPage == mLastPage){
                return;
            }
            updatePaginationList(getArrivalsForPage(mCurrentPage));
        }

        private ArrayList<TrainArrival> getArrivalsForPage(int pageNumber){

            if(pageNumber >= getNumberPages()){
                return new ArrayList<TrainArrival>();
            }
            ArrayList<TrainArrival> arrivalsList = getValidTrainsList();
            int startIndex = pageNumber * MAX_ARRIVALS_PER_PAGE;
            int endIndex = (startIndex + MAX_ARRIVALS_PER_PAGE) > arrivalsList.size() ? arrivalsList.size() : startIndex + MAX_ARRIVALS_PER_PAGE;
            ArrayList<TrainArrival> resultList = new ArrayList<TrainArrival>(arrivalsList.subList(startIndex, endIndex));

            return resultList;
        }

        private int getNumberPages(){
            int validTrainCount = getValidTrainsList().size();
            if(validTrainCount <= MAX_ARRIVALS_PER_PAGE){
                return 1;
            }
            return (int)(Math.ceil(getValidTrainsList().size() / MAX_ARRIVALS_PER_PAGE))+1;
        }

        public ArrayList<TrainArrival> getValidTrainsList(){
            if(mArrivalsList == null){
                return new ArrayList<TrainArrival>();
            }

            ArrayList<TrainArrival> validList = new ArrayList<TrainArrival>();
            Date now = new Date();
            now.setTime( now.getTime() - VALID_ARRIVED_TRAIN_TIME_BUFFER); // 1 min in ms
            for (TrainArrival train: mArrivalsList
                    ) {
                if(train.arrivalTime.after(now)){
                    validList.add(train);
                }
            }

            return validList;
        }

        @Override
        public int getCount() {
            return mPaginatedList.size();
        }

        @Override
        public Object getItem(int i) {
            return mPaginatedList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {

            // Get Arrival
            TrainArrival arrival = mPaginatedList.get(index);

            LayoutInflater inflator = getActivity().getLayoutInflater();

            View row = inflator.inflate(R.layout.list_arrival_row, null);

            TextView indexTextView = (TextView)row.findViewById(R.id.list_arrival_index_label);
            TextView destinationTextView = (TextView)row.findViewById(R.id.list_arrival_destination_label);
            TextView displayTextView = (TextView)row.findViewById(R.id.list_arrival_display);

            indexTextView.setText(Integer.toString(arrival.index+1));
            indexTextView.setBackgroundResource(R.color.dashRowBackground);
            indexTextView.setTextColor( ContextCompat.getColor(getContext(),R.color.dashRowText));

            int backgroundColor = UiHelper.Colors.getRouteBackgroundResourceByCtaRoute(arrival.route);
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
