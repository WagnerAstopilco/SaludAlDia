package com.example.saludaldia.ui.adult;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.ui.history.HistoryActivity;
import com.example.saludaldia.ui.notification.NotificationsActivity;
import com.example.saludaldia.ui.setting.SettingsActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.ui.treatment.TreatmentsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.widget.Button;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdultProfileActivity extends AppCompatActivity {

    private EditText edtNames, edtLastNames, edtEmail, edtPhone, edtAge, edtWeight, edtAllergies;
    private Button btnTreatments, btnHistory, btnSave, btnCancel;

    private ImageButton btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adult_profile);
        AdultToolbar.setup(this);
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setTitle("Perfil de usuario");
        }

        edtNames = findViewById(R.id.edtNames);
        edtLastNames = findViewById(R.id.edtLastNames);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAge = findViewById(R.id.edtAge);
        edtWeight = findViewById(R.id.edtWeight);
        edtAllergies = findViewById(R.id.edtAllergies);
        btnTreatments = findViewById(R.id.btnTreatments);
        btnHistory = findViewById(R.id.btnHistory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnEdit = findViewById(R.id.btnEdit);

        btnTreatments.setOnClickListener(v -> {
            startActivity(new Intent(this, TreatmentsActivity.class));
        });

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });
        btnEdit.setOnClickListener(v -> enableEditing(true));
        btnSave.setOnClickListener(v -> updateUserProfile());
        btnCancel.setOnClickListener(V -> enableEditing(false));
        loadUserProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.adult_toolbar_menu, menu);
        MenuItem profileItem = menu.add(Menu.NONE, R.id.settings, Menu.NONE, "Configuraciones");
        profileItem.setIcon(R.drawable.settings);
        profileItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem logoutItem = menu.add(Menu.NONE, R.id.notifications, Menu.NONE, "Notificaciones");
        logoutItem.setIcon(R.drawable.notifications);
        logoutItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.notifications) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Usamos get para obtener campos individuales y manejamos valores nulos
                        String names = documentSnapshot.getString("names");
                        String lastNames = documentSnapshot.getString("lastNames");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNumber");
                        Long age = documentSnapshot.getLong("age");
                        Double weight = documentSnapshot.getDouble("weight");
                        List<String> allergies = (List<String>) documentSnapshot.get("allergies");
                        String role = documentSnapshot.getString("role");

                        edtNames.setText(names != null ? names : "");
                        edtLastNames.setText(lastNames != null ? lastNames : "");
                        edtEmail.setText(email != null ? email : "");
                        edtPhone.setText(phone != null ? phone : "");
                        edtAge.setText(age != null ? String.valueOf(age) : "");
                        edtWeight.setText(weight != null ? weight + " kg" : "");
                        edtAllergies.setText(allergies != null && !allergies.isEmpty() ? String.join(", ", allergies) : "Ninguna");
                    } else {
                        Toast.makeText(this, "Perfil no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void enableEditing(boolean enable) {
        edtNames.setEnabled(enable);
        edtLastNames.setEnabled(enable);
        edtPhone.setEnabled(enable);
        edtAge.setEnabled(enable);
        edtWeight.setEnabled(enable);
        edtAllergies.setEnabled(enable);
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void updateUserProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtener valores de los EditText
        String names = edtNames.getText().toString().trim();
        String lastNames = edtLastNames.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim().replace(" kg", ""); // quitar " kg" si está
        String allergiesStr = edtAllergies.getText().toString().trim();

        // Parsear edad y peso si es posible
        Integer age = null;
        Double weight = null;
        try {
            if (!ageStr.isEmpty()) {
                age = Integer.parseInt(ageStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Edad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (!weightStr.isEmpty()) {
                weight = Double.parseDouble(weightStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Peso inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir alergias a lista separada por coma
        List<String> allergies = List.of();
        if (!allergiesStr.isEmpty() && !allergiesStr.equalsIgnoreCase("Ninguna")) {
            allergies = List.of(allergiesStr.split("\\s*,\\s*")); // split por coma y espacio
        }

        // Crear mapa con los campos a actualizar
        Map<String, Object> updates = new HashMap<>();
        updates.put("names", names);
        updates.put("lastNames", lastNames);
        updates.put("phoneNumber", phone);
        if (age != null) updates.put("age", age);
        if (weight != null) updates.put("weight", weight);
        updates.put("allergies", allergies);

        // Actualizar en Firestore
        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
