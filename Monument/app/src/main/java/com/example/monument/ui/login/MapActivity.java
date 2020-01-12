package com.example.monument.ui.login;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.monument.ShopActivity;
import com.example.monument.R;
import com.example.monument.data.model.ClusterMarker;
import com.example.monument.data.model.Landmark;
import com.example.monument.util.MyClusterManagerRenderer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener, View.OnClickListener, LocationListener {

    private GoogleMap mMap;
    private FloatingActionButton storeButton;
    private FloatingActionButton profileButton;

    private ArrayList<Icon> icons;
    private ArrayList<LatLng> monuments;
    private ArrayList<MarkerOptions> markers = new ArrayList<>();
    private ArrayList<Location> mLocationList = new ArrayList<>();
    private ArrayList<Landmark> mLandmarkList = new ArrayList<>();


    private ArrayList<Integer> pictures = new ArrayList<Integer>();
    public static User user;
    public String name;
    private int counter = 0;
    //LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;

    private Intent intent;

    private int PERMISSIONS_REQUEST_ENABLE_GPS = 1;

    private int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

    private Button placeBalloonButton;
    private TextView nearestBalloonText;
    private ImageView previewBaloonImage;
    private ImageView expandedPreviewImage;
    public static Marker myMarker;

    private ArrayList<Balloon> otherBalloons;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if (!isLocationEnabled(this)) {
            buildAlertMessageForGps();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intent = new Intent(this, MonumentActivity.class);
        placeBalloonButton = findViewById(R.id.placeBalloonButton);
        nearestBalloonText = findViewById(R.id.nearestBalloon);
        previewBaloonImage = findViewById(R.id.previewBalloonImage);
        expandedPreviewImage = findViewById(R.id.expandedPreviewImage);
        placeBalloonButton.setOnClickListener(this);
        expandedPreviewImage.setOnClickListener(this);
        previewBaloonImage.setOnClickListener(this);
        expandedPreviewImage.setElevation(-1);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        monuments = new ArrayList<>();
        storeButton = findViewById(R.id.storeButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        otherBalloons = new ArrayList<>();


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
                    public void onClick(DialogInterface dialogInterface, int i) {

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

    public void addMarkers(){

        pictures.add(R.drawable.arc_de_triomphe);pictures.add(R.drawable.chichen_itza);
        pictures.add(R.drawable.colosseum); pictures.add(R.drawable.eiffel_tower);
        pictures.add(R.drawable.great_sphinx_of_giza); pictures.add(R.drawable.colosseum);
        pictures.add(R.drawable.mount_rushmore); pictures.add(R.drawable.statue_of_liberty);
        pictures.add(R.drawable.sydney_opera_house); pictures.add(R.drawable.taipei_101);
        pictures.add(R.drawable.taj_mahal);


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
        addMarkers();
        getLocationPermission();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        googleMap.setOnMarkerClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(34, -119)));

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

                        mMap.addMarker(new MarkerOptions().position(monuments.get(monuments.size() - 1)).
                                title(document.getId()).icon(BitmapDescriptorFactory.
                                fromResource(pictures.get(counter))).snippet("Monument"));
                        counter = counter + 1;
                    }

                }
                //mClusterManager.cluster();

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

                        if (document.getId().equals(getIntent().getStringExtra("user"))) {
                            user = new User(document.getId(), document.getLong("Currency"));
                            name = document.getString("Name");
                            found = true;
                            if ((ArrayList<String>) document.get("Monuments") != null) {
                                user.setVisited((ArrayList<String>) document.get("Monuments"));
                            } else {
                                Log.d("Status of arraylist", "empty");
                                user.setVisited(new ArrayList<String>());
                            }
                            if(document.get("Balloon") != null) {
                                myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(document.getGeoPoint("Balloon").getLatitude(), document.getGeoPoint("Balloon").getLongitude())).title("Your Marker"));
                            }
                            else {

                                myMarker = null;
                            }

                        }
                        else{
                            otherBalloons.add(new Balloon());
                            otherBalloons.get(otherBalloons.size() -1 ).geoPoint = document.getGeoPoint("Balloon");
                            otherBalloons.get(otherBalloons.size() -1 ).name = document.getId();
                            if(document.getGeoPoint("Balloon") == null){
                                otherBalloons.remove(otherBalloons.size() - 1);
                            }
                        }
                    }
                    if (!found) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("Currency", 0);
                        data.put("Name", getIntent().getStringExtra("name"));
                        userData.document(getIntent().getStringExtra("user")).set(data);
                    }

                    initializeLocation();

                } else {
                    //Log.w(TAG, "Error getting documents.", task.getException());
                }
            }


        });



        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

    }

    private void initializeLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("currency", user.getCurrency());
                intent.putExtra("user", user.getUser());
                startActivity(intent);
            }
        });

        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ShopActivity.class);
                intent.putExtra("currency", user.getCurrency());
                intent.putExtra("user", user.getUser());
                intent.putExtra("name", getIntent().getStringExtra("name"));
                startActivity(intent);
            }
        });

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

                            if (results[0] < 1000) {
                                intent.putExtra("monumentName", marker.getTitle());
                                intent.putExtra("user", user.getUser());
                                intent.putExtra("currency", user.getCurrency());
                                if (user.getVisited() != null) {
                                    intent.putStringArrayListExtra("monuments", user.getVisited());
                                }
                                startActivity(intent);
                            } else {
                                rejectLocation();
                            }

                        }
                    }
                });

        return false;
    }

    private void rejectLocation() {
        Toast.makeText(this, "You aren't close enough", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View v) {
        if (v.getId() == placeBalloonButton.getId()) {
            dispatchTakePictureIntent();
        }
        else if (v.getId() == previewBaloonImage.getId()){
            //expandedPreviewImage.setImageAlpha(255);
            expandedPreviewImage.setElevation(10);
        }
        else if (v.getId() == expandedPreviewImage.getId()){
            //expandedPreviewImage.setImageAlpha(0);
            expandedPreviewImage.setElevation(-1);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(user.getUser());
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    String currentPhotoPath;

    private File createImageFile(String name) throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + name + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            /*
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            */

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();


            Uri file = Uri.fromFile(new File(currentPhotoPath));
            StorageReference riversRef = storageReference.child("Images/" + user.getUser() +".jpg");

            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            saveLocation();


                        }

                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        }
    }
    public void saveLocation(){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> balloonPosition = new HashMap<>();
                            balloonPosition.put("Balloon", new GeoPoint(location.getLatitude(), location.getLongitude()));
                            db.collection("UserData").document(user.getUser()).set(balloonPosition);
                            if(myMarker != null) {myMarker.remove();}
                            myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Balloon"));
                        }
                    }
                });
    }

    public class Balloon implements Comparable<Balloon>{

        public float distanceToPlayer;
        public String name;
        public GeoPoint geoPoint;


        @Override
        public int compareTo(Balloon o) {
            return Float.compare(distanceToPlayer, o.distanceToPlayer);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        for (Balloon i : otherBalloons){
            if(i.geoPoint != null) {
                float distance[] = new float[2];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), i.geoPoint.getLatitude(), i.geoPoint.getLongitude(), distance);
                i.distanceToPlayer = distance[0];
                Log.d("distance:",location.getLatitude() +" " + i.geoPoint.getLatitude() + " " +distance[0]);
            }
        }

        Collections.sort(otherBalloons);
        if(otherBalloons.size() != 0){

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference pathReference = storageReference.child("Images/" + otherBalloons.get(0).name +".jpg");
            final long FIVE_MEGABYTE = 1024 * 1024 * 5;
            pathReference.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bmp= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    previewBaloonImage.setImageBitmap(bmp);
                    expandedPreviewImage.setImageBitmap(bmp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });



            Log.d("Scored:", otherBalloons.get(0).name +" " + otherBalloons.get(0).distanceToPlayer);
            nearestBalloonText.setText("The nearest balloon is " +otherBalloons.get(0).distanceToPlayer +"m away");

            if(otherBalloons.get(0).distanceToPlayer < 2.5 ) {
                //Score!
                Log.d("Scored:", "Scored " + otherBalloons.get(0).name + "'s Balloon!");
                final FirebaseFirestore db = FirebaseFirestore.getInstance();

                final CollectionReference userData = db.collection("UserData");
                userData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                //Toast.makeText(this, document.getData(), Toast.LENGTH_SHORT).show();
                                Log.d("user", document.getId() + " => " + document.getData());

                                if (document.getId().equals(otherBalloons.get(0).name)) {
                                    DocumentReference docRef = db.collection("UserData").document(document.getId());

                                    Map<String,Object> updates = new HashMap<>();
                                    updates.put("Balloon", FieldValue.delete());

                                    docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        // [START_EXCLUDE]
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            DocumentReference player = FirebaseFirestore.getInstance().collection("UserData").document(user.getUser());
                                            Map<String,Object> updates = new HashMap<>();
                                            updates.put("Currency", user.getCurrency() + 5);
                                            player.set(updates);
                                            otherBalloons.remove(0);
                                        }
                                        // [START_EXCLUDE]
                                    });

                                }


                            }


                        }
                    }
                });



            }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final CollectionReference userData = db.collection("UserData");
        userData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        if (document.getId().equals(getIntent().getStringExtra("user"))) {
                            user = new User(document.getId(), document.getLong("Currency"));

                        }
                    }
                }

            }
        });

    }
}
