package com.example.blooddonarnet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    EditText name, email, password, confirmPassword;
    Button signupBtn;
    TextView loginText;
    RadioGroup roleGroup;
    RadioButton radioDonor, radioReceiver;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // UI
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmpassword);
        signupBtn = findViewById(R.id.signupbtn);
        loginText = findViewById(R.id.logintxt);
        roleGroup = findViewById(R.id.roleGroup);
        radioDonor = findViewById(R.id.radioDonor);
        radioReceiver = findViewById(R.id.radioReceiver);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signupBtn.setOnClickListener(v -> {

            String userName = name.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if (userName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!userPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Check role selected
            int selectedId = roleGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = (selectedId == R.id.radioDonor) ? "donor" : "receiver";

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String userId = auth.getCurrentUser().getUid();

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("name", userName);
                            userMap.put("email", userEmail);
                            userMap.put("uid", userId);
                            userMap.put("role", role); // ✅ SAVE ROLE HERE

                            db.collection("users")
                                    .document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show();

                                        // ✅ Direct navigation based on role
                                        if (role.equals("donor")) {
                                            startActivity(new Intent(this, DonorRegistrationActivity.class));
                                        } else {
                                            startActivity(new Intent(this, ReceiverHomeActivity.class));
                                        }

                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            Toast.makeText(this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}