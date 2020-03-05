package com.android.debasrito.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class status extends AppCompatActivity implements LocationListener {

    Intent i;
    TextView namevalue,mobilevalue;
    String dname,dmobile,car,id,pname,pmobile,cartype;
    Double plat,plong;
    Button router,done,logout,hospital;
    Boolean status,active;
    LocationManager locationManager;
    Location loc;
    //int count=0;
    long starttime,totaltime,totaltimefinal,cost;
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        i=getIntent();
        status= (Boolean) i.getExtras().getBoolean("STATUS");
        active= (Boolean) i.getExtras().getBoolean("ACTIVE");
        dname=i.getStringExtra("NAME");
        dmobile=i.getStringExtra("MOBILE");
        car=i.getStringExtra("CAR");
        id=i.getStringExtra("ID");
        cartype=i.getStringExtra("CARTYPE");
        //pname=i.getStringExtra("PATIENT");
        //pmobile=i.getStringExtra("PMOB");
        //plat=i.getStringExtra("PLAT");
        //plong=i.getStringExtra("PLONG");
        namevalue=(TextView) findViewById(R.id.namevalue);
        mobilevalue=(TextView) findViewById(R.id.mobilevalue);
        router=(Button) findViewById(R.id.router);
        done=(Button) findViewById(R.id.Done);
        logout=(Button) findViewById(R.id.logout);
        hospital=(Button) findViewById(R.id.hospital);
        hospital.setEnabled(false);
        router.setEnabled(false);
        done.setEnabled(false);
        if (!isNetworkAvailable())
        {
            Toast.makeText(status.this,"Please enable internet.",Toast.LENGTH_SHORT).show();
        }
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
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("drivers/"+id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("driveractive").getValue(Boolean.class)&&!dataSnapshot.child("driverstatus").getValue(Boolean.class))
                {
                    pname=dataSnapshot.child("patientname").getValue(String.class);
                    pmobile=dataSnapshot.child("patientmobile").getValue(String.class);
                    plat=dataSnapshot.child("patientlatitude").getValue(Double.class);
                    plong=dataSnapshot.child("patientlongitude").getValue(Double.class);
                    //reference.child("time").setValue(count++);
                    namevalue.setText(pname);
                    mobilevalue.setText(pmobile);
                    logout.setEnabled(false);
                    router.setEnabled(true);
                    hospital.setEnabled(true);
                    done.setEnabled(true);
                }
                /*else
                {
                    count=0;
                }*/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("driverstatus").setValue(true);
                status=true;
                reference.child("driveractive").setValue(false);
                active=false;
                finish();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("driverstatus").setValue(true);
                status=true;
                reference.child("patientname").setValue("Please wait");
                pname="Please wait";
                reference.child("patientmobile").setValue("Please wait");
                pmobile="Please wait";
                reference.child("patientlatitude").setValue(0.0);
                plat=0.0;
                reference.child("patientlongitude").setValue(0.0);
                plong=0.0;
                //reference.child("endtime").setValue(Calendar.getInstance().getTimeInMillis());
                /*reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //totaltime=dataSnapshot.child("endtime").getValue(Long.class)-dataSnapshot.child("starttime").getValue(Long.class);
                        totaltime=dataSnapshot.child("time").getValue(int.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/
                namevalue.setText(pname);
                mobilevalue.setText(pmobile);
                done.setEnabled(false);
                router.setEnabled(false);
                hospital.setEnabled(false);
                logout.setEnabled(true);
                /*totaltimefinal=(((totaltime/100)/60)/60);
                if(totaltimefinal<0.25)
                    cost=25;
                else
                    cost=totaltimefinal*100;
                System.out.println(cost);*/
                //Intent intent = new Intent();
                //startActivity(intent);
            }
        });
        hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent=new Intent(status.this,hospitalfinder.class);
                //startActivity(intent);
                Uri gmmIntentUri=Uri.parse("geo:0,0?q=hospitals");
                Intent mapIntent=new Intent(Intent.ACTION_VIEW,gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        router.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+plat.toString()+","+plong.toString()));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        loc=location;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("drivers/"+id);
        reference.child("driverlatitude").setValue(loc.getLatitude());
        reference.child("driverlongitude").setValue(loc.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(status.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
