package com.example.blooddonarnet;

import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class ReceiverRequestAdapter
        extends RecyclerView.Adapter<ReceiverRequestAdapter.ViewHolder> {

    Context context;
    ArrayList<Request> list;

    public ReceiverRequestAdapter(
            Context context,
            ArrayList<Request> list
    ) {

        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_receiver_request,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Request request = list.get(position);

        holder.status.setText(
                "Status: " + request.getStatus()
        );

        holder.blood.setText(
                request.getBloodGroup()
        );

        if (request.getStatus().equals("accepted")
                && !request.isReceived()) {

            holder.receive.setVisibility(View.VISIBLE);

        } else {

            holder.receive.setVisibility(View.GONE);
        }

        holder.receive.setOnClickListener(v -> {

            HashMap<String, Object> updates =
                    new HashMap<>();

            updates.put("isReceived", true);

            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(request.getRequestId())
                    .update(updates)
                    .addOnSuccessListener(unused -> {

                        updateDonorStats(
                                request.getDonorId()
                        );

                        Toast.makeText(
                                context,
                                "Marked as Received",
                                Toast.LENGTH_SHORT
                        ).show();

                        holder.receive.setVisibility(
                                View.GONE
                        );
                    });
        });
    }

    private void updateDonorStats(String donorId) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(donorId)
                .get()
                .addOnSuccessListener(doc -> {

                    Long saved =
                            doc.getLong("livesSaved");

                    if (saved == null)
                        saved = 0L;

                    saved++;

                    HashMap<String, Object> map =
                            new HashMap<>();

                    map.put("livesSaved", saved);

                    map.put(
                            "nextDonationTime",
                            System.currentTimeMillis()
                                    + (90L * 24 * 60 * 60 * 1000)
                    );

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(donorId)
                            .update(map);
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView status, blood;

        Button receive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            status =
                    itemView.findViewById(R.id.tvStatus);

            blood =
                    itemView.findViewById(R.id.tvBlood);

            receive =
                    itemView.findViewById(R.id.btnReceived);
        }
    }
}