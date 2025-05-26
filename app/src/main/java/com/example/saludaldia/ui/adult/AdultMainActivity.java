package com.example.saludaldia.ui.adult;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludaldia.R;
import com.example.saludaldia.adapter.TreatmentAdapter;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.TreatmentRepository;
import com.example.saludaldia.ui.login.LoginActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.ui.treatment.NewTreatmentActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.saludaldia.data.repository.MedicationRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdultMainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private TreatmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adult_main);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            // Establecer el título
            getSupportActionBar().setTitle(" Bienvenido");

            // Mostrar el logo en la izquierda del título
            getSupportActionBar().setLogo(R.drawable.logo); // Asegúrate que logo.png o .svg esté en res/drawable/
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // RecyclerView y adaptador
        RecyclerView recyclerView = findViewById(R.id.treatmentRecyclerView);
        Map<String, List<Medication>> medicationsMap = new HashMap<>();
        adapter = new TreatmentAdapter(new ArrayList<>(), medicationsMap, true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Botón para nuevo tratamiento
        FloatingActionButton fab = findViewById(R.id.fabAddTreatment);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AdultMainActivity.this, NewTreatmentActivity.class);
            startActivity(intent); // No usamos startActivityForResult ya
        });
    }

    // Siempre que la vista se vuelve visible, recarga la lista
    @Override
    protected void onResume() {
        super.onResume();
        loadTreatments();
    }

//    private void loadTreatments() {
//        TreatmentRepository.getActiveTreatmentsForCurrentUser(treatments -> {
//            adapter.updateList(treatments);
//        }, e -> {
//            Toast.makeText(this, "Error al cargar tratamientos", Toast.LENGTH_SHORT).show();
//        });
//    }
private void loadTreatments() {
    TreatmentRepository.getActiveTreatmentsForCurrentUser(treatments -> {
        Map<String, List<Medication>> medicationsMap = new HashMap<>();

        if (treatments.isEmpty()) {
            adapter.updateList(treatments, medicationsMap);
            return;
        }

        final int total = treatments.size();
        final int[] loadedCount = {0};

        for (Treatment treatment : treatments) {
            MedicationRepository.getMedicationsByTreatmentId(treatment.getTreatmentId(),
                    meds -> {
                        medicationsMap.put(treatment.getTreatmentId(), meds);
                        loadedCount[0]++;
                        if (loadedCount[0] == total) {
                            // Cuando se han cargado todos los medicamentos
                            adapter.updateList(treatments, medicationsMap);
                        }
                    },
                    e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == total) {
                            adapter.updateList(treatments, medicationsMap);
                        }
                        Log.e("loadTreatments", "Error al cargar medicamentos", e);
                    }
            );
        }

    }, e -> {
        Toast.makeText(this, "Error al cargar tratamientos", Toast.LENGTH_SHORT).show();
    });
}


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
            startActivity(new Intent(this, AdultProfileActivity.class));
            return true;
        } else if (itemId == R.id.logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        } else {
            return false;
        }
    }
}

