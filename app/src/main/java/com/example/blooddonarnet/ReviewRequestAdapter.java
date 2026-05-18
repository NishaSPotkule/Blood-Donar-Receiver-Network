package com.example.blooddonarnet;

import android.content.Context;
import android.location.Location;
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

public class ReviewRequestAdapter extends RecyclerView.Adapter<ReviewRequestAdapter.ViewHolder> {

    Context context;
    ArrayList<Request> list;

    double donorLat;
    double donorLng;

    public ReviewRequestAdapter(
            Context context,
            ArrayList<Request> list,
            double donorLat,
            double donorLng
    ) {

        this.context = context;
        this.list = list;
        this.donorLat = donorLat;
        this.donorLng = donorLng;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_request,
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

        try {

            Request request = list.get(position);

            String name = request.getReceiverName();
            String phone = request.getReceiverPhone();
            String blood = request.getBloodGroup();
            String status = request.getStatus();

            if (name == null)
                name = "Unknown";

            if (phone == null)
                phone = "No Phone";

            if (blood == null)
                blood = "N/A";

            if (status == null)
                status = "pending";

            holder.name.setText(name);

            holder.phone.setText(phone);

            holder.blood.setText(blood);

            // STATUS

            if (request.isReceived()) {

                holder.status.setText(
                        "Status: BLOOD RECEIVED"
                );

                holder.status.setTextColor(
                        context.getResources().getColor(
                                android.R.color.holo_green_dark
                        )
                );

            } else {

                holder.status.setText(
                        "Status: " + status.toUpperCase()
                );
            }

            // DISTANCE

            try {

                float[] results = new float[1];

                Location.distanceBetween(
                        donorLat,
                        donorLng,
                        request.getReceiverLatitude(),
                        request.getReceiverLongitude(),
                        results
                );

                float distanceKm =
                        results[0] / 1000;

                holder.distance.setText(
                        String.format(
                                "%.1f km away",
                                distanceKm
                        )
                );

            } catch (Exception e) {

                holder.distance.setText(
                        "Distance unavailable"
                );
            }

            // BUTTON DISABLE CONDITIONS

            if (status.equals("accepted")
                    || status.equals("rejected")
                    || request.isReceived()) {

                holder.accept.setEnabled(false);
                holder.reject.setEnabled(false);

                holder.accept.setAlpha(0.5f);
                holder.reject.setAlpha(0.5f);

            } else {

                holder.accept.setEnabled(true);
                holder.reject.setEnabled(true);

                holder.accept.setAlpha(1f);
                holder.reject.setAlpha(1f);
            }

            // ACCEPT BUTTON

            holder.accept.setOnClickListener(v -> {

                FirebaseFirestore.getInstance()
                        .collection("requests")
                        .document(request.getRequestId())
                        .update("status", "accepted")

                        .addOnSuccessListener(unused -> {

                            request.setStatus("accepted");

                            holder.status.setText(
                                    "Status: ACCEPTED"
                            );

                            holder.accept.setEnabled(false);
                            holder.reject.setEnabled(false);

                            holder.accept.setAlpha(0.5f);
                            holder.reject.setAlpha(0.5f);

                            Toast.makeText(
                                    context,
                                    "Request Accepted",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
            });

            // REJECT BUTTON

            holder.reject.setOnClickListener(v -> {

                FirebaseFirestore.getInstance()
                        .collection("requests")
                        .document(request.getRequestId())
                        .update("status", "rejected")

                        .addOnSuccessListener(unused -> {

                            request.setStatus("rejected");

                            holder.status.setText(
                                    "Status: REJECTED"
                            );

                            holder.accept.setEnabled(false);
                            holder.reject.setEnabled(false);

                            holder.accept.setAlpha(0.5f);
                            holder.reject.setAlpha(0.5f);

                            Toast.makeText(
                                    context,
                                    "Request Rejected",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
            });

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    context,
                    "Adapter Error",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    @Override
    public int getItemCount() {

        return list.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView name;
        TextView phone;
        TextView blood;
        TextView status;
        TextView distance;

        Button accept;
        Button reject;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            name =
                    itemView.findViewById(
                            R.id.tvReceiverName
                    );

            phone =
                    itemView.findViewById(
                            R.id.tvPhone
                    );

            blood =
                    itemView.findViewById(
                            R.id.tvBloodGroup
                    );

            status =
                    itemView.findViewById(
                            R.id.tvStatus
                    );

            distance =
                    itemView.findViewById(
                            R.id.tvDistance
                    );

            accept =
                    itemView.findViewById(
                            R.id.btnAccept
                    );

            reject =
                    itemView.findViewById(
                            R.id.btnReject
                    );
        }
    }
}