package com.SmartLogix.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryTest {

    @Test
    void testInventoryGettersAndSetters() {
        Inventory inventory = new Inventory();
        inventory.setProductoCodigo("P1");
        inventory.setStock(100);
        inventory.setAlmacenCodigo("A1");

        assertEquals("P1", inventory.getProductoCodigo());
        assertEquals(100, inventory.getStock());
        assertEquals("A1", inventory.getAlmacenCodigo());
    }
}
