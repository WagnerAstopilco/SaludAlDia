package com.example.saludaldia.data.model;

import java.util.ArrayList;
import java.util.List;
public class User {
    private String userId;
    private String names;
    private String lastNames;
    private String email;
    private String phoneNumber;
    private String role;
    private Integer age;
    private Double weight;
    private List<String> allergies;
    private List<String> treatmentIds;
    private UserSettings settings;
    private List<String> linkedUserIds;
    private History history;
    private List<ReminderInstance> notifications;

    public User() {
        this.linkedUserIds=new ArrayList<>();
    }

    public User(String userId, String names,String lastNames, String email, String phoneNumber, String role, Integer age, Double weight, List<String> allergies, List<String> treatmentIds, UserSettings settings, List<String> linkedUserIds, History history, List<ReminderInstance> notifications) {
        this.userId = userId;
        this.names = names;
        this.lastNames=lastNames;
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
        this.notifications = notifications;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String name) {
        this.names = name;
    }

    public String getLastNames() {
        return lastNames;
    }

    public void setLastNames(String lastNames) {
        this.lastNames = lastNames;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
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

    public List<ReminderInstance> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<ReminderInstance> notifications) {
        this.notifications = notifications;
    }
}
