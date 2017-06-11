package com.example.airport;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


/**
 * Created by Zain on 5/15/17.
 */

public class View_Holder_StudentList extends RecyclerView.ViewHolder {

    TextView id, name;

    public View_Holder_StudentList(View itemView) {
        super(itemView);
        id = (TextView) itemView.findViewById(R.id.studentListID);
        name = (TextView) itemView.findViewById(R.id.studentListName);
    }
}
