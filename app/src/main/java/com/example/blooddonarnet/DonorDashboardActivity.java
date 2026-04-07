package com.example.blooddonarnet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DonorDashboardActivity extends AppCompatActivity {

    TextView welcomeText, statusText, livesSaved, nextDate;
    Switch availabilitySwitch;
    Button editProfileBtn, logoutBtn;

    FirebaseAuth auth;
    FirebaseFirestore db;
    ImageView logout;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_dashboard);


        welcomeText = findViewById(R.id.welcomeText);
        statusText = findViewById(R.id.statusText);
        livesSaved = findViewById(R.id.livesSaved);
        nextDate = findViewById(R.id.nextDate);
        logout=findViewById(R.id.logout);

        availabilitySwitch = findViewById(R.id.availabilitySwitch);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        logout.setOnClickListener(v -> startActivity(new Intent(this,LoginActivity.class)));


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        uid = auth.getCurrentUser().getUid();

        loadUserData();


        availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            String status = isChecked ? "Available" : "Not Available";
            statusText.setText("Status: " + status);

            if (isChecked) {
                statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                statusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            db.collection("users")
                    .document(uid)
                    .update("availability", isChecked);
        });


        editProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, DonorRegistrationActivity.class));
        });


        logoutBtn.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }


    private void loadUserData() {

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {


                        String name = doc.getString("name");
                        welcomeText.setText("Hello, " + name + " 👋");


                        Boolean available = doc.getBoolean("availability");

                        if (available != null && available) {
                            availabilitySwitch.setChecked(true);
                            statusText.setText("Status: Available");
                            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            availabilitySwitch.setChecked(false);
                            statusText.setText("Status: Not Available");
                        }


                        Long donations = doc.getLong("donationsCount");

                        if (donations != null) {
                            long lives = donations * 3;
                            livesSaved.setText(String.valueOf(lives));
                        } else {
                            livesSaved.setText("0");
                        }


                        String lastDonation = doc.getString("lastDonation");

                        if (lastDonation != null) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date lastDate = sdf.parse(lastDonation);

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(lastDate);
                                cal.add(Calendar.DAY_OF_YEAR, 90);

                                String nextDateStr = sdf.format(cal.getTime());
                                nextDate.setText(nextDateStr);

                            } catch (Exception e) {
                                nextDate.setText("Error");
                            }
                        } else {
                            nextDate.setText("Not donated yet");
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                });
    }
}