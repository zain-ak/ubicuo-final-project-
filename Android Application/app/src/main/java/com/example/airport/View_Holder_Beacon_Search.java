package com.example.airport;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


/**
 * Created by Zain on 5/20/17.
 */

public class View_Holder_Beacon_Search extends RecyclerView.ViewHolder {

    TextView beaconText;

    public View_Holder_Beacon_Search(View itemView) {
        super(itemView);
        beaconText = (TextView) itemView.findViewById(R.id.beaconItemText);
    }
}
