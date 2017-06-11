package com.example.airport;

import android.app.DialogFragment;
import android.graphics.Color;
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
import java.util.List;

/**
 * Created by Zain on 5/17/17.
 */

public class StudentAttendanceFragment extends DialogFragment {

    private RecyclerView recyclerView;
    private Recycler_View_Adapter_Fragment adapter;
    View rootView;
    String userID, classID, subjectType, subjectCode;
    String attendanceURL = "";
    TextView noRecords;
    ImageView noRec;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.student_attendance_fragment, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.studentAttendanceRecylcerView);
        noRecords = (TextView) rootView.findViewById(R.id.noRec);
        noRec = (ImageView) rootView.findViewById(R.id.noRecImg);

        userID = getArguments().getString("userID");
        classID = getArguments().getString("classID");
        subjectType = getArguments().getString("subjectType");
        subjectCode = getArguments().getString("subjectCode");

        if (subjectType.equalsIgnoreCase("Lab"))
            attendanceURL = getResources().getString(R.string.ip) + "/getLabs.php";
        else
            attendanceURL = getResources().getString(R.string.ip) + "/getTutorials.php";

        StudentAttendanceFragment.getAttendance obj = new StudentAttendanceFragment.getAttendance();
        obj.execute(userID, subjectCode);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), R.color.colorPrimaryDark));

        return rootView;
    }

    private class getAttendance extends AsyncTask<String , String , String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params)
        {
            String id = params[0];
            String sub_name = params[1];

            try
            {
                URL url = new URL(attendanceURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String post_data = URLEncoder.encode("Sid","UTF-8")+"="+ URLEncoder.encode(id,"UTF-8")+"&"
                        + URLEncoder.encode("Sname","UTF-8")+"="+ URLEncoder.encode(sub_name,"UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String result="";
                String line="";

                while((line = bufferedReader.readLine())!= null)
                {
                    result += line;
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s)
        {
            //Toast.makeText(getContext() , s , Toast.LENGTH_SHORT).show();
            try
            {
                JSONArray ja = new JSONArray(s);
                JSONObject jo = null;

                List<Attendance> data = new ArrayList<>();

                for (int i = 0; i < ja.length(); i++)
                {
                    jo = ja.getJSONObject(i);
                    int cameraEntry = jo.getInt("camera_entry");
                    int cameraExit = jo.getInt("camera_exit");
                    int beaconVerify = jo.getInt("beacon_verif");
                    int overall_attendance = jo.getInt("overall_attendance");
                    String week = jo.getString("week");
                    String Date = jo.getString("date");

                    //Toast.makeText(getContext() , cameraEntry +" "+Date , Toast.LENGTH_SHORT).show();
                    data.add(new Attendance(cameraEntry,cameraExit,beaconVerify,overall_attendance, "Tutorial #"+week, Date));
                    //Log.d("Data Item", Boolean.toString(data.get(i).isEntry()));
                }

                Log.d("Data Size", Integer.toString(data.size()));
                Log.d("Item", data.get(0).getDescription());
                if (data.size() > 0) {
                    noRecords.setVisibility(TextView.INVISIBLE);
                    noRec.setVisibility(ImageView.INVISIBLE);
                }
                adapter = new Recycler_View_Adapter_Fragment(data, getContext(), Color.BLACK);
                recyclerView.setAdapter(adapter);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
