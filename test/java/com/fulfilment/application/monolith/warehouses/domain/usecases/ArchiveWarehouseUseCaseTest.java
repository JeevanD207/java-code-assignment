package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArchiveWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore);
    }

    @Test
    void shouldArchiveWarehouseSuccessfully() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH-001";

        archiveWarehouseUseCase.archive(warehouse);

        assertNotNull(warehouse.archivedAt);
        verify(warehouseStore).update(warehouse);
    }

    @Test
    void shouldThrowExceptionWhenWarehouseIsNull() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> archiveWarehouseUseCase.archive(null)
        );

        assertEquals("Warehouse is required", exception.getMessage());

        verifyNoInteractions(warehouseStore);
    }
}