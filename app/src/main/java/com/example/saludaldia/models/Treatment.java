package com.example.saludaldia.models;

import java.util.List;
import java.util.Date;
public class Treatment {
    private String treatmentId;
    private String userId; // Usuario al que pertenece
    private String name;   // Nombre del tratamiento
    private Date startDate;
    private Date endDate;
    private List<Medication> medications; // Lista de medicamentos embebidos
    private String description;

    public Treatment() {

    }

    public Treatment(String treatmentId, String userId, String name, Date startDate, Date endDate, List<Medication> medications, String description) {
        this.treatmentId = treatmentId;
        this.userId = userId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.medications = medications;
        this.description = description;
    }

    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
