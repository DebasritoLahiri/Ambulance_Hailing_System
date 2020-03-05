package com.android.debasrito.driver;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class hospitalfinder extends AppCompatActivity implements LocationListener{
GoogleApiClient mGoogleApiClient;
Location loc;
LocationManager locationManager;
private String TAG = MainActivity.class.getSimpleName();
private ListView lv;
ArrayList<HashMap<String,String>> hospitalList;
JSONObject jsonObj;
JSONArray results;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitalfinder);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (!isNetworkAvailable())
        {
            Toast.makeText(hospitalfinder.this,"Please enable internet.",Toast.LENGTH_SHORT).show();
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        while(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        try {
            assert locationManager != null;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
        hospitalList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        HttpHandler sh = new HttpHandler();
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+loc.getLatitude()+","+loc.getLongitude()+"&rankby=distance&type=hospital&key=AIzaSyAPOJW2kijtdN2w2KDjYInf5Is7m8VlELg";
        String jsonStr = sh.makeServiceCall(url);
        System.out.println(jsonStr);
        try {
            jsonObj = new JSONObject(jsonStr);
            results = jsonObj.getJSONArray("results");
            for(int i=0;i<results.length();i++)
            {
                JSONObject res = results.getJSONObject(i);
                String name=res.getString("name");
                String lat= String.valueOf(res.getJSONObject("geometry").getJSONObject("location").getDouble("latitude"));
                String lon= String.valueOf(res.getJSONObject("geometry").getJSONObject("location").getDouble("longitude"));
                String rating= String.valueOf(res.getDouble("rating"));
                String id=res.getString("id");
                String vicinity=res.getString("vicinity");
                HashMap<String, String> hosp = new HashMap<>();
                hosp.put("id", id);
                hosp.put("name", name);
                hosp.put("rating", rating);
                hosp.put("vicinity", vicinity);
                hosp.put("lat",lat);
                hosp.put("lon",lon);
                hospitalList.add(hosp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ListAdapter adapter = new SimpleAdapter(this, hospitalList,
                R.layout.singledet, new String[]{ "name","rating","vicinity"},
                new int[]{R.id.name, R.id.rating, R.id.vicinity});
        lv.setAdapter(adapter);
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selected = new HashMap<>(hospitalList.get(position));
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+selected.get("lat")+","+selected.get("long")));
                startActivity(intent);
            }
        });
    }
/*    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }*/
    public void onLocationChanged(Location location) {
        loc=location;
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    public void onProviderEnabled(String provider) {

    }
    public void onProviderDisabled(String provider) {
        Toast.makeText(hospitalfinder.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
