package com.example.saludaldia.data.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

public class MedicationTest {

    @Test
    public void medication_defaultConstructor_initializesCorrectly() {
        // Arrange & Act
        Medication medication = new Medication();

        // Assert
        // Verifica que todos los campos son nulos/por defecto
        assertNull("medicationId should be null", medication.getMedicationId());
        assertNull("treatmentId should be null", medication.getTreatmentId());
        assertNull("name should be null", medication.getName());
        assertNull("presentation should be null", medication.getPresentation());
        assertNull("via should be null", medication.getVia());
        assertNull("dose should be null", medication.getDose());
        assertNull("notes should be null", medication.getNotes());
        assertEquals("number_days should be 0", 0, medication.getNumber_days());
        assertFalse("active should be false", medication.getIsActive());
        assertNull("reminder should be null", medication.getReminder());
    }

    @Test
    public void medication_parameterizedConstructor_setsAllFieldsCorrectly() {
        // Arrange
        String medicationId = "M001";
        String treatmentId = "T001";
        String name = "Ibuprofeno";
        String presentation = "Tableta";
        String via = "Oral";
        String dose = "200mg";
        String notes = "Tomar con alimentos.";
        int numberDays = 7;
        boolean active = true;
        Reminder reminder = new Reminder(); // Mock Reminder object

        // Act
        Medication medication = new Medication(medicationId, treatmentId, name, presentation, via, dose, notes, numberDays, active, reminder);

        // Assert
        assertEquals("Medication ID should match", medicationId, medication.getMedicationId());
        assertEquals("Treatment ID should match", treatmentId, medication.getTreatmentId());
        assertEquals("Name should match", name, medication.getName());
        assertEquals("Presentation should match", presentation, medication.getPresentation());
        assertEquals("Via should match", via, medication.getVia());
        assertEquals("Dose should match", dose, medication.getDose());
        assertEquals("Notes should match", notes, medication.getNotes());
        assertEquals("Number of days should match", numberDays, medication.getNumber_days());
        assertTrue("Active status should match", medication.getIsActive());
        assertEquals("Reminder should match", reminder, medication.getReminder());
    }

    @Test
    public void medication_parameterizedConstructor_handlesNullValuesForStringsAndReminder() {
        // Arrange & Act
        Medication medication = new Medication(null, null, null, null, null, null, null, 0, false, null);

        // Assert
        assertNull("medicationId should be null", medication.getMedicationId());
        assertNull("treatmentId should be null", medication.getTreatmentId());
        assertNull("name should be null", medication.getName());
        assertNull("presentation should be null", medication.getPresentation());
        assertNull("via should be null", medication.getVia());
        assertNull("dose should be null", medication.getDose());
        assertNull("notes should be null", medication.getNotes());
        assertEquals("number_days should be 0", 0, medication.getNumber_days());
        assertFalse("active should be false", medication.getIsActive());
        assertNull("reminder should be null", medication.getReminder());
    }

    @Test
    public void medication_setters_updateFieldsCorrectly() {
        // Arrange
        Medication medication = new Medication();
        String newMedicationId = "M002";
        String newTreatmentId = "T002";
        String newName = "Paracetamol";
        String newPresentation = "Gotas";
        String newVia = "Sublingual";
        String newDose = "5ml";
        String newNotes = "Tomar cada 8 horas.";
        int newNumberDays = 10;
        boolean newActive = false;
        Reminder newReminder = new Reminder();

        // Act
        medication.setMedicationId(newMedicationId);
        medication.setTreatmentId(newTreatmentId);
        medication.setName(newName);
        medication.setPresentation(newPresentation);
        medication.setVia(newVia);
        medication.setDose(newDose);
        medication.setNotes(newNotes);
        medication.setNumber_days(newNumberDays);
        medication.setIsActive(newActive);
        medication.setReminder(newReminder);

        // Assert
        assertEquals("Medication ID should be updated", newMedicationId, medication.getMedicationId());
        assertEquals("Treatment ID should be updated", newTreatmentId, medication.getTreatmentId());
        assertEquals("Name should be updated", newName, medication.getName());
        assertEquals("Presentation should be updated", newPresentation, medication.getPresentation());
        assertEquals("Via should be updated", newVia, medication.getVia());
        assertEquals("Dose should be updated", newDose, medication.getDose());
        assertEquals("Notes should be updated", newNotes, medication.getNotes());
        assertEquals("Number of days should be updated", newNumberDays, medication.getNumber_days());
        assertEquals("Active status should be updated", newActive, medication.getIsActive());
        assertEquals("Reminder should be updated", newReminder, medication.getReminder());
    }

    @Test
    public void medication_reminderObject_isReferenceNotCopy() {
        // Arrange
        Reminder originalReminder = new Reminder();
        Medication medication = new Medication("M003", "T003", "Amoxicilina", "Cápsula", "Oral", "500mg", "Después de comidas", 10, true, originalReminder);

        ArrayList<String> times=new ArrayList<>(Arrays.asList("08:00", "12:30", "18:45", "22:00"));
        originalReminder.setScheduleTimes(times);


        // Assert
        // El objeto Reminder dentro de Medication debería haber cambiado
        // ya que tu modelo actual no hace una copia defensiva.
        assertEquals("Reminder time should reflect original object's change", "10:00", medication.getReminder().getScheduleTimes());
        assertEquals("Reminder frequency should reflect original object's change", 2, medication.getReminder().getFrequency());

        // Nota: Si Reminder fuera un objeto mutable y quisieras que Medication tuviera una copia defensiva,
        // deberías modificar el constructor y el setter de Medication para crear una nueva instancia de Reminder:
        // this.reminder = (reminder != null) ? new Reminder(reminder.getReminderId(), reminder.getTime(), reminder.getFrequency()) : null;
        // Si hicieras ese cambio, esta prueba fallaría y necesitarías ajustarla para esperar los valores originales.
    }
}