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

public class ReviewRequestAdapter
        extends RecyclerView.Adapter<ReviewRequestAdapter.ViewHolder> {

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

        Request request = list.get(position);

        holder.name.setText(
                request.getReceiverName()
        );

        holder.phone.setText(
                request.getReceiverPhone()
        );

        holder.blood.setText(
                request.getBloodGroup()
        );

        holder.status.setText(
                request.getStatus()
        );

        // Distance Calculation

        float[] results = new float[1];

        Location.distanceBetween(
                donorLat,
                donorLng,
                request.getReceiverLatitude(),
                request.getReceiverLongitude(),
                results
        );

        float distanceKm = results[0] / 1000;

        holder.distance.setText(
                String.format("%.1f km away", distanceKm)
        );

        // Accept Request

        holder.accept.setOnClickListener(v -> {

            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(request.getRequestId())
                    .update("status", "accepted")
                    .addOnSuccessListener(unused -> {

                        holder.status.setText("Accepted");

                        Toast.makeText(
                                context,
                                "Request Accepted",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        });

        // Reject Request

        holder.reject.setOnClickListener(v -> {

            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(request.getRequestId())
                    .update("status", "rejected")
                    .addOnSuccessListener(unused -> {

                        holder.status.setText("Rejected");

                        Toast.makeText(
                                context,
                                "Request Rejected",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView name,
                phone,
                blood,
                status,
                distance;

        Button accept,
                reject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(
                    R.id.tvReceiverName
            );

            phone = itemView.findViewById(
                    R.id.tvPhone
            );

            blood = itemView.findViewById(
                    R.id.tvBloodGroup
            );

            status = itemView.findViewById(
                    R.id.tvStatus
            );

            distance = itemView.findViewById(
                    R.id.tvDistance
            );

            accept = itemView.findViewById(
                    R.id.btnAccept
            );

            reject = itemView.findViewById(
                    R.id.btnReject
            );
        }
    }
}