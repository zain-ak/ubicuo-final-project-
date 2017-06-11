package com.example.airport;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.estimote.sdk.EstimoteSDK.getApplicationContext;

/**
 * Created by Zain on 5/19/17.
 */

public class BeaconSearchFragment extends DialogFragment {

    private BeaconManager beaconManager;
    private Region region;
    private static final Map<String, List<String>> PLACES_BY_BEACONS;
    RecyclerView recyclerView;
    String beaconURL;
    TextView beaconText;
    ImageView beaconImg;
    String userID, classID, locationID;
    List<String> data;
    String major, minor;


    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("14:1", new ArrayList<String>() {
            {
                add("You are in Block 14, Room 1");
            }});
        placesByBeacons.put("14:2", new ArrayList<String>() {{
            add("You are in Block 14, Room 2");
        }});
        placesByBeacons.put("14:3", new ArrayList<String>() {{
            add("You are in Block 14, Room 3");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.beacon_search_fragment, container, false);
        major = "14"; minor = "302";
        recyclerView = (RecyclerView) rootView.findViewById(R.id.beaconSearchRecylcerView);
        //beaconText = (TextView) rootView.findViewById(R.id.beaconSearchText);
        //beaconImg = (ImageView) rootView.findViewById(R.id.beaconSearchImg);

        userID = getArguments().getString("userID");
        classID = getArguments().getString("classID");
        locationID = getArguments().getString("locationID");

        beaconURL = this.getResources().getString(R.string.ip) + "/getBeacon.php";

        List<String> haha = new ArrayList<String>();
        haha.add("You are in Block 14, Room 302");


        Recycler_View_Adapter_Beacon_Search adapter = new Recycler_View_Adapter_Beacon_Search(getApplicationContext(), haha);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);



        Log.d("HAHA", "Beacon Search hahaha");

//        beaconManager = new BeaconManager(getActivity());
//
//        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
//            @Override
//            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
//                if (!list.isEmpty()) {
//                    Log.d("Beacons Found", Integer.toString(list.size()));
//                    for (int i = 0; i < list.size(); i++)
//                        Log.d("Beacon Powers", "Beacon " + Integer.toString(list.get(i).getMajor()) + ":" + Integer.toString(list.get(i).getMinor()) + ": " + Integer.toString(list.get(i).getRssi()));
//                    Beacon nearestBeacon = list.get(0);
//                    List<String> places = placesNearBeacon(nearestBeacon);
//                    // TODO: update the UI here
//                    Log.d("Measured Power", Integer.toString(nearestBeacon.getMeasuredPower()));
//                    List<String> listItems = new ArrayList<String>();
//                    for (int i = 0; i < places.size(); i++) {
//                        listItems.add(places.get(i));
//                    }
//                    List<String> haha = new ArrayList<String>();
//                    haha.add("You are in Block 14, Room 302");
//
//
//                }
//            }
//        });
//        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        //getBeacon cObj = new getBeacon();
        //cObj.execute(classID);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (data.get(1).equalsIgnoreCase(major) && data.get(2).equalsIgnoreCase(minor))
                            Toast.makeText(getActivity() , "Your location has been verified!" , Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getActivity() , "Your location could not be verified" , Toast.LENGTH_LONG).show();

                    }
                }));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        beaconManager = new BeaconManager(getActivity());

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Log.d("Beacons Found", Integer.toString(list.size()));
                    for (int i = 0; i < list.size(); i++)
                        Log.d("Beacon Powers", "Beacon " + Integer.toString(list.get(i).getMajor()) + ":" + Integer.toString(list.get(i).getMinor()) + ": " + Integer.toString(list.get(i).getRssi()));
                    Beacon nearestBeacon = list.get(0);
                    List<String> places = placesNearBeacon(nearestBeacon);
                    // TODO: update the UI here
                    Log.d("Measured Power", Integer.toString(nearestBeacon.getMeasuredPower()));
                    List<String> listItems = new ArrayList<String>();
                    for (int i = 0; i < places.size(); i++) {
                        listItems.add(places.get(i));
                    }
                    List<String> haha = new ArrayList<String>();
                    haha.add("You are in Block 14, Room 302");
                    Recycler_View_Adapter_Beacon_Search adapter = new Recycler_View_Adapter_Beacon_Search(getApplicationContext(), haha);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(adapter);

                }
            }
        });
        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());



    }

    @Override
    public void onDestroy() {
        beaconManager.stopRanging(region);
        super.onDestroy();
    }

}
