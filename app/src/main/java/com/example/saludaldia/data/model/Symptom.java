package com.example.saludaldia.data.model;

import java.util.Date;
public class Symptom {
    private String symptomId;
    private String userId;
    private String treatmentId;
    private String medicationId;
    private Date dateReported;
    private String symptomDescription;
    private String severity;
    private String notes;

    public Symptom() {
    }

    public Symptom(String symptomId, String userId, String treatmentId, String medicationId, Date dateReported, String symptomDescription, String severity, String notes) {
        this.symptomId = symptomId;
        this.userId = userId;
        this.treatmentId = treatmentId;
        this.medicationId = medicationId;
        this.dateReported = dateReported;
        this.symptomDescription = symptomDescription;
        this.severity = severity;
        this.notes = notes;
    }

    public String getSymptomId() {
        return symptomId;
    }

    public void setSymptomId(String symptomId) {
        this.symptomId = symptomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    public Date getDateReported() {
        return dateReported;
    }

    public void setDateReported(Date dateReported) {
        this.dateReported = dateReported;
    }

    public String getSymptomDescription() {
        return symptomDescription;
    }

    public void setSymptomDescription(String symptomDescription) {
        this.symptomDescription = symptomDescription;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
