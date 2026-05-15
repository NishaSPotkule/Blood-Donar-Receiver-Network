package com.example.blooddonarnet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

public class DonorDashboardActivity extends AppCompatActivity {

    TextView welcomeText, statusText, livesSaved, nextDate;
    Switch availabilitySwitch;

    CardView profile, logout,reviewRequest;

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
        reviewRequest=findViewById(R.id.reviewRequests);

        profile = findViewById(R.id.profile);
        logout = findViewById(R.id.logout);
        reviewRequest.setOnClickListener(v -> startActivity(new Intent(this,ReviewRequestsActivity.class)));

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {

            startActivity(
                    new Intent(this, LoginActivity.class)
            );

            finish();
            return;
        }

        uid = auth.getCurrentUser().getUid();

        // Save FCM Token
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {

                    db.collection("users")
                            .document(uid)
                            .update("fcmToken", token);

                    Log.d("FCM_TOKEN", token);
                });

        loadUserData();


        listenForBloodRequests();

        availabilitySwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    String status =
                            isChecked
                                    ? "Available"
                                    : "Not Available";

                    statusText.setText(
                            "Status: " + status
                    );

                    if (isChecked) {

                        statusText.setTextColor(
                                getResources().getColor(
                                        android.R.color.holo_red_dark
                                )
                        );

                    } else {

                        statusText.setTextColor(
                                getResources().getColor(
                                        android.R.color.darker_gray
                                )
                        );
                    }

                    db.collection("users")
                            .document(uid)
                            .update("availability", isChecked);
                });

        profile.setOnClickListener(v ->

                startActivity(
                        new Intent(
                                this,
                                ProfileActivity.class
                        )
                )
        );

        logout.setOnClickListener(v -> {

            auth.signOut();

            Intent intent =
                    new Intent(
                            this,
                            LoginActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();
        });
    }


    private void loadUserData() {

        db.collection("users")
                .document(uid)
                .get()

                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String name =
                            doc.getString("name");

                    welcomeText.setText(
                            "Hello, " + name + " 👋"
                    );

                    Boolean available =
                            doc.getBoolean("availability");

                    if (available != null && available) {

                        availabilitySwitch.setChecked(true);

                        statusText.setText(
                                "Status: Available"
                        );

                        statusText.setTextColor(
                                getResources().getColor(
                                        android.R.color.holo_red_dark
                                )
                        );

                    } else {

                        availabilitySwitch.setChecked(false);

                        statusText.setText(
                                "Status: Not Available"
                        );
                    }

                    Long donations =
                            doc.getLong("donationsCount");

                    if (donations == null) {
                        donations = 0L;
                    }

                    long lives = donations * 3;

                    livesSaved.setText(
                            String.valueOf(lives)
                    );

                    String next =
                            doc.getString("nextDonation");

                    if (next != null) {

                        nextDate.setText(next);

                    } else {

                        nextDate.setText(
                                "Not donated yet"
                        );
                    }
                })

                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Error loading data",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }


    private void listenForBloodRequests() {

        FirebaseFirestore.getInstance()
                .collection("requests")


                .whereEqualTo("donorId", uid)

                .whereEqualTo("status", "pending")

                .addSnapshotListener((value, error) -> {

                    if (error != null) {

                        Log.e(
                                "FIRESTORE",
                                error.getMessage()
                        );

                        return;
                    }

                    if (value == null) return;

                    for (DocumentChange dc :
                            value.getDocumentChanges()) {

                        if (dc.getType()
                                == DocumentChange.Type.ADDED) {

                            Request request =
                                    dc.getDocument()
                                            .toObject(Request.class);

                            Log.d(
                                    "REQUEST",
                                    "New Request Received"
                            );

                            showBloodNotification(
                                    request.getBloodGroup()
                            );
                        }
                    }
                });
    }

    private void showBloodNotification(String bloodGroup) {

        String channelId = "blood_alert";

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                NOTIFICATION_SERVICE
                        );

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "Blood Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Blood Donation Notifications"
            );

            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        channelId
                )

                        .setSmallIcon(R.drawable.blood)

                        .setContentTitle(
                                "Blood Request"
                        )

                        .setContentText(
                                bloodGroup
                                        + " blood donor needed nearby"
                        )

                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )

                        .setAutoCancel(true);

        manager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }
}