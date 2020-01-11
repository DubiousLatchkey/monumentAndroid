package com.example.monument.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.monument.R;

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
        }
    }
}
