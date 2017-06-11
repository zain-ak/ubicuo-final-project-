package com.example.airport;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



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
 * Created by Zain on 4/23/17.
 */

public class LabFragment extends Fragment {

    private RecyclerView recyclerView;
    private Recycler_View_Adapter_Fragment adapter;
    View rootView;
    String user_id, class_id;
    String subjectCode, subjectName;
    String attendanceURL = "";
    int color, darkColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.subject_fragment, container, false);
        //List<Attendance> data = fill_with_data();
        color = getArguments().getInt("Color", 0);
        darkColor = getArguments().getInt("DarkColor", 0);
        user_id = getArguments().getString("ID");
        subjectName = getArguments().getString("subjectName");
        subjectCode = getArguments().getString("subjectID");

        Log.d("ID", user_id);
        Log.d("Subject", subjectCode);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.subjectRecyclerView);
//        adapter = new Recycler_View_Adapter_Fragment(data, getActivity(), darkColor);
//        recyclerView.setAdapter(adapter);

        attendanceURL = getResources().getString(R.string.ip) + "/getLabs.php";
        getAttendanceThread obj = new getAttendanceThread();
        obj.execute(user_id , subjectCode);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(), color));


        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        recyclerView.addOnItemTouchListener(
//                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override public void onItemClick(View view, int position) {
//                        Intent toSubjectIntent = new Intent(getContext(), SubjectActivity.class);
//                        toSubjectIntent.putExtra("subjectCode", adapter.getSubjectCode(position));
//                        toSubjectIntent.putExtra("subjectColor", adapter.getSubjectColor(position));
//                        startActivity(toSubjectIntent);
//                    }
//                })
//        );
    }

//    private List<Attendance> fill_with_data() {
//
//        List<Attendance> data = new ArrayList<>();
//
//        data.add(new Attendance(true, true, true, true, "Lab #1", "21st Jan '17"));
//        data.add(new Attendance(false, true, true, false, "Lab #2", "28th Jan '17"));
//        data.add(new Attendance(true, false, false, false, "Lab #3", "4th Feb '17"));
//        data.add(new Attendance(true, true, true, true, "Lab #4", "11th Feb '17"));
//        data.add(new Attendance(true, true, true, true, "Lab #5", "18th Feb '17"));
//        data.add(new Attendance(true, true, true, true, "Lab #6", "25th Feb '17"));
//
//        Collections.sort(data, new Comparator<Attendance>() {
//            @Override
//            public int compare(Attendance lhs, Attendance rhs) {
//                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
//                if (lhs.getDescription().compareTo(rhs.getDescription()) > 1)
//                    return -1;
//                else return 1;
//            }
//        });
//
//        //numOfSubjects = data.size();
//        return data;
//    }

    private class getAttendanceThread extends AsyncTask<String , String , String>
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
                    data.add(new Attendance(cameraEntry,cameraExit,beaconVerify,overall_attendance, "Lab #"+week, Date));
                    //Log.d("Data Item", Boolean.toString(data.get(i).isEntry()));


                }

                Log.d("Data Size", Integer.toString(data.size()));
                Log.d("Item", data.get(0).getDescription());

                adapter = new Recycler_View_Adapter_Fragment(data, getContext(), color);
                recyclerView.setAdapter(adapter);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

}
