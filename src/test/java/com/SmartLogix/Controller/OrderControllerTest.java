package com.SmartLogix.Controller;

import com.SmartLogix.Model.Order;
import com.SmartLogix.Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder() {
        Order requestOrder = new Order();
        Order createdOrder = new Order();
        createdOrder.setId(1L);

        when(orderService.placeOrder(any(Order.class))).thenReturn(createdOrder);

        ResponseEntity<Order> response = orderController.createOrder(requestOrder);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdOrder, response.getBody());
        verify(orderService, times(1)).placeOrder(requestOrder);
    }

    @Test
    void testGetAll() {
        List<Order> orders = Arrays.asList(new Order(), new Order());
        when(orderService.getAllOrders()).thenReturn(orders);

        ResponseEntity<List<Order>> response = orderController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orders, response.getBody());
        verify(orderService, times(1)).getAllOrders();
    }
}
