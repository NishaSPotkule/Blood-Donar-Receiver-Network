// ReceiverRequestAdapter.java

package com.example.blooddonarnet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

        String status = request.getStatus();

        if (status == null) {
            status = "pending";
        }

        holder.status.setText(
                "Status: " + status.toUpperCase()
        );

        holder.blood.setText(
                request.getBloodGroup()
        );

        holder.donorName.setText(
                "Donor: " + request.getDonorName()
        );

        holder.donorPhone.setText(
                "Phone: " + request.getDonorPhone()
        );

        if ("accepted".equals(request.getStatus())
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

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        db.collection("users")
                .document(donorId)
                .get()

                .addOnSuccessListener(doc -> {

                    if (!doc.exists())
                        return;

                    Long livesSaved =
                            doc.getLong("livesSaved");

                    if (livesSaved == null) {
                        livesSaved = 0L;
                    }

                    livesSaved++;

                    long nextDonationMillis =
                            System.currentTimeMillis()
                                    + (90L * 24 * 60 * 60 * 1000);

                    java.text.SimpleDateFormat sdf =
                            new java.text.SimpleDateFormat(
                                    "dd MMM yyyy",
                                    java.util.Locale.getDefault()
                            );

                    String nextDonationDate =
                            sdf.format(
                                    new java.util.Date(
                                            nextDonationMillis
                                    )
                            );

                    HashMap<String, Object> updates =
                            new HashMap<>();

                    updates.put(
                            "livesSaved",
                            livesSaved
                    );

                    updates.put(
                            "nextDonation",
                            nextDonationDate
                    );

                    updates.put(
                            "availability",
                            false
                    );

                    db.collection("users")
                            .document(donorId)
                            .update(updates)

                            .addOnSuccessListener(unused ->

                                    Toast.makeText(
                                            context,
                                            "Donor stats updated",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView status,
                blood,
                donorName,
                donorPhone;

        Button receive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            status =
                    itemView.findViewById(R.id.tvStatus);

            blood =
                    itemView.findViewById(R.id.tvBlood);

            donorName =
                    itemView.findViewById(R.id.tvDonorName);

            donorPhone =
                    itemView.findViewById(R.id.tvDonorPhone);

            receive =
                    itemView.findViewById(R.id.btnReceived);
        }
    }
}