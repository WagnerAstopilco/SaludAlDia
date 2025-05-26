package com.example.saludaldia.ui.caregiver;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class CaregiverProfileActivity extends AppCompatActivity {

    private TextView txtNames, txtLastNames, txtEmail, txtPhone, txtAge, txtWeight, txtAllergies, txtRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.caregiver_profile);

        txtNames = findViewById(R.id.txtNames);
        txtLastNames = findViewById(R.id.txtLastNames);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtAge = findViewById(R.id.txtAge);
        txtWeight = findViewById(R.id.txtWeight);
        txtAllergies = findViewById(R.id.txtAllergies);
        txtRole = findViewById(R.id.txtRole);

        loadUserProfile();
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    txtNames.setText(user.getNames());
                    txtLastNames.setText(user.getLastNames());
                    txtEmail.setText(user.getEmail());
                    txtPhone.setText(user.getPhoneNumber());
                    txtAge.setText(String.valueOf(user.getAge()));
                    txtWeight.setText(user.getWeight() + " kg");
                    txtAllergies.setText(user.getAllergies() != null ? String.join(", ", user.getAllergies()) : "Ninguna");
                    txtRole.setText(user.getRole());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Manejo de error
            }
        });
    }
}
