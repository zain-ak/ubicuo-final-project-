package com.example.airport;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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
 * Created by Zain on 5/15/17.
 */

public class StudentListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Recycler_View_Adapter_StudentList adapter;
    private String classID, title, listURL, subjectType;
    List<Student> data;

    ProgressDialog dialog;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        recyclerView = (RecyclerView) findViewById(R.id.studentRecyclerView);


        final Intent i = getIntent();
        classID = i.getStringExtra("classID");
        subjectType = i.getStringExtra("subjectType");
        title = i.getStringExtra("subjectCode") + " - " + subjectType;

        getSupportActionBar().setTitle(title);

        StudentListActivity.getStudentListThread obj = new StudentListActivity.getStudentListThread();
        listURL = this.getResources().getString(R.string.ip) + "/student_list.php";
        obj.execute(classID);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext(), R.color.colorPrimaryDark));

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Bundle bundle = new Bundle();
                        bundle.putString("userID", data.get(position).getId());
                        bundle.putString("classID", classID);
                        bundle.putString("subjectType", subjectType);
                        bundle.putString("subjectCode", i.getStringExtra("subjectCode"));

                        StudentAttendanceFragment studentAttendance = new StudentAttendanceFragment();
                        studentAttendance.setArguments(bundle);

                        //getFragmentManager().beginTransaction().add(studentAttendance, "StudentAttendance").commit();
                      FragmentManager fm = getFragmentManager();

                      studentAttendance.show(fm, "ok");

                    }
                })
        );
    }

    private class getStudentListThread extends AsyncTask<String , String , String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            context = StudentListActivity.this;
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            String classID = params[0];

            try
            {
                URL url = new URL(listURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String post_data = URLEncoder.encode("Cid","UTF-8")+"="+ URLEncoder.encode(classID,"UTF-8");
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
            try
            {
                Log.d("String", s);
                JSONArray ja = new JSONArray(s);
                JSONObject jo = null;

                data = new ArrayList<>();

                for (int i = 0; i < ja.length(); i++)
                {
                    jo = ja.getJSONObject(i);
                    String id = jo.getString("id");
                    String first_name = jo.getString("first_name");
                    String last_name = jo.getString("last_name");

                    data.add(new Student(first_name, last_name, id));
                }


                //data = fill_with_data();

                dialog.dismiss();

                Log.d("ThreadDataSize", Integer.toString(data.size()));
                adapter = new Recycler_View_Adapter_StudentList(data, getApplication());
                Log.d("AdapterSize", Integer.toString(adapter.getItemCount()));
                recyclerView.setAdapter(adapter);


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
