package com.example.saludaldia.data.model;

import java.util.Date;
public class ReminderInstance {
    private String instanceId;
    private String medicationId;     // Para trazabilidad directa
    private String treatmentId;      // Útil para reportes
    private String userId;           // Útil para notificaciones y filtros
    private Date scheduledTime;      // Fecha y hora exacta de esta instancia
    private String status;           // "pending", "confirmed", "missed"
    private boolean notificationSent;

    public ReminderInstance() {
    }

    public ReminderInstance(String instanceId, String medicationId, String treatmentId, String userId, Date scheduledTime, String status, boolean notificationSent) {
        this.instanceId = instanceId;
        this.medicationId = medicationId;
        this.treatmentId = treatmentId;
        this.userId = userId;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.notificationSent = notificationSent;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }
}
