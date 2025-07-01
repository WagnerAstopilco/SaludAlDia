package com.example.saludaldia.data.model;

import java.util.Date;

public class Treatment {
    private String treatmentId;
    private String userId;
    private String name;
    private Date startDate;
    private Date endDate;
    private String description;
    private String state;

    public Treatment() { }

    public Treatment(String treatmentId, String userId, String name, Date startDate, Date endDate,  String description,String state) {
        this.treatmentId = treatmentId;
        this.userId = userId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.state=state;
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

   public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }
}
