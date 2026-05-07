package com.SmartLogix.Client;

import com.SmartLogix.Model.Inventory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", url = "http://localhost:8082/api/inventory")
public interface InventoryClient {

    // CAMBIA ESTO: Ahora apunta a /total/{productoCodigo}
    @GetMapping("/total/{productoCodigo}")
    Inventory getTotalStock(@PathVariable("productoCodigo") String productoCodigo);
}