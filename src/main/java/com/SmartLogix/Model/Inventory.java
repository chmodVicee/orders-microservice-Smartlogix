package com.SmartLogix.Model;
import lombok.Data;

@Data
public class Inventory {
    private String productoCodigo;
    private Integer stock;
    private String almacenCodigo;
}