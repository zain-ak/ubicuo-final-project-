package com.example.airport;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * Created by Zain on 5/20/17.
 */

public class Recycler_View_Adapter_Beacon_Search extends RecyclerView.Adapter<View_Holder_Beacon_Search>  {

    List<String> list = Collections.emptyList();
    Context context;

    Recycler_View_Adapter_Beacon_Search(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public View_Holder_Beacon_Search onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_search_row_layout, parent, false);
        View_Holder_Beacon_Search holder = new View_Holder_Beacon_Search(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(View_Holder_Beacon_Search holder, int position) {
        holder.beaconText.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
