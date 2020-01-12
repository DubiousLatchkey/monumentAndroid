package com.example.monument;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView bgapp, clover;
    LinearLayout textsplash, texthome, menus;
    Animation frombottom;
    TextView coins;
    long currency;
    Button button, button2, button3, button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currency = getIntent().getLongExtra("currency", 0);

        frombottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);

        bgapp = (ImageView) findViewById(R.id.bgapp);
        clover = (ImageView) findViewById(R.id.clover);
        textsplash = (LinearLayout) findViewById(R.id.textsplash);
        texthome = (LinearLayout) findViewById(R.id.texthome);
        menus = (LinearLayout) findViewById(R.id.menus);
        coins = (TextView) findViewById(R.id.currency);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);

        bgapp.animate().translationY(-1500).setDuration(800).setStartDelay(500);
        clover.animate().alpha(0).setDuration(800).setStartDelay(800);
        textsplash.animate().translationY(140).alpha(0).setDuration(800).setStartDelay(300);

        texthome.startAnimation(frombottom);
        menus.startAnimation(frombottom);
        coins.setText(Long.toString(currency));

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == button.getId()){
            if (currency - 5 < 0) {
                Toast toast = Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
            else {
                currency -= 5;
                Toast toast = Toast.makeText(this, "Purchase Successful!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
        }
        else if(v.getId() == button2.getId()) {
            if (currency - 10 < 0) {
                Toast toast = Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
            else {
                currency -= 10;
                Toast toast = Toast.makeText(this, "Purchase Successful!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
        }
        else if(v.getId() == button3.getId()) {
            if (currency - 15 < 0) {
                Toast toast = Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
            else {
                currency -= 15;
                Toast toast = Toast.makeText(this, "Purchase Successful!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
        }
        else if(v.getId() == button4.getId()) {
            if (currency - 20 < 0) {
                Toast toast = Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
            else {
                currency -= 20;
                Toast toast = Toast.makeText(this, "Purchase Successful!", Toast.LENGTH_SHORT);
                toast.getView().getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                toast.show();
            }
        }
        coins.setText(Long.toString(currency));
    }
}
