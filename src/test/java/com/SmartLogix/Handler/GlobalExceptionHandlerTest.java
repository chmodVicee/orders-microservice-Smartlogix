package com.SmartLogix.Handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Test message");
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error en la solicitud", response.getBody().get("error"));
        assertEquals("Test message", response.getBody().get("mensaje"));
    }
}
