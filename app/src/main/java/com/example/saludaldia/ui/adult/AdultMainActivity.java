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
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.FragmentPageAdapter;
import com.example.saludaldia.ui.OCR.OcrCaptureActivity;
import com.example.saludaldia.ui.login.LoginActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.ui.treatment.NewTreatmentActivity;
import com.example.saludaldia.utils.FontScaleContextWrapper;
import com.example.saludaldia.utils.CalendarServiceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdultMainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private static final String TAG = "AdultMainActivity";
    private static final String APP_CALENDAR_NAME = "SaludAlDia Recordatorios";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_AUTHORIZATION = 9002;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private FragmentPageAdapter pagerAdapter;

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

        pagerAdapter = new FragmentPageAdapter(this);
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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FloatingActionButton fab = findViewById(R.id.fabAddTreatment);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AdultMainActivity.this, NewTreatmentActivity.class);
            startActivity(intent);
        });
        FloatingActionButton fabOCR = findViewById(R.id.fabOCR);
        fabOCR.setOnClickListener(v -> {
            Intent intent = new Intent(AdultMainActivity.this, OcrCaptureActivity.class);
            startActivity(intent);
        });

        signInSilently();
    }

    private void signInSilently() {
        mGoogleSignInClient.silentSignIn()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mGoogleSignInAccount = task.getResult();
                        Log.d(TAG, "Silent sign in successful. Initializing Calendar service.");
                        initializeCalendarService(mGoogleSignInAccount);
                    } else {
                        Log.d(TAG, "Silent sign in failed, starting interactive sign in.");
                        signIn();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult llamado. Request Code: " + requestCode + ", Result Code: " + resultCode);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == RESULT_OK) {
                if (mGoogleSignInAccount != null) {
                    initializeCalendarService(mGoogleSignInAccount);
                }
            } else {
                Log.e(TAG, "User denied authorization for Calendar API.");
                Toast.makeText(this, "Autorización de calendario denegada.", Toast.LENGTH_LONG).show();
                CalendarServiceManager.getInstance().setCalendarService(null, null);
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Log.d(TAG, "Google sign in successful, initializing Calendar service.");
                mGoogleSignInAccount = account;
                initializeCalendarService(account);
            } else {
                Log.e(TAG, "Google sign in failed: Account is null.");
                Toast.makeText(this, "Inicio de sesión de Google fallido.", Toast.LENGTH_LONG).show();
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Error en el inicio de sesión de Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
    public Fragment getFragmentFromViewPager(int position) {
        if (pagerAdapter != null) {
            return pagerAdapter.getFragment(position);
        }
        return null;
    }

    private void initializeCalendarService(GoogleSignInAccount account) {
        if (account == null) {
            Log.e(TAG, "Cannot initialize Calendar service: GoogleSignInAccount is null.");
            CalendarServiceManager.getInstance().setCalendarService(null, null);
            return;
        }

        executorService.execute(() -> {
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(CalendarScopes.CALENDAR));
                credential.setSelectedAccount(account.getAccount());

                // ¡Línea corregida aquí!
                Calendar service = new Calendar.Builder(
                        AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                        .setApplicationName(getString(R.string.app_name))
                        .build();

                String calendarId = checkOrCreateAppCalendar(service);

                mainHandler.post(() -> {
                    CalendarServiceManager.getInstance().setCalendarService(service, calendarId);
                    if (calendarId != null) {
                        Toast.makeText(this, "Servicio de Calendar inicializado y calendario encontrado/creado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Servicio de Calendar inicializado, pero no se pudo obtener el ID del calendario.", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (UserRecoverableAuthIOException e) {
                Log.e(TAG, "UserRecoverableAuthIOException: " + e.getMessage());
                mainHandler.post(() -> {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    CalendarServiceManager.getInstance().setCalendarService(null, null);
                });
            } catch (IOException e) {
                Log.e(TAG, "Error initializing Calendar service: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error al inicializar servicio de calendario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    CalendarServiceManager.getInstance().setCalendarService(null, null);
                });
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during Calendar service initialization: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error inesperado al inicializar servicio de calendario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    CalendarServiceManager.getInstance().setCalendarService(null, null);
                });
            }
        });
    }

    private String checkOrCreateAppCalendar(Calendar service) throws IOException {
        String calendarId = null;
        CalendarList calendarList = service.calendarList().list().execute();
        if (calendarList.getItems() != null) {
            for (CalendarListEntry entry : calendarList.getItems()) {
                if (APP_CALENDAR_NAME.equals(entry.getSummary())) {
                    calendarId = entry.getId();
                    Log.d(TAG, "Found existing app calendar: " + APP_CALENDAR_NAME + " ID: " + calendarId);
                    break;
                }
            }
        }

        if (calendarId == null) {
            com.google.api.services.calendar.model.Calendar newCalendar = new com.google.api.services.calendar.model.Calendar();
            newCalendar.setSummary(APP_CALENDAR_NAME);
            newCalendar.setTimeZone(java.util.TimeZone.getDefault().getID());
            newCalendar.setDescription("Eventos de recordatorio para la aplicación " + getString(R.string.app_name) + ".");

            com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(newCalendar).execute();
            calendarId = createdCalendar.getId();
            Log.d(TAG, "Created new app calendar: " + APP_CALENDAR_NAME + " ID: " + calendarId);
        }
        return calendarId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}