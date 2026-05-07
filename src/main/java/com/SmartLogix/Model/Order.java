package com.SmartLogix.Model;

import com.SmartLogix.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    private OrderStatus estado;
}