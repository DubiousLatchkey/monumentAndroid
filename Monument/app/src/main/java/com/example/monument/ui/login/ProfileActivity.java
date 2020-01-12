package com.example.monument.ui.login;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView currencyText;
    private Button backButton;
    EditText name;
    TextView email;
    private TextView levelText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currencyText = findViewById(R.id.currencyText);
        backButton = findViewById(R.id.backButton);
        name = findViewById(R.id.nameText);
        email = findViewById(R.id.userText);
        levelText = findViewById(R.id.levelNumber);

        email.setText(getIntent().getStringExtra("user"));
        backButton.setOnClickListener(this);
        long currencyAmount = getIntent().getLongExtra("currency", 0);
        currencyText.setText(Long.toString(currencyAmount));
        name.setText(getIntent().getStringExtra("name"));



    }


    @Override
    public void onClick(View v) {
        if(v.getId() == backButton.getId()){
            Intent intent = new Intent(this, MapActivity.class);
            setName(name.getText().toString());
            intent.putExtra("user", email.getText().toString());
            startActivity(intent);
        }

    }

    private void setName(final String value){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference userData = db.collection("UserData");
        userData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Toast.makeText(this, document.getData(), Toast.LENGTH_SHORT).show();

                        if (document.getId().equals(getIntent().getStringExtra("user"))) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("Name", value);
                            updates.put("Currency", getIntent().getLongExtra("currency", 0));

                            db.collection("UserData").document(document.getId()).set(updates);

                            break;
                        }
                    }

                } else {
                    //Log.w(TAG, "Error getting documents.", task.getException());
                }
            }


        });

    }

}
