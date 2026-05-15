package com.example.blooddonarnet;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReceiverRequestsActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    ReceiverRequestAdapter adapter;

    ArrayList<Request> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_requests);

        recyclerView = findViewById(R.id.recyclerReceiverRequests);

        list = new ArrayList<>();

        adapter = new ReceiverRequestAdapter(this, list);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {

        String receiverId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        FirebaseFirestore.getInstance()
                .collection("requests")
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();

                    for (var doc : query.getDocuments()) {

                        Request request =
                                doc.toObject(Request.class);

                        if (request == null)
                            continue;

                        request.setRequestId(doc.getId());

                        list.add(request);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}