package com.example.airport;

import android.content.res.ColorStateList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by Zain on 4/23/17.
 */

public class View_Holder_Fragment extends RecyclerView.ViewHolder {

    TextView entry, exit, beacon, description, date;
    ImageView entryImg, exitImg, beaconImg, overallImg;

    public View_Holder_Fragment(View itemView, int color) {
        super(itemView);
        entry = (TextView) itemView.findViewById(R.id.attendanceEntry);
        exit = (TextView) itemView.findViewById(R.id.attendanceExit);
        beacon = (TextView) itemView.findViewById(R.id.attendanceBeacon);
        description = (TextView) itemView.findViewById(R.id.attendanceDescription);
        date = (TextView) itemView.findViewById(R.id.attendanceDate);

        entryImg = (ImageView) itemView.findViewById(R.id.entryImage);
        exitImg = (ImageView) itemView.findViewById(R.id.exitImage);
        beaconImg = (ImageView) itemView.findViewById(R.id.beaconImage);
        overallImg = (ImageView) itemView.findViewById(R.id.overallImage);

        description.setTextColor(ColorStateList.valueOf(color));
        entry.setTextColor(ColorStateList.valueOf(color));
        exit.setTextColor(ColorStateList.valueOf(color));
        beacon.setTextColor(ColorStateList.valueOf(color));


    }
}
