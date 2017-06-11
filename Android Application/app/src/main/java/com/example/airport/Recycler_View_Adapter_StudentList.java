package com.example.airport;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.Collections;
import java.util.List;

/**
 * Created by Zain on 5/15/17.
 */

class Recycler_View_Adapter_StudentList extends RecyclerView.Adapter<View_Holder_StudentList> {

    List<Student> list = Collections.emptyList();
    Context context;

    Recycler_View_Adapter_StudentList (List<Student> list, Context context) {
        Log.d("AdapterListSize", Integer.toString(list.size()));
        this.list = list;
        Log.d("AdapterListSize2", Integer.toString(this.list.size()));
        this.context = context;
    }


    @Override
    public View_Holder_StudentList onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_row_layout, parent, false);
        View_Holder_StudentList holder = new View_Holder_StudentList(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(View_Holder_StudentList holder, int position) {
        holder.id.setText(list.get(position).getId());
        holder.name.setText(list.get(position).getFirst_name() + " " + list.get(position).getLast_name());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
