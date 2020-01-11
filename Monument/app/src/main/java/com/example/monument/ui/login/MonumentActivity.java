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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monument.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MonumentActivity extends AppCompatActivity implements View.OnClickListener{

    TextView title;
    Button cameraButton;
    ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument);

        title = findViewById(R.id.monumentName);
        cameraButton = findViewById(R.id.photoButton);
        preview = findViewById(R.id.previewImageView);

        cameraButton.setOnClickListener(this);

        title.setText(getIntent().getStringExtra("monumentName"));
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
            preview.setImageBitmap(imageBitmap);

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
        Toast.makeText(this, ("That does look like the" +title.getText()), Toast.LENGTH_SHORT).show();
    }
    private void gotItWrong(){
        Toast.makeText(this, ("That doesn't look like the" +title.getText()), Toast.LENGTH_SHORT).show();
    }
}
