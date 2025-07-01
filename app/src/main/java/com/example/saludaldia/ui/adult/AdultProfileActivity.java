package com.example.saludaldia.ui.adult;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.ui.history.HistoryActivity;
import com.example.saludaldia.ui.notification.NotificationsActivity;
import com.example.saludaldia.ui.setting.SettingsActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.ui.treatment.TreatmentsActivity;
import com.example.saludaldia.utils.FontScaleContextWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.widget.Button;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdultProfileActivity extends AppCompatActivity { // Recomiendo cambiar a AdultMainActivity

    private EditText edtNames, edtLastNames, edtEmail, edtPhone, edtAge, edtWeight, edtAllergies;
    private Button btnTreatments, btnHistory, btnSave, btnCancel;
    private ImageButton btnEdit;
    private ImageView qrImageView;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings_prefs", MODE_PRIVATE);
        String currentFontSize = prefs.getString("font_size", "medium");

        Context contextForFont = FontScaleContextWrapper.wrap(newBase);

        super.attachBaseContext(contextForFont);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adult_profile);
        AdultToolbar.setup(this);
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setTitle(getString(R.string.adult_profile_activity_title));
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
        qrImageView = findViewById(R.id.qrImageView);

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

        generateAndDisplayQrCode();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
        String savedFontSize = prefs.getString("font_size", "medium");

        float currentFontScale = getResources().getConfiguration().fontScale;
        float expectedFontScale = 1.0f;

        switch (savedFontSize) {
            case "small":
                expectedFontScale = 0.85f;
                break;
            case "large":
                expectedFontScale = 1.3f;
                break;
            default: // medium
                expectedFontScale = 1.0f;
        }

        if (Math.abs(currentFontScale - expectedFontScale) > 0.001f) {
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem settingsItem = menu.add(Menu.NONE, R.id.settings, Menu.NONE, "Configuraciones");
        settingsItem.setIcon(R.drawable.settings);
        settingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem notificationsItem = menu.add(Menu.NONE, R.id.notifications, Menu.NONE, "Notificaciones");
        notificationsItem.setIcon(R.drawable.notifications);
        notificationsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String names = documentSnapshot.getString("names");
                        String lastNames = documentSnapshot.getString("lastNames");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNumber");
                        Long age = documentSnapshot.getLong("age");
                        Double weight = documentSnapshot.getDouble("weight");
                        List<String> allergies = (List<String>) documentSnapshot.get("allergies");
                        edtNames.setText(names != null ? names : "");
                        edtLastNames.setText(lastNames != null ? lastNames : "");
                        edtEmail.setText(email != null ? email : "");
                        edtPhone.setText(phone != null ? phone : "");
                        edtAge.setText(age != null ? String.valueOf(age) : "");
                        edtWeight.setText(weight != null ? String.valueOf(weight) : "");
                        edtAllergies.setText(allergies != null && !allergies.isEmpty() ? android.text.TextUtils.join(", ", allergies) : "");
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
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String names = edtNames.getText().toString().trim();
        String lastNames = edtLastNames.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();
        String allergiesStr = edtAllergies.getText().toString().trim();

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

        List<String> allergies = new ArrayList<>();
        if (!allergiesStr.isEmpty()) {
            String[] allergyArray = allergiesStr.split("\\s*,\\s*");
            for (String allergy : allergyArray) {
                if (!allergy.trim().isEmpty()) {
                    allergies.add(allergy.trim());
                }
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("names", names);
        updates.put("lastNames", lastNames);
        updates.put("phoneNumber", phone);
        updates.put("age", age);
        updates.put("weight", weight);
        updates.put("allergies", allergies);

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

    private void generateAndDisplayQrCode() {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(
                        userId,
                        BarcodeFormat.QR_CODE,
                        400,
                        400
                );
                qrImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Toast.makeText(this, "Error al generar el QR.", Toast.LENGTH_SHORT).show();
                qrImageView.setVisibility(View.GONE);
            }
        } else {
            qrImageView.setVisibility(View.GONE);
        }
    }
}
