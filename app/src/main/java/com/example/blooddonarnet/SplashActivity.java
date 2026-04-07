package com.example.blooddonarnet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

        public class SplashActivity extends AppCompatActivity {

            FirebaseAuth auth;
            FirebaseFirestore db;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_splash);

                auth = FirebaseAuth.getInstance();
                db = FirebaseFirestore.getInstance();

                new Handler().postDelayed(() -> {

                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {

                        String uid = user.getUid();

                        db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(doc -> {

                                    if (doc != null && doc.exists()) {

                                        String role = doc.getString("role");

                                        // ✅ SAFE CHECK
                                        if (role == null) {
                                            Toast.makeText(this, "User data missing", Toast.LENGTH_SHORT).show();
                                            auth.signOut(); // 🔥 prevent crash loop
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                            return;
                                        }

                                        if (role.equals("donor")) {
                                            startActivity(new Intent(this, DonorDashboardActivity.class));
                                        } else if (role.equals("receiver")) {
                                            startActivity(new Intent(this, ReceiverHomeActivity.class));
                                        } else {
                                            startActivity(new Intent(this, LoginActivity.class));
                                        }

                                    } else {
                                        // 🔥 USER NOT IN FIRESTORE
                                        auth.signOut();
                                        startActivity(new Intent(this, SignupActivity.class));
                                    }

                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                });

                    } else {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }

                }, 1500);
            }
        }
