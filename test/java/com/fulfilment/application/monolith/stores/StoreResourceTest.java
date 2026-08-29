package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.mock.PanacheMock;
import jakarta.enterprise.event.Event;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.mockito.MockedStatic;
import org.mockito.Mockito;


class StoreResourceTest {

    private StoreResource resource;

    private Event<StoreChanged> storeChangedEvent;

    @BeforeEach
    void setUp() {
        resource = new StoreResource();
        storeChangedEvent = org.mockito.Mockito.mock(Event.class);
        resource.storeChangedEvent = storeChangedEvent;
    }


    // =====================================================
    // GET ALL
    // =====================================================

    @Test
    void shouldGetAllStores() {

        Store store1 = new Store();
        store1.name = "Store A";
        store1.quantityProductsInStock = 10;

        Store store2 = new Store();
        store2.name = "Store B";
        store2.quantityProductsInStock = 20;

        try (MockedStatic<Store> mockedStore = Mockito.mockStatic(Store.class)) {

            mockedStore.when(() -> Store.listAll(Sort.by("name")))
                    .thenReturn(List.of(store1, store2));

            List<Store> result = resource.get();

            assertEquals(2, result.size());
            assertEquals("Store A", result.get(0).name);
            assertEquals("Store B", result.get(1).name);
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoStoresExist() {

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(List.of())
                .when(Store.class)
                .listAll(any(Sort.class));

        List<Store> result = resource.get();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =====================================================
    // GET SINGLE
    // =====================================================

    @Test
    void shouldGetStoreById() {

        Store store = new Store();
        store.name = "Store A";
        store.quantityProductsInStock = 10;

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(store)
                .when(Store.class)
                .findById(1L);

        Store result = resource.getSingle(1L);

        assertNotNull(result);
        assertEquals("Store A", result.name);
        assertEquals(10, result.quantityProductsInStock);
    }

    @Test
    void shouldThrow404WhenStoreDoesNotExist() {

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(null)
                .when(Store.class)
                .findById(999L);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.getSingle(999L)
        );

        assertEquals(404, exception.getResponse().getStatus());

        assertEquals(
                "Store with id of 999 does not exist.",
                exception.getMessage()
        );
    }

    // =====================================================
    // CREATE
    // =====================================================

    @Test
    void shouldThrow422WhenIdIsProvidedDuringCreate() {

        Store store = new Store();
        store.id = 100L;
        store.name = "Store A";

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.create(store)
        );

        assertEquals(422, exception.getResponse().getStatus());

        assertEquals(
                "Id was invalidly set on request.",
                exception.getMessage()
        );
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Test
    void shouldUpdateStoreSuccessfully() {

        Store existingStore = new Store();
        existingStore.name = "Old Store";
        existingStore.quantityProductsInStock = 10;

        Store updatedStore = new Store();
        updatedStore.name = "New Store";
        updatedStore.quantityProductsInStock = 50;

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(existingStore)
                .when(Store.class)
                .findById(1L);

        Store result = resource.update(1L, updatedStore);

        assertEquals("New Store", result.name);
        assertEquals(50, result.quantityProductsInStock);

        verify(storeChangedEvent)
                .fire(any(StoreChanged.class));
    }

    @Test
    void shouldThrow422WhenStoreNameIsNullDuringUpdate() {

        Store updatedStore = new Store();
        updatedStore.name = null;

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.update(1L, updatedStore)
        );

        assertEquals(422, exception.getResponse().getStatus());

        assertEquals(
                "Store Name was not set on request.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrow404WhenUpdatingNonExistingStore() {

        Store updatedStore = new Store();
        updatedStore.name = "Updated Store";

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(null)
                .when(Store.class)
                .findById(999L);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.update(999L, updatedStore)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    // =====================================================
    // PATCH
    // =====================================================

    @Test
    void shouldPatchStoreSuccessfully() {

        Store existingStore = new Store();
        existingStore.name = "Old Store";
        existingStore.quantityProductsInStock = 10;

        Store updatedStore = new Store();
        updatedStore.name = "New Store";
        updatedStore.quantityProductsInStock = 30;

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(existingStore)
                .when(Store.class)
                .findById(1L);

        Store result = resource.patch(1L, updatedStore);

        assertEquals("New Store", result.name);
        assertEquals(30, result.quantityProductsInStock);

        verify(storeChangedEvent)
                .fire(any(StoreChanged.class));
    }

    @Test
    void shouldThrow422WhenStoreNameIsNullDuringPatch() {

        Store updatedStore = new Store();
        updatedStore.name = null;

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.patch(1L, updatedStore)
        );

        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    void shouldThrow404WhenPatchingNonExistingStore() {

        Store updatedStore = new Store();
        updatedStore.name = "Updated Store";

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(null)
                .when(Store.class)
                .findById(999L);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.patch(999L, updatedStore)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    // =====================================================
    // DELETE
    // =====================================================

    @Test
    void shouldThrow404WhenDeletingNonExistingStore() {

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(null)
                .when(Store.class)
                .findById(999L);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.delete(999L)
        );

        assertEquals(404, exception.getResponse().getStatus());

        assertEquals(
                "Store with id of 999 does not exist.",
                exception.getMessage()
        );
    }

    // =====================================================
    // ERROR MAPPER
    // =====================================================

    @Test
    void shouldMapWebApplicationException() {

        StoreResource.ErrorMapper mapper =
                new StoreResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        WebApplicationException exception =
                new WebApplicationException("Store not found", 404);

        Response response = mapper.toResponse(exception);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void shouldMapGeneralExceptionTo500() {

        StoreResource.ErrorMapper mapper =
                new StoreResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        RuntimeException exception =
                new RuntimeException("Something went wrong");

        Response response = mapper.toResponse(exception);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void shouldPatchStoreWhenExistingFieldsAreEmpty() {

        Store existingStore = new Store();
        existingStore.name = null;
        existingStore.quantityProductsInStock = 0;

        Store updatedStore = new Store();
        updatedStore.name = "New Store";
        updatedStore.quantityProductsInStock = 50;

        PanacheMock.mock(Store.class);

        PanacheMock.doReturn(existingStore)
                .when(Store.class)
                .findById(1L);

        Store result = resource.patch(1L, updatedStore);

        // Since existing name is null, patch should not update it
        assertNull(result.name);

        // Since existing quantity is 0, patch should not update it
        assertEquals(0, result.quantityProductsInStock);

        verify(storeChangedEvent)
                .fire(any(StoreChanged.class));
    }
}