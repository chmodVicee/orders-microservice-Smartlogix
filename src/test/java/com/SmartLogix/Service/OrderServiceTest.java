package com.SmartLogix.Service;

import com.SmartLogix.Client.InventoryClient;
import com.SmartLogix.Enum.OrderStatus;
import com.SmartLogix.Model.Inventory;
import com.SmartLogix.Model.Order;
import com.SmartLogix.Repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPlaceOrder_Success() {
        Order request = new Order();
        request.setProductoCodigo("P1");
        request.setCantidad(5);

        Inventory inventory = new Inventory();
        inventory.setStock(10);

        when(inventoryClient.getTotalStock("P1")).thenReturn(inventory);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.placeOrder(request);

        assertNotNull(result.getNumeroPedido());
        assertEquals(OrderStatus.PENDIENTE, result.getEstado());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_InsufficientStock() {
        Order request = new Order();
        request.setProductoCodigo("P1");
        request.setCantidad(15);

        Inventory inventory = new Inventory();
        inventory.setStock(10);

        when(inventoryClient.getTotalStock("P1")).thenReturn(inventory);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(request);
        });

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetAllOrders() {
        List<Order> orders = Arrays.asList(new Order(), new Order());
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testGetOrderByNumero_Found() {
        Order order = new Order();
        order.setNumeroPedido("123");
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));

        Order result = orderService.getOrderByNumero("123");

        assertEquals("123", result.getNumeroPedido());
    }

    @Test
    void testGetOrderByNumero_NotFound() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList());

        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderByNumero("123");
        });
    }

    @Test
    void testUpdateOrderStatus() {
        Order order = new Order();
        order.setNumeroPedido("123");
        order.setEstado(OrderStatus.PENDIENTE);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.updateOrderStatus("123", OrderStatus.COMPLETADO);

        assertEquals(OrderStatus.COMPLETADO, result.getEstado());
        verify(orderRepository, times(1)).save(order);
    }
}
