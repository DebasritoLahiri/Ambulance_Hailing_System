package com.android.debasrito.driver;

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
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView nameget, mobileget, carget;
    String dname, dmobile, car, id, cartype="Select";
    Button check;
    Intent checkintent;
    LocationManager locationManager;
    Location loc;
    Boolean status=true,active=false,found=false;
    Spinner typeselect;
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        nameget=(TextView) findViewById(R.id.name);
        mobileget=(TextView) findViewById(R.id.mobile);
        carget=(TextView) findViewById(R.id.vehiclenumber);
        check=(Button) findViewById(R.id.checkstatus);
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
        check.setOnClickListener(new View.OnClickListener() {
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
                } else if(TextUtils.isEmpty(carget.getText())) {
                    carget.setError("Car number required");
                } else {
                    dname = nameget.getText().toString();
                    dmobile = mobileget.getText().toString();
                    car = carget.getText().toString();
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("drivers");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot datas : dataSnapshot.getChildren()) {
                                if (datas.child("drivername").getValue(String.class).equals(dname) && datas.child("cartype").getValue(String.class).equals(cartype) && datas.child("drivercar").getValue(String.class).equals(car) && datas.child("drivermobile").getValue(String.class).equals(dmobile)) {
                                    status = datas.child("driverstatus").getValue(Boolean.class);
                                    active = datas.child("driveractive").getValue(Boolean.class);
                                    id= datas.getKey();
                                    found=true;
                                if (!active) //not logged in. set logged in and go to next intent. pass status as true to disable find route function
                                {
                                    checkintent = new Intent(MainActivity.this, status.class);
                                    checkintent.putExtra("NAME", dname);
                                    checkintent.putExtra("MOBILE", dmobile);
                                    checkintent.putExtra("CAR", car);
                                    checkintent.putExtra("ID",id);
                                    checkintent.putExtra("STATUS",true);
                                    checkintent.putExtra("ACTIVE",true);
                                    checkintent.putExtra("CARTYPE",cartype);
                                    reference.child(id).child("driveractive").setValue(true);
                                    reference.child(id).child("driverstatus").setValue(true);
                                    //checkintent.putExtra("PATIENT", "Please wait");
                                    //checkintent.putExtra("PMOB", "Please wait");
                                    //checkintent.putExtra("PLAT","Please wait");
                                    //checkintent.putExtra("PLON","Please wait");
                                    startActivity(checkintent);
                                    finish();
                                }
                                else if(active && !status)//logged in and patient taken
                                     {
                                         //get details of patient and pass that along with status to set up find route buttons
                                         checkintent = new Intent(MainActivity.this, status.class);
                                         checkintent.putExtra("NAME", dname);
                                         checkintent.putExtra("MOBILE", dmobile);
                                         checkintent.putExtra("CAR", car);
                                         checkintent.putExtra("ID",id);
                                         checkintent.putExtra("STATUS",false);
                                         checkintent.putExtra("ACTIVE",true);
                                         checkintent.putExtra("CARTYPE",cartype);
                                         //checkintent.putExtra("PATIENT", "Please wait");
                                         //checkintent.putExtra("PMOB", "Please wait");
                                         //checkintent.putExtra("PLAT","Please wait");
                                         //checkintent.putExtra("PLON","Please wait");
                                         startActivity(checkintent);
                                         finish();
                                     }
                                else if(active && status) //logged in and patient not taken
                                    {
                                        //go to next intent. pass status to disable find route button
                                        checkintent = new Intent(MainActivity.this, status.class);
                                        checkintent.putExtra("NAME", dname);
                                        checkintent.putExtra("MOBILE", dmobile);
                                        checkintent.putExtra("CAR", car);
                                        checkintent.putExtra("ID",id);
                                        checkintent.putExtra("STATUS",true);
                                        checkintent.putExtra("ACTIVE",true);
                                        checkintent.putExtra("CARTYPE",cartype);
                                        //checkintent.putExtra("PATIENT", "Please wait");
                                        //checkintent.putExtra("PMOB", "Please wait");
                                        //checkintent.putExtra("PLAT","Please wait");
                                        //checkintent.putExtra("PLON","Please wait");
                                        startActivity(checkintent);
                                        finish();
                                    }
                                break;
                                }
                                }
                            if(!found)
                            {
                                //else account not found. create it using push() method
                                id=reference.push().getKey();
                                assert id != null;
                                reference.child(id).setValue(new details(dname,dmobile,car,"Please wait","Please wait",cartype,true,true,loc.getLatitude(),loc.getLongitude(),0.0,0.0));
                                checkintent = new Intent(MainActivity.this, status.class);
                                checkintent.putExtra("NAME", dname);
                                checkintent.putExtra("MOBILE", dmobile);
                                checkintent.putExtra("CAR", car);
                                checkintent.putExtra("ID",id);
                                checkintent.putExtra("STATUS",true);
                                checkintent.putExtra("ACTIVE",true);
                                checkintent.putExtra("CARTYPE",cartype);
                                startActivity(checkintent);
                                finish();
                            }
                            }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
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
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
class details{
    public String drivername,drivermobile,drivercar,patientname,patientmobile,cartype;
    public Boolean driverstatus, driveractive;
    public Double driverlatitude,driverlongitude,patientlongitude,patientlatitude;
    details(String drivername, String drivermobile, String drivercar, String patientname, String patientmobile, String cartype, Boolean driverstatus, Boolean driveractive, Double driverlatitude, Double driverlongitude,Double patientlatitude,Double patientlongitude)
    {
        this.drivername=drivername;
        this.drivermobile=drivermobile;
        this.drivercar=drivercar;
        this.patientname=patientname;
        this.patientmobile=patientmobile;
        this.driverstatus=driverstatus;
        this.driveractive=driveractive;
        this.driverlatitude=driverlatitude;
        this.driverlongitude=driverlongitude;
        this.patientlatitude=patientlatitude;
        this.patientlongitude=patientlongitude;
        this.cartype=cartype;
    }
}
