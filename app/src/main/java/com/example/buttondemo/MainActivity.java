package com.example.buttondemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";

    private LinearLayout listLayout;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listLayout = findViewById(R.id.main_list_layout);
        listLayout.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in));

        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMapsActivity1();
            }
        });

        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { launchInfoActivity(); }
        });

        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { launchServerActivity(); }
        });

        button4 = findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { launchBoggleActivity(); }
        });
    }

    private void launchMapsActivity1() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void launchInfoActivity() {
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        startActivity(intent);
    }

    private void launchServerActivity() {
        Intent intent = new Intent(MainActivity.this, ServerActivity.class);
        startActivity(intent);
    }

    private void launchBoggleActivity() {
        Intent intent = new Intent(MainActivity.this, BoggleActivity.class);
        startActivity(intent);
    }
}