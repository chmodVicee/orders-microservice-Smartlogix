package com.SmartLogix.Dto;

import com.SmartLogix.Model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    private Order orden;
    private Object usuario;
}