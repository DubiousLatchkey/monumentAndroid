package com.example.monument.ui.login;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.monument.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private ArrayList<LatLng> monuments;

    public User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        monuments = new ArrayList<LatLng>();
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

        googleMap.setOnMarkerClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

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

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(this, MonumentActivity.class);
        intent.putExtra("monumentName", marker.getTitle());
        startActivity(intent);
        return false;
    }
}
