package com.megatech.fms;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.view.RefuelRecyclerView;
import com.megatech.fms.view.RefuelRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RefuelListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RefuelListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RefuelListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    List<RefuelItemData> lstData ;

    private boolean _self;

    public RefuelListFragment() {
        // Required empty public constructor
    }
    public RefuelListFragment(boolean self) {
        // Required empty public constructor
        _self = self;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RefuelListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RefuelListFragment newInstance(String param1, String param2) {
        RefuelListFragment fragment = new RefuelListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private Timer timer = new Timer();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                        refreshdata(Activity.RESULT_OK);
            }
        },1000*60*5,1000*60*5);

    }

    RefuelRecyclerViewAdapter mAdapter;
    public  void refresh()
    {
        refreshdata(Activity.RESULT_OK);
    }

    private void refreshdata(int resultOk) {

        final View view = this.getView();
        final Activity activity = getActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                lstData = DataHelper.getRefuelList(_self,0);
                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (lstData == null) {
                                Toast.makeText(activity, R.string.no_internet_error, Toast.LENGTH_LONG).show();
                                //this.getActivity().finishAffinity();
                            } else {
                                mAdapter = new RefuelRecyclerViewAdapter((UserBaseActivity) getActivity(), lstData);
                                if (rv == null)
                                    rv = (RefuelRecyclerView) view;
                                if (rv != null)
                                    rv.setAdapter(mAdapter);
                                filter(filterQuery);
                            }
                        }
                    });

            }
        }).start();

    }

    private CharSequence filterQuery = "";

    public void filter(CharSequence sequence) {
        filterQuery = sequence;
        if (mAdapter != null)
            mAdapter.getFilter().filter(sequence);
    }
    @Override
    public void onResume() {
        super.onResume();

        refreshdata(Activity.RESULT_OK);
        /*timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                refreshdata(Activity.RESULT_OK);
            }
        },1000*60*5,1000*60*5);
        */


    }

    @Override
    public void onPause() {
        super.onPause();
        //timer.cancel();
    }

    View rootview;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_self_refuel, container, false);


        //RecyclerView rv = rootview.findViewById(R.id.refuelitem_list);

        if (activity == null)
            activity = getActivity();
        rv = new RefuelRecyclerView(activity);

        rv.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshdata(Activity.RESULT_OK);
                }
            });



        return  rv;
    }

    protected Activity activity;
    protected RecyclerView rv;
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
