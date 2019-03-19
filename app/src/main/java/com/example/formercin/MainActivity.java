package com.example.formercin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Location location;
    TextView altitudeTextView;
    private static final int REQUEST_CHECK_SETTINGS = 99;
    private static final int REQUEST_UPDATE_SETTINGS = 56;


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        altitudeTextView = (TextView) findViewById(R.id.textView);




    }

    public void startLocation() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(MainActivity.this).checkLocationSettings(builder.build());


        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                // Location Enabled
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    permission_check();
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

            } catch (ApiException exception) {

                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Enable Location", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(MainActivity.this, "Enable Location", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(getIntent());
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        startLocation();

                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "Enable Location", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
            case REQUEST_UPDATE_SETTINGS:

                break;
        }
    }

    public void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1);
        locationRequest.setFastestInterval(1);
        locationRequest.setSmallestDisplacement(0);
    }

    private void buildLocationCallBack(final Context context) {

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);


                Location location = locationResult.getLastLocation();

                MainActivity.this.location = location;
                double altitude = location.getAltitude();


                altitudeTextView.setText(altitude + " meter");


                //                if (fusedLocationProviderClient != null && altitude != 0.0) {
                //                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                //                }

            }



        };

    }

    public void permission_check() {

        String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION};

        if (!hasPermissions(MainActivity.this, PERMISSIONS)) {

            int PERMISSION_ALL = 1;

            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                String[] PERMISSIONS = {
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                };
                if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                } else {

                    startLocation();
                }
                break;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        buildLocationRequest();
        buildLocationCallBack(MainActivity.this);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permission_check();
                return;
            }
        }
        startLocation();
    }
}

