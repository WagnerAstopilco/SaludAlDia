package com.example.saludaldia.models;

import java.util.List;
public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String role; // "user" o "caregiver"
    private int age;
    private float weight;
    private List<String> allergies;
    private List<String> treatmentIds;
    private UserSettings settings;
    private List<String> linkedUserIds;
    private History history;
    private List<ReminderInstance> notifications;

    public User() {
    }

    public User(String userId, String name, String email, String phoneNumber, String role, int age, float weight, List<String> allergies, List<String> treatmentIds, UserSettings settings, List<String> linkedUserIds, History history) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.age = age;
        this.weight = weight;
        this.allergies = allergies;
        this.treatmentIds = treatmentIds;
        this.settings = settings;
        this.linkedUserIds = linkedUserIds;
        this.history = history;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getTreatmentIds() {
        return treatmentIds;
    }

    public void setTreatmentIds(List<String> treatmentIds) {
        this.treatmentIds = treatmentIds;
    }

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings;
    }

    public List<String> getLinkedUserIds() {
        return linkedUserIds;
    }

    public void setLinkedUserIds(List<String> linkedUserIds) {
        this.linkedUserIds = linkedUserIds;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }
}
