package com.example.saludaldia.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        settingsRepository = new UserSettingsRepository();
        settingsRepository.getUserSettings(
                settings -> {
                    runOnUiThread(() -> inflateUIAndSetup(settings));
                },
                error -> runOnUiThread(() -> inflateUIAndSetup(null))
        );
    }
    private void inflateUIAndSetup(UserSettings settings) {
        setContentView(R.layout.activity_settings);
        AdultToolbar.setup(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.settingsActivity_title));
        }
        initializeViews();
        loadUserSettings();
        setupSaveButton();
    }

    private void initializeViews() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerFontSize = findViewById(R.id.spinnerFontSize);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotificationSound = findViewById(R.id.switchNotificationSound);
        btnSave = findViewById(R.id.btnSaveSettings);
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

        SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);

        String savedLanguage = LanguageManager.getLanguage(this);
        boolean savedDarkMode = prefs.getBoolean("dark_mode", false);
        String savedFontSize = prefs.getString("font_size", "medium");
        boolean savedNotificationSound = prefs.getBoolean("notification_sound", true);

        currentSettings = new UserSettings();
        currentSettings.setLanguage(savedLanguage);
        currentSettings.setDarkMode(savedDarkMode);
        currentSettings.setFontSize(savedFontSize);
        currentSettings.setNotificationSound(savedNotificationSound);

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
    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            int selectedLanguageIndex = spinnerLanguage.getSelectedItemPosition();
            String selectedLanguage = languageCodes[selectedLanguageIndex];
            boolean darkModeSelected = switchDarkMode.isChecked();
            boolean soundOnSelected = switchNotificationSound.isChecked();
            int selectedFontSizeIndex = spinnerFontSize.getSelectedItemPosition();
            String selectedFontSize = fontSizeValues[selectedFontSizeIndex];

            boolean hasLanguageChanged = !selectedLanguage.equals(currentSettings.getLanguage());
            boolean hasDarkModeChanged = darkModeSelected != currentSettings.isDarkMode();
            boolean hasSoundChanged = soundOnSelected != currentSettings.isNotificationSound();
            boolean hasFontSizeChanged = !selectedFontSize.equals(currentSettings.getFontSize());

            boolean hasChanges = hasLanguageChanged || hasDarkModeChanged || hasSoundChanged || hasFontSizeChanged;


            if (!hasChanges) {
                Toast.makeText(this, "No se realizaron cambios", Toast.LENGTH_SHORT).show();
                return;
            }
            final UserSettings proposedSettings = new UserSettings();
            proposedSettings.setLanguage(selectedLanguage);
            proposedSettings.setDarkMode(darkModeSelected);
            proposedSettings.setNotificationSound(soundOnSelected);
            proposedSettings.setFontSize(selectedFontSize);

            settingsRepository.saveUserSettings(proposedSettings,
                    unused -> {
                        SharedPreferences.Editor editor = getSharedPreferences("settings_prefs", MODE_PRIVATE).edit();
                        editor.putBoolean("dark_mode", darkModeSelected);
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
                .setTitle(getString(R.string.restart_app_title))
                .setMessage(getString(R.string.restart_app_message))
                .setPositiveButton(getString(R.string.restart_button), (dialog, which) -> {


                        LanguageManager.setLocale(SettingsActivity.this, selectedLanguage);
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                })
                .setNegativeButton(getString(R.string.cancel_button), (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "No se aplicó ningun cambio", Toast.LENGTH_LONG).show();
                    loadUserSettings();
                })
                .setCancelable(false)
                .show();
    }

}
