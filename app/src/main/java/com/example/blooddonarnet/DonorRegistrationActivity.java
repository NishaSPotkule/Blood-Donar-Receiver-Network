package com.example.blooddonarnet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class DonorRegistrationActivity extends AppCompatActivity {

    EditText nameEt, phoneEt;
    Spinner bloodSpinner;
    Button registerBtn;

    FirebaseAuth auth;
    FirebaseFirestore db;

    FusedLocationProviderClient fusedLocationClient;

    double latitude = 0.0, longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_registration);

        nameEt = findViewById(R.id.nameEt);
        phoneEt=findViewById(R.id.phoneEt);
        bloodSpinner = findViewById(R.id.bloodSpinner);
        registerBtn = findViewById(R.id.registerBtn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupSpinner();
        getCurrentLocation();

        registerBtn.setOnClickListener(v -> saveData());
    }


    private void setupSpinner() {
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                bloodGroups
        );

        bloodSpinner.setAdapter(adapter);
    }


    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    } else {
                        Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveData() {

        String name = nameEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();
        String bloodGroup = bloodSpinner.getSelectedItem().toString();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Location not available. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("phone", phone);
        userMap.put("bloodGroup", bloodGroup);
        userMap.put("role", "donor");
        userMap.put("latitude", latitude);
        userMap.put("longitude", longitude);

        db.collection("users")
                .document(uid)
                .update(userMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, DonorDashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {


                    db.collection("users")
                            .document(uid)
                            .set(userMap)
                            .addOnSuccessListener(unused -> {
                                startActivity(new Intent(this, DonorDashboardActivity.class));
                                finish();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}