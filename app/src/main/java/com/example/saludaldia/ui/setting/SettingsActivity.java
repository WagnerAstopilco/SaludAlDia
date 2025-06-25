package com.example.saludaldia.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.UserSettings;
import com.example.saludaldia.data.repository.UserSettingsRepository;
import com.example.saludaldia.ui.login.LoginActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.utils.FontScaleContextWrapper;
import com.example.saludaldia.utils.LanguageManager;
import com.example.saludaldia.utils.ThemeHelper;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerLanguage, spinnerFontSize;
    private Switch switchDarkMode, switchNotificationSound;
    private Button btnSave;

    private UserSettingsRepository settingsRepository;
    private UserSettings currentSettings;

    private final String[] visibleLanguages = {"Español", "Inglés"};
    private final String[] languageCodes = {"es", "en"};
    private final String[] fontSizeLabels = {"Pequeña", "Mediana", "Grande"};
    private final String[] fontSizeValues = {"small", "medium", "large"};
//    private boolean languageChangedAndNeedsRecreate = false;
    @Override
    protected void attachBaseContext(Context newBase) {
//        SharedPreferences prefs = newBase.getSharedPreferences("settings_prefs", MODE_PRIVATE);
//        String currentFontSize = prefs.getString("font_size", "medium");
//
//        Context contextForFont = FontScaleContextWrapper.wrap(newBase);
//
//        super.attachBaseContext(contextForFont);
        // Primero aplica el idioma desde LanguageManager
        Context languageContext = LanguageManager.setLocale(newBase);

        // Luego aplica el escalado de fuente sobre el contexto del idioma
        SharedPreferences prefs = languageContext.getSharedPreferences("settings_prefs", MODE_PRIVATE);
        String currentFontSize = prefs.getString("font_size", "medium");
        Context contextForFont = FontScaleContextWrapper.wrap(languageContext);

        super.attachBaseContext(contextForFont);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d("IdiomaApp", "Idioma actual en onCreate: " + getResources().getConfiguration().locale.getLanguage());
        settingsRepository = new UserSettingsRepository();

//        if (savedInstanceState != null && savedInstanceState.getBoolean("language_recreated", false)) {
//            languageChangedAndNeedsRecreate = true;
//        }

        settingsRepository.getUserSettings(
                settings -> {
                    runOnUiThread(() -> inflateUIAndSetup(settings));
                },
                error -> runOnUiThread(() -> inflateUIAndSetup(null))
        );
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // Guarda el estado de si la actividad se recreó por idioma
//        outState.putBoolean("language_recreated", languageChangedAndNeedsRecreate);
//    }
    private void inflateUIAndSetup(UserSettings settings) {
        setContentView(R.layout.activity_settings);
        AdultToolbar.setup(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.settingsActivity_title));
        }
        initializeViews();
        loadUserSettings();
//        setupSpinnerListeners();
        setupSaveButton();
    }

    private void initializeViews() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerFontSize = findViewById(R.id.spinnerFontSize);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotificationSound = findViewById(R.id.switchNotificationSound);
        btnSave = findViewById(R.id.btnSaveSettings);

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item, visibleLanguages);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerLanguage.setAdapter(adapter);
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array,
                android.R.layout.simple_spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fontSizeLabels);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(fontSizeAdapter);
    }

    private void loadUserSettings() {


//        int languageIndex = 0;
//        for (int i = 0; i < languageCodes.length; i++) {
//            if (languageCodes[i].equals(currentSettings.getLanguage())) {
//                languageIndex = i;
//                break;
//            }
//        }
//        spinnerLanguage.setSelection(languageIndex);
//        String currentSavedLangCode = LanguageManager.getLanguage(this);
//        int languageIndex = 0;
//        for (int i = 0; i < languageCodes.length; i++) {
//            if (languageCodes[i].equals(currentSavedLangCode)) {
//                languageIndex = i;
//                break;
//            }
//        }
//        spinnerLanguage.setSelection(languageIndex);
//
//        String fontSize = currentSettings.getFontSize() != null ? currentSettings.getFontSize() : "medium";
//        int fontSizeIndex = 1;
//        for (int i = 0; i < fontSizeValues.length; i++) {
//            if (fontSizeValues[i].equals(fontSize)) {
//                fontSizeIndex = i;
//                break;
//            }
//        }
//        spinnerFontSize.setSelection(fontSizeIndex);
//
//        switchDarkMode.setChecked(currentSettings.isDarkMode());
//        switchNotificationSound.setChecked(currentSettings.isNotificationSound());
        SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);

        String savedLanguage = LanguageManager.getLanguage(this); // Lee el idioma a través de LanguageManager
        boolean savedDarkMode = prefs.getBoolean("dark_mode", false);
        String savedFontSize = prefs.getString("font_size", "medium");
        boolean savedNotificationSound = prefs.getBoolean("notification_sound", true); // Asume valor por defecto

        // Actualiza currentSettings con lo que realmente está guardado
        currentSettings = new UserSettings();
        currentSettings.setLanguage(savedLanguage);
        currentSettings.setDarkMode(savedDarkMode);
        currentSettings.setFontSize(savedFontSize);
        currentSettings.setNotificationSound(savedNotificationSound);

        // Actualiza la UI para reflejar estos valores guardados
        int languageIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentSettings.getLanguage())) {
                languageIndex = i;
                break;
            }
        }
        spinnerLanguage.setSelection(languageIndex, false);

        int fontSizeIndex = 1;
        for (int i = 0; i < fontSizeValues.length; i++) {
            if (fontSizeValues[i].equals(currentSettings.getFontSize())) {
                fontSizeIndex = i;
                break;
            }
        }
        spinnerFontSize.setSelection(fontSizeIndex, false);

        switchDarkMode.setChecked(currentSettings.isDarkMode());
        switchNotificationSound.setChecked(currentSettings.isNotificationSound());

    }
