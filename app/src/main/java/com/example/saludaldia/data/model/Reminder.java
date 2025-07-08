package com.example.saludaldia.data.model;

import java.util.Date;
import java.util.List;
public class Reminder {
    private String reminderId;
    private String medicationId;
    private Date startDate;
    private Date endDate;
    private boolean isRecurring;
    private String frequency;
    private List<String> days;
    private boolean isActive;
    private List<String> scheduleTimes;

    private List<String> calendarEventIds;

    public Reminder() {

    }

    public Reminder(String reminderId, String medicationId, Date startDate, Date endDate, boolean isRecurring, String frequency, List<String> days, boolean isActive, List<String> scheduleTimes, List<String> calendarEventIds) {
        this.reminderId = reminderId;
        this.medicationId = medicationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isRecurring = isRecurring;
        this.frequency = frequency;
        this.days = days;
        this.isActive = isActive;
        this.scheduleTimes = scheduleTimes;
        this.calendarEventIds=calendarEventIds;
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

    public boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(boolean recurring) {
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

    public boolean getActive() {
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

    public List<String> getCalendarEventIds() {
        return calendarEventIds;
    }

    public void setCalendarEventIds(List<String> calendarEventIds) {
        this.calendarEventIds = calendarEventIds;
    }
}
