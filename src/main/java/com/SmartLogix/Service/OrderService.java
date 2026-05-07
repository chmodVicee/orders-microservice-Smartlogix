package com.SmartLogix.Service;

import com.SmartLogix.Client.InventoryClient;
import com.SmartLogix.Model.Order;
import com.SmartLogix.Enum.OrderStatus;
import com.SmartLogix.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public Order placeOrder(Order request) {
        var inventory = inventoryClient.getTotalStock(request.getProductoCodigo());

        if (inventory != null && inventory.getStock() >= request.getCantidad()) {
            Order order = Order.builder()
                    .numeroPedido(UUID.randomUUID().toString())
                    .productoCodigo(request.getProductoCodigo())
                    .cantidad(request.getCantidad())
                    .estado(OrderStatus.PENDIENTE)
                    .build();

            return orderRepository.save(order);
        } else {
            throw new RuntimeException("Stock insuficiente. Disponible total: " +
                    (inventory != null ? inventory.getStock() : 0));
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderByNumero (String numeroPedido) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getNumeroPedido().equals(numeroPedido))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("Pedido no encontrado: " +numeroPedido));
    }

    public Order updateOrderStatus (String numeroPedido, OrderStatus nuevoEstado) {
        Order order = getOrderByNumero(numeroPedido);
        order.setEstado(nuevoEstado);
        return orderRepository.save(order);
    }
}