package com.example.monument.ui.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.monument.R;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView currencyText;
    private Button backButton;
    EditText name;
    TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currencyText = findViewById(R.id.currencyText);
        backButton = findViewById(R.id.backButton);
        name = findViewById(R.id.nameText);
        email = findViewById(R.id.userText);



    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);

        email.setText(getIntent().getStringExtra("user"));
        backButton.setOnClickListener(this);
        Long currencyAmount = getIntent().getExtras().getLong("currency");
        currencyText.setText(currencyAmount.toString());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

}
