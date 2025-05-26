
package com.example.saludaldia.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.ui.register.RecoverPasswordActivity;
import com.example.saludaldia.ui.register.RegisterActivity;
import com.example.saludaldia.ui.setting.SettingsActivity;
import com.example.saludaldia.utils.ThemeHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.saludaldia.ui.adult.AdultMainActivity;
import com.example.saludaldia.ui.caregiver.CaregiverMainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.saludaldia.data.model.UserSettings;
import com.example.saludaldia.data.repository.UserSettingsRepository;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ViewGroup layoutLogin;
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private SignInButton btnGoogleSignIn;
    private TextView txtRegister, txtRecoverPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("user_settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        String languageCode = prefs.getString("language", "es");  // idioma por defecto español

        // Cambiar idioma (debes tener el método setLocale en tu SettingsActivity o en un helper)
        Context context = SettingsActivity.setLocale(this, languageCode);
        // Aplica tema oscuro o claro
        ThemeHelper.applyTheme(darkMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutLogin.setVisibility(View.GONE); // Oculto al inicio

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        txtRegister = findViewById(R.id.txtRegister);
        txtRecoverPassword = findViewById(R.id.txtRecoverPassword);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            verificarRol();
                        } else {
                            Exception exception = task.getException();
                            String errorMsg = "Error al iniciar sesión";

                            if (exception != null) {
                                String message = exception.getMessage();

                                if (message != null) {
                                    if (message.contains("The password is invalid") || message.contains("There is no user record")) {
                                        errorMsg = "Credenciales incorrectas.";
                                    } else if (message.contains("This operation is not allowed") || message.contains("sign-in provider is disabled")) {
                                        errorMsg = "Este correo está registrado con Google. Usa el botón de Google para iniciar sesión.";
                                    }
                                }
                            }

                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        txtRecoverPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RecoverPasswordActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            // Usuario logueado con Firebase (email o google)
            verificarRol();
        } else {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                // Cuenta Google conectada, pero no en Firebase? Loguear con token
                firebaseAuthWithGoogle(account);
            } else {
                // No hay sesión activa, mostrar formulario login
                layoutLogin.setVisibility(View.VISIBLE);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users").document(uid).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Usuario ya existe, verificar rol
                                verificarRol();
                            } else {
                                // Usuario nuevo, registrar datos básicos
                                String nombre = acct.getDisplayName() != null ? acct.getDisplayName() : "";
                                String email = acct.getEmail() != null ? acct.getEmail() : "";

                                Map<String, Object> nuevoUsuario = new HashMap<>();
                                nuevoUsuario.put("userId", uid);
                                nuevoUsuario.put("names", nombre);
                                nuevoUsuario.put("email", email);
                                nuevoUsuario.put("role", null); // No se ha definido aún

                                db.collection("users").document(uid)
                                        .set(nuevoUsuario)
                                        .addOnSuccessListener(aVoid -> {
                                            // Crear historial
                                            HistoryRepository.createHistoryForUser(uid, new HistoryRepository.OnHistoryCreatedListener() {
                                                @Override
                                                public void onSuccess(History history) {
                                                    // Crear configuración después del historial
                                                    UserSettings settings = new UserSettings(
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
                                                    UserSettingsRepository settingsRepo = new UserSettingsRepository();
                                                    settingsRepo.saveUserSettings(
                                                            settings,
                                                            success -> {
                                                                // Redirigir a seleccionar rol
                                                                startActivity(new Intent(LoginActivity.this, SelectRoleActivity.class));
                                                                finish();
                                                            },
                                                            error -> {
                                                                Toast.makeText(LoginActivity.this, "Error al guardar configuración: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                layoutLogin.setVisibility(View.VISIBLE);
                                                            }
                                                    );
                                                }

                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(LoginActivity.this, "Error al crear historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    layoutLogin.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al registrar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            layoutLogin.setVisibility(View.VISIBLE);
                                        });

                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al acceder a datos del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            layoutLogin.setVisibility(View.VISIBLE);
                        });

            } else {
                Toast.makeText(this, "Fallo en la autenticación con Google.", Toast.LENGTH_SHORT).show();
                layoutLogin.setVisibility(View.VISIBLE);
            }
        });
    }


    private void verificarRol() {
        String uid = mAuth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role"); // Asegúrate de usar el nombre correcto del campo
                        if ("adulto mayor".equals(role)) {
                            startActivity(new Intent(this, AdultMainActivity.class));
                        } else if ("cuidador".equals(role)) {
                            startActivity(new Intent(this, CaregiverMainActivity.class));
                        } else {
                            // Si el documento existe pero no tiene rol
                            startActivity(new Intent(this, SelectRoleActivity.class));
                        }
                    } else {
                        // El documento no existe, redirigir a seleccionar rol
                        startActivity(new Intent(this, SelectRoleActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al verificar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    layoutLogin.setVisibility(View.VISIBLE);
                });
    }
}