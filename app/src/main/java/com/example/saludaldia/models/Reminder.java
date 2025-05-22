package com.example.saludaldia.models;

import java.util.Date;
import java.util.List;
public class Reminder {
    private String reminderId;
    private String medicationId;
    private Date startDate;
    private Date endDate;
    private boolean isRecurring;     // Si el recordatorio es repetitivo
    private String frequency;  // ej. "daily", "weekly", "specific_days"
    private List<String> days;       // Días de repetición, ej: ["Monday", "Wednesday"]
    private boolean isActive;        // Estado del recordatorio (activo/inactivo)
    private List<String> scheduleTimes; // ["08:00", "20:00"]

    public Reminder() {

    }

    public Reminder(String reminderId, String medicationId, Date startDate, Date endDate, boolean isRecurring, String frequency, List<String> days, boolean isActive, List<String> scheduleTimes) {
        this.reminderId = reminderId;
        this.medicationId = medicationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isRecurring = isRecurring;
        this.frequency = frequency;
        this.days = days;
        this.isActive = isActive;
        this.scheduleTimes = scheduleTimes;
    }

    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
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

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<String> getScheduleTimes() {
        return scheduleTimes;
    }

    public void setScheduleTimes(List<String> scheduleTimes) {
        this.scheduleTimes = scheduleTimes;
    }
}
