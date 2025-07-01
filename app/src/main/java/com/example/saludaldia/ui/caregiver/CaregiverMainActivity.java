package com.example.saludaldia.ui.caregiver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.LinkedUsersAdapter;
import com.example.saludaldia.data.model.User;
import com.example.saludaldia.ui.login.LoginActivity;
import com.example.saludaldia.ui.toolbar.CaregiverToolbar;
import com.example.saludaldia.utils.FontScaleContextWrapper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.ArrayList;
import java.util.List;

public class CaregiverMainActivity extends AppCompatActivity {

    private static final String TAG = "CaregiverMainActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100; // Define un código para tu solicitud
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FloatingActionButton fabScanQr;
    private RecyclerView rvLinkedUsers;
    private TextView tvNoLinkedUsers;
    private LinkedUsersAdapter linkedUsersAdapter;
    private List<User> linkedUsersList;
    private final ActivityResultLauncher<ScanOptions> qrScannerLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) {
            Toast.makeText(CaregiverMainActivity.this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
        } else {
            String scannedUserId = result.getContents();
            addLinkedUser(scannedUserId);
        }
    });

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
        setContentView(R.layout.activity_caregiver_main);
        CaregiverToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.adult_main_activity_title));
            getSupportActionBar().setLogo(R.drawable.logo);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        fabScanQr = findViewById(R.id.fabScanQr);
        rvLinkedUsers = findViewById(R.id.rvLinkedUsers);
        tvNoLinkedUsers = findViewById(R.id.tvNoLinkedUsers);

        linkedUsersList = new ArrayList<>();
        linkedUsersAdapter = new LinkedUsersAdapter(linkedUsersList);
        rvLinkedUsers.setLayoutManager(new LinearLayoutManager(this));
        rvLinkedUsers.setAdapter(linkedUsersAdapter);

        fabScanQr.setOnClickListener(v -> checkCameraPermissionAndStartScanner());
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            loadLinkedUsers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem profileItem = menu.add(Menu.NONE, R.id.menu_profile, Menu.NONE, "Ver perfil");
        profileItem.setIcon(R.drawable.profile);
        profileItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem logoutItem = menu.add(Menu.NONE, R.id.logout, Menu.NONE, "Cerrar Sesión");
        logoutItem.setIcon(R.drawable.logout);
        logoutItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_profile) {
            startActivity(new Intent(this, CaregiverProfileActivity.class));
            return true;
        } else if (itemId == R.id.logout) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.logout_alert_title))
                    .setMessage(getString(R.string.logout_alert_message))
                    .setPositiveButton(getString(R.string.logout_alert_button_confirm), (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(this, getString(R.string.logout_alert_logout), Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .setNegativeButton(getString(R.string.logout_alert_button_cancel), null)
                    .show();

            return true;
        } else {
            return false;
        }
    }

    private void checkCameraPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScanner();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }
    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt(getString(R.string.caregiver_main_activity_promt_scanner));
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);
        qrScannerLauncher.launch(options);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchScanner();
            } else {
                Toast.makeText(this, getString(R.string.caregiver_main_activity_permission_scan_denied), Toast.LENGTH_LONG).show();
            }
        }
    }
    private void addLinkedUser(String newLinkedUserId) {
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.caregiver_main_activity_login_required), Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUserModel = documentSnapshot.toObject(User.class);
                        if (currentUserModel != null) {
                            List<String> currentLinkedUserIds = currentUserModel.getLinkedUserIds();

                            if (currentLinkedUserIds.contains(newLinkedUserId)) {
                                Toast.makeText(this, getString(R.string.caregiver_main_activity_user_linked), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (currentUserId.equals(newLinkedUserId)) {
                                Toast.makeText(this, getString(R.string.caregiver_main_activity_not_yourself), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            currentLinkedUserIds.add(newLinkedUserId);

                            db.collection("users").document(currentUserId)
                                    .update("linkedUserIds", currentLinkedUserIds)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Usuario vinculado correctamente.", Toast.LENGTH_SHORT).show();
                                        loadLinkedUsers();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al vincular usuario.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error: Tu perfil de usuario no se encontró.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar tu perfil.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadLinkedUsers() {
        if (currentUser == null) {
            tvNoLinkedUsers.setVisibility(View.VISIBLE);
            rvLinkedUsers.setVisibility(View.GONE);
            return;
        }

        String currentUserId = currentUser.getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUserModel = documentSnapshot.toObject(User.class);
                        if (currentUserModel != null && currentUserModel.getLinkedUserIds() != null && !currentUserModel.getLinkedUserIds().isEmpty()) {
                            List<String> linkedIds = currentUserModel.getLinkedUserIds();
                            fetchUserDetails(linkedIds);
                        } else {
                            linkedUsersList.clear();
                            linkedUsersAdapter.setLinkedUsersList(linkedUsersList);
                            tvNoLinkedUsers.setVisibility(View.VISIBLE);
                            rvLinkedUsers.setVisibility(View.GONE);
                        }
                    } else {
                        linkedUsersList.clear();
                        linkedUsersAdapter.setLinkedUsersList(linkedUsersList);
                        tvNoLinkedUsers.setVisibility(View.VISIBLE);
                        rvLinkedUsers.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar usuarios vinculados.", Toast.LENGTH_SHORT).show();
                    tvNoLinkedUsers.setVisibility(View.VISIBLE);
                    rvLinkedUsers.setVisibility(View.GONE);
                });
    }

    private void fetchUserDetails(List<String> userIds) {
        if (userIds.isEmpty()) {
            linkedUsersList.clear();
            linkedUsersAdapter.setLinkedUsersList(linkedUsersList);
            tvNoLinkedUsers.setVisibility(View.VISIBLE);
            rvLinkedUsers.setVisibility(View.GONE);
            return;
        }

        List<User> fetchedUsers = new ArrayList<>();
        db.collection("users")
                .whereIn("userId", userIds) // ¡Asegúrate de que este campo sea 'userId' en tu Firestore!
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            fetchedUsers.add(user);
                        } else {
                            Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                        }
                    }
                    linkedUsersList.clear();
                    linkedUsersList.addAll(fetchedUsers);

                    linkedUsersAdapter.setLinkedUsersList(linkedUsersList);

                    if (linkedUsersList.isEmpty()) {
                        tvNoLinkedUsers.setVisibility(View.VISIBLE);
                        rvLinkedUsers.setVisibility(View.GONE);
                    } else {
                        tvNoLinkedUsers.setVisibility(View.GONE);
                        rvLinkedUsers.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar detalles de usuarios.", Toast.LENGTH_SHORT).show();
                    tvNoLinkedUsers.setVisibility(View.VISIBLE);
                    rvLinkedUsers.setVisibility(View.GONE);
                });
    }
}
