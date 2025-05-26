package com.example.saludaldia.data.model;


public class UserSettings {
    private String userId;
    private String language; // "es", "en", etc.
    private String fontSize; // "small", "medium", "large"
    private boolean notificationSound;
    private boolean darkMode;
    private boolean highContrast;
    private boolean textToSpeech;
    private boolean largeButtons;
    private boolean reduceAnimations;

    public UserSettings() {
    }

    public UserSettings(String userId, String language, String fontSize, boolean notificationSound, boolean darkMode, boolean highContrast, boolean textToSpeech, boolean largeButtons, boolean reduceAnimations) {
        this.userId = userId;
        this.language = language;
        this.fontSize = fontSize;
        this.notificationSound = notificationSound;
        this.darkMode = darkMode;
        this.highContrast = highContrast;
        this.textToSpeech = textToSpeech;
        this.largeButtons = largeButtons;
        this.reduceAnimations = reduceAnimations;
    }
    // Getters y Setters

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public boolean isNotificationSound() { return notificationSound; }
    public void setNotificationSound(boolean notificationSound) { this.notificationSound = notificationSound; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public boolean isHighContrast() { return highContrast; }
    public void setHighContrast(boolean highContrast) { this.highContrast = highContrast; }

    public boolean isTextToSpeech() { return textToSpeech; }
    public void setTextToSpeech(boolean textToSpeech) { this.textToSpeech = textToSpeech; }

    public boolean isLargeButtons() { return largeButtons; }
    public void setLargeButtons(boolean largeButtons) { this.largeButtons = largeButtons; }

    public boolean isReduceAnimations() { return reduceAnimations; }
    public void setReduceAnimations(boolean reduceAnimations) { this.reduceAnimations = reduceAnimations; }
}

