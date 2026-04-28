package com.example.blooddonarnet;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DonorRequestActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Request> list;
    RequestAdapter adapter;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_request);

        recyclerView = findViewById(R.id.requestRecycler);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        list = new ArrayList<>();
        adapter = new RequestAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {

        String donorId = auth.getCurrentUser().getUid();

        firestore.collection("requests")
                .whereEqualTo("donorId", donorId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {

                    list.clear();

                    for (var doc : value.getDocuments()) {
                        Request model = doc.toObject(Request.class);
                        model.setRequestId(doc.getId());
                        list.add(model);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
