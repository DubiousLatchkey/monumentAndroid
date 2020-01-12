package com.example.monument.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monument.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonumentActivity extends AppCompatActivity implements View.OnClickListener{

    TextView title;
    Button cameraButton;
    ImageView preview;
    LinearLayout previewLayout;
    ArrayList<String> visitedMonuments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument);

        title = findViewById(R.id.monumentName);
        cameraButton = findViewById(R.id.photoButton);
        preview = findViewById(R.id.previewImageView);
        previewLayout = findViewById(R.id.previewLayout);

        cameraButton.setOnClickListener(this);

        title.setText(getIntent().getStringExtra("monumentName"));

        visitedMonuments = getIntent().getStringArrayListExtra("monuments");
        Log.d("tag", visitedMonuments.get(0));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == cameraButton.getId()){
            //Log.d("Example:", "clicked button");
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            /*ViewGroup.LayoutParams param = previewLayout.getLayoutParams();
            param.width = imageBitmap.getWidth();
            param.height = imageBitmap.getHeight();
            previewLayout.setLayoutParams(param);*/
            preview.setImageBitmap(imageBitmap);

            //Check image against landmarks
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                    .getVisionCloudLandmarkDetector();

            Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                            Log.d("location", "Successfully uploaded");
                            ArrayList<FirebaseVisionCloudLandmark> landmarks = new ArrayList<FirebaseVisionCloudLandmark>();

                            for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {

                                Rect bounds = landmark.getBoundingBox();
                                String landmarkName = landmark.getLandmark();
                                String entityId = landmark.getEntityId();
                                float confidence = landmark.getConfidence();

                                // Multiple locations are possible, e.g., the location of the depicted
                                // landmark and the location the picture was taken.
                                for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                                    double latitude = loc.getLatitude();
                                    double longitude = loc.getLongitude();
                                }

                                landmarks.add(landmark);


                                Log.d("Name", landmarkName);
                                Log.d("Confidence", Float.toString(confidence));
                            }

                            if(landmarks.size() > 0 && landmarks.get(0).getLandmark().equals(title.getText())){
                                gotItRight();
                            }
                            else {
                                gotItWrong();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("location", "Failure");
                        }
                    });
        }
    }

    private void gotItRight(){
        Toast.makeText(this, ("That does look like the " +title.getText()), Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                            HashMap<String, Object> data = new HashMap<>();
                            found = true;
                            visitedMonuments = getIntent().getExtras().getStringArrayList("monuments");
                            if(visitedMonuments == null){
                                visitedMonuments = new ArrayList<>();
                            }
                            boolean checkedIn = false;
                            for(String i : visitedMonuments){
                                if(i.equals(title.getText().toString())){
                                    checkedIn = true;
                                    break;
                                }
                            }
                            if(!checkedIn) {
                                visitedMonuments.add(title.getText().toString());
                                data.put("Currency", getIntent().getLongExtra("currency", 0) + 5);
                                data.put("Monuments", visitedMonuments);
                                userData.document(getIntent().getStringExtra("user")).set(data);
                            }
                            else {
                                alreadyCheckedIn();
                            }


                            break;
                        }
                    }
                    if(!found){
                        Map<String, Object> data = new HashMap<>();
                        data.put("Currency", 5);
                        visitedMonuments = new ArrayList<>();
                        visitedMonuments.add(title.getText().toString());
                        data.put("Monuments", visitedMonuments);
                        userData.document(getIntent().getStringExtra("user")).set(data);
                    }

                } else {
                    //Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });


    }

    private void alreadyCheckedIn(){
        Toast.makeText(this, ("You've already checked into the " +title.getText()), Toast.LENGTH_SHORT).show();
    }

    private void gotItWrong(){
        Toast.makeText(this, ("That doesn't look like the " +title.getText()), Toast.LENGTH_SHORT).show();
    }
}
