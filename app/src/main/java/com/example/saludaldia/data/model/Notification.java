
package com.example.saludaldia.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String relatedReminderId;
    private String relatedMedicationId;
    private String relatedTreatmentId;
    private long notificationTriggerTimeMillis;
    @ServerTimestamp
    private Date timestamp;
    private boolean dismissed;
    private boolean completed;

    public Notification() {
    }

    public Notification(String id, String userId, String title, String message, String relatedReminderId, String relatedMedicationId, String relatedTreatmentId, long notificationTriggerTimeMillis, Date timestamp, boolean dismissed, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.relatedReminderId = relatedReminderId;
        this.relatedMedicationId = relatedMedicationId;
        this.relatedTreatmentId = relatedTreatmentId;
        this.notificationTriggerTimeMillis = notificationTriggerTimeMillis;
        this.timestamp = timestamp;
        this.dismissed = dismissed;
        this.completed = completed;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedReminderId() {
        return relatedReminderId;
    }

    public void setRelatedReminderId(String relatedReminderId) {
        this.relatedReminderId = relatedReminderId;
    }

    public String getRelatedMedicationId() {
        return relatedMedicationId;
    }

    public void setRelatedMedicationId(String relatedMedicationId) {
        this.relatedMedicationId = relatedMedicationId;
    }

    public String getRelatedTreatmentId() {
        return relatedTreatmentId;
    }

    public void setRelatedTreatmentId(String relatedTreatmentId) {
        this.relatedTreatmentId = relatedTreatmentId;
    }

    public long getNotificationTriggerTimeMillis() {
        return notificationTriggerTimeMillis;
    }

    public void setNotificationTriggerTimeMillis(long notificationTriggerTimeMillis) {
        this.notificationTriggerTimeMillis = notificationTriggerTimeMillis;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}