package com.example.saludaldia.data.model;

import java.util.ArrayList;
import java.util.List;

public class History {
    private String historyId;
    private String userId;

    private List<String> eventsIds;

    public History() {
        this.eventsIds = new ArrayList<>();
    }

    public History(String historyId, String userId, List<String> eventsIds) {
        this.historyId = historyId;
        this.userId = userId;
        this.eventsIds = eventsIds != null ? eventsIds : new ArrayList<>();
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

    public List<String> getEventsIds() {
        return eventsIds;
    }

    public void setEventsIds(List<String> eventsIds) {
        this.eventsIds = eventsIds;
    }
}