package com.SmartLogix.Model;

import com.SmartLogix.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ordenes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroPedido;
    private String productoCodigo;
    private Integer cantidad;
    private String almacenCodigo;

    private String username;
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private OrderStatus estado;

    @Column(name = "inventario_sincronizado")
    private Boolean inventarioSincronizado = false;
}