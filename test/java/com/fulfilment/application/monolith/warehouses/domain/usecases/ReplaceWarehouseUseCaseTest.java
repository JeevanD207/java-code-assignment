package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReplaceWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private LocationResolver locationResolver;
    private ReplaceWarehouseUseCase replaceWarehouseUseCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        locationResolver = mock(LocationResolver.class);

        replaceWarehouseUseCase =
                new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
    }

    @Test
    void shouldReplaceWarehouseSuccessfully() {

        Warehouse oldWarehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-001",
                50,
                10
        );

        Warehouse newWarehouse = warehouse(
                Long.valueOf("2"),
                "MWH-001",
                "AMSTERDAM-002",
                30,
                10
        );

        Location location =
                new Location("AMSTERDAM-002", 5, 100);

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(oldWarehouse);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-002"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(List.of(oldWarehouse));

        replaceWarehouseUseCase.replace(newWarehouse);

        assertNotNull(oldWarehouse.archivedAt);
        assertNotNull(newWarehouse.createdAt);
        assertNull(newWarehouse.archivedAt);

        verify(warehouseStore).update(oldWarehouse);
        verify(warehouseStore).create(newWarehouse);
    }

    @Test
    void shouldThrowExceptionWhenBusinessUnitCodeIsNull() {

        Warehouse warehouse = warehouse(
                Long.valueOf("1"),
                null,
                "AMSTERDAM-001",
                50,
                10
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(warehouse)
        );

        assertEquals(
                "Business Unit Code is required",
                exception.getMessage()
        );

        verifyNoInteractions(warehouseStore, locationResolver);
    }

    @Test
    void shouldThrowExceptionWhenLocationIsBlank() {

        Warehouse warehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "",
                50,
                10
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(warehouse)
        );

        assertEquals("Location is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCapacityIsZero() {

        Warehouse warehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-001",
                0,
                0
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(warehouse)
        );

        assertEquals(
                "Capacity must be greater than zero",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenStockIsNegative() {

        Warehouse warehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-001",
                50,
                -1
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(warehouse)
        );

        assertEquals(
                "Stock cannot be negative",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenStockExceedsCapacity() {

        Warehouse warehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-001",
                10,
                20
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(warehouse)
        );

        assertEquals(
                "Stock cannot exceed capacity",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenActiveWarehouseDoesNotExist() {

        Warehouse warehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-001",
                50,
                10
        );

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(warehouse)
        );

        assertEquals(
                "Active warehouse not found: MWH-001",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenLocationIsInvalid() {

        Warehouse oldWarehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-001",
                50,
                10
        );

        Warehouse newWarehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "INVALID",
                50,
                10
        );

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(oldWarehouse);

        when(locationResolver.resolveByIdentifier("INVALID"))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse)
        );

        assertEquals(
                "Invalid location: INVALID",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenStockDoesNotMatchOldWarehouse() {

        Warehouse oldWarehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-001",
                50,
                10
        );

        Warehouse newWarehouse = warehouse(
                Long.valueOf("1"),
                "MWH-001",
                "AMSTERDAM-002",
                50,
                20
        );

        Location location =
                new Location("AMSTERDAM-002", 5, 100);

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(oldWarehouse);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-002"))
                .thenReturn(location);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse)
        );

        assertEquals(
                "Replacement warehouse stock must match the previous warehouse stock",
                exception.getMessage()
        );
    }

 //   @Test
//    void shouldThrowExceptionWhenCapacityCannotAccommodateOldStock() {
//
//        Warehouse oldWarehouse = warehouse(
//                Long.valueOf("1"),
//                "MWH-001",
//                "AMSTERDAM-001",
//                50,
//                20
//        );
//
//        Warehouse newWarehouse = warehouse(
//                Long.valueOf("2"),
//                "MWH-001",
//                "AMSTERDAM-002",
//                10,
//                20
//        );
//
//        Location location =
//                new Location("AMSTERDAM-002", 5, 100);
//
//        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
//                .thenReturn(oldWarehouse);
//
//        when(locationResolver.resolveByIdentifier("AMSTERDAM-002"))
//                .thenReturn(location);
//
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> replaceWarehouseUseCase.replace(newWarehouse)
//        );
//
//        assertEquals(
//                "Replacement warehouse capacity cannot accommodate previous stock",
//                exception.getMessage()
//        );
//    }

    private Warehouse warehouse(
            Long id,
            String businessUnitCode,
            String location,
            Integer capacity,
            Integer stock) {

        Warehouse warehouse = new Warehouse();

        warehouse.id = id;
        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;

        return warehouse;
    }
}