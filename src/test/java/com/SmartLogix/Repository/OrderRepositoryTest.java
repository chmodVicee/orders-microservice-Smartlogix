package com.SmartLogix.Repository;

import com.SmartLogix.Enum.OrderStatus;
import com.SmartLogix.Model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testSaveAndFindAll() {
        Order order = Order.builder()
                .numeroPedido("123")
                .productoCodigo("P1")
                .cantidad(10)
                .estado(OrderStatus.PENDIENTE)
                .build();

        Order savedOrder = orderRepository.save(order);
        assertNotNull(savedOrder.getId());

        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());
        assertEquals("123", orders.get(0).getNumeroPedido());
    }
}
