package com.example.buttondemo;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity implements LocationListener {

    final static String TAG = "InfoActivity";

    final static int LOCATION_REQUEST_CODE = 1;

    private LocationManager locationManager;
    private Geocoder geocoder;

    private LinearLayout listLayout;
    private TextView manuText;
    private TextView modelText;
    private TextView locationText;
    private Button locationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        listLayout = findViewById(R.id.server_list_layout);

        manuText = findViewById(R.id.server_ip_server);
        manuText.setText(Build.MANUFACTURER);

        modelText = findViewById(R.id.server_time_server);
        modelText.setText(Build.MODEL);

        locationText = findViewById(R.id.server_your_name);
        locationButton = findViewById(R.id.location_button);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            listLayout.setVisibility(INVISIBLE);
            new AlertDialog.Builder(this)
                    .setTitle("Permissions Needed")
                    .setMessage("Grant location permissions in order to receive location information.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            ActivityCompat.requestPermissions(InfoActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                        }
                    })
                    .create()
                    .show();
        } else {
            locationButton.setVisibility(INVISIBLE);
            listLayout.setAnimation(AnimationUtils.loadAnimation(InfoActivity.this, R.anim.fade_in));
            setupLocation();
        }

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(InfoActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (listLayout.getVisibility() == INVISIBLE) {
                    listLayout.setVisibility(VISIBLE);
                    listLayout.setAnimation(AnimationUtils.loadAnimation(InfoActivity.this, R.anim.fade_in));
                }

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationText.setText("Needs location permissions");
                    locationText.setTextColor(getResources().getColor(R.color.error));
                    return;
                }

                locationText.setTextColor(getResources().getColor(R.color.white));
                locationButton.setVisibility(INVISIBLE);
                setupLocation();
        }
    }

    private void setupLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        locationText.setText("Fetching city...");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            // Coordinates in Hong Kong (where getLocality returns null)
            // List<Address> addresses = geocoder.getFromLocation(22.438781, 114.168037, 1);
            Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
            String city = address.getLocality();
            if (city == null) city = address.getSubLocality();
            if (city == null) city = address.getAdminArea();
            if (city == null) city = address.getSubAdminArea();
            if (city == null) city = "Could not fetch city";
            locationText.setText(city);
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}