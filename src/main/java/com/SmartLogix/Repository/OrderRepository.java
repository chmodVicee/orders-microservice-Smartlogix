package com.SmartLogix.Repository;

import com.SmartLogix.Enum.OrderStatus;
import com.SmartLogix.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByInventarioSincronizadoFalse();
    List<Order> findByEstado(OrderStatus estado);
}