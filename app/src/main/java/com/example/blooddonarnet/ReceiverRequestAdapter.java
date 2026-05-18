// ReceiverRequestAdapter.java

package com.example.blooddonarnet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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

        // =========================
        // STATUS UI
        // =========================

        if (request.isReceived()) {

            holder.status.setText(
                    "Status: BLOOD RECEIVED"
            );

            holder.status.setTextColor(
                    Color.parseColor("#2E7D32")
            );

        } else {

            holder.status.setText(
                    "Status: " + status.toUpperCase()
            );

            if (status.equals("accepted")) {

                holder.status.setTextColor(
                        Color.parseColor("#2E7D32")
                );

            } else if (status.equals("rejected")) {

                holder.status.setTextColor(
                        Color.parseColor("#C62828")
                );

            } else {

                holder.status.setTextColor(
                        Color.parseColor("#FF9800")
                );
            }
        }

        // =========================
        // SET DATA
        // =========================

        holder.blood.setText(
                request.getBloodGroup()
        );

        holder.donorName.setText(
                "Donor: " + request.getDonorName()
        );

        holder.donorPhone.setText(
                "Phone: " + request.getDonorPhone()
        );

        // =========================
        // BUTTON STATE
        // =========================

        if (request.isReceived()) {

            holder.receive.setText(
                    "Received"
            );

            holder.receive.setEnabled(false);

            holder.receive.setVisibility(
                    View.VISIBLE
            );

            holder.receive.setBackgroundColor(
                    Color.GRAY
            );

        } else if ("accepted".equals(status)) {

            holder.receive.setText(
                    "Mark Received"
            );

            holder.receive.setEnabled(true);

            holder.receive.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.receive.setVisibility(
                    View.GONE
            );
        }

        // =========================
        // RECEIVE BUTTON CLICK
        // =========================

        holder.receive.setOnClickListener(v -> {

            // Prevent double click

            if (request.isReceived()) {
                return;
            }

            holder.receive.setEnabled(false);

            HashMap<String, Object> updates =
                    new HashMap<>();

            updates.put("isReceived", true);

            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(request.getRequestId())
                    .update(updates)

                    .addOnSuccessListener(unused -> {

                        // UPDATE LOCAL OBJECT

                        request.setReceived(true);

                        // UPDATE DONOR STATS

                        updateDonorStats(
                                request.getDonorId()
                        );

                        // UPDATE UI IMMEDIATELY

                        holder.status.setText(
                                "Status: BLOOD RECEIVED"
                        );

                        holder.status.setTextColor(
                                Color.parseColor("#2E7D32")
                        );

                        holder.receive.setText(
                                "Received"
                        );

                        holder.receive.setEnabled(false);

                        holder.receive.setBackgroundColor(
                                Color.GRAY
                        );

                        Toast.makeText(
                                context,
                                "Blood marked as received",
                                Toast.LENGTH_SHORT
                        ).show();

                    })

                    .addOnFailureListener(e -> {

                        holder.receive.setEnabled(true);

                        Toast.makeText(
                                context,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        });
    }

    // =========================
    // UPDATE DONOR STATS
    // =========================

    private void updateDonorStats(String donorId) {

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        db.collection("users")
                .document(donorId)
                .get()

                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        return;
                    }

                    Long livesSaved =
                            doc.getLong("livesSaved");

                    if (livesSaved == null) {
                        livesSaved = 0L;
                    }

                    // ADD 3 LIVES

                    livesSaved = livesSaved + 3;

                    // NEXT DONATION AFTER 90 DAYS

                    long nextDonationMillis =
                            System.currentTimeMillis()
                                    + (90L * 24 * 60 * 60 * 1000);

                    SimpleDateFormat sdf =
                            new SimpleDateFormat(
                                    "dd MMM yyyy",
                                    Locale.getDefault()
                            );

                    String nextDonationDate =
                            sdf.format(
                                    new Date(nextDonationMillis)
                            );

                    HashMap<String, Object> donorUpdates =
                            new HashMap<>();

                    donorUpdates.put(
                            "livesSaved",
                            livesSaved
                    );

                    donorUpdates.put(
                            "nextDonation",
                            nextDonationDate
                    );

                    donorUpdates.put(
                            "nextDonationTime",
                            nextDonationMillis
                    );

                    // DONOR NOT AVAILABLE UNTIL NEXT DATE

                    donorUpdates.put(
                            "availability",
                            false
                    );

                    db.collection("users")
                            .document(donorId)
                            .update(donorUpdates)

                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        context,
                                        "Donor stats updated successfully",
                                        Toast.LENGTH_SHORT
                                ).show();
                            })

                            .addOnFailureListener(e -> {

                                Toast.makeText(
                                        context,
                                        e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                });
    }
    @Override
    public int getItemCount() {

        return list.size();
    }

    // =========================
    // VIEW HOLDER
    // =========================

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