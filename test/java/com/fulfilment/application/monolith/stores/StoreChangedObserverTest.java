package com.fulfilment.application.monolith.stores;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoreChangedObserverTest {

    private StoreChangedObserver observer;
    private LegacyStoreManagerGateway legacyStoreManagerGateway;

    @BeforeEach
    void setUp() {
        legacyStoreManagerGateway = mock(LegacyStoreManagerGateway.class);

        observer = new StoreChangedObserver();
        observer.legacyStoreManagerGateway = legacyStoreManagerGateway;
    }

    @Test
    void shouldCreateStoreWhenChangeTypeIsCreate() {

        Store store = new Store();
        store.name = "Test Store";
        store.quantityProductsInStock = 10;

        StoreChanged event =
                new StoreChanged(store, StoreChanged.ChangeType.CREATE);

        observer.onStoreChanged(event);

        verify(legacyStoreManagerGateway)
                .createStoreOnLegacySystem(store);
    }

    @Test
    void shouldUpdateStoreWhenChangeTypeIsNotCreate() {

        Store store = new Store();
        store.name = "Test Store";
        store.quantityProductsInStock = 20;

        StoreChanged event =
                new StoreChanged(store, StoreChanged.ChangeType.UPDATE);

        observer.onStoreChanged(event);

        verify(legacyStoreManagerGateway)
                .updateStoreOnLegacySystem(store);
    }
}