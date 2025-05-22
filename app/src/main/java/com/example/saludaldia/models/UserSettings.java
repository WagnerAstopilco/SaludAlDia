package com.example.saludaldia.models;

public class UserSettings {
    private String language; // "es", "en"
    private String notificationSound;
    private boolean darkMode;

    public UserSettings() {

    }

    public UserSettings(String language, String notificationSound, boolean darkMode) {
        this.language = language;
        this.notificationSound = notificationSound;
        this.darkMode = darkMode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getNotificationSound() {
        return notificationSound;
    }

    public void setNotificationSound(String notificationSound) {
        this.notificationSound = notificationSound;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }
}
