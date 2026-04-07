package com.example.blooddonarnet;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ReceiverHomeActivity extends AppCompatActivity {

    Button btnAPlus, btnAMinus, btnBPlus, btnBMinus,
            btnOPlus, btnOMinus, btnABPlus, btnABMinus;

    TextView username;

    EditText etLocation;

    CardView cardAvailableDonors;

    String selectedBlood = "A+";

    FusedLocationProviderClient locationClient;
    double userLat = 0.0, userLng = 0.0;

    private static final int LOCATION_REQUEST = 100;

    FirebaseFirestore firestore;
    FirebaseAuth auth;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_home);


        btnAPlus = findViewById(R.id.btnAPlus);
        btnAMinus = findViewById(R.id.btnAMinus);
        btnBPlus = findViewById(R.id.btnBPlus);
        btnBMinus = findViewById(R.id.btnBMinus);
        btnOPlus = findViewById(R.id.btnOPlus);
        btnOMinus = findViewById(R.id.btnOMinus);
        btnABPlus = findViewById(R.id.btnABPlus);
        btnABMinus = findViewById(R.id.btnABMinus);

        username = findViewById(R.id.tvUsername);

        etLocation = findViewById(R.id.etLocation);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ImageView menu = findViewById(R.id.menu);
        NavigationView navigationView = findViewById(R.id.navigationView);

// Open drawer
        menu.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

// Handle clicks
        navigationView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_profile) {

                Intent intent = new Intent(ReceiverHomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }

            if (item.getItemId() == R.id.nav_logout) {

                new AlertDialog.Builder(ReceiverHomeActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            FirebaseAuth.getInstance().signOut();

                            Toast.makeText(ReceiverHomeActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ReceiverHomeActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }




            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        cardAvailableDonors = findViewById(R.id.cardAvailableDonors);


        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        prefs = getSharedPreferences("app_data", MODE_PRIVATE);
        selectedBlood = prefs.getString("bloodGroup", "A+");


        highlightSavedBlood();


        locationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        loadUsername();


       


        btnAPlus.setOnClickListener(v -> selectBlood("A+", btnAPlus));
        btnAMinus.setOnClickListener(v -> selectBlood("A-", btnAMinus));
        btnBPlus.setOnClickListener(v -> selectBlood("B+", btnBPlus));
        btnBMinus.setOnClickListener(v -> selectBlood("B-", btnBMinus));
        btnOPlus.setOnClickListener(v -> selectBlood("O+", btnOPlus));
        btnOMinus.setOnClickListener(v -> selectBlood("O-", btnOMinus));
        btnABPlus.setOnClickListener(v -> selectBlood("AB+", btnABPlus));
        btnABMinus.setOnClickListener(v -> selectBlood("AB-", btnABMinus));

        // Available donors click
        cardAvailableDonors.setOnClickListener(v -> {

            if (userLat == 0.0 || userLng == 0.0) {
                Toast.makeText(this, "Location not ready", Toast.LENGTH_SHORT).show();
                return;
            }

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = auth.getCurrentUser().getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("bloodGroup", selectedBlood);
            data.put("lat", userLat);
            data.put("lng", userLng);

            // Save inside same user document (no overwrite)
            firestore.collection("users")
                    .document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {

                        Intent intent = new Intent(this, AvailableDonorsActivity.class);
                        intent.putExtra("bloodGroup", selectedBlood);
                        intent.putExtra("lat", userLat);
                        intent.putExtra("lng", userLng);
                        startActivity(intent);

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void selectBlood(String blood, Button selectedBtn) {
        selectedBlood = blood;


        prefs.edit().putString("bloodGroup", blood).apply();

        resetButtonColors();
        selectedBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void highlightSavedBlood() {
        resetButtonColors();

        switch (selectedBlood) {
            case "A+": btnAPlus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case "A-": btnAMinus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case "B+": btnBPlus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case "B-": btnBMinus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case "O+": btnOPlus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case "O-": btnOMinus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case "AB+": btnABPlus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case "AB-": btnABMinus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); break;
        }
    }

    private void resetButtonColors() {
        int gray = getResources().getColor(android.R.color.darker_gray);

        btnAPlus.setBackgroundColor(gray);
        btnAMinus.setBackgroundColor(gray);
        btnBPlus.setBackgroundColor(gray);
        btnBMinus.setBackgroundColor(gray);
        btnOPlus.setBackgroundColor(gray);
        btnOMinus.setBackgroundColor(gray);
        btnABPlus.setBackgroundColor(gray);
        btnABMinus.setBackgroundColor(gray);
    }

    private void getUserLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            return;
        }

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();


                etLocation.setText("Lat: " + userLat + ", Lng: " + userLng);

            } else {
                Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsername() {

        if (auth == null || auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");

                        if (name != null) {
                            username.setText(name);
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getUserLocation();
        }
    }
}