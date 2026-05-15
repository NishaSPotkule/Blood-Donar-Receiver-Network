package com.example.blooddonarnet;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReviewRequestsActivity
        extends AppCompatActivity {

    RecyclerView recyclerView;

    ReviewRequestAdapter adapter;

    ArrayList<Request> list;

    TextView requestCount;

    FirebaseFirestore db;
    FirebaseAuth auth;

    double donorLat;
    double donorLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_requests);

        recyclerView =
                findViewById(R.id.recyclerRequests);

        requestCount =
                findViewById(R.id.requestCount);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        list = new ArrayList<>();

        loadDonorLocation();
    }

    private void loadDonorLocation() {

        String donorId =
                auth.getCurrentUser().getUid();

        db.collection("users")
                .document(donorId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        Double lat =
                                document.getDouble("latitude");

                        Double lng =
                                document.getDouble("longitude");

                        if (lat != null)
                            donorLat = lat;

                        if (lng != null)
                            donorLng = lng;

                        // Setup Adapter

                        adapter =
                                new ReviewRequestAdapter(
                                        this,
                                        list,
                                        donorLat,
                                        donorLng
                                );

                        recyclerView.setLayoutManager(
                                new LinearLayoutManager(this)
                        );

                        recyclerView.setAdapter(adapter);

                        loadRequests();
                    }
                });
    }

    private void loadRequests() {

        String donorId =
                auth.getCurrentUser().getUid();

        db.collection("requests")
                .whereEqualTo("donorId", donorId)
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();

                    for (var doc :
                            query.getDocuments()) {

                        Request request =
                                doc.toObject(Request.class);

                        if (request == null)
                            continue;

                        request.setRequestId(
                                doc.getId()
                        );

                        Double receiverLat =
                                doc.getDouble(
                                        "receiverLatitude"
                                );

                        Double receiverLng =
                                doc.getDouble(
                                        "receiverLongitude"
                                );

                        if (receiverLat != null)
                            request.setReceiverLatitude(
                                    receiverLat
                            );

                        if (receiverLng != null)
                            request.setReceiverLongitude(
                                    receiverLng
                            );

                        list.add(request);
                    }

                    requestCount.setText(
                            list.size() + " Requests"
                    );

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No Requests Found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}