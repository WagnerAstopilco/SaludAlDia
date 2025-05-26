package com.example.saludaldia.data.model;

import java.util.Date;
public class HistoryEvent {
    private String eventId;
    private String historyId;           // Usuario al que pertenece el historial
    private Date timestamp;      // Fecha y hora del evento
    private String eventType;        // Tipo de evento: "med_taken", "med_added", "med_edited", "med_deleted", "symptom_reported", etc.
    private String details;
    private Symptom symptom;

    public HistoryEvent() {
    }

    public HistoryEvent(String eventId, String historyId, Date timestamp, String eventType, String details, Symptom symptom) {
        this.eventId = eventId;
        this.historyId = historyId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.details = details;
        this.symptom = symptom;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
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

    public Symptom getSymptom() {
        return symptom;
    }

    public void setSymptom(Symptom symptom) {
        this.symptom = symptom;
    }
}
