package com.example.saludaldia.data.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
public class HistoryEvent {
    private String eventId;
    private String historyId;
    @ServerTimestamp
    private Date timestamp;
    private String eventType;
    private String details;

    public HistoryEvent() {
    }

    public HistoryEvent(String eventId, String historyId,  String eventType, String details) {
        this.eventId = eventId;
        this.historyId = historyId;
        this.eventType = eventType;
        this.details = details;
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


}
