package com.example.airport;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Zain on 4/22/17.
 */

public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {


    List<Subject> list = Collections.emptyList();
    List<Integer> colorList = new ArrayList<Integer>();
    List<Integer> tempColorList = new ArrayList<Integer>();
    Context context;

    public Recycler_View_Adapter(List<Subject> list, Context context) {
        this.list = list;
        this.context = context;
        colorList.add(Color.parseColor("#1abc9c"));
        colorList.add(Color.parseColor("#2980b9"));
        colorList.add(Color.parseColor("#e67e22"));
        colorList.add(Color.parseColor("#c0392b"));
        colorList.add(Color.parseColor("#7f8c8d"));
        colorList.add(Color.parseColor("#9b59b6"));
        colorList.add(Color.parseColor("#1abc9c"));
        colorList.add(Color.parseColor("#2980b9"));
        colorList.add(Color.parseColor("#e67e22"));
        colorList.add(Color.parseColor("#c0392b"));
        colorList.add(Color.parseColor("#7f8c8d"));
        colorList.add(Color.parseColor("#9b59b6"));
        colorList.add(Color.parseColor("#1abc9c"));
        colorList.add(Color.parseColor("#2980b9"));
        colorList.add(Color.parseColor("#e67e22"));
        colorList.add(Color.parseColor("#c0392b"));
        colorList.add(Color.parseColor("#7f8c8d"));
        colorList.add(Color.parseColor("#9b59b6"));
        tempColorList = new ArrayList<Integer>(colorList);

    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_row_layout, parent, false);
        View_Holder holder = new View_Holder(v, colorGenerator());
        return holder;

    }

    @Override
    public void onBindViewHolder(View_Holder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        //holder.title.setText(list.get(position).getTitle());
        holder.code.setText(list.get(position).getCode());
        if (list.get(position).getAttendanceRate().equalsIgnoreCase(""))
            holder.attendanceRate.setText(list.get(position).getClass_type() + " " + list.get(position).getClass_no());
        else
            holder.attendanceRate.setText(list.get(position).getAttendanceRate());

        //animate(holder);

    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    public String getSubjectCode(int position) {
        return list.get(position).getCode();
    }

    public int getSubjectColor(int position) {
        return colorList.get(position);
    }

    public String getSubjectTitle (int position) { return list.get(position).getTitle(); }

    public String getClassID (int position) {return list.get(position).getClassID();}

    public String getSubjectType (int position) { return list.get(position).getClass_type() + " " + list.get(position).getClass_no(); }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Insert a new item to the RecyclerView on a predefined position
    public void insert(int position, Subject data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(Subject subject) {
        int position = list.indexOf(subject);
        list.remove(position);
        notifyItemRemoved(position);
    }

    private int colorGenerator () {
       int index = ThreadLocalRandom.current().nextInt(0, tempColorList.size());
       int temp = tempColorList.get(index);
       tempColorList.remove(index);

       for (Subject s : list)
           if (s.getColor() == -1) {
               s.setColor(temp);
                break;
           }

       return temp;
    }

}
