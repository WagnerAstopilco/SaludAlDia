package com.example.saludaldia.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.model.UserSettings;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.data.repository.UserSettingsRepository;
import com.example.saludaldia.ui.adult.AdultMainActivity;
import com.example.saludaldia.ui.caregiver.CaregiverMainActivity;
import com.example.saludaldia.ui.login.SelectRoleActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtNames, edtEmail, edtPassword, edtConfirmPassword;
    private RadioGroup roleGroup;
    private Button btnRegister, btnBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtNames = findViewById(R.id.edtNames);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        roleGroup = findViewById(R.id.roleGroup);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnBackToLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String names = edtNames.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();
            int selectedRoleId = roleGroup.getCheckedRadioButtonId();

            if (names.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedRoleId == -1) {
                Toast.makeText(this, "Completa todos los campos y selecciona un rol", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRoleBtn = findViewById(selectedRoleId);
            String role = selectedRoleBtn.getText().toString().toLowerCase();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", uid);
                    userData.put("names", names);
                    userData.put("email", email);
                    userData.put("role", role);

                    UserSettingsRepository settingsRepository = new UserSettingsRepository();

                    db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener(unused -> {
                                HistoryRepository.createHistoryForUser(uid, new HistoryRepository.OnHistoryCreatedListener() {
                                    @Override
                                    public void onSuccess(History history) {
                                        // Una vez creado el historial, guarda las configuraciones
                                        UserSettings defaultSettings = new UserSettings(
                                                uid,
                                                "es",
                                                "medium",
                                                false,
                                                false,
                                                false,
                                                false,
                                                false,
                                                false
                                        );

                                        UserSettingsRepository settingsRepository = new UserSettingsRepository();
                                        settingsRepository.saveUserSettings(defaultSettings,
                                                success -> {
                                                    redirectToRoleActivity(role);
                                                },
                                                error -> Toast.makeText(RegisterActivity.this, "Error al guardar configuración: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                                    }

                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "Error al crear historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Error al guardar datos del usuario", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void redirectToRoleActivity(String role) {
        if ("adulto mayor".equals(role)) {
            startActivity(new Intent(this, AdultMainActivity.class));
        } else if ("cuidador".equals(role)) {
            startActivity(new Intent(this, CaregiverMainActivity.class));
        } else {
            startActivity(new Intent(this, SelectRoleActivity.class));
        }
        finish();
    }
}
