package com.SmartLogix.Service;

import com.SmartLogix.Dto.OrderResponseDTO;
import com.SmartLogix.Model.Order;
import com.SmartLogix.Repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final HttpServletRequest request;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<OrderResponseDTO> getAllOrdersWithUsers() {
        List<Order> listaDeOrdenes = orderRepository.findAll();
        List<OrderResponseDTO> responseList = new ArrayList<>();

        String bearerToken = request.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            headers.set("Authorization", bearerToken);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Object userData = null;
        try {
            String usersUrl = "http://localhost:8083/api/users/profile";
            ResponseEntity<Object> userResponse = restTemplate.exchange(usersUrl, HttpMethod.GET, entity, Object.class);
            userData = userResponse.getBody();
        } catch (Exception e) {
            log.error("No se pudieron obtener los datos del usuario: {}", e.getMessage());
            userData = "Detalles del usuario no disponibles temporalmente";
        }

        for (Order unaOrden : listaDeOrdenes) {
            responseList.add(new OrderResponseDTO(unaOrden, userData));
        }

        return responseList;
    }

    public Order placeOrder(Order order) {
        String bearerToken = request.getHeader("Authorization");
        String usernameActual = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        order.setUsername(usernameActual);
        order.setFecha(java.time.LocalDateTime.now());
        if (order.getNumeroPedido() == null || order.getNumeroPedido().isEmpty()) {
            order.setNumeroPedido("PED-" + System.currentTimeMillis());
        }
        if (order.getEstado() == null) {
            order.setEstado(com.SmartLogix.Enum.OrderStatus.PENDIENTE);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            headers.set("Authorization", bearerToken);
        }

        String getStockUrl = "http://localhost:8082/api/inventory/" + order.getProductoCodigo() + "/" + order.getAlmacenCodigo();
        Map stockActualResponse;
        try {
            ResponseEntity<Map> stockResponse = restTemplate.exchange(
                    getStockUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            stockActualResponse = stockResponse.getBody();
        } catch (Exception e) {
            log.error("No se pudo obtener el stock actual para {} en {}: {}",
                    order.getProductoCodigo(), order.getAlmacenCodigo(), e.getMessage());
            throw new IllegalStateException("No se pudo verificar el stock disponible para este producto/almacén");
        }

        int stockActual = ((Number) stockActualResponse.get("stock")).intValue();
        int nuevoStock = stockActual - order.getCantidad();

        if (nuevoStock < 0) {
            throw new IllegalStateException(
                    "Stock insuficiente. Disponible: " + stockActual + ", solicitado: " + order.getCantidad());
        }

        Order savedOrder = orderRepository.save(order);

        Map<String, Object> inventoryRequest = new HashMap<>();
        inventoryRequest.put("productoCodigo", savedOrder.getProductoCodigo());
        inventoryRequest.put("almacenCodigo", savedOrder.getAlmacenCodigo());
        inventoryRequest.put("stock", nuevoStock);
        HttpEntity<Map<String, Object>> inventoryEntity = new HttpEntity<>(inventoryRequest, headers);
        try {
            String inventoryUrl = "http://localhost:8082/api/inventory/update";
            restTemplate.exchange(inventoryUrl, HttpMethod.POST, inventoryEntity, Object.class);
            log.info("Inventario actualizado con éxito mediante /update para la orden: {}", savedOrder.getId());
        } catch (Exception e) {
            log.error("Error al intentar actualizar el stock en el Microservicio de Inventario: {}", e.getMessage());
        }
        return savedOrder;
    }
}