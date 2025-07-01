package com.example.saludaldia.data.model;

public class Medication {
    private String medicationId;
    private String treatmentId;
    private String name;
    private String presentation;
    private String via;
    private String dose;
    private String notes;
    private int number_days;
    private boolean active;
    private Reminder reminder;

    public Medication() {

    }

    public Medication(String medicationId, String treatmentId, String name,String presentation,String via, String dose, String notes,int number_days, boolean active, Reminder reminder) {
        this.medicationId = medicationId;
        this.treatmentId = treatmentId;
        this.name = name;
        this.presentation=presentation;
        this.via=via;
        this.dose = dose;
        this.notes = notes;
        this.number_days=number_days;
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

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
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

    public int getNumber_days() {
        return number_days;
    }

    public void setNumber_days(int number_days) {
        this.number_days = number_days;
    }

    public boolean getIsActive() {
        return active;
    }

    public void setIsActive(boolean active) {
        this.active = active;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }
}
