package com.example.saludaldia.models;

import java.util.List;
public class History {
    private String historyId;        // ID único del registro histórico
    private String userId;           // Usuario al que pertenece el historial
    private List<HistoryEvent> events;

    public History() {
    }

    public History(String historyId, String userId, List<HistoryEvent> events) {
        this.historyId = historyId;
        this.userId = userId;
        this.events = events;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<HistoryEvent> getEvents() {
        return events;
    }

    public void setEvents(List<HistoryEvent> events) {
        this.events = events;
    }
}
