package com.holemcross.deltatrack.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.holemcross.deltatrack.data.TrainArrival;
import com.holemcross.deltatrack.exceptions.CtaServiceException;
import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.services.CtaService;

import org.json.JSONException;

import java.util.ArrayList;

import Helpers.KeyManager;

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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<TrainArrival> arrivals;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public StationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StationFragment newInstance(String param1, String param2) {
        StationFragment fragment = new StationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        arrivals = new ArrayList<TrainArrival>();

        Integer defaultMapId = 40730;
        FetchArrivalsTask task = new FetchArrivalsTask();
        task.execute(defaultMapId);
        Log.v(Log_TAG, "Task has executed!");
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

    private class FetchArrivalsTask extends AsyncTask<Integer, Void, ArrayList<TrainArrival>> {

        @Override
        protected ArrayList<TrainArrival> doInBackground(Integer...mapIds){
            Log.v(Log_TAG, "Entered Task.doInBackground()!");
            if(mapIds.length == 0){
                // No Map Ids
                return new ArrayList<TrainArrival>();
            }
            Integer mapId = mapIds[0];
            String apiKey = KeyManager.GetCtaApiKey(getContext());

            CtaService service = new CtaService();
            ArrayList<TrainArrival> resultList = null;
            try{
                resultList = service.GetTrainArrivalsForMapId(mapId, apiKey);
            }catch(CtaServiceException ex){
                return null;
            }

            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<TrainArrival> trainArrivals) {
            //super.onPostExecute(trainArrivals);
            if(trainArrivals != null){
                for ( TrainArrival train: trainArrivals
                     ) {
                    Log.v(Log_TAG, "Train " + train.route.toString() + " Arrival Time: " + train.arrivalTime);
                }
            }
        }
    }
}
