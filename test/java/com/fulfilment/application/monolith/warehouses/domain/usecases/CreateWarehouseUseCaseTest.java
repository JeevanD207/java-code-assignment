package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;

    @Mock
    private LocationResolver locationResolver;

    private CreateWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateWarehouseUseCase(
                warehouseStore,
                locationResolver
        );
    }

    // ---------------------------------------------------------
    // SUCCESS CASE
    // ---------------------------------------------------------

    @Test
    void shouldCreateWarehouseSuccessfully() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "AMSTERDAM-001",
                20,
                10
        );

        Location location = new Location(
                "AMSTERDAM-001",
                5,
                100
        );

        when(warehouseStore.findByBusinessUnitCode("MWH-100"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(List.of());

        useCase.create(warehouse);

        ArgumentCaptor<Warehouse> captor =
                ArgumentCaptor.forClass(Warehouse.class);

        verify(warehouseStore).create(captor.capture());

        Warehouse createdWarehouse = captor.getValue();

        assertEquals("MWH-100",
                createdWarehouse.businessUnitCode);

        assertEquals("AMSTERDAM-001",
                createdWarehouse.location);

        assertEquals(20,
                createdWarehouse.capacity);

        assertEquals(10,
                createdWarehouse.stock);

        assertNotNull(createdWarehouse.createdAt);

        assertNull(createdWarehouse.archivedAt);
    }


    // ---------------------------------------------------------
    // DUPLICATE BUSINESS UNIT CODE
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenBusinessUnitCodeAlreadyExists() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "AMSTERDAM-001",
                20,
                10
        );

        Warehouse existingWarehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                10,
                5
        );

        when(warehouseStore.findByBusinessUnitCode("MWH-100"))
                .thenReturn(existingWarehouse);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Business Unit Code already exists: MWH-100",
                exception.getMessage()
        );

        verify(warehouseStore, never()).create(any());
    }


    // ---------------------------------------------------------
    // INVALID LOCATION
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenLocationIsInvalid() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "INVALID-LOCATION",
                20,
                10
        );

        when(warehouseStore.findByBusinessUnitCode("MWH-100"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("INVALID-LOCATION"))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Invalid location: INVALID-LOCATION",
                exception.getMessage()
        );

        verify(warehouseStore, never()).create(any());
    }


    // ---------------------------------------------------------
    // MAXIMUM NUMBER OF WAREHOUSES
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenMaximumWarehouseCountReached() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                10,
                5
        );

        Location location = new Location(
                "ZWOLLE-001",
                1,
                100
        );

        Warehouse existingWarehouse = warehouse(
                "MWH-001",
                "ZWOLLE-001",
                20,
                10
        );

        when(warehouseStore.findByBusinessUnitCode("MWH-100"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(List.of(existingWarehouse));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Maximum number of warehouses reached for location: ZWOLLE-001",
                exception.getMessage()
        );

        verify(warehouseStore, never()).create(any());
    }


    // ---------------------------------------------------------
    // MAXIMUM CAPACITY EXCEEDED
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenMaximumCapacityExceeded() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                20,
                10
        );

        Location location = new Location(
                "ZWOLLE-001",
                5,
                40
        );

        Warehouse existingWarehouse = warehouse(
                "MWH-001",
                "ZWOLLE-001",
                30,
                10
        );

        when(warehouseStore.findByBusinessUnitCode("MWH-100"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(List.of(existingWarehouse));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Maximum capacity exceeded for location: ZWOLLE-001",
                exception.getMessage()
        );

        verify(warehouseStore, never()).create(any());
    }


    // ---------------------------------------------------------
    // BUSINESS UNIT CODE VALIDATION
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenBusinessUnitCodeIsNull() {

        Warehouse warehouse = warehouse(
                null,
                "ZWOLLE-001",
                20,
                10
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Business Unit Code is required",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenBusinessUnitCodeIsBlank() {

        Warehouse warehouse = warehouse(
                "   ",
                "ZWOLLE-001",
                20,
                10
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Business Unit Code is required",
                exception.getMessage()
        );
    }


    // ---------------------------------------------------------
    // LOCATION VALIDATION
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenLocationIsNull() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                null,
                20,
                10
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Location is required",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenLocationIsBlank() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "   ",
                20,
                10
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Location is required",
                exception.getMessage()
        );
    }


    // ---------------------------------------------------------
    // CAPACITY VALIDATION
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenCapacityIsNull() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                null,
                10
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Capacity must be greater than zero",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenCapacityIsZero() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                0,
                0
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Capacity must be greater than zero",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenCapacityIsNegative() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                -10,
                0
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Capacity must be greater than zero",
                exception.getMessage()
        );
    }


    // ---------------------------------------------------------
    // STOCK VALIDATION
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenStockIsNull() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                20,
                null
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Stock cannot be negative",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenStockIsNegative() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                20,
                -1
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Stock cannot be negative",
                exception.getMessage()
        );
    }


    // ---------------------------------------------------------
    // STOCK GREATER THAN CAPACITY
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenStockExceedsCapacity() {

        Warehouse warehouse = warehouse(
                "MWH-100",
                "ZWOLLE-001",
                20,
                25
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.create(warehouse)
                );

        assertEquals(
                "Stock cannot exceed capacity",
                exception.getMessage()
        );
    }


    // ---------------------------------------------------------
    // HELPER METHOD
    // ---------------------------------------------------------

    private Warehouse warehouse(
            String businessUnitCode,
            String location,
            Integer capacity,
            Integer stock) {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;

        return warehouse;
    }
}