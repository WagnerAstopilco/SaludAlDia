package com.example.saludaldia.data.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

public class TreatmentTest {

    @Test
    public void treatment_defaultConstructor_initializesCorrectly() {
        // Arrange & Act
        Treatment treatment = new Treatment();

        // Assert
        // Verifica que todos los campos son nulos por defecto, según la implementación
        assertNull("El treatmentId debería ser nulo", treatment.getTreatmentId());
        assertNull("El userId debería ser nulo", treatment.getUserId());
        assertNull("El nombre debería ser nulo", treatment.getName());
        assertNull("La fecha de inicio debería ser nula", treatment.getStartDate());
        assertNull("La fecha de fin debería ser nula", treatment.getEndDate());
        assertNull("La descripción debería ser nula", treatment.getDescription());
        assertNull("El estado debería ser nulo", treatment.getState());
    }

    @Test
    public void treatment_parameterizedConstructor_setsAllFieldsCorrectly() {
        // Arrange
        String treatmentId = "T001";
        String userId = "U001";
        String name = "Fisioterapia";
        Date startDate = new Date(1672531200000L); // 01/01/2023 00:00:00 GMT
        Date endDate = new Date(1675209600000L);   // 01/02/2023 00:00:00 GMT
        String description = "Sesiones de rehabilitación muscular.";
        String state = "Activo";

        // Act
        Treatment treatment = new Treatment(treatmentId, userId, name, startDate, endDate, description, state);

        // Assert
        assertEquals("El ID del tratamiento debería coincidir", treatmentId, treatment.getTreatmentId());
        assertEquals("El ID del usuario debería coincidir", userId, treatment.getUserId());
        assertEquals("El nombre debería coincidir", name, treatment.getName());
        assertEquals("La fecha de inicio debería coincidir", startDate, treatment.getStartDate());
        assertEquals("La fecha de fin debería coincidir", endDate, treatment.getEndDate());
        assertEquals("La descripción debería coincidir", description, treatment.getDescription());
        assertEquals("El estado debería coincidir", state, treatment.getState());
    }

    @Test
    public void treatment_parameterizedConstructor_handlesNullValues() {
        // Arrange & Act
        // Prueba con todos los campos nulos
        Treatment treatment = new Treatment(null, null, null, null, null, null, null);

        // Assert
        assertNull("El treatmentId debería ser nulo", treatment.getTreatmentId());
        assertNull("El userId debería ser nulo", treatment.getUserId());
        assertNull("El nombre debería ser nulo", treatment.getName());
        assertNull("La fecha de inicio debería ser nula", treatment.getStartDate());
        assertNull("La fecha de fin debería ser nula", treatment.getEndDate());
        assertNull("La descripción debería ser nula", treatment.getDescription());
        assertNull("El estado debería ser nulo", treatment.getState());
    }

    @Test
    public void treatment_setTreatmentId_updatesCorrectly() {
        // Arrange
        Treatment treatment = new Treatment();
        String newId = "T002";

        // Act
        treatment.setTreatmentId(newId);

        // Assert
        assertEquals("El ID del tratamiento debería actualizarse", newId, treatment.getTreatmentId());
    }

    @Test
    public void treatment_setUserId_updatesCorrectly() {
        // Arrange
        Treatment treatment = new Treatment();
        String newId = "U002";

        // Act
        treatment.setUserId(newId);

        // Assert
        assertEquals("El ID del usuario debería actualizarse", newId, treatment.getUserId());
    }

    @Test
    public void treatment_setName_updatesCorrectly() {
        // Arrange
        Treatment treatment = new Treatment();
        String newName = "Terapia Ocupacional";

        // Act
        treatment.setName(newName);

        // Assert
        assertEquals("El nombre debería actualizarse", newName, treatment.getName());
    }

    @Test
    public void treatment_setStartDate_updatesCorrectly() {
        // Arrange
        Treatment treatment = new Treatment();
        Date newDate = new Date();

        // Act
        treatment.setStartDate(newDate);

        // Assert
        assertEquals("La fecha de inicio debería actualizarse", newDate, treatment.getStartDate());
    }

    @Test
    public void treatment_setEndDate_updatesCorrectly() {
        // Arrange
        Treatment treatment = new Treatment();
        Date newDate = new Date();

        // Act
        treatment.setEndDate(newDate);

        // Assert
        assertEquals("La fecha de fin debería actualizarse", newDate, treatment.getEndDate());
    }

    @Test
    public void treatment_setDescription_updatesCorrectly() {
        // Arrange
        Treatment treatment = new Treatment();
        String newDescription = "Nueva descripción del tratamiento.";

        // Act
        treatment.setDescription(newDescription);

        // Assert
        assertEquals("La descripción debería actualizarse", newDescription, treatment.getDescription());
    }

    @Test
    public void treatment_setState_updatesCorrectly() {
        // Arrange
        Treatment treatment = new Treatment();
        String newState = "Completado";

        // Act
        treatment.setState(newState);

        // Assert
        assertEquals("El estado debería actualizarse", newState, treatment.getState());
    }

    @Test
    public void treatment_dateObjects_areIndependentCopies() {
        // Arrange
        Date originalStartDate = new Date(1672531200000L);
        Date originalEndDate = new Date(1675209600000L);
        Treatment treatment = new Treatment("T003", "U003", "Dieta", originalStartDate, originalEndDate, "Plan alimenticio", "Activo");

        // Act
        // Modificar las fechas originales después de pasarlas al constructor
        originalStartDate.setTime(0L); // Cambiar a 01/01/1970
        originalEndDate.setTime(0L);

        // Assert
        // Las fechas dentro del objeto Treatment NO deberían haber cambiado
        // Ya que Date es mutable, lo ideal es que el constructor o los setters creen nuevas instancias (copias)
        // Pero tu modelo actual no hace una copia defensiva.
        // Por lo tanto, esta prueba verificará el comportamiento actual (que es que se mantienen las referencias)
        // y si quisieras un comportamiento más seguro, deberías modificar el modelo.
        assertEquals("La fecha de inicio NO debería cambiar si se modifica la original después de la asignación", new Date(1672531200000L), treatment.getStartDate());
        assertEquals("La fecha de fin NO debería cambiar si se modifica la original después de la asignación", new Date(1675209600000L), treatment.getEndDate());
        // Nota: Si quisieras que el modelo fuera inmutable en cuanto a las fechas, el constructor y los setters
        // deberían hacer una copia defensiva:
        // this.startDate = (startDate != null) ? new Date(startDate.getTime()) : null;
        // Si haces ese cambio, esta prueba pasaría porque las fechas serían copias independientes.
    }
}