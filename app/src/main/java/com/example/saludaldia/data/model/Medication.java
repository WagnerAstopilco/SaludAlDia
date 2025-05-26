package com.example.saludaldia.data.model;

public class Medication {
    private String medicationId;
    private String treatmentId;
    private String name;
    private String dose;  // ej. "500mg"
    private String notes;
    private boolean active;
    private Reminder reminder;

    public Medication() {

    }

    public Medication(String medicationId, String treatmentId, String name, String dose, String notes, boolean active, Reminder reminder) {
        this.medicationId = medicationId;
        this.treatmentId = treatmentId;
        this.name = name;
        this.dose = dose;
        this.notes = notes;
        this.active = active;
        this.reminder = reminder;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }
}
