package com.example.saludaldia.data.model;


import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryTest {

    @Test
    public void history_defaultConstructor_initializesEventsIdsList() {
        // Arrange
        History history = new History();

        // Assert
        // Verifica que la lista de eventos no es nula y está vacía por defecto
        assertNotNull("La lista de eventos no debería ser nula", history.getEventsIds());
        assertTrue("La lista de eventos debería estar vacía", history.getEventsIds().isEmpty());
    }

    @Test
    public void history_parameterizedConstructor_setsAllFieldsCorrectly() {
        // Arrange
        String historyId = "hist123";
        String userId = "user456";
        List<String> eventIds = Arrays.asList("event001", "event002");

        // Act
        History history = new History(historyId, userId, eventIds);

        // Assert
        assertEquals("El ID del historial debería coincidir", historyId, history.getHistoryId());
        assertEquals("El ID del usuario debería coincidir", userId, history.getUserId());
        assertEquals("La lista de eventos debería coincidir", eventIds, history.getEventsIds());
    }

    @Test
    public void history_parameterizedConstructor_handlesNullEventsIds() {
        // Arrange
        String historyId = "hist789";
        String userId = "user101";

        // Act
        // Prueba con una lista de eventos nula
        History history = new History(historyId, userId, null);

        // Assert
        assertEquals("El ID del historial debería coincidir", historyId, history.getHistoryId());
        assertEquals("El ID del usuario debería coincidir", userId, history.getUserId());
        // La lista de eventos no debería ser nula, sino una lista vacía
        assertNotNull("La lista de eventos no debería ser nula cuando se pasa null", history.getEventsIds());
        assertTrue("La lista de eventos debería estar vacía cuando se pasa null", history.getEventsIds().isEmpty());
    }

    @Test
    public void history_setHistoryId_updatesCorrectly() {
        // Arrange
        History history = new History();
        String newHistoryId = "newHistId";

        // Act
        history.setHistoryId(newHistoryId);

        // Assert
        assertEquals("El ID del historial debería actualizarse", newHistoryId, history.getHistoryId());
    }

    @Test
    public void history_setUserId_updatesCorrectly() {
        // Arrange
        History history = new History();
        String newUserId = "newUserId";

        // Act
        history.setUserId(newUserId);

        // Assert
        assertEquals("El ID del usuario debería actualizarse", newUserId, history.getUserId());
    }

    @Test
    public void history_setEventsIds_updatesCorrectly() {
        // Arrange
        History history = new History();
        List<String> newEventIds = Arrays.asList("evt003", "evt004");

        // Act
        history.setEventsIds(newEventIds);

        // Assert
        assertEquals("La lista de eventos debería actualizarse", newEventIds, history.getEventsIds());
    }

    @Test
    public void history_setEventsIds_handlesNullList() {
        // Arrange
        History history = new History();
        List<String> initialEvents = new ArrayList<>();
        initialEvents.add("initialEvent");
        history.setEventsIds(initialEvents);

        // Act
        history.setEventsIds(null); // Establece la lista a null

        // Assert
        // Verifica que la lista se puede establecer a null si es lo que se espera (tu implementación actual lo permite)
        assertEquals("La lista de eventos debería ser nula si se establece a null", null, history.getEventsIds());
    }
}