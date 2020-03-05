package com.android.debasrito.ambulanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

public class finder extends AppCompatActivity {
    Intent i,detailsintent;
    String patient, pmobile;
    Location loc;
    Location dloc=new Location("");
    String dname = "PLEASE WAIT", dmobile = "PLEASE WAIT", did = "PLEASE WAIT",cardet = "PLEASE WAIT",cartype;
    double dist = 1000000000,distance;
    Boolean status,active;
    boolean notavail=true;
    Double dlat,dlong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finder);
        i = getIntent();
        patient = i.getStringExtra("NAME");
        pmobile = i.getStringExtra("MOBILE");
        loc = i.getParcelableExtra("LOCATION");
        cartype=i.getStringExtra("CARTYPE");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("drivers");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        status=datas.child("driverstatus").getValue(Boolean.class);
                        active=datas.child("driveractive").getValue(Boolean.class);
                        if (status && active && cartype.equals(datas.child("cartype").getValue(String.class))) {
                            notavail=false;
                            LatLng from = new LatLng(loc.getLatitude(), loc.getLongitude());
                            dlat=datas.child("driverlatitude").getValue(Double.class);
                            dlong=datas.child("driverlongitude").getValue(Double.class);
                            assert dlong != null;
                            assert dlat !=null;
                            LatLng to = new LatLng(dlat, dlong);
                            //Calculating the distance in meters
                            distance = SphericalUtil.computeDistanceBetween(from,to);
                            if (distance < dist) {
                                dist = distance;
                                did = datas.getKey();
                                dloc.setLatitude(dlat);
                                dloc.setLongitude(dlong);
                                dname = datas.child("drivername").getValue(String.class);
                                dmobile = datas.child("drivermobile").getValue(String.class);
                                cardet = datas.child("drivercar").getValue(String.class);
                            }
                        }
                    }
                }
                detailsintent=new Intent(finder.this,details.class);
                detailsintent.putExtra("NAME", patient);
                detailsintent.putExtra("MOBILE", pmobile);
                detailsintent.putExtra("LOCATION", (Parcelable) loc);
                detailsintent.putExtra("STATUS",status);
                detailsintent.putExtra("ACTIVE",active);
                detailsintent.putExtra("DRIVER",dname);
                detailsintent.putExtra("DMOBILE",dmobile);
                detailsintent.putExtra("DLOC",(Parcelable) dloc);
                detailsintent.putExtra("CAR",cardet);
                detailsintent.putExtra("ID",did);
                detailsintent.putExtra("NOTAVAIL",notavail);
                detailsintent.putExtra("CARTYPE",cartype);
                startActivity(detailsintent);
                finish();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
