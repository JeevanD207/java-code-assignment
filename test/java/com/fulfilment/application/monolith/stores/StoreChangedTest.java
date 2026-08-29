package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StoreChangedTest {

    @Test
    void shouldCreateStoreChangedEvent() {

        Store store = new Store();
        store.name = "Test Store";
        store.quantityProductsInStock = 10;

        StoreChanged event =
                new StoreChanged(store, StoreChanged.ChangeType.CREATE);

        assertEquals(store, event.store);
        assertEquals(StoreChanged.ChangeType.CREATE, event.changeType);
    }

    @Test
    void shouldCreateUpdateStoreChangedEvent() {

        Store store = new Store();
        store.name = "Updated Store";
        store.quantityProductsInStock = 20;

        StoreChanged event =
                new StoreChanged(store, StoreChanged.ChangeType.UPDATE);

        assertEquals(store, event.store);
        assertEquals(StoreChanged.ChangeType.UPDATE, event.changeType);
    }
}