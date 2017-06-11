package com.example.airport;

import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    TextView welcomeText;
    private String userType, user_id, firstName, lastName, viewURL, classURL;
    private int numOfSubjects;
    FloatingActionButton fab;
    RecyclerView recyclerView;
    Recycler_View_Adapter adapter;
    List<Subject> data;
    List<StudentClass> studentClasses;
    StudentClass current = null;

    private BeaconManager beaconManager;
    private Region region;
    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("14:1", new ArrayList<String>() {{
                add("You are in Block 14, Room 1");
            }});
        placesByBeacons.put("14:2", new ArrayList<String>() {{
            add("You are in Block 14, Room 2");
        }});
        placesByBeacons.put("14:3", new ArrayList<String>() {{
            add("You are in Block 14, Room 3");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }

    ProgressDialog dialog;
    Context context;

    public static final String[] daysOfWeek = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main);
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        getSupportActionBar().setTitle("Ubicuo");
        //getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_launcher_app));

        Intent i = getIntent();
        userType = i.getStringExtra("userType");
        user_id = i.getStringExtra("ID");
        firstName = i.getStringExtra("firstName");
        lastName = i.getStringExtra("lastName");

        welcomeText = (TextView) findViewById(R.id.welcomeText);
        welcomeText.setText("Welcome " + firstName + "!");

        //the layout on which you are working
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.constraint_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (userType.equalsIgnoreCase("Teacher"))
            fab.hide();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current != null) {
                    Toast.makeText(getApplicationContext() , current.getSubjectCode() + " " + current.getClassType() + " is in session right now" , Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("userID", user_id);
                    bundle.putString("classID", current.getClassID());
                    bundle.putString("locationID", current.getLocationID());

                    BeaconSearchFragment beaconSearch = new BeaconSearchFragment();
                    beaconSearch.setArguments(bundle);

                    //getFragmentManager().beginTransaction().add(studentAttendance, "StudentAttendance").commit();
                    FragmentManager fm = getFragmentManager();

                    beaconSearch.show(fm, "beaconOK");
                }
                else
                    Toast.makeText(getApplicationContext() , "You don't have any classes going on" , Toast.LENGTH_LONG).show();
            }
        });

        //set the properties for button
        Button btnTag = new Button(this);
        btnTag.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        btnTag.setText("Button");
        //btnTag.setId();

        //add button to the layout
        //layout.addView(btnTag);

        btnTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });


        if (userType.equalsIgnoreCase("Student")) {
            getClassesThread cObj = new getClassesThread();
            getSubjectsThread obj = new getSubjectsThread();
            viewURL = this.getResources().getString(R.string.ip) + "/view.php";
            classURL = this.getResources().getString(R.string.ip) + "/getStudentClasses.php";
            obj.execute(user_id);
            cObj.execute(user_id);
        }

        if (userType.equalsIgnoreCase("Teacher")) {
            getTeacherSubjectsThread obj = new getTeacherSubjectsThread();
            viewURL = this.getResources().getString(R.string.ip) + "/view_teacher.php";
            obj.execute(user_id);
        }

        beaconManager = new BeaconManager(this);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Log.d("Beacons Found", Integer.toString(list.size()));
                    for (int i = 0; i < list.size(); i++)
                        Log.d("Beacon Powers", "Beacon " + Integer.toString(list.get(i).getMajor()) + ":" + Integer.toString(list.get(i).getMinor()) + ": " + Integer.toString(list.get(i).getRssi()));
                    Beacon nearestBeacon = list.get(0);
                    List<String> places = placesNearBeacon(nearestBeacon);
                    // TODO: update the UI here
                    Log.d("Measured Power", Integer.toString(nearestBeacon.getMeasuredPower()));
                    List<String> listItems = new ArrayList<String>();
                    for (int i = 0; i < places.size(); i++) {
                        listItems.add(places.get(i));
                    }
                    //Recycler_View_Adapter_Beacon_Search adapter = new Recycler_View_Adapter_Beacon_Search(getApplicationContext(), listItems);

                    //recyclerView.setAdapter(adapter);
                }
            }
        });
        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);


        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        Log.d(i.getStringExtra("userType"), user_id);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (userType.equalsIgnoreCase("Student"))
                            toStudentIntent(user_id, adapter, position);
                        else
                            toTeacherIntent(adapter, position);
                    }
                })
        );



    }

    private void toStudentIntent(String user_id, Recycler_View_Adapter adapter, int position) {
        Intent toSubjectIntent = new Intent(getApplicationContext(), SubjectActivity.class);
        toSubjectIntent.putExtra("subjectCode", adapter.getSubjectCode(position));
        toSubjectIntent.putExtra("subjectColor", adapter.getSubjectColor(position));
        toSubjectIntent.putExtra("ID", user_id);
        toSubjectIntent.putExtra("subjectName", adapter.getSubjectTitle(position));
        startActivity(toSubjectIntent);
    }

    private void toTeacherIntent(Recycler_View_Adapter adapter, int position) {
        Intent toStudentListIntent = new Intent(getApplicationContext(), StudentListActivity.class);
        toStudentListIntent.putExtra("classID", adapter.getClassID(position));
        toStudentListIntent.putExtra("subjectCode", adapter.getSubjectCode(position));
        toStudentListIntent.putExtra("subjectType", adapter.getSubjectType(position));
        startActivity(toStudentListIntent);
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_app)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    public StudentClass getCurrentClass () {
        DateTime today = new DateTime();
        Log.d("GCC", "Getting current class");
        StudentClass selected = null;

        for (int i = 0; i < studentClasses.size(); i++) {
            if (daysOfWeek[today.getDayOfWeek()].equalsIgnoreCase(studentClasses.get(i).getDOW())) {
                Log.d("GCC", "There's a class today");
                DateTime temp = new DateTime().withTime(studentClasses.get(i).getHR(), studentClasses.get(i).getMin(), 0, 0);
                if (Minutes.minutesBetween(today, temp).getMinutes() <= 15) {
                    Log.d("GCC", "There's a class soon!");
                    selected = studentClasses.get(i);
                }
                else
                    Log.d("GCC", "There's no class anytime soon");
            }

        }

        return selected;
    }


    private List<Subject> fill_with_data() {

        List<Subject> data = new ArrayList<>();

//        data.add(new Subject("Final Project", "CSCI321", "100%"));
//        data.add(new Subject("Distributed Systems", "CSCI319", "72%"));
//        data.add(new Subject("Mobile Applications", "CSCI342", "89%"));
//        data.add(new Subject("E-Business Fundementals", "ISIT204", "99%"));
//        data.add(new Subject("Computer Security", "CSCI262", "100%"));
//        data.add(new Subject("Network Security", "CSCI368", "72%"));

        Collections.sort(data, new Comparator<Subject>() {
            @Override
            public int compare(Subject lhs, Subject rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if (lhs.getCode().compareTo(rhs.getCode()) > 1)
                    return 1;
                else return -1;
            }
        });

        numOfSubjects = data.size();
        return data;
    }


    private class getSubjectsThread extends AsyncTask<String , String , String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            context = MainActivity.this;
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            String user_name = params[0];

            try
            {
                URL url = new URL(viewURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String post_data = URLEncoder.encode("Sid","UTF-8")+"="+ URLEncoder.encode(user_name,"UTF-8");
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
                    String name = jo.getString("subjects");
                    String title = jo.getString("title");

                    data.add(new Subject(title, name, "100%", "", "", ""));
                }


                //data = fill_with_data();

                dialog.dismiss();

                Log.d("ThreadDataSize", Integer.toString(data.size()));
                adapter = new Recycler_View_Adapter(data, getApplication());
                Log.d("AdapterSizeMain", Integer.toString(adapter.getItemCount()));
                recyclerView.setAdapter(adapter);


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private class getTeacherSubjectsThread extends AsyncTask<String , String , String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            context = MainActivity.this;
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            String user_name = params[0];

            try
            {
                URL url = new URL(viewURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String post_data = URLEncoder.encode("Tid","UTF-8")+"="+ URLEncoder.encode(user_name,"UTF-8");
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
                    String name = jo.getString("id");
                    String title = jo.getString("title");
                    String class_type = jo.getString("class_type");
                    String class_no = jo.getString("class_no");
                    String class_id = jo.getString("class_id");

                    Log.d("Class types", class_type + " " + class_no);
                    if (class_no.equalsIgnoreCase("null"))
                        class_no = "";
                    data.add(new Subject(title, name, "", class_type, class_no, class_id));
                }


                //data = fill_with_data();

                dialog.dismiss();

                Log.d("ThreadDataSize", Integer.toString(data.size()));
                adapter = new Recycler_View_Adapter(data, getApplication());
                Log.d("AdapterSizeMain", Integer.toString(adapter.getItemCount()));
                recyclerView.setAdapter(adapter);


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private class getClassesThread  extends AsyncTask<String, String, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
//            context = MainActivity.this;
//            dialog = new ProgressDialog(context);
//            dialog.setMessage("Loading...");
//            dialog.setCancelable(false);
//            dialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            String user_name = params[0];

            try
            {
                URL url = new URL(classURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String post_data = URLEncoder.encode("Sid","UTF-8")+"="+ URLEncoder.encode(user_name,"UTF-8");
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

                studentClasses = new ArrayList<>();

                for (int i = 0; i < ja.length(); i++)
                {
                    jo = ja.getJSONObject(i);
                    String class_id = jo.getString("id");
                    String subjectCode = jo.getString("subject_id");
                    String classType = jo.getString("class_type");
                    String dow = jo.getString("day_of_week");
                    String time = jo.getString("time");
                    String location_id = jo.getString("location_id");

                    Log.d("Class Info", class_id + " " + dow + " " + time + " " + location_id);
                    studentClasses.add(new StudentClass(class_id, subjectCode, classType, dow, time, location_id));
                }


                //data = fill_with_data();

                dialog.dismiss();

                Log.d("ThreadDataSize", Integer.toString(studentClasses.size()));
                current = getCurrentClass();
                Log.d("Current class", current.getClassID());

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }



}
