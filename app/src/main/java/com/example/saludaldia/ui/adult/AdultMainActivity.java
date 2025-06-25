package com.example.saludaldia.ui.adult;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.saludaldia.R;
import com.example.saludaldia.adapter.FragmentPageAdapter;
import com.example.saludaldia.ui.login.LoginActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.ui.treatment.NewTreatmentActivity;
import com.example.saludaldia.utils.FontScaleContextWrapper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Importaciones para Google Calendar API
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar; // Clase Calendar de la API de Google
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdultMainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private static final String TAG = "AdultMainActivity";
    private Calendar mCalendarService; // Objeto para interactuar con la API de Calendar
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String appCalendarId = null;
    private static final String APP_CALENDAR_NAME = "SaludAlDia Recordatorios"; // Nombre específico para el calendario de tu app

    // 1. Interfaz para comunicar a los Fragments cuando el servicio de Calendar esté listo
    public interface ListCalendarEventsListener {
        void onCalendarServiceReady(Calendar calendarService, String calendarId);
        void onCalendarServiceError(String message);
    }

    private ListCalendarEventsListener listCalendarEventsListener;

    // Método para que los Fragments puedan registrarse como listeners
    public void setListCalendarEventsListener(ListCalendarEventsListener listener) {
        this.listCalendarEventsListener = listener;
    }

    // Métodos para que los Fragments puedan obtener el servicio y el ID directamente si ya están listos
    public Calendar getCalendarService() {
        return mCalendarService;
    }

    public String getAppCalendarId() {
        return appCalendarId;
    }


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
        setContentView(R.layout.activity_adult_main);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.adult_main_activity_title));
            getSupportActionBar().setLogo(R.drawable.logo);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        FragmentPageAdapter pagerAdapter = new FragmentPageAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.adult_main_activity_treatment_tab);
                            break;
                        case 1:
                            tab.setText(R.string.adult_main_activity_calendar_tab);
                            break;
                    }
                }).attach();

        // **Asegúrate de que este `GoogleSignInOptions` tenga el scope de Calendar**
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR)) // <--- ¡Esta línea es CRUCIAL!
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        FloatingActionButton fab = findViewById(R.id.fabAddTreatment);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AdultMainActivity.this, NewTreatmentActivity.class);
            startActivity(intent);
        });

        // **Lógica para inicializar el servicio de Google Calendar**
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // Verifica si hay una cuenta de Google logueada Y si tiene el scope de Calendar
        if (account != null && account.getGrantedScopes().contains(new Scope(CalendarScopes.CALENDAR))) {
            initializeCalendarService(account);
        } else {
            Log.d(TAG, "No Google account or Calendar scope not granted. Calendar functionality might be limited.");
            Toast.makeText(this, "Para usar el calendario, por favor inicia sesión con Google y acepta los permisos.", Toast.LENGTH_LONG).show();
        }
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

    // 2. Método para inicializar el servicio de Google Calendar
    private void initializeCalendarService(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccount(account.getAccount());

        mCalendarService = new Calendar.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("SaludAlDia") // Asegúrate de que este nombre sea descriptivo
                .build();

        Log.d(TAG, "Google Calendar service initialized successfully in AdultMainActivity.");
        mainHandler.post(() -> Toast.makeText(this, "Servicio de Calendar inicializado.", Toast.LENGTH_SHORT).show());

        // Una vez que el servicio está listo, verificar/crear el calendario de la app
        checkOrCreateAppCalendar();
    }

    // 3. Método para buscar o crear el calendario de la app
    private void checkOrCreateAppCalendar() {
        if (mCalendarService == null) {
            Log.e(TAG, "Calendar service not initialized. Cannot check or create app calendar.");
            if (listCalendarEventsListener != null) {
                listCalendarEventsListener.onCalendarServiceError("Servicio de calendario no inicializado.");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                String foundCalendarId = null;
                // Listar calendarios del usuario
                List<CalendarListEntry> calendarList = mCalendarService.calendarList().list().execute().getItems();
                if (calendarList != null) {
                    for (CalendarListEntry entry : calendarList) {
                        if (APP_CALENDAR_NAME.equals(entry.getSummary())) {
                            foundCalendarId = entry.getId();
                            break;
                        }
                    }
                }

                if (foundCalendarId != null) {
                    appCalendarId = foundCalendarId;
                    mainHandler.post(() -> {
                        Toast.makeText(AdultMainActivity.this, "Calendario '" + APP_CALENDAR_NAME + "' encontrado.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Found app calendar with ID: " + appCalendarId);
                        // 4. Notificar al listener (CalendarFragment) que el servicio está listo
                        if (listCalendarEventsListener != null) {
                            listCalendarEventsListener.onCalendarServiceReady(mCalendarService, appCalendarId);
                        }
                    });
                } else {
                    // Si no se encuentra, crear uno nuevo
                    com.google.api.services.calendar.model.Calendar newCalendar = new com.google.api.services.calendar.model.Calendar();
                    newCalendar.setSummary(APP_CALENDAR_NAME);
                    // Establecer la zona horaria del dispositivo para el nuevo calendario
                    newCalendar.setTimeZone(java.util.TimeZone.getDefault().getID());

                    com.google.api.services.calendar.model.Calendar createdCalendar = mCalendarService.calendars().insert(newCalendar).execute();
                    appCalendarId = createdCalendar.getId();

                    mainHandler.post(() -> {
                        Toast.makeText(AdultMainActivity.this, "Calendario '" + APP_CALENDAR_NAME + "' creado exitosamente.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Created new app calendar with ID: " + appCalendarId);
                        // 4. Notificar al listener (CalendarFragment) que el servicio está listo
                        if (listCalendarEventsListener != null) {
                            listCalendarEventsListener.onCalendarServiceReady(mCalendarService, appCalendarId);
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Error accessing Calendar API to check/create calendar: " + e.getMessage());
                final String errorMessage = "Error al gestionar el calendario de la app: " + e.getMessage();
                mainHandler.post(() -> {
                    Toast.makeText(AdultMainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    if (listCalendarEventsListener != null) {
                        listCalendarEventsListener.onCalendarServiceError(errorMessage);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Asegúrate de apagar el executorService cuando la Activity se destruye
        if (executorService != null) {
            executorService.shutdown();
        }
        // Limpia el listener si la Activity se destruye mientras el Fragment está activo
        if (listCalendarEventsListener != null) {
            listCalendarEventsListener = null;
        }
    }
}
