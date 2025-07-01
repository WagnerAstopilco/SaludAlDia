package com.example.saludaldia.data.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReminderTest {

    @Test
    public void reminder_defaultConstructor_initializesCorrectly() {
        // Arrange & Act
        Reminder reminder = new Reminder();

        // Assert
        // Verify all fields are null/default as per implementation
        assertNull("reminderId should be null", reminder.getReminderId());
        assertNull("medicationId should be null", reminder.getMedicationId());
        assertNull("startDate should be null", reminder.getStartDate());
        assertNull("endDate should be null", reminder.getEndDate());
        assertFalse("isRecurring should be false", reminder.getIsRecurring());
        assertNull("frequency should be null", reminder.getFrequency());
        assertNull("days should be null", reminder.getDays());
        assertFalse("isActive should be false", reminder.getIsActive());
        assertNull("scheduleTimes should be null", reminder.getScheduleTimes());
        assertNull("calendarEventIds should be null", reminder.getCalendarEventIds());
    }

    @Test
    public void reminder_parameterizedConstructor_setsAllFieldsCorrectly() {
        // Arrange
        String reminderId = "R001";
        String medicationId = "M001";
        Date startDate = new Date(1672531200000L); // Jan 1, 2023 00:00:00 GMT
        Date endDate = new Date(1675209600000L);   // Feb 1, 2023 00:00:00 GMT
        boolean isRecurring = true;
        String frequency = "Daily";
        List<String> days = Arrays.asList("Mon", "Wed", "Fri");
        boolean isActive = true;
        List<String> scheduleTimes = Arrays.asList("08:00", "14:00");
        List<String> calendarEventIds = Arrays.asList("calEvt001", "calEvt002");

        // Act
        Reminder reminder = new Reminder(reminderId, medicationId, startDate, endDate, isRecurring, frequency, days, isActive, scheduleTimes, calendarEventIds);

        // Assert
        assertEquals("Reminder ID should match", reminderId, reminder.getReminderId());
        assertEquals("Medication ID should match", medicationId, reminder.getMedicationId());
        assertEquals("Start Date should match", startDate, reminder.getStartDate());
        assertEquals("End Date should match", endDate, reminder.getEndDate());
        assertEquals("Is Recurring should match", isRecurring, reminder.getIsRecurring());
        assertEquals("Frequency should match", frequency, reminder.getFrequency());
        assertEquals("Days list should match", days, reminder.getDays());
        assertEquals("Is Active should match", isActive, reminder.getIsActive());
        assertEquals("Schedule Times list should match", scheduleTimes, reminder.getScheduleTimes());
        assertEquals("Calendar Event IDs list should match", calendarEventIds, reminder.getCalendarEventIds());
    }

    @Test
    public void reminder_parameterizedConstructor_handlesNullValuesForNullableFields() {
        // Arrange & Act
        // Test with null for all nullable fields (Strings, Dates, Lists)
        Reminder reminder = new Reminder(null, null, null, null, false, null, null, false, null, null);

        // Assert
        assertNull("reminderId should be null", reminder.getReminderId());
        assertNull("medicationId should be null", reminder.getMedicationId());
        assertNull("startDate should be null", reminder.getStartDate());
        assertNull("endDate should be null", reminder.getEndDate());
        assertFalse("isRecurring should be false", reminder.getIsRecurring());
        assertNull("frequency should be null", reminder.getFrequency());
        assertNull("days should be null", reminder.getDays());
        assertFalse("isActive should be false", reminder.getIsActive());
        assertNull("scheduleTimes should be null", reminder.getScheduleTimes());
        assertNull("calendarEventIds should be null", reminder.getCalendarEventIds());
    }

    @Test
    public void reminder_setters_updateFieldsCorrectly() {
        // Arrange
        Reminder reminder = new Reminder();
        String newReminderId = "R002";
        String newMedicationId = "M002";
        Date newStartDate = new Date(1677619200000L); // Mar 1, 2023 00:00:00 GMT
        Date newEndDate = new Date(1680297600000L);   // Apr 1, 2023 00:00:00 GMT
        boolean newIsRecurring = false;
        String newFrequency = "Weekly";
        List<String> newDays = Arrays.asList("Tue", "Thu");
        boolean newIsActive = false;
        List<String> newScheduleTimes = Arrays.asList("10:00", "16:00");
        List<String> newCalendarEventIds = Arrays.asList("calEvt003");

        // Act
        reminder.setReminderId(newReminderId);
        reminder.setMedicationId(newMedicationId);
        reminder.setStartDate(newStartDate);
        reminder.setEndDate(newEndDate);
        reminder.setIsRecurring(newIsRecurring);
        reminder.setFrequency(newFrequency);
        reminder.setDays(newDays);
        reminder.setIsActive(newIsActive);
        reminder.setScheduleTimes(newScheduleTimes);
        reminder.setCalendarEventIds(newCalendarEventIds);

        // Assert
        assertEquals("Reminder ID should be updated", newReminderId, reminder.getReminderId());
        assertEquals("Medication ID should be updated", newMedicationId, reminder.getMedicationId());
        assertEquals("Start Date should be updated", newStartDate, reminder.getStartDate());
        assertEquals("End Date should be updated", newEndDate, reminder.getEndDate());
        assertEquals("Is Recurring should be updated", newIsRecurring, reminder.getIsRecurring());
        assertEquals("Frequency should be updated", newFrequency, reminder.getFrequency());
        assertEquals("Days list should be updated", newDays, reminder.getDays());
        assertEquals("Is Active should be updated", newIsActive, reminder.getIsActive());
        assertEquals("Schedule Times list should be updated", newScheduleTimes, reminder.getScheduleTimes());
        assertEquals("Calendar Event IDs list should be updated", newCalendarEventIds, reminder.getCalendarEventIds());
    }

    @Test
    public void reminder_startDateObject_isReferenceNotCopy() {
        // Arrange
        Date originalStartDate = new Date(1672531200000L); // Jan 1, 2023
        Reminder reminder = new Reminder("R003", "M003", originalStartDate, null, false, null, null, true, null, null);

        // Act
        // Modify the original Date object after passing it to the constructor
        originalStartDate.setTime(0L); // Change to Jan 1, 1970

        // Assert
        // The Date object within the Reminder should have changed
        // as your current model does not make a defensive copy.
        assertEquals("Start Date should reflect original object's change", new Date(0L), reminder.getStartDate());

        // Note: For mutable objects like Date, it's safer to make defensive copies in constructors/setters:
        // this.startDate = (startDate != null) ? new Date(startDate.getTime()) : null;
        // If you implement this, then this test would need to be adjusted to expect the original date.
    }

    @Test
    public void reminder_endDateObject_isReferenceNotCopy() {
        // Arrange
        Date originalEndDate = new Date(1675209600000L); // Feb 1, 2023
        Reminder reminder = new Reminder("R004", "M004", null, originalEndDate, false, null, null, true, null, null);

        // Act
        originalEndDate.setTime(0L); // Change to Jan 1, 1970

        // Assert
        assertEquals("End Date should reflect original object's change", new Date(0L), reminder.getEndDate());
    }

    @Test
    public void reminder_daysList_isReferenceNotCopy() {
        // Arrange
        List<String> originalDays = new ArrayList<>(Arrays.asList("Mon", "Tue"));
        Reminder reminder = new Reminder("R005", "M005", null, null, true, "Daily", originalDays, true, null, null);

        // Act
        // Modify the original list after passing it to the constructor
        originalDays.add("Wed");
        originalDays.remove("Mon");

        // Assert
        // The list within the Reminder should have changed
        // as your current model does not make a defensive copy.
        assertEquals("Days list should reflect original list's change", Arrays.asList("Tue", "Wed"), reminder.getDays());

        // Note: For mutable objects like List, it's safer to make defensive copies:
        // this.days = (days != null) ? new ArrayList<>(days) : null;
        // If you implement this, then this test would need to be adjusted.
    }

    @Test
    public void reminder_scheduleTimesList_isReferenceNotCopy() {
        // Arrange
        List<String> originalTimes = new ArrayList<>(Arrays.asList("08:00", "12:00"));
        Reminder reminder = new Reminder("R006", "M006", null, null, true, "Daily", null, true, originalTimes, null);

        // Act
        originalTimes.add("18:00");

        // Assert
        assertEquals("Schedule times list should reflect original list's change", Arrays.asList("08:00", "12:00", "18:00"), reminder.getScheduleTimes());
    }

    @Test
    public void reminder_calendarEventIdsList_isReferenceNotCopy() {
        // Arrange
        List<String> originalEventIds = new ArrayList<>(Arrays.asList("id1", "id2"));
        Reminder reminder = new Reminder("R007", "M007", null, null, false, null, null, true, null, originalEventIds);

        // Act
        originalEventIds.add("id3");

        // Assert
        assertEquals("Calendar event IDs list should reflect original list's change", Arrays.asList("id1", "id2", "id3"), reminder.getCalendarEventIds());
    }
}