package com.example.blooddonarnet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DonorDashboardActivity extends AppCompatActivity {

    TextView welcomeText, statusText, livesSaved, nextDate;
    Switch availabilitySwitch;
    ImageView profileimg;
    CardView profile, logout;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_dashboard);


        welcomeText = findViewById(R.id.welcomeText);
        statusText = findViewById(R.id.statusText);
        livesSaved = findViewById(R.id.livesSaved);
        nextDate = findViewById(R.id.nextDate);
        availabilitySwitch = findViewById(R.id.availabilitySwitch);
        profileimg = findViewById(R.id.profileimg);
        profile = findViewById(R.id.profile);
        logout = findViewById(R.id.logout);



        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update("fcmToken", token);
                });

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        uid = auth.getCurrentUser().getUid();

        loadUserData();
        loadProfileImage();


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


        profile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );


        logout.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


    }

    private void loadProfileImage() {

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String imageUrl = document.getString("profileImage");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .into(profileimg);
                        } else {
                            profileimg.setImageResource(R.drawable.profile);
                        }
                    }
                });
    }


    private void loadUserData() {

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;


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
                    if (donations == null) donations = 0L;

                    long lives = donations * 3;
                    livesSaved.setText(String.valueOf(lives));


                    String next = doc.getString("nextDonation");

                    if (next != null) {
                        nextDate.setText(next);
                    } else {
                        nextDate.setText("Not donated yet");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
                );
    }
}