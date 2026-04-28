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

import java.util.ArrayList;
import java.util.HashSet;

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
        adapter = new AvailableDonorAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        userLat = getIntent().getDoubleExtra("lat", 0);
        userLng = getIntent().getDoubleExtra("lng", 0);
        blood = getIntent().getStringExtra("bloodGroup");

        loadDonors();
    }

    private void loadDonors() {

        if (blood == null) {
            Toast.makeText(this, "Blood group missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String receiverId = auth.getCurrentUser().getUid();


        firestore.collection("requests")
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnSuccessListener(requestQuery -> {

                    requestedDonors.clear();

                    for (var doc : requestQuery.getDocuments()) {
                        String donorId = doc.getString("donorId");
                        if (donorId != null) {
                            requestedDonors.add(donorId);
                        }
                    }


                    loadDonorList();
                });
    }

    private void loadDonorList() {

        firestore.collection("users")
                .whereEqualTo("bloodGroup", blood)
                .whereEqualTo("role", "donor")
                .whereEqualTo("availability", true)
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();

                    for (var doc : query.getDocuments()) {

                        if (auth.getCurrentUser() != null &&
                                doc.getId().equals(auth.getCurrentUser().getUid())) {
                            continue;
                        }

                        AvailableDonarModel donor = doc.toObject(AvailableDonarModel.class);

                        if (donor == null) continue;

                        donor.setUid(doc.getId());

                        if (donor.getLatitude() == 0 || donor.getLongitude() == 0) {
                            continue;
                        }

                        float[] results = new float[1];

                        Location.distanceBetween(
                                userLat, userLng,
                                donor.getLatitude(), donor.getLongitude(),
                                results
                        );

                        float distanceKm = results[0] / 1000;

                        donor.setDistance(distanceKm);

                        if (requestedDonors.contains(donor.getUid())) {
                            donor.setRequested(true);
                        } else {
                            donor.setRequested(false);
                        }

                        if (distanceKm <= 20) {
                            list.add(donor);
                        }
                    }

                    list.sort((d1, d2) -> Float.compare(d1.getDistance(), d2.getDistance()));

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        Toast.makeText(this, "No donors nearby", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading donors", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", e.toString());
                });
    }
}