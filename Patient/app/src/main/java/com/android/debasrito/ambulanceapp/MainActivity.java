package com.android.debasrito.ambulanceapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements  LocationListener {
    TextView nameget, mobileget;
    String name, mobile, cartype="Select";
    Button book;
    Intent finderintent;
    LocationManager locationManager;
    Location loc;
    Spinner typeselect;
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nameget = (TextView) findViewById(R.id.name);
        mobileget = (TextView) findViewById(R.id.mobile);
        book = (Button) findViewById(R.id.book);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        while(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
        final List<String> list=new ArrayList<>();
        list.add("Select");list.add("AC");list.add("Non-AC");
        ArrayAdapter<String> type=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,list);
        type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeselect=(Spinner) findViewById(R.id.typelist);
        typeselect.setAdapter(type);
        typeselect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cartype=list.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable())
                {
                    Toast.makeText(MainActivity.this,"Please enable internet.",Toast.LENGTH_SHORT).show();
                }
                else if(cartype.equals(list.get(0)))
                {
                    Toast.makeText(MainActivity.this, "Please choose a car type.", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(nameget.getText())) {
                    nameget.setError("Name Required");
                } else if (TextUtils.isEmpty(mobileget.getText())) {
                    mobileget.setError("Mobile number required");
                } else {
                    name = nameget.getText().toString();
                    mobile = mobileget.getText().toString();
                    finderintent = new Intent(MainActivity.this, finder.class);
                    finderintent.putExtra("NAME", name);
                    finderintent.putExtra("MOBILE", mobile);
                    finderintent.putExtra("LOCATION", (Parcelable) loc);
                    finderintent.putExtra("CARTYPE",cartype);
                    startActivity(finderintent);
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        loc=location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