//    private void setupSpinnerListeners() {
//        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedLangCode = languageCodes[position];
//                String currentAppLangCode = LanguageManager.getLanguage(SettingsActivity.this);
//
//                if (!selectedLangCode.equals(currentAppLangCode)) {
//                    // Si el idioma cambia, establece la bandera y recrea la actividad
//                    LanguageManager.setLocale(SettingsActivity.this, selectedLangCode);
//                    languageChangedAndNeedsRecreate = true; // Establecer la bandera
//                    recreate(); // Recrea la actividad para aplicar el nuevo idioma
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // No hacer nada
//            }
//        });

        // Puedes agregar listeners para los otros spinners/switches si necesitas acciones inmediatas
        // o si prefieres que los cambios se apliquen solo al presionar "Guardar".
        // Para este ejemplo, solo el idioma causa un recreate inmediato.
//    }
    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            int selectedLanguageIndex = spinnerLanguage.getSelectedItemPosition();
            String selectedLanguage = languageCodes[selectedLanguageIndex];
            boolean darkModeSelected = switchDarkMode.isChecked();
            boolean soundOnSelected = switchNotificationSound.isChecked();
            int selectedFontSizeIndex = spinnerFontSize.getSelectedItemPosition();
            String selectedFontSize = fontSizeValues[selectedFontSizeIndex];

//            String initialLanguageCode = currentSettings.getLanguage();
            boolean hasLanguageChanged = !selectedLanguage.equals(currentSettings.getLanguage());
            boolean hasDarkModeChanged = darkModeSelected != currentSettings.isDarkMode();
            boolean hasSoundChanged = soundOnSelected != currentSettings.isNotificationSound();
            boolean hasFontSizeChanged = !selectedFontSize.equals(currentSettings.getFontSize());

            boolean hasChanges = hasLanguageChanged || hasDarkModeChanged || hasSoundChanged || hasFontSizeChanged;


            if (!hasChanges) {
                Toast.makeText(this, "No se realizaron cambios", Toast.LENGTH_SHORT).show();
                return;
            }

//            UserSettings updatedSettings = new UserSettings();
//            updatedSettings.setLanguage(selectedLanguage);
//            updatedSettings.setDarkMode(darkModeSelected);
//            updatedSettings.setNotificationSound(soundOnSelected);
//            updatedSettings.setFontSize(selectedFontSize);
            final UserSettings proposedSettings = new UserSettings();
            proposedSettings.setLanguage(selectedLanguage);
            proposedSettings.setDarkMode(darkModeSelected);
            proposedSettings.setNotificationSound(soundOnSelected);
            proposedSettings.setFontSize(selectedFontSize);

            settingsRepository.saveUserSettings(proposedSettings,
                    unused -> {
                        // Guardar en SharedPreferences (esto ya lo tenías)
                        SharedPreferences.Editor editor = getSharedPreferences("settings_prefs", MODE_PRIVATE).edit();
                        editor.putBoolean("dark_mode", darkModeSelected);
//                        editor.putString("language", selectedLanguage);
                        editor.putString("font_size", selectedFontSize);
                        editor.apply();

                        boolean wasDark = currentSettings.isDarkMode();
                        boolean sizeChanged=!selectedFontSize.equals(currentSettings.getFontSize());
                        boolean languageChanged=!selectedLanguage.equals(currentSettings.getLanguage());


                        Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();

                        if (wasDark != darkModeSelected) {
                            ThemeHelper.applyTheme(darkModeSelected);
                        }

                        if (sizeChanged || wasDark != darkModeSelected) {
                            currentSettings = proposedSettings;
                            recreate();
                        }
                        if (hasLanguageChanged) {
                            showRestartConfirmationDialog(selectedLanguage,proposedSettings);
                        }
                    },
                    e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });
    }
    private void showRestartConfirmationDialog(final String selectedLanguage,final UserSettings proposedSettings) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.restart_app_title)) // Título del diálogo
                .setMessage(getString(R.string.restart_app_message)) // Mensaje de confirmación
                .setPositiveButton(getString(R.string.restart_button), (dialog, which) -> {


                        LanguageManager.setLocale(SettingsActivity.this, selectedLanguage);
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class); // Usa tu actividad principal
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                })
                .setNegativeButton(getString(R.string.cancel_button), (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "No se aplicó ningun cambio", Toast.LENGTH_LONG).show();
                    // Opcional: podrías recrear solo SettingsActivity si quieres que al menos esa se refresque.
                    loadUserSettings();
                })
                .setCancelable(false)
                .show();
    }

}
