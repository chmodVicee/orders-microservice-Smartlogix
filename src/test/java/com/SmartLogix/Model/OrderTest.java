package com.SmartLogix.Model;

import com.SmartLogix.Enum.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    @Test
    void testOrderBuilderAndGetters() {
        Order order = Order.builder()
                .id(1L)
                .numeroPedido("123")
                .productoCodigo("P1")
                .cantidad(10)
                .almacenCodigo("A1")
                .estado(OrderStatus.PENDIENTE)
                .build();

        assertEquals(1L, order.getId());
        assertEquals("123", order.getNumeroPedido());
        assertEquals("P1", order.getProductoCodigo());
        assertEquals(10, order.getCantidad());
        assertEquals("A1", order.getAlmacenCodigo());
        assertEquals(OrderStatus.PENDIENTE, order.getEstado());
    }
}
