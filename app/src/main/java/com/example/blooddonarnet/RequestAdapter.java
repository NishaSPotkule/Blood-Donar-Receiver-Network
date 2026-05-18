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

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    Context context;
    ArrayList<Request> list;

    public RequestAdapter(Context context, ArrayList<Request> list) {
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Request model = list.get(position);

        holder.nameText.setText(model.getReceiverName());
        holder.bloodText.setText("Blood: " + model.getBloodGroup());
        holder.phoneText.setText("Phone: " + model.getReceiverPhone());

        holder.acceptBtn.setOnClickListener(v -> updateStatus(model, "accepted"));
        holder.rejectBtn.setOnClickListener(v -> updateStatus(model, "rejected"));
    }

    private void updateStatus(Request model, String status) {

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(model.getRequestId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Request " + status, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameText, bloodText, phoneText;
        Button acceptBtn, rejectBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.tvReceiverName);
            bloodText = itemView.findViewById(R.id.tvBloodGroup);
            phoneText = itemView.findViewById(R.id.tvPhone);
            acceptBtn = itemView.findViewById(R.id.btnAccept);
            rejectBtn = itemView.findViewById(R.id.btnReject);
        }
    }
}
