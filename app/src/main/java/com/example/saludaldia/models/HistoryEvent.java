package com.example.saludaldia.models;

import java.util.Date;
public class HistoryEvent {
    private String eventId;
    private String userId;           // Usuario al que pertenece el historial
    private String treatmentId;      // Opcional, para relacionar con tratamiento
    private String medicationId;     // Opcional, para relacionar con medicamento
    private Date timestamp;      // Fecha y hora del evento
    private String eventType;        // Tipo de evento: "med_taken", "med_added", "med_edited", "med_deleted", "symptom_reported", etc.
    private String details;

    public HistoryEvent() {
    }

    public HistoryEvent(String eventId, String userId, String treatmentId, String medicationId, Date timestamp, String eventType, String details) {
        this.eventId = eventId;
        this.userId = userId;
        this.treatmentId = treatmentId;
        this.medicationId = medicationId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.details = details;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
