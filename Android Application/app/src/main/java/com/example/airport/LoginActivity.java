package com.example.airport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import at.favre.lib.dali.Dali;

/**
 * Created by Zain on 4/24/17.
 */

public class LoginActivity extends AppCompatActivity {

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

    //ImageView imageView;
    ConstraintLayout constraintLayout;
    EditText loginEmail;
    EditText loginPassword;
    ImageButton loginButton;

    String username = "";
    String password = "";

    String login_url = null;
    //String login_url = "http://172.28.22.212/login_another.php";

    ProgressDialog dialog;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        constraintLayout = (ConstraintLayout) findViewById(R.id.conLayout);
        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginButton = (ImageButton) findViewById(R.id.loginButton);

        constraintLayout.setBackground(Dali.create(this).load(R.drawable.loginbg1).blurRadius(12).get());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
            }
        });

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

    }

    public boolean isOnline() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting())
            return true;
        else
            return false;

    }

    public void login(View view)
    {

        username = loginEmail.getText().toString();
        //Toast.makeText(this , username , Toast.LENGTH_SHORT).show();
        password = loginPassword.getText().toString();

        if (username.length() == 0 || password.length() == 0 || !isOnline()) {
            Toast.makeText(this, "Please enter both username & password", Toast.LENGTH_SHORT).show();
        }
        else if (!isOnline())
            Toast.makeText(this, "Please make sure you have a network connection", Toast.LENGTH_SHORT).show();
        else
        {
            if (username.contains("uowmail")) {
                login_url = this.getResources().getString(R.string.ip) + "/login_another.php";
                LoginThread obj = new LoginThread();
                obj.execute(username, password);
            }
            if (username.contains("uowdubai")) {
                login_url = this.getResources().getString(R.string.ip) + "/login_teacher.php";
                TeacherLoginThread obj = new TeacherLoginThread();
                obj.execute(username, password);
            }
        }
    }

    private class LoginThread extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context = LoginActivity.this;
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String user_name = params[0];
            String user_pass = params[1];

            try {
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                //httpURLConnection.setReadTimeout(20000 /* milliseconds */);
                //httpURLConnection.setConnectTimeout(20000 /* milliseconds */);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("Sid", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8") + "&"
                        + URLEncoder.encode("Password", "UTF-8") + "=" + URLEncoder.encode(user_pass, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPreExecute();

            try {
                String id = "";
                String pass = "";
                String f_name = "";
                String l_name = "";
                JSONArray ja = new JSONArray(s);
                JSONObject jo = null;

                for (int i = 0; i < ja.length(); i++) {

                    jo = ja.getJSONObject(i);
                    id = jo.getString("id");
                    pass = jo.getString("password");
                    f_name = jo.getString("first_name");
                    l_name = jo.getString("last_name");
                    //Toast.makeText(getApplicationContext() , id+" "+pass , Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(getApplicationContext() , password , Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                if (password.equals(pass)) {
                    //Toast.makeText(getApplicationContext() , "YES" , Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("userType", "Student");
                    i.putExtra("ID", id);
                    i.putExtra("firstName", f_name);
                    i.putExtra("lastName", l_name);
                    //dialog.dismiss();
                    startActivity(i);
                    finish();
                } else {
                    //dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Try Again!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class TeacherLoginThread extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context = LoginActivity.this;
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String user_name = params[0];
            String user_pass = params[1];

            try {
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                //httpURLConnection.setReadTimeout(20000 /* milliseconds */);
                //httpURLConnection.setConnectTimeout(20000 /* milliseconds */);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("Tid", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8") + "&"
                        + URLEncoder.encode("Password", "UTF-8") + "=" + URLEncoder.encode(user_pass, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPreExecute();

            try {
                String id = "";
                String f_name = "";
                String l_name = "";
                String pass = "";
                JSONArray ja = new JSONArray(s);
                JSONObject jo = null;

                for (int i = 0; i < ja.length(); i++) {

                    jo = ja.getJSONObject(i);
                    id = jo.getString("teacher_id");
                    f_name = jo.getString("first_name");
                    l_name = jo.getString("last_name");
                    pass = jo.getString("password");
                    //Toast.makeText(getApplicationContext() , id+" "+pass , Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(getApplicationContext() , password , Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                if (password.equals(pass)) {
                    //Toast.makeText(getApplicationContext() , "YES" , Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("userType", "Teacher");
                    i.putExtra("ID", id);
                    i.putExtra("firstName", f_name);
                    i.putExtra("lastName", l_name);
                    //dialog.dismiss();
                    startActivity(i);
                    finish();
                } else {
                    //dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Try Again!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
