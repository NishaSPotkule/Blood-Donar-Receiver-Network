package com.example.blooddonarnet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView signupText;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginbtn);
        signupText = findViewById(R.id.signuptxt);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔥 AUTO LOGIN FIX
        if (auth.getCurrentUser() != null) {

            String uid = auth.getCurrentUser().getUid();

            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {

                        if (doc.exists()) {

                            String role = doc.getString("role");

                            if ("donor".equals(role)) {
                                startActivity(new Intent(this, DonorDashboardActivity.class));
                            } else {
                                startActivity(new Intent(this, ReceiverHomeActivity.class));
                            }

                            finish();
                        }
                    });
        }

        loginBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = auth.getCurrentUser().getUid();

                            db.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(doc -> {

                                        if (doc.exists()) {

                                            String role = doc.getString("role");

                                            if ("donor".equals(role)) {
                                                startActivity(new Intent(this, DonorDashboardActivity.class));
                                            } else {
                                                startActivity(new Intent(this, ReceiverHomeActivity.class));
                                            }

                                            finish();
                                        }
                                    });

                        } else {
                            Toast.makeText(this, "Login Failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        signupText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }
}