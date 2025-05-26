package com.example.saludaldia.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.UserSettings;
import com.example.saludaldia.data.repository.UserSettingsRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsRepository = new UserSettingsRepository();

        settingsRepository.getUserSettings(
                settings -> {
                    boolean dark = settings != null && settings.isDarkMode();
                    ThemeHelper.applyTheme(dark);
                    runOnUiThread(() -> inflateUIAndSetup(settings));
                },
                error -> runOnUiThread(() -> inflateUIAndSetup(null))
        );
    }

    private void inflateUIAndSetup(UserSettings settings) {
        setContentView(R.layout.activity_settings);
        AdultToolbar.setup(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Configuración");
        }
        initializeViews();
        loadUserSettings(settings);
        setupSaveButton();
    }

    private void initializeViews() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerFontSize = findViewById(R.id.spinnerFontSize);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotificationSound = findViewById(R.id.switchNotificationSound);
        btnSave = findViewById(R.id.btnSaveSettings);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, visibleLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fontSizeLabels);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(fontSizeAdapter);
    }

    private void loadUserSettings(UserSettings settings) {
        currentSettings = settings != null ? settings : new UserSettings();

        int languageIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentSettings.getLanguage())) {
                languageIndex = i;
                break;
            }
        }
        spinnerLanguage.setSelection(languageIndex);

        String fontSize = currentSettings.getFontSize() != null ? currentSettings.getFontSize() : "medium";
        int fontSizeIndex = 1;
        for (int i = 0; i < fontSizeValues.length; i++) {
            if (fontSizeValues[i].equals(fontSize)) {
                fontSizeIndex = i;
                break;
            }
        }
        spinnerFontSize.setSelection(fontSizeIndex);

        switchDarkMode.setChecked(currentSettings.isDarkMode());
        switchNotificationSound.setChecked(currentSettings.isNotificationSound());

    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            int selectedLanguageIndex = spinnerLanguage.getSelectedItemPosition();
            String selectedLanguage = languageCodes[selectedLanguageIndex];
            boolean darkModeSelected = switchDarkMode.isChecked();
            boolean soundOnSelected = switchNotificationSound.isChecked();
            int selectedFontSizeIndex = spinnerFontSize.getSelectedItemPosition();
            String selectedFontSize = fontSizeValues[selectedFontSizeIndex];

            // Validar si hubo cambios reales
            boolean hasChanges = !selectedLanguage.equals(currentSettings.getLanguage()) ||
                    darkModeSelected != currentSettings.isDarkMode() ||
                    soundOnSelected != currentSettings.isNotificationSound() ||
                    !selectedFontSize.equals(currentSettings.getFontSize());

            if (!hasChanges) {
                Toast.makeText(this, "No se realizaron cambios", Toast.LENGTH_SHORT).show();
                return;
            }

            UserSettings updatedSettings = new UserSettings();
            updatedSettings.setLanguage(selectedLanguage);
            updatedSettings.setDarkMode(darkModeSelected);
            updatedSettings.setNotificationSound(soundOnSelected);
            updatedSettings.setFontSize(selectedFontSize);

            settingsRepository.saveUserSettings(updatedSettings,
                    unused -> {
                        // Guardar localmente en SharedPreferences
                        SharedPreferences.Editor editor = getSharedPreferences("settings_prefs", MODE_PRIVATE).edit();
                        editor.putBoolean("dark_mode", darkModeSelected);
                        editor.putString("language", selectedLanguage);
                        editor.putString("font_size", selectedFontSize);
                        editor.apply();

                        boolean wasDark = currentSettings.isDarkMode();
                        currentSettings = updatedSettings;

                        Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();

                        if (wasDark != darkModeSelected) {
                            ThemeHelper.applyTheme(darkModeSelected);
                            recreate();
                        }
                    },
                    e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });
    }

    // (Opcional) Método para cambiar el idioma en tiempo real
    public static Context setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
