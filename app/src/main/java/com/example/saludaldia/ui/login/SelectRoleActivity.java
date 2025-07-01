package com.example.saludaldia.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.ui.adult.AdultMainActivity;
import com.example.saludaldia.ui.caregiver.CaregiverMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SelectRoleActivity extends AppCompatActivity {

    private Button btnRolAdulto, btnRolCuidador;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_role);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnRolAdulto = findViewById(R.id.btnRolAdulto);
        btnRolCuidador = findViewById(R.id.btnRolCuidador);

        btnRolAdulto.setOnClickListener(v -> asignarRol("adulto mayor"));
        btnRolCuidador.setOnClickListener(v -> asignarRol("cuidador"));
    }

    private void asignarRol(String rol) {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .update("role", rol)
                .addOnSuccessListener(unused -> {
                    if (rol.equals("adulto mayor")) {
                        startActivity(new Intent(this, AdultMainActivity.class));
                    } else {
                        startActivity(new Intent(this, CaregiverMainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al asignar el rol: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
