package com.example.blooddonarnet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class AvailableDonorAdapter extends RecyclerView.Adapter<AvailableDonorAdapter.ViewHolder> {

    Context context;
    ArrayList<AvailableDonarModel> list;

    public AvailableDonorAdapter(Context context, ArrayList<AvailableDonarModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_donor_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AvailableDonarModel donor = list.get(position);

        holder.name.setText(donor.getName());
        holder.blood.setText(donor.getBloodGroup());
        holder.distance.setText(String.format("%.1f km away", donor.getDistance()));

        if (donor.isRequested()) {
            holder.request.setText("Requested");
            holder.request.setEnabled(false);
        } else {
            holder.request.setText("Request");
            holder.request.setEnabled(true);
        }

        holder.request.setOnClickListener(v -> {

            String receiverId = FirebaseAuth.getInstance().getUid();

            if (receiverId == null) return;

            holder.request.setEnabled(false);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(receiverId)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot doc) -> {

                        HashMap<String, Object> map = new HashMap<>();

                        map.put("receiverId", receiverId);
                        map.put("donorId", donor.getUid());
                        map.put("bloodGroup", donor.getBloodGroup());
                        map.put("status", "pending");
                        map.put("timestamp", System.currentTimeMillis());

                        // receiver data
                        map.put("receiverName", doc.getString("name"));
                        map.put("receiverPhone", doc.getString("phone"));
                        map.put("receiverLatitude", doc.getDouble("latitude"));
                        map.put("receiverLongitude", doc.getDouble("longitude"));

                        FirebaseFirestore.getInstance()
                                .collection("requests")
                                .add(map)
                                .addOnSuccessListener(r -> {

                                    donor.setRequested(true);
                                    holder.request.setText("Requested");

                                    Toast.makeText(context,
                                            "Request Sent",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {

                                    holder.request.setEnabled(true);
                                    holder.request.setText("Request");

                                    Toast.makeText(context,
                                            e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    });
        });

        holder.call.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + donor.getPhone()));
            context.startActivity(intent);
        });

        holder.message.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + donor.getPhone()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, blood, distance;
        Button call, message, request;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvName);
            blood = itemView.findViewById(R.id.tvBlood);
            distance = itemView.findViewById(R.id.tvDistance);

            call = itemView.findViewById(R.id.btnCall);
            message = itemView.findViewById(R.id.btnMessage);
            request = itemView.findViewById(R.id.btnRequest);
        }
    }
}