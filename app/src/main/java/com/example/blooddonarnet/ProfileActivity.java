package com.example.blooddonarnet;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvPhone, tvBlood, tvRole;
    ImageView profileImage;

    Uri imageUri;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvBlood = findViewById(R.id.tvBlood);
        tvRole = findViewById(R.id.tvRole);
        profileImage = findViewById(R.id.profileImage);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadUserData();

        profileImage.setOnClickListener(v -> openGallery());
    }


    ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    profileImage.setImageURI(uri);
                    uploadToCloudinary();
                }
            });

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }


    private void loadUserData() {

        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        tvName.setText(document.getString("name"));
                        tvEmail.setText(document.getString("email"));

                        String phone = document.getString("phone");
                        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not Available");

                        tvBlood.setText(document.getString("bloodGroup"));
                        tvRole.setText(document.getString("role"));

                        String imageUrl = document.getString("profileImage");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.profile)
                                    .into(profileImage);
                        }
                    }
                });
    }


    private void uploadToCloudinary() {

        if (imageUri == null) return;

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {

                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] bytes = getBytes(inputStream);

                String uploadUrl = "https://api.cloudinary.com/v1_1/dlnmdpbss/image/upload";

                OkHttpClient client = new OkHttpClient();

                RequestBody fileBody = RequestBody.create(bytes);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg", fileBody)
                        .addFormDataPart("upload_preset", "android_upload")
                        .build();

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                String responseData = response.body().string();

                JSONObject json = new JSONObject(responseData);
                String imageUrl = json.getString("secure_url");

                runOnUiThread(() -> {
                    db.collection("users")
                            .document(auth.getCurrentUser().getUid())
                            .update("profileImage", imageUrl);

                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }


    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }
}