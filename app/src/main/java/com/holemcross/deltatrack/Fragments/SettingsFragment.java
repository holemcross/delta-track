package com.holemcross.deltatrack.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.activities.DeltaTrackActivity;
import com.holemcross.deltatrack.data.CtaRoutes;
import com.holemcross.deltatrack.data.Station;

import java.util.ArrayList;

import helpers.Constants;
import helpers.KeyManager;
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

    private static final String LOG_TAG = Constants.APP_NAME + " | " + SettingsFragment.class.getSimpleName();
    private static final String ARG_STATIONS = "stations";
    private static final String MENU_OPTION_SELECT_STATION_NAME = "Select Station";
    private static final int MENU_OPTION_SELECT_STATION_INDEX = 0;
    private static final String MENU_OPTION_APIKEY_NAME = "API Key";
    private static final int MENU_OPTION_APIKEY_INDEX = 1;
    private static final String MENU_OPTION_REFRESH_FREQUENCY_NAME = "Refresh Frequency";
    private static final int MENU_OPTION_REFRESH_FREQUENCY_INDEX = 2;
    private static final String MENU_OPTION_UPDATE_STATIONS_NAME = "Update Stations";
    private static final int MENU_OPTION_UPDATE_STATIONS_INDEX = 3;

    private OnSettingsFragmentInteractionListener mListener;
    private ListView mMenuListView;
    private ListView mStationListView;
    private MenuOptionListAdapter mMenuOptionListAdapter;
    private StationsListAdapter mStationsListAdapter;
    private EditText mApiKeyEditField;
    private EditText mRefreshRateEditField;

    private ArrayList<Station> mStationList;
    private String[] mMenuOptionsList;
    private int mSelectedMenuOptionIndex;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        hideActionBar();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stations Parameter 1.
     * @return A new instance of fragment SettingsFragment.
     */
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

        if (getArguments() != null) {
            byte[] stationBytes = getArguments().getByteArray(ARG_STATIONS);
            mStationList = (ArrayList<Station>) Serializer.deserializeObject(stationBytes);
        }

        if(mStationList == null){
            // Stations not yet loaded
            Log.v(LOG_TAG, "Stations were not loaded!");

        }

        // Load Non changing Items
        mMenuOptionsList = new String[]{
                MENU_OPTION_SELECT_STATION_NAME,
                MENU_OPTION_APIKEY_NAME,
                MENU_OPTION_REFRESH_FREQUENCY_NAME,
                MENU_OPTION_UPDATE_STATIONS_NAME
        };

        if(mMenuOptionListAdapter == null){
            mMenuOptionListAdapter = new MenuOptionListAdapter(getContext(), R.layout.list_settings_menu_row, mMenuOptionsList);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Menu List
        mSelectedMenuOptionIndex = 0;
        mMenuListView = (ListView) getActivity().findViewById(R.id.settings_menu_list_view);
        mMenuListView.setAdapter(mMenuOptionListAdapter);
        mMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Ignore same index clicks
                if(i == mSelectedMenuOptionIndex){
                    return;
                }
                // Change to corresponding content
                setContentView(i);
            }
        });

        if(mStationsListAdapter == null){
            mStationsListAdapter = new StationsListAdapter(mStationList);
        }

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


        // Remove old context view
        LinearLayout contextView = (LinearLayout)getActivity().findViewById(R.id.settings_menu_context_view);
        cleanupContextView(contextView);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME,Context.MODE_PRIVATE);
        switch(newIndex){
            case MENU_OPTION_SELECT_STATION_INDEX:
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT,Gravity.CENTER);
                mStationListView = new ListView(getActivity());
                mStationListView.setLayoutParams(layoutParams);
                contextView.addView(mStationListView);
                mStationsListAdapter.updateStations(mStationList);
                mStationListView.setAdapter(mStationsListAdapter);
                mStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Station station = (Station)adapterView.getItemAtPosition(i);
                        changeStation(station);
                    }
                });
                break;
            case MENU_OPTION_APIKEY_INDEX:

                // Get Current Key
                String apiKeyValue = KeyManager.getCtaApiKey(getActivity());

                TextView apiKeyTextLabel = new TextView(getActivity());
                apiKeyTextLabel.setText("CTA API Key");
                apiKeyTextLabel.setLayoutParams(layoutParams);
                mApiKeyEditField = new EditText(getActivity());
                mApiKeyEditField.setText(apiKeyValue);
                mApiKeyEditField.setMinEms(16);
                mApiKeyEditField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        hideActionBar();
                        return false;
                    }
                });
                mApiKeyEditField.setLayoutParams(layoutParams);

                Button apiKeyUpdateButton = new Button(getActivity());
                apiKeyUpdateButton.setText("Update Key");
                apiKeyUpdateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Validate field
                        if(mApiKeyEditField.getText().length() != Constants.Keys.CTA_API_KEY_LENGTH){
                            // Create validation error
                            Toast toast = Toast.makeText(getActivity(), "API Key invalid, please check and try again.", Toast.LENGTH_SHORT);
                            toast.show();
                        } else{
                            KeyManager.saveCtaApiKey(getActivity(),mApiKeyEditField.getText().toString() );
                            Toast toast = Toast.makeText(getActivity(), "API Key saved.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
                apiKeyUpdateButton.setLayoutParams(layoutParams);


                contextView.addView(apiKeyTextLabel);
                contextView.addView(mApiKeyEditField);
                contextView.addView(apiKeyUpdateButton);
                break;
            case MENU_OPTION_REFRESH_FREQUENCY_INDEX:
                // Get Refresh Rate
                int refreshRate = sharedPreferences.getInt(Constants.SystemSettings.STATE_ARRIVALS_REFRESH_RATE, Constants.DEFAULT_ARRIVALS_REFRESH_RATE);

                TextView refreshFrequencyTextLabel = new TextView(getActivity());
                refreshFrequencyTextLabel.setText("Refresh Rate (Seconds)");
                refreshFrequencyTextLabel.setLayoutParams(layoutParams);

                mRefreshRateEditField = new EditText(getActivity());
                mRefreshRateEditField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mRefreshRateEditField.setText(Integer.toString(refreshRate));
                mRefreshRateEditField.setMinEms(8);
                mRefreshRateEditField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        hideActionBar();
                        return false;
                    }
                });
                mRefreshRateEditField.setLayoutParams(layoutParams);

                Button refreshRateUpdateButton = new Button(getActivity());
                refreshRateUpdateButton.setText("Set");
                refreshRateUpdateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Validate field
                        String inputString = mRefreshRateEditField.getText().toString();
                        if(TextUtils.isEmpty(inputString)){
                            // Create validation error
                            Toast toast = Toast.makeText(getActivity(), "Must enter a value", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }

                        int inputValue = -1;
                        try{
                            inputValue = Integer.parseInt(inputString);

                        }catch(NumberFormatException ex){
                            Toast toast = Toast.makeText(getActivity(), "Must enter a number", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }

                        if(inputValue < 0){
                            Toast toast = Toast.makeText(getActivity(), "Must enter a positive value", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }

                        SharedPreferences.Editor refreshEditor = sharedPreferences.edit();
                        refreshEditor.putInt(Constants.SystemSettings.STATE_ARRIVALS_REFRESH_RATE, inputValue);
                        refreshEditor.commit();

                        Toast toast = Toast.makeText(getActivity(), "Refresh rate changed.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                refreshRateUpdateButton.setLayoutParams(layoutParams);

                LinearLayout refreshLinearLayout = new LinearLayout(getActivity());
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT,Gravity.CENTER);
                refreshLinearLayout.setLayoutParams(layoutParams);

                refreshLinearLayout.addView(refreshFrequencyTextLabel);
                refreshLinearLayout.addView(mRefreshRateEditField);
                refreshLinearLayout.addView(refreshRateUpdateButton);

                contextView.addView(refreshLinearLayout);
                break;

            case MENU_OPTION_UPDATE_STATIONS_INDEX:
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER);
                TextView stationRefreshTextView = new TextView(getActivity());
                stationRefreshTextView.setText("Refresh Stations");
                stationRefreshTextView.setLayoutParams(layoutParams);

                Button refreshStationsButton = new Button(getActivity());
                refreshStationsButton.setText("Refresh");
                refreshStationsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
                        //// 2. Chain together various setter methods to set the dialog characteristics
                        //builder.setMessage("Are you sure? This may take a moment.")
                        //        .setTitle("Refresh Stations");
//
                        //// 3. Get the AlertDialog from create()
                        //AlertDialog dialog = builder.create();

                        ((DeltaTrackActivity)getActivity()).refreshStations();
                    }
                });
                refreshStationsButton.setLayoutParams(layoutParams);

                contextView.addView(stationRefreshTextView);
                contextView.addView(refreshStationsButton);
                break;
        }

        mSelectedMenuOptionIndex = newIndex;
    }

    private void cleanupContextView(LinearLayout contextView){
        switch(mSelectedMenuOptionIndex){
            case MENU_OPTION_SELECT_STATION_INDEX:
                if(mStationsListAdapter != null){
                    mStationsListAdapter.clear();
                }
                if(mStationListView != null){
                    mStationListView = null;
                }

        }
        contextView.removeAllViews();

        // Hide Bar
        hideActionBar();
    }

    private void hideActionBar(){
        //getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
        //        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        ////getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        ////getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        //////Hide AutoBar
        //ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        //if (actionBar != null) {
        //    actionBar.hide();
        //}
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

        public void updateStations(ArrayList<Station> newStations){
            mStations = newStations;
            notifyDataSetChanged();
        }

        public void clear(){
            mStations = new ArrayList<Station>();
            notifyDataSetChanged();
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
            optionText.setText(station.getCleanNameDescription());

            LinearLayout routes = (LinearLayout) row.findViewById(R.id.list_settings_station_row_route_layout);

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
