//
//package com.example.saludaldia.ui.login;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.saludaldia.R;
//import com.example.saludaldia.data.model.History;
//import com.example.saludaldia.data.repository.HistoryRepository;
//import com.example.saludaldia.ui.register.RecoverPasswordActivity;
//import com.example.saludaldia.ui.register.RegisterActivity;
//import com.example.saludaldia.ui.setting.SettingsActivity;
//import com.example.saludaldia.utils.FontScaleContextWrapper;
//import com.example.saludaldia.utils.ThemeHelper;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.SignInButton;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.common.api.Scope;
//import com.google.android.gms.tasks.Task;
//import com.google.api.services.calendar.CalendarScopes;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.GoogleAuthProvider;
//import com.example.saludaldia.ui.adult.AdultMainActivity;
//import com.example.saludaldia.ui.caregiver.CaregiverMainActivity;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.example.saludaldia.data.model.UserSettings;
//import com.example.saludaldia.data.repository.UserSettingsRepository;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private ViewGroup layoutLogin;
//    private EditText edtEmail, edtPassword;
//    private Button btnLogin;
//    private SignInButton btnGoogleSignIn;
//    private TextView txtRegister, txtRecoverPassword;
//    private FirebaseAuth mAuth;
//    private GoogleSignInClient mGoogleSignInClient;
//
//    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == RESULT_OK) {
//                    Intent data = result.getData();
//                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                    try {
//                        GoogleSignInAccount account = task.getResult(ApiException.class);
//                        firebaseAuthWithGoogle(account);
//                    } catch (ApiException e) {
//                        Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//    );
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        layoutLogin = findViewById(R.id.layoutLogin);
//        layoutLogin.setVisibility(View.GONE); // Oculto al inicio
//
//        mAuth = FirebaseAuth.getInstance();
//
//        edtEmail = findViewById(R.id.edtEmail);
//        edtPassword = findViewById(R.id.edtPassword);
//        btnLogin = findViewById(R.id.btnLogin);
//        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
//        txtRegister = findViewById(R.id.txtRegister);
//        txtRecoverPassword = findViewById(R.id.txtRecoverPassword);
//
//        btnLogin.setOnClickListener(v -> {
//            String email = edtEmail.getText().toString().trim();
//            String password = edtPassword.getText().toString().trim();
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            mAuth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            verificarRol();
//                        } else {
//                            Exception exception = task.getException();
//                            String errorMsg = "Error al iniciar sesión";
//
//                            if (exception != null) {
//                                String message = exception.getMessage();
//
//                                if (message != null) {
//                                    if (message.contains("The password is invalid") || message.contains("There is no user record")) {
//                                        errorMsg = "Credenciales incorrectas.";
//                                    } else if (message.contains("This operation is not allowed") || message.contains("sign-in provider is disabled")) {
//                                        errorMsg = "Este correo está registrado con Google. Usa el botón de Google para iniciar sesión.";
//                                    }
//                                }
//                            }
//
//                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
//                        }
//                    });
//        });
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .requestScopes(new Scope(CalendarScopes.CALENDAR))
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        btnGoogleSignIn.setOnClickListener(v -> {
//            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//            googleSignInLauncher.launch(signInIntent);
//        });
//
//        txtRegister.setOnClickListener(v -> {
//            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
//        });
//
//        txtRecoverPassword.setOnClickListener(v -> {
//            startActivity(new Intent(LoginActivity.this, RecoverPasswordActivity.class));
//        });
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (mAuth.getCurrentUser() != null) {
//            // Usuario logueado con Firebase (email o google)
//            verificarRol();
//        } else {
//            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//            if (account != null) {
//                // Cuenta Google conectada, pero no en Firebase? Loguear con token
//                firebaseAuthWithGoogle(account);
//            } else {
//                // No hay sesión activa, mostrar formulario login
//                layoutLogin.setVisibility(View.VISIBLE);
//            }
//        }
//    }
//
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
//            if (task.isSuccessful()) {
//                String uid = mAuth.getCurrentUser().getUid();
//                FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//                db.collection("users").document(uid).get()
//                        .addOnSuccessListener(documentSnapshot -> {
//                            if (documentSnapshot.exists()) {
//                                // Usuario ya existe, verificar rol
//                                verificarRol();
//                            } else {
//                                // Usuario nuevo, registrar datos básicos
//                                String nombre = acct.getDisplayName() != null ? acct.getDisplayName() : "";
//                                String email = acct.getEmail() != null ? acct.getEmail() : "";
//
//                                Map<String, Object> nuevoUsuario = new HashMap<>();
//                                nuevoUsuario.put("userId", uid);
//                                nuevoUsuario.put("names", nombre);
//                                nuevoUsuario.put("email", email);
//                                nuevoUsuario.put("role", null); // No se ha definido aún
//
//                                db.collection("users").document(uid)
//                                        .set(nuevoUsuario)
//                                        .addOnSuccessListener(aVoid -> {
//                                            // Crear historial
//                                            HistoryRepository.createHistoryForUser(uid, new HistoryRepository.OnHistoryCreatedListener() {
//                                                @Override
//                                                public void onSuccess(History history) {
//                                                    // Crear configuración después del historial
//                                                    UserSettings settings = new UserSettings(
//                                                            uid,
//                                                            "es",
//                                                            "medium",
//                                                            false,
//                                                            false,
//                                                            false,
//                                                            false,
//                                                            false,
//                                                            false
//                                                    );
//                                                    UserSettingsRepository settingsRepo = new UserSettingsRepository();
//                                                    settingsRepo.saveUserSettings(
//                                                            settings,
//                                                            success -> {
//                                                                // Redirigir a seleccionar rol
//                                                                startActivity(new Intent(LoginActivity.this, SelectRoleActivity.class));
//                                                                finish();
//                                                            },
//                                                            error -> {
//                                                                Toast.makeText(LoginActivity.this, "Error al guardar configuración: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                                                                layoutLogin.setVisibility(View.VISIBLE);
//                                                            }
//                                                    );
//                                                }
//
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Toast.makeText(LoginActivity.this, "Error al crear historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                                    layoutLogin.setVisibility(View.VISIBLE);
//                                                }
//                                            });
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            Toast.makeText(this, "Error al registrar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                            layoutLogin.setVisibility(View.VISIBLE);
//                                        });
//
//                            }
//                        })
//                        .addOnFailureListener(e -> {
//                            Toast.makeText(this, "Error al acceder a datos del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            layoutLogin.setVisibility(View.VISIBLE);
//                        });
//
//            } else {
//                Toast.makeText(this, "Fallo en la autenticación con Google.", Toast.LENGTH_SHORT).show();
//                layoutLogin.setVisibility(View.VISIBLE);
//            }
//        });
//    }
//
//
//    private void verificarRol() {
//        String uid = mAuth.getCurrentUser().getUid();
//
//        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
//
//        FirebaseFirestore.getInstance().collection("users")
//                .document(uid)
//                .get()
////                .addOnSuccessListener(documentSnapshot -> {
////                    if (documentSnapshot.exists()) {
////                        String role = documentSnapshot.getString("role"); // Asegúrate de usar el nombre correcto del campo
////                        if ("adulto mayor".equals(role)) {
////                            startActivity(new Intent(this, AdultMainActivity.class));
////                        } else if ("cuidador".equals(role)) {
////                            startActivity(new Intent(this, CaregiverMainActivity.class));
////                        } else {
////                            // Si el documento existe pero no tiene rol
////                            startActivity(new Intent(this, SelectRoleActivity.class));
////                        }
////                    } else {
////                        // El documento no existe, redirigir a seleccionar rol
////                        startActivity(new Intent(this, SelectRoleActivity.class));
////                    }
////                    finish();
////                })
////                .addOnFailureListener(e -> {
////                    Toast.makeText(this, "Error al verificar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////                    layoutLogin.setVisibility(View.VISIBLE);
////                });
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String role = documentSnapshot.getString("role");
//                        Intent intent;
//                        if ("adulto mayor".equals(role)) {
//                            intent = new Intent(this, AdultMainActivity.class);
//                        } else if ("cuidador".equals(role)) {
//                            intent = new Intent(this, CaregiverMainActivity.class);
//                        } else {
//                            intent = new Intent(this, SelectRoleActivity.class);
//                        }
//
//                        // **** IMPORTANTE: PASAR LA INFORMACIÓN DE LA CUENTA DE GOOGLE ****
//                        if (googleAccount != null) {
//                            intent.putExtra("google_account_id", googleAccount.getId());
//                            intent.putExtra("google_account_email", googleAccount.getEmail());
//
//                        }
//                        startActivity(intent);
//                        finish();
//                    } else {
//
//                        Intent intent = new Intent(this, SelectRoleActivity.class);
//                        if (googleAccount != null) {
//                            intent.putExtra("google_account_id", googleAccount.getId());
//                            intent.putExtra("google_account_email", googleAccount.getEmail());
//                        }
//                        startActivity(intent);
//                        finish();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Error al verificar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    layoutLogin.setVisibility(View.VISIBLE);
//                });
//    }
//}
package com.example.saludaldia.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Agregado para depuración
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Agregado para indicar que puede ser nulo
import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.ui.register.RecoverPasswordActivity;
import com.example.saludaldia.ui.register.RegisterActivity;
import com.example.saludaldia.ui.setting.SettingsActivity; // Asegúrate de que esto se sigue usando si es necesario
import com.example.saludaldia.utils.FontScaleContextWrapper; // Asegúrate de que esto se sigue usando si es necesario
import com.example.saludaldia.utils.LanguageManager;
import com.example.saludaldia.utils.ThemeHelper; // Asegúrate de que esto se sigue usando si es necesario
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

import java.util.Collections; // Cambiado de Arrays.asList() a Collections.singletonList() para un solo scope
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // Para logs de depuración

    private ViewGroup layoutLogin;
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private SignInButton btnGoogleSignIn;
    private TextView txtRegister, txtRecoverPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Launcher para el inicio de sesión con Google
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
    @Override
    protected void attachBaseContext(Context newBase) {
        // PASO 1: Aplica el idioma guardado usando LanguageManager.
        // LanguageManager.setLocale() leerá el idioma de SharedPreferences
        // y retornará un Context con el Locale actualizado.
        Context languageContext = LanguageManager.setLocale(newBase);

        // PASO 2: Luego, aplica el escalado de fuente sobre el contexto que ya tiene el idioma.
        // Esto asegura que ambos ajustes (idioma y fuente) se apliquen de forma encadenada.
        SharedPreferences prefs = languageContext.getSharedPreferences("settings_prefs", MODE_PRIVATE);
        // Asegúrate de que "settings_prefs" sea el mismo nombre que usas para guardar la fuente.
        String currentFontSize = prefs.getString("font_size", "medium");
        Context contextForFont = FontScaleContextWrapper.wrap(languageContext);

        // PASO 3: Llama al método super para que el contexto modificado se use para la actividad.
        super.attachBaseContext(contextForFont);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutLogin.setVisibility(View.GONE); // Oculto al inicio, se mostrará si no hay sesión

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        txtRegister = findViewById(R.id.txtRegister);
        txtRecoverPassword = findViewById(R.id.txtRecoverPassword);

        // Configuración de Google Sign-In
        // Es crucial incluir requestIdToken para la autenticación con Firebase y para la persistencia.
        // Y requestScopes con CalendarScopes.CALENDAR para el acceso total al calendario.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de que este ID sea correcto
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR)) // Scope amplio para Google Calendar
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- Listeners para los botones ---
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
                            Log.d(TAG, "Email sign in successful.");
                            verificarRol(null); // No hay GoogleSignInAccount asociado a este login
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
                                    } else if (message.contains("network error")) { // Añadido: error de red
                                        errorMsg = "Problema de red. Verifica tu conexión.";
                                    }
                                }
                            }
                            Log.e(TAG, "Email sign in failed: " + errorMsg, exception);
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            Log.d(TAG, "Initiating Google Sign-In flow.");
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
        Log.d(TAG, "onStart: Checking current user session.");

        // 1. Verificar si hay un usuario de Firebase autenticado
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "onStart: Firebase user detected: " + mAuth.getCurrentUser().getEmail());

            // 2. Intentar obtener la cuenta de Google Sign-In si el usuario es de Google
            // Intentamos silentSignIn() para asegurarnos de que la cuenta de Google esté activa
            mGoogleSignInClient.silentSignIn()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount googleAccount = task.getResult();
                            Log.d(TAG, "onStart: Silent Google Sign-In successful. ID: " + googleAccount.getId());

                            // Verificar que la cuenta de Google coincide con el usuario actual de Firebase
                            if (googleAccount.getEmail() != null &&
                                    mAuth.getCurrentUser().getEmail() != null &&
                                    googleAccount.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                                Log.d(TAG, "onStart: Google account matches Firebase user.");
                                verificarRol(googleAccount); // Pasar la cuenta de Google
                            } else {
                                Log.d(TAG, "onStart: Google account does NOT match Firebase user. Proceeding with Firebase credentials only.");
                                verificarRol(null); // No hay cuenta de Google relevante para este Firebase user
                            }
                        } else {
                            // Falló el inicio de sesión silencioso de Google
                            Log.w(TAG, "onStart: Silent Google Sign-In failed.", task.getException());
                            // Esto es normal si el usuario inició sesión con email/password, o si la sesión de Google expiró.
                            // Continúa verificando rol solo con credenciales de Firebase.
                            verificarRol(null); // No hay GoogleSignInAccount activo
                        }
                    });
        } else {
            // No hay usuario logueado en Firebase.
            Log.d(TAG, "onStart: No Firebase user detected. Attempting silent Google Sign-In for new session.");
            // Intentar un inicio de sesión silencioso con Google para ver si hay una sesión pendiente.
            mGoogleSignInClient.silentSignIn()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount account = task.getResult();
                            Log.d(TAG, "onStart: Silent Google Sign-In found previous account. Authenticating with Firebase.");
                            // Sesión de Google encontrada, autenticar con Firebase
                            firebaseAuthWithGoogle(account);
                        } else {
                            // No hay sesión activa de Firebase ni de Google. Mostrar formulario de login.
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
                                // Usuario ya existe, verificar rol
                                verificarRol(acct); // Pasar la cuenta de Google
                            } else {
                                Log.d(TAG, "New Google user. Registering in Firestore.");
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
                                            Log.d(TAG, "User data saved to Firestore. Creating history.");
                                            // Crear historial
                                            HistoryRepository.createHistoryForUser(uid, new HistoryRepository.OnHistoryCreatedListener() {
                                                @Override
                                                public void onSuccess(History history) {
                                                    Log.d(TAG, "History created. Creating settings.");
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
                                                                Log.d(TAG, "User settings saved. Redirecting to role selection.");
                                                                // Redirigir a seleccionar rol
                                                                verificarRol(acct); // Pasar la cuenta de Google
                                                            },
                                                            error -> {
                                                                Log.e(TAG, "Error saving user settings: " + error.getMessage(), error);
                                                                Toast.makeText(LoginActivity.this, "Error al guardar configuración: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                layoutLogin.setVisibility(View.VISIBLE);
                                                            }
                                                    );
                                                }

                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Error creating history: " + e.getMessage(), e);
                                                    Toast.makeText(LoginActivity.this, "Error al crear historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    layoutLogin.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error registering new user in Firestore: " + e.getMessage(), e);
                                            Toast.makeText(this, "Error al registrar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            layoutLogin.setVisibility(View.VISIBLE);
                                        });

                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error accessing user data in Firestore: " + e.getMessage(), e);
                            Toast.makeText(this, "Error al acceder a datos del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            layoutLogin.setVisibility(View.VISIBLE);
                        });

            } else {
                Log.e(TAG, "Firebase auth with Google failed.", task.getException());
                Toast.makeText(this, "Fallo en la autenticación con Google.", Toast.LENGTH_SHORT).show();
                layoutLogin.setVisibility(View.VISIBLE);
            }
        });
    }

    // Método verificarRol modificado para aceptar un GoogleSignInAccount
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

                        // **** IMPORTANTE: PASAR LA INFORMACIÓN DE LA CUENTA DE GOOGLE ****
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
                        // Documento de usuario no existe, redirigir a seleccionar rol
                        // Esto debería ocurrir principalmente para usuarios recién registrados via Google
                        // o si el documento fue borrado manualmente.
                        Log.w(TAG, "User document does not exist in Firestore for UID: " + uid + ". Redirecting to SelectRoleActivity.");
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