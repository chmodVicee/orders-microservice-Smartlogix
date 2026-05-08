package com.SmartLogix.Client;

import com.SmartLogix.Model.Inventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryClientTest {

    @Mock
    private InventoryClient inventoryClient;

    @Test
    void testGetTotalStock() {
        Inventory inventory = new Inventory();
        inventory.setStock(50);
        inventory.setProductoCodigo("P1");

        when(inventoryClient.getTotalStock("P1")).thenReturn(inventory);

        Inventory result = inventoryClient.getTotalStock("P1");

        assertEquals(50, result.getStock());
        assertEquals("P1", result.getProductoCodigo());
    }
}
