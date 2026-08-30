package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

    private final LegacyStoreManagerGateway gateway =
            new LegacyStoreManagerGateway();

    @Test
    void shouldCreateStoreOnLegacySystem() {

        Store store = new Store();
        store.name = "TestStore";
        store.quantityProductsInStock = 10;

        assertDoesNotThrow(() ->
                gateway.createStoreOnLegacySystem(store)
        );
    }

    @Test
    void shouldUpdateStoreOnLegacySystem() {

        Store store = new Store();
        store.name = "UpdatedStore";
        store.quantityProductsInStock = 25;

        assertDoesNotThrow(() ->
                gateway.updateStoreOnLegacySystem(store)
        );
    }
}