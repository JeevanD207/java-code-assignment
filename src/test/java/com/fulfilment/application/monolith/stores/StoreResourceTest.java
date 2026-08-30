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
}