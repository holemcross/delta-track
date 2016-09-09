package com.holemcross.deltatrack.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.activities.DeltaTrackActivity;
import com.holemcross.deltatrack.data.CtaRoutes;
import com.holemcross.deltatrack.data.Station;

import java.util.ArrayList;
import java.util.List;

import helpers.Constants;
import helpers.Serializer;
import helpers.UiHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnSettingsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STATIONS = "stations";

    private static final String MENU_OPTION_NAME_STATION = "Station";
    private static final String MENU_OPTION_NAME_APIKEY = "API Key";
    private static final String MENU_OPTION_NAME_REFRESH_FREQUENCY = "Refresh Frequency";

    private static final String LOG_TAG = Constants.APP_NAME + " | " + SettingsFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String[] mMenuOptionsList;
    private MenuOptionListAdapter mMenuOptionListAdapter;
    private ListView mMenuListView;
    private ListView mStationListView;
    private StationsListAdapter mStationsListAdapter;

    private int mSelectedMenuOptionIndex;

    private ArrayList<Station> mStationList;

    private OnSettingsFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        // Hide AutoBar
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            //actionBar.setSubtitle("Settings");
            //actionBar.setHomeButtonEnabled(true);
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stations Parameter 1.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(ArrayList<Station> stations) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();

        byte[] stationBytes = Serializer.serializeObject(stations);

        args.putByteArray(ARG_STATIONS, stationBytes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedMenuOptionIndex = 0;

        if (getArguments() != null) {
            byte[] stationBytes = getArguments().getByteArray(ARG_STATIONS);
            mStationList = (ArrayList<Station>) Serializer.deserializeObject(stationBytes);
        }

        if(mStationList == null){
            // Stations not yet loaded
            Log.v(LOG_TAG, "Stations were not loaded!");

        }

        mMenuOptionsList = new String[]{MENU_OPTION_NAME_STATION,
                MENU_OPTION_NAME_APIKEY,
                MENU_OPTION_NAME_REFRESH_FREQUENCY};

        if(mMenuOptionListAdapter == null){
            mMenuOptionListAdapter = new MenuOptionListAdapter(getContext(), R.layout.list_settings_menu_row, mMenuOptionsList);
        }

        if(mStationsListAdapter == null){
            mStationsListAdapter = new StationsListAdapter(mStationList);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMenuListView = (ListView) getActivity().findViewById(R.id.settings_menu_list_view);
        mMenuListView.setAdapter(mMenuOptionListAdapter);

        setContentView(mSelectedMenuOptionIndex);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSettingsFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsFragmentInteractionListener) {
            mListener = (OnSettingsFragmentInteractionListener) context;
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

    private void setContentView(int newIndex){

        //if(newIndex == mSelectedMenuOptionIndex){
        //    return;
        //}

        LinearLayout contextView = (LinearLayout)getActivity().findViewById(R.id.settings_menu_context_view);
        contextView.removeAllViews();

        // Remove old context view
        switch(newIndex){
            case 0:
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT,Gravity.CENTER);
                mStationListView = new ListView(getActivity());
                mStationListView.setLayoutParams(layoutParams);
                contextView.addView(mStationListView);
                mStationListView.setAdapter(mStationsListAdapter);
                mStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Station station = (Station)adapterView.getItemAtPosition(i);
                        changeStation(station);
                    }
                });
                break;
        }

        mSelectedMenuOptionIndex = newIndex;
    }

    private void changeStation(Station newStation){
        ((DeltaTrackActivity)getActivity()).changeStation(newStation);
        // Change Fragment
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
    public interface OnSettingsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSettingsFragmentInteraction();
    }

    ///////////////////////////////////
    //      Arrivals Array Adapter

    private class MenuOptionListAdapter extends ArrayAdapter<String> {

        public MenuOptionListAdapter(Context context, int resource) {
            super(context, resource);
        }

        public MenuOptionListAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public int getCount() {
            return mMenuOptionsList.length;
        }

        @Override
        public String getItem(int i) {
            return mMenuOptionsList[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {

            LayoutInflater inflator = getActivity().getLayoutInflater();

            View row = inflator.inflate(R.layout.list_settings_menu_row, null);

            TextView optionText = (TextView) row.findViewById(R.id.list_settings_menu_row_text_view);
            optionText.setText(mMenuOptionsList[index]);

            return row;
        }
    }

    ///////////////////////////////////
    //      Station Array Adapter

    private class StationsListAdapter extends BaseAdapter {

        private ArrayList<Station> mStations;

        public StationsListAdapter(ArrayList<Station> stations) {
            super();
            mStations = stations;
        }



        @Override
        public int getCount() {return mStations.size();}

        @Override
        public Station getItem(int i) {
            return mStations.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {

            Station station = mStations.get(index);

            LayoutInflater inflater = getActivity().getLayoutInflater();

            View row = inflater.inflate(R.layout.list_settings_station_row, null);

            TextView optionText = (TextView) row.findViewById(R.id.list_settings_station_row_text_view);
            optionText.setText(station.stationName);

            LinearLayout routes = (LinearLayout) row.findViewById(R.id.list_settings_station_row_route_layout);
            // DEBUG
            List<CtaRoutes> tempList  = station.getRoutes();
            if(tempList.size() > 1){
                Log.v(LOG_TAG, "Route size of: "+ tempList.size());
            }

            LinearLayout.LayoutParams commonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            for (CtaRoutes route: station.getRoutes()
                 ) {
                View view = new View(getActivity());
                view.setBackgroundResource(UiHelper.Colors.getRouteBackgroundResourceByCtaRoute(route));

                view.setLayoutParams(new LinearLayout.LayoutParams(commonParams));
                routes.addView(view);
            }

            return row;
        }
    }
}
