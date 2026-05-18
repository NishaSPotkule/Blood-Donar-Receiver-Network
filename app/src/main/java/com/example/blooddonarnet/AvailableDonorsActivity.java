package com.example.blooddonarnet;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class AvailableDonorsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AvailableDonorAdapter adapter;
    ArrayList<AvailableDonarModel> list;

    double userLat, userLng;
    String blood;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    HashSet<String> requestedDonors = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_donors);

        recyclerView = findViewById(R.id.recyclerView);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        list = new ArrayList<>();

        adapter = new AvailableDonorAdapter(
                this,
                list
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);

        userLat = getIntent()
                .getDoubleExtra("lat", 0);

        userLng = getIntent()
                .getDoubleExtra("lng", 0);

        blood = getIntent()
                .getStringExtra("bloodGroup");

        loadDonors();
    }

    private void loadDonors() {

        if (blood == null || blood.isEmpty()) {

            Toast.makeText(
                    this,
                    "Blood group missing",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String receiverId =
                auth.getCurrentUser().getUid();

        firestore.collection("requests")
                .whereEqualTo(
                        "receiverId",
                        receiverId
                )
                .get()
                .addOnSuccessListener(requestQuery -> {

                    requestedDonors.clear();

                    for (QueryDocumentSnapshot doc :
                            requestQuery) {

                        String donorId =
                                doc.getString("donorId");

                        if (donorId != null) {

                            requestedDonors.add(donorId);
                        }
                    }

                    loadDonorList();
                });
    }

    private void loadDonorList() {

        List<String> compatibleGroups =
                getCompatibleDonors(blood);

        firestore.collection("users")
                .whereEqualTo("role", "donor")
                .whereEqualTo("availability", true)
                .get()

                .addOnSuccessListener(query -> {

                    list.clear();

                    for (QueryDocumentSnapshot doc :
                            query) {

                        // SKIP CURRENT USER

                        if (auth.getCurrentUser() != null
                                && doc.getId().equals(
                                auth.getCurrentUser().getUid()
                        )) {

                            continue;
                        }

                        AvailableDonarModel donor =
                                doc.toObject(
                                        AvailableDonarModel.class
                                );

                        if (donor == null)
                            continue;

                        donor.setUid(doc.getId());

                        // CHECK NEXT DONATION TIME

                        Long nextDonationTime =
                                doc.getLong(
                                        "nextDonationTime"
                                );

                        if (nextDonationTime != null) {

                            long currentTime =
                                    System.currentTimeMillis();

                            // donor not eligible yet

                            if (currentTime < nextDonationTime) {

                                continue;
                            }
                        }

                        // BLOOD GROUP MATCH

                        if (donor.getBloodGroup() == null
                                || !compatibleGroups.contains(
                                donor.getBloodGroup()
                        )) {

                            continue;
                        }

                        // LOCATION CHECK

                        if (donor.getLatitude() == 0
                                || donor.getLongitude() == 0) {

                            continue;
                        }

                        float[] results =
                                new float[1];

                        Location.distanceBetween(
                                userLat,
                                userLng,
                                donor.getLatitude(),
                                donor.getLongitude(),
                                results
                        );

                        float distanceKm =
                                results[0] / 1000;

                        donor.setDistance(distanceKm);

                        // REQUEST STATUS

                        donor.setRequested(
                                requestedDonors.contains(
                                        donor.getUid()
                                )
                        );

                        // MAX DISTANCE

                        if (distanceKm <= 20) {

                            list.add(donor);
                        }
                    }

                    // SORT BY DISTANCE

                    list.sort((d1, d2) ->
                            Float.compare(
                                    d1.getDistance(),
                                    d2.getDistance()
                            )
                    );

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No compatible donors nearby",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Error loading donors",
                            Toast.LENGTH_SHORT
                    ).show();

                    Log.e(
                            "ERROR",
                            e.toString()
                    );
                });
    }

    // BLOOD COMPATIBILITY

    private List<String> getCompatibleDonors(
            String receiverBlood
    ) {

        switch (receiverBlood) {

            case "A+":
                return Arrays.asList(
                        "A+",
                        "A-",
                        "O+",
                        "O-"
                );

            case "A-":
                return Arrays.asList(
                        "A-",
                        "O-"
                );

            case "B+":
                return Arrays.asList(
                        "B+",
                        "B-",
                        "O+",
                        "O-"
                );

            case "B-":
                return Arrays.asList(
                        "B-",
                        "O-"
                );

            case "AB+":
                return Arrays.asList(
                        "A+",
                        "A-",
                        "B+",
                        "B-",
                        "AB+",
                        "AB-",
                        "O+",
                        "O-"
                );

            case "AB-":
                return Arrays.asList(
                        "A-",
                        "B-",
                        "AB-",
                        "O-"
                );

            case "O+":
                return Arrays.asList(
                        "O+",
                        "O-"
                );

            case "O-":
                return Arrays.asList(
                        "O-"
                );

            default:
                return new ArrayList<>();
        }
    }
}