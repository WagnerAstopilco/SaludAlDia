package com.example.saludaldia.ui.register;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.saludaldia.R;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverPasswordActivity extends AppCompatActivity {

    private EditText edtEmailRecover;
    private Button btnRecover, btnBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        mAuth = FirebaseAuth.getInstance();

        edtEmailRecover = findViewById(R.id.edtEmailRecover);
        btnRecover = findViewById(R.id.btnRecover);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnRecover.setOnClickListener(v -> {
            String email = edtEmailRecover.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa un correo", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Enlace enviado a tu correo", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }
}