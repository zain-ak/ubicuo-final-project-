package com.example.airport;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * Created by Zain on 4/23/17.
 */

public class Recycler_View_Adapter_Fragment extends RecyclerView.Adapter<View_Holder_Fragment> {

    List<Attendance> list = Collections.emptyList();
    Context context;
    int color;

    public Recycler_View_Adapter_Fragment(List<Attendance> list, Context context, int color) {
        this.list = list;
        this.context = context;
        this.color = color;
    }

    @Override
    public View_Holder_Fragment onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_row_layout, parent, false);
        View_Holder_Fragment holder = new View_Holder_Fragment(v, color);
        return holder;

    }

    @Override
    public void onBindViewHolder(View_Holder_Fragment holder, int position) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        //holder.title.setText(list.get(position).getTitle());
//        holder.entry.setText(String.valueOf(list.get(position).isEntry()));
//        holder.exit.setText(String.valueOf(list.get(position).isExit()));
//        holder.beacon.setText(String.valueOf(list.get(position).isBeacon()));
        holder.description.setText(list.get(position).getDescription());
        holder.date.setText(list.get(position).getDate());

        if (list.get(position).isEntry() == 1)
            holder.entryImg.setImageResource(R.drawable.tick);
        else
            holder.entryImg.setImageResource(R.drawable.cross);

        if (list.get(position).isExit() == 1)
            holder.exitImg.setImageResource(R.drawable.tick);
        else
            holder.exitImg.setImageResource(R.drawable.cross);

        if (list.get(position).isBeacon() == 1)
            holder.beaconImg.setImageResource(R.drawable.tick);
        else
            holder.beaconImg.setImageResource(R.drawable.cross);

        if (list.get(position).isOverall() == 1)
            holder.overallImg.setImageResource(R.drawable.tick);
        else
            holder.overallImg.setImageResource(R.drawable.cross);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
