package com.SmartLogix.Enum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderStatusTest {

    @Test
    void testEnumValues() {
        assertEquals("PENDIENTE", OrderStatus.PENDIENTE.name());
        assertEquals("PROCESADO", OrderStatus.PROCESADO.name());
        assertEquals("COMPLETADO", OrderStatus.COMPLETADO.name());
        assertEquals("CANCELADO", OrderStatus.CANCELADO.name());
    }
}
