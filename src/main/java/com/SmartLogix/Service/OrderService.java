package com.SmartLogix.Service;

import com.SmartLogix.Dto.OrderResponseDTO;
import com.SmartLogix.Model.Order;
import com.SmartLogix.Repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
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

    private static final String INVENTORY_BASE = "http://localhost:8082/api/inventory";

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

        try {
            String getStockUrl = INVENTORY_BASE + "/" + order.getProductoCodigo() + "/" + order.getAlmacenCodigo();
            ResponseEntity<Map> stockResponse = restTemplate.exchange(
                    getStockUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map stockActualResponse = stockResponse.getBody();

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

            try {
                restTemplate.exchange(INVENTORY_BASE + "/update",
                        HttpMethod.POST, new HttpEntity<>(inventoryRequest, headers), Object.class);
                savedOrder.setInventarioSincronizado(true);
                orderRepository.save(savedOrder);
                log.info("Orden {} creada y stock sincronizado correctamente.", savedOrder.getId());
            } catch (Exception e) {
                log.warn("Orden {} guardada pero no se pudo actualizar el stock. Se reintentará automáticamente.", savedOrder.getId());
            }

            return savedOrder;

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Inventory MS no disponible. Orden '{}' guardada y en cola para sincronizacion.", order.getNumeroPedido());
            order.setInventarioSincronizado(false);
            return orderRepository.save(order);
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void sincronizarPendientes() {
        List<Order> pendientes = orderRepository.findByInventarioSincronizadoFalse();
        if (pendientes.isEmpty()) return;

        log.info("Sincronizacion automatica: {} orden(es) pendiente(s) de descontar en inventory.", pendientes.size());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (Order orden : pendientes) {
            try {
                // Consultar stock actual
                String getUrl = INVENTORY_BASE + "/" + orden.getProductoCodigo() + "/" + orden.getAlmacenCodigo();
                ResponseEntity<Map> stockResponse = restTemplate.exchange(
                        getUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                Map stockData = stockResponse.getBody();

                int stockActual = ((Number) stockData.get("stock")).intValue();
                int nuevoStock = stockActual - orden.getCantidad();

                if (nuevoStock < 0) {
                    log.warn("Sin stock para sincronizar orden {} ({} en {}). Stock: {}, Pedido: {}. Se reintentara.",
                            orden.getId(), orden.getProductoCodigo(), orden.getAlmacenCodigo(),
                            stockActual, orden.getCantidad());
                    continue;
                }

                // Descontar stock
                Map<String, Object> updateBody = new HashMap<>();
                updateBody.put("productoCodigo", orden.getProductoCodigo());
                updateBody.put("almacenCodigo", orden.getAlmacenCodigo());
                updateBody.put("stock", nuevoStock);

                restTemplate.exchange(INVENTORY_BASE + "/update",
                        HttpMethod.POST, new HttpEntity<>(updateBody, headers), Object.class);

                orden.setInventarioSincronizado(true);
                orderRepository.save(orden);
                log.info("Orden {} sincronizada: stock de '{}' en '{}' reducido de {} a {}.",
                        orden.getId(), orden.getProductoCodigo(), orden.getAlmacenCodigo(),
                        stockActual, nuevoStock);

            } catch (Exception e) {
                log.debug("Inventory MS aun no disponible para sincronizar orden {}: {}", orden.getId(), e.getMessage());
            }
        }
    }
}
