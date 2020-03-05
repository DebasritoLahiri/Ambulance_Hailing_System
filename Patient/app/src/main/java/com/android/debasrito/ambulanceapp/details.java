package com.android.debasrito.ambulanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

public class details extends AppCompatActivity implements OnMapReadyCallback {
    TextView caseid, name, mobile,car;
    Intent i;
    String patient, pmobile;
    private MapView mapView;
    private GoogleMap gmap;
    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyDhdIQRxEf0jaIepeZylPW64flu6zKgu1I";
    Location loc;
    Location dloc=new Location("");
    String dname = "PLEASE WAIT", dmobile = "PLEASE WAIT", did = "PLEASE WAIT", cardet = "PLEASE WAIT", cartype;
    double dist = 1000000000,distance;
    Marker dmark,dmark2,pmark,pmark2;
    Boolean status,active;
    boolean notavail=true;
    Double dlat,dlong;
    //long cost,totaltime,totaltimefinal;
    final DatabaseReference referencemap = FirebaseDatabase.getInstance().getReference("drivers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        caseid = (TextView) findViewById(R.id.caseid);
        name = (TextView) findViewById(R.id.drivername);
        mobile = (TextView) findViewById(R.id.mobile);
        car = (TextView) findViewById(R.id.cardet);
        i = getIntent();
        patient = i.getStringExtra("NAME");
        pmobile = i.getStringExtra("MOBILE");
        loc = i.getParcelableExtra("LOCATION");
        status=i.getBooleanExtra("STATUS",false);
        active=i.getBooleanExtra("ACTIVE",false);
        dname=i.getStringExtra("DRIVER");
       dmobile= i.getStringExtra("DMOBILE");
        dloc=i.getParcelableExtra("DLOC");
        cardet=i.getStringExtra("CAR");
        did=i.getStringExtra("ID");
        notavail=i.getBooleanExtra("NOTAVAIL",true);
        cartype=i.getStringExtra("CARTYPE");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("drivers");
        if (notavail) {
            Toast.makeText(this, "No ambulance found", Toast.LENGTH_LONG).show();
            finish();
        } else {
            reference.child(did).child("driverstatus").setValue(false);
            reference.child(did).child("patientlatitude").setValue(loc.getLatitude());
            reference.child(did).child("patientlongitude").setValue(loc.getLongitude());
            reference.child(did).child("patientname").setValue(patient);
            reference.child(did).child("patientmobile").setValue(pmobile);
            caseid.setText(did);
            name.setText(dname);
            mobile.setText(dmobile);
            car.setText(cardet);
        }
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.map);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(12);
        LatLng ppos = new LatLng(loc.getLatitude(), loc.getLongitude());
        pmark = googleMap.addMarker(new MarkerOptions().position(ppos)
                .title("Your position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        if (dloc != null) {
            LatLng dpos = new LatLng(dloc.getLatitude(), dloc.getLongitude());
            dmark = gmap.addMarker(new MarkerOptions().position(dpos)
                    .title("Ambulance position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            Polyline line = gmap.addPolyline(new PolylineOptions()
                    .add(new LatLng(loc.getLatitude(), loc.getLongitude()), new LatLng(dloc.getLatitude(), dloc.getLongitude()))
                    .width(5)
                    .color(Color.RED));
        }
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ppos));
        //try new..................................................................
        referencemap.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!notavail) {
                    dloc.setLatitude(dataSnapshot.child(did).child("driverlatitude").getValue(Double.class));
                    dloc.setLongitude(dataSnapshot.child(did).child("driverlongitude").getValue(Double.class));
                    gmap.clear();
                    LatLng ppos = new LatLng(loc.getLatitude(), loc.getLongitude());
                    pmark2 = gmap.addMarker(new MarkerOptions().position(ppos)
                            .title("Your position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    LatLng dpos = new LatLng(dloc.getLatitude(), dloc.getLongitude());
                    dmark2 = gmap.addMarker(new MarkerOptions().position(dpos)
                            .title("Ambulance position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                    Polyline line = gmap.addPolyline(new PolylineOptions()
                            .add(new LatLng(loc.getLatitude(), loc.getLongitude()), new LatLng(dloc.getLatitude(), dloc.getLongitude()))
                            .width(10)
                            .color(Color.BLACK));
                }
                /*else if(dataSnapshot.child(did).child("driverstatus").getValue(Boolean.class))
                {
                    //Trip end calculations.
                    //totaltime=dataSnapshot.child("endtime").getValue(Long.class)-dataSnapshot.child("starttime").getValue(Long.class);
                    totaltime=dataSnapshot.child("time").getValue(int.class);
                    totaltimefinal=(((totaltime/100)/60)/60);
                    if(totaltimefinal<0.25)
                        cost=25;
                    else
                        cost=totaltimefinal*100;
                    System.out.println(cost);
                    //Intent intent = new Intent();
                    //startActivity(intent);

                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //End try new................................................................................
    }
}