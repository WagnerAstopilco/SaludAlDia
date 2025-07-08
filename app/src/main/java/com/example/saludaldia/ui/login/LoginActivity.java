package com.example.saludaldia.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.ui.register.RecoverPasswordActivity;
import com.example.saludaldia.utils.FontScaleContextWrapper;
import com.example.saludaldia.utils.LanguageManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.saludaldia.ui.adult.AdultMainActivity;
import com.example.saludaldia.ui.caregiver.CaregiverMainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.saludaldia.data.model.UserSettings;
import com.example.saludaldia.data.repository.UserSettingsRepository;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private ViewGroup layoutLogin;
    private SignInButton btnGoogleSignIn;
    private TextView txtRecoverPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount pendingGoogleAccount;
    private String pendingFirebaseUid;
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "Google sign in successful. ID: " + account.getId());
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Google sign in cancelled or failed with non-OK result.");
                    Toast.makeText(this, "Inicio de sesión con Google cancelado.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> consentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean consentAccepted = result.getData().getBooleanExtra(ConsentActivity.RESULT_CONSENT_ACCEPTED, false);
                    if (consentAccepted) {
                        Log.d(TAG, "Consentimiento aceptado en ConsentActivity. Procediendo con el registro.");
                        if (pendingFirebaseUid != null && pendingGoogleAccount != null) {
                            registerNewUserInFirestore(pendingFirebaseUid, pendingGoogleAccount);
                        } else {
                            Log.e(TAG, "Datos de usuario pendientes no encontrados después del consentimiento.");
                            Toast.makeText(this, "Error: No se pudo completar el registro. Intente de nuevo.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            mGoogleSignInClient.signOut();
                            layoutLogin.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.d(TAG, "Consentimiento rechazado en ConsentActivity. Cerrando sesión.");
                        mAuth.signOut();
                        mGoogleSignInClient.signOut();
                        Toast.makeText(this, "Debe aceptar la política de privacidad para usar la aplicación.", Toast.LENGTH_LONG).show();
                        layoutLogin.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "ConsentActivity finalizada sin resultado OK.");
                    mAuth.signOut();
                    mGoogleSignInClient.signOut();
                    Toast.makeText(this, "Operación cancelada. Debe aceptar la política de privacidad.", Toast.LENGTH_LONG).show();
                    layoutLogin.setVisibility(View.VISIBLE);
                }
                pendingFirebaseUid = null;
                pendingGoogleAccount = null;
            }
    );


    @Override
    protected void attachBaseContext(Context newBase) {
        Context languageContext = LanguageManager.setLocale(newBase);
        SharedPreferences prefs = languageContext.getSharedPreferences("settings_prefs", MODE_PRIVATE);
        String currentFontSize = prefs.getString("font_size", "medium");
        Context contextForFont = FontScaleContextWrapper.wrap(languageContext);
        super.attachBaseContext(contextForFont);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
            Log.d(TAG, "Firestore persistence enabled successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Firestore persistence: " + e.getMessage(), e);
        }
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutLogin.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        txtRecoverPassword = findViewById(R.id.txtRecoverPassword);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setOnClickListener(v -> {
            Log.d(TAG, "Initiating Google Sign-In flow.");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        txtRecoverPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RecoverPasswordActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Checking current user session.");
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "onStart: Firebase user detected: " + mAuth.getCurrentUser().getEmail());
            mGoogleSignInClient.silentSignIn()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount googleAccount = task.getResult();
                            Log.d(TAG, "onStart: Silent Google Sign-In successful. ID: " + googleAccount.getId());
                            if (googleAccount.getEmail() != null &&
                                    mAuth.getCurrentUser().getEmail() != null &&
                                    googleAccount.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                                Log.d(TAG, "onStart: Google account matches Firebase user.");
                                verificarRol(googleAccount);
                            } else {
                                Log.d(TAG, "onStart: Google account does NOT match Firebase user. Proceeding with Firebase credentials only (no Google account to pass).");
                                verificarRol(null);
                            }
                        } else {
                            Log.w(TAG, "onStart: Silent Google Sign-In failed or no active Google session.", task.getException());
                            verificarRol(null);
                        }
                    });
        } else {
            Log.d(TAG, "onStart: No Firebase user detected. Attempting silent Google Sign-In for new session.");
            mGoogleSignInClient.silentSignIn()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount account = task.getResult();
                            Log.d(TAG, "onStart: Silent Google Sign-In found previous account. Authenticating with Firebase.");
                            firebaseAuthWithGoogle(account);
                        } else {
                            Log.d(TAG, "onStart: No active Google or Firebase session. Showing login form.");
                            layoutLogin.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle: ID token: " + acct.getIdToken());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase auth with Google successful.");
                String uid = mAuth.getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users").document(uid).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "User already exists in Firestore.");
                                verificarRol(acct);
                            } else {
                                Log.d(TAG, "New Google user. Checking for consent.");
                                pendingFirebaseUid = uid;
                                pendingGoogleAccount = acct;

                                Intent intent = new Intent(LoginActivity.this, ConsentActivity.class);
                                consentLauncher.launch(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error accessing user data in Firestore: " + e.getMessage(), e);
                            Toast.makeText(this, "Error al acceder a datos del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            layoutLogin.setVisibility(View.VISIBLE);
                            mAuth.signOut();
                        });

            } else {
                Log.e(TAG, "Firebase auth with Google failed.", task.getException());
                Toast.makeText(this, "Fallo en la autenticación con Google.", Toast.LENGTH_SHORT).show();
                layoutLogin.setVisibility(View.VISIBLE);
            }
        });
    }

    private void registerNewUserInFirestore(@NonNull String uid, @NonNull GoogleSignInAccount acct) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String nombre = acct.getDisplayName() != null ? acct.getDisplayName() : "";
        String email = acct.getEmail() != null ? acct.getEmail() : "";

        Map<String, Object> nuevoUsuario = new HashMap<>();
        nuevoUsuario.put("userId", uid);
        nuevoUsuario.put("names", nombre);
        nuevoUsuario.put("email", email);
        nuevoUsuario.put("role", null);

        db.collection("users").document(uid)
                .set(nuevoUsuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore after consent. Creating history.");
                    HistoryRepository.createHistoryForUser(uid, new HistoryRepository.OnHistoryCreatedListener() {
                        @Override
                        public void onSuccess(History history) {
                            Log.d(TAG, "History created. Creating settings.");
                            UserSettings settings = new UserSettings(
                                    uid,
                                    "es",
                                    "medium",
                                    false, false, false, false, false, false
                            );
                            UserSettingsRepository settingsRepo = new UserSettingsRepository();
                            settingsRepo.saveUserSettings(
                                    settings,
                                    success -> {
                                        Log.d(TAG, "User settings saved. Redirecting to role selection.");
                                        verificarRol(acct);
                                    },
                                    error -> {
                                        Log.e(TAG, "Error saving user settings: " + error.getMessage(), error);
                                        Toast.makeText(LoginActivity.this, "Error al guardar configuración: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                        mGoogleSignInClient.signOut();
                                        layoutLogin.setVisibility(View.VISIBLE);
                                    }
                            );
                        }

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error creating history: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Error al crear historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            mGoogleSignInClient.signOut();
                            layoutLogin.setVisibility(View.VISIBLE);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error registering new user in Firestore: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al registrar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    mGoogleSignInClient.signOut();
                    layoutLogin.setVisibility(View.VISIBLE);
                });
    }

    private void verificarRol(@Nullable GoogleSignInAccount googleAccount) {
        String uid = mAuth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Intent intent;
                        if ("adulto mayor".equals(role)) {
                            intent = new Intent(this, AdultMainActivity.class);
                        } else if ("cuidador".equals(role)) {
                            intent = new Intent(this, CaregiverMainActivity.class);
                        } else {
                            intent = new Intent(this, SelectRoleActivity.class);
                        }

                        if (googleAccount != null) {
                            intent.putExtra("google_account_id", googleAccount.getId());
                            intent.putExtra("google_account_email", googleAccount.getEmail());
                            intent.putExtra("google_id_token", googleAccount.getIdToken());
                            Log.d(TAG, "Passing Google Account to next activity. ID: " + googleAccount.getId());
                        } else {
                            Log.d(TAG, "No Google Account to pass to next activity (user might be email/password or Google silent sign-in failed).");
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "User document does not exist in Firestore for UID: " + uid + " after supposed registration. Redirecting to SelectRoleActivity.");
                        Intent intent = new Intent(this, SelectRoleActivity.class);
                        if (googleAccount != null) {
                            intent.putExtra("google_account_id", googleAccount.getId());
                            intent.putExtra("google_account_email", googleAccount.getEmail());
                            intent.putExtra("google_id_token", googleAccount.getIdToken());
                        }
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verifying user role from Firestore: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al verificar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    layoutLogin.setVisibility(View.VISIBLE);
                });
    }
}