package com.example.monument.ui.login;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.monument.MainActivity;
import com.example.monument.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener{

    private GoogleMap mMap;
    private FloatingActionButton storeButton;
    private FloatingActionButton profileButton;

    private ArrayList<LatLng> monuments;

    public static User user;
    //LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;

    private Intent intent;

    private int PERMISSIONS_REQUEST_ENABLE_GPS = 1;

    private int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if (!isLocationEnabled(this)){
            buildAlertMessageForGps();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intent = new Intent(this, MonumentActivity.class);


        monuments = new ArrayList<>();
        storeButton = findViewById(R.id.storeButton);
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class); //Placeholder, TODO: replace with store class
                startActivity(intent);
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);




    }


    private void buildAlertMessageForGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){

                        dialogInterface.cancel();
                        Context context = getApplicationContext();
                        CharSequence text = "Location services turned off";
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        googleMap.setOnMarkerClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(34, -119)));
        ;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final CollectionReference locations = db.collection("Locations");

        locations.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Toast.makeText(this, document.getData(), Toast.LENGTH_SHORT).show();
                        Log.d("example", document.getId() + " => " + document.getData());
                        GeoPoint geoPoint = document.getGeoPoint("location");
                        monuments.add(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                        mMap.addMarker(new MarkerOptions().position(monuments.get(monuments.size() - 1)).title(document.getId()));
                    }

                } else {
                    //Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });

        final CollectionReference userData = db.collection("UserData");
        userData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    boolean found = false;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Toast.makeText(this, document.getData(), Toast.LENGTH_SHORT).show();
                        Log.d("user", document.getId() + " => " + document.getData());

                        if(document.getId().equals(getIntent().getStringExtra("user"))){
                            user = new User(document.getId(), document.getLong("Currency"));
                            found = true;
                            if((ArrayList<String>)document.get("Monuments") != null) {
                                user.setVisited((ArrayList<String>) document.get("Monuments"));
                            }
                            else {
                                Log.d("Status of arraylist", "empty");
                                user.setVisited(new ArrayList<String>());
                            }
                            break;
                        }
                    }
                    if(!found){
                        Map<String, Object> data = new HashMap<>();
                        data.put("Currency", 0);
                        userData.document(getIntent().getStringExtra("user")).set(data);
                    }

                } else {
                    //Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });


        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("currency", user.getCurrency());
                intent.putExtra("email", user.getUser());
                startActivity(intent);
            }
        });

        //locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        //Log.d("Location", locationManager.getLastKnownLocation());

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            float[] results = new float[2];
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(), marker.getPosition().latitude, marker.getPosition().longitude, results);
                            Log.d("distance: ", Float.toString(results[0]));

                            if(results[0] < 1000){
                                intent.putExtra("monumentName", marker.getTitle());
                                intent.putExtra("user", user.getUser());
                                intent.putExtra("currency", user.getCurrency());
                                if(user.getVisited() != null) {
                                    intent.putStringArrayListExtra("monuments", user.getVisited());
                                }
                                startActivity(intent);
                            }
                            else {
                                rejectLocation();
                            }

                        }
                    }
                });

        return false;
    }
    private void rejectLocation(){
        Toast.makeText(this,"You aren't close enough", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

}
