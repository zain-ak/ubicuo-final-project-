package com.example.airport;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;



/**
 * Created by Zain on 4/22/17.
 */

public class View_Holder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView title;
        TextView code;
        TextView attendanceRate;

        View_Holder(View itemView, int color) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardView);
            cv.setCardBackgroundColor(color);
            title = (TextView) itemView.findViewById(R.id.title);
            code = (TextView) itemView.findViewById(R.id.code);
            attendanceRate = (TextView) itemView.findViewById(R.id.attendanceRate);
        }



}
