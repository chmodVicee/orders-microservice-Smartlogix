package com.SmartLogix.Controller;

import com.SmartLogix.Dto.OrderResponseDTO;
import com.SmartLogix.Enum.OrderStatus;
import com.SmartLogix.Model.Order;
import com.SmartLogix.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place-order")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order nuevaOrden = orderService.placeOrder(order);
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDTO>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrdersWithUsers());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus estado) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, estado));
    }
}