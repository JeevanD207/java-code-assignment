package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WarehouseResourceImplTest {

    private WarehouseResourceImpl resource;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private CreateWarehouseOperation createWarehouseOperation;

    @Mock
    private ArchiveWarehouseOperation archiveWarehouseOperation;

    @Mock
    private ReplaceWarehouseOperation replaceWarehouseOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        resource = new WarehouseResourceImpl();

        resource.warehouseRepository = warehouseRepository;
        resource.createWarehouseOperation = createWarehouseOperation;
        resource.archiveWarehouseOperation = archiveWarehouseOperation;
        resource.replaceWarehouseOperation = replaceWarehouseOperation;
    }

    // =========================================================
    // LIST ALL
    // =========================================================

    @Test
    void shouldListAllWarehouses() {

        var warehouse1 = domainWarehouse(
                1L,
                "BU001",
                "Amsterdam",
                100,
                20
        );

        var warehouse2 = domainWarehouse(
                2L,
                "BU002",
                "Rotterdam",
                200,
                50
        );

        when(warehouseRepository.getAll())
                .thenReturn(List.of(warehouse1, warehouse2));

        List<com.warehouse.api.beans.Warehouse> result =
                resource.listAllWarehousesUnits();

        assertEquals(2, result.size());

        assertEquals("1", result.get(0).getId());
        assertEquals("BU001", result.get(0).getBusinessUnitCode());
        assertEquals("Amsterdam", result.get(0).getLocation());
        assertEquals(100, result.get(0).getCapacity());
        assertEquals(20, result.get(0).getStock());

        assertEquals("2", result.get(1).getId());
        assertEquals("BU002", result.get(1).getBusinessUnitCode());

        verify(warehouseRepository).getAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoWarehousesExist() {

        when(warehouseRepository.getAll())
                .thenReturn(List.of());

        List<com.warehouse.api.beans.Warehouse> result =
                resource.listAllWarehousesUnits();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateNewWarehouse() {

        com.warehouse.api.beans.Warehouse request =
                apiWarehouse("BU001", "Amsterdam", 100, 20);

        doAnswer(invocation -> {
            com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                    warehouse = invocation.getArgument(0);

            warehouse.id = 1L;
            return null;
        }).when(createWarehouseOperation).create(any());

        com.warehouse.api.beans.Warehouse result =
                resource.createANewWarehouseUnit(request);

        assertEquals("1", result.getId());
        assertEquals("BU001", result.getBusinessUnitCode());
        assertEquals("Amsterdam", result.getLocation());
        assertEquals(100, result.getCapacity());
        assertEquals(20, result.getStock());

        verify(createWarehouseOperation).create(any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateOperationFails() {

        com.warehouse.api.beans.Warehouse request =
                apiWarehouse("BU001", "Amsterdam", 100, 20);

        doThrow(new IllegalArgumentException("Invalid warehouse"))
                .when(createWarehouseOperation)
                .create(any());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> resource.createANewWarehouseUnit(request)
        );

        assertEquals("Invalid warehouse", exception.getMessage());
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    void shouldGetWarehouseById() {

        var warehouse = domainWarehouse(
                1L,
                "BU001",
                "Amsterdam",
                100,
                20
        );

        when(warehouseRepository.findWarehouseById(1L))
                .thenReturn(warehouse);

        com.warehouse.api.beans.Warehouse result =
                resource.getAWarehouseUnitByID("1");

        assertEquals("1", result.getId());
        assertEquals("BU001", result.getBusinessUnitCode());
        assertEquals("Amsterdam", result.getLocation());

        verify(warehouseRepository).findWarehouseById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenWarehouseDoesNotExist() {

        when(warehouseRepository.findWarehouseById(999L))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> resource.getAWarehouseUnitByID("999")
        );

        assertEquals("Warehouse not found: 999", exception.getMessage());
    }

    @Test
    void shouldThrowBadRequestWhenWarehouseIdIsNotANumber() {

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> resource.getAWarehouseUnitByID("abc")
        );

        assertEquals(
                "Warehouse ID must be a number",
                exception.getMessage()
        );

        verifyNoInteractions(warehouseRepository);
    }


    // =========================================================
    // ARCHIVE
    // =========================================================

    @Test
    void shouldArchiveWarehouse() {

        var warehouse = domainWarehouse(
                1L,
                "BU001",
                "Amsterdam",
                100,
                20
        );

        when(warehouseRepository.findWarehouseById(1L))
                .thenReturn(warehouse);

        resource.archiveAWarehouseUnitByID("1");

        verify(warehouseRepository).findWarehouseById(1L);
        verify(archiveWarehouseOperation).archive(warehouse);
    }

    @Test
    void shouldThrowNotFoundWhenArchivingNonExistingWarehouse() {

        when(warehouseRepository.findWarehouseById(999L))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> resource.archiveAWarehouseUnitByID("999")
        );

        assertEquals("Warehouse not found: 999", exception.getMessage());

        verifyNoInteractions(archiveWarehouseOperation);
    }

    @Test
    void shouldThrowBadRequestWhenArchiveIdIsNotANumber() {

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> resource.archiveAWarehouseUnitByID("invalid")
        );

        assertEquals(
                "Warehouse ID must be a number",
                exception.getMessage()
        );
    }


    // =========================================================
    // REPLACE
    // =========================================================

    @Test
    void shouldReplaceWarehouseSuccessfully() {

        com.warehouse.api.beans.Warehouse request =
                apiWarehouse("BU001", "New Location", 200, 50);

        doAnswer(invocation -> {
            com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                    warehouse = invocation.getArgument(0);

            warehouse.id = 1L;
            return null;
        }).when(replaceWarehouseOperation).replace(any());

        com.warehouse.api.beans.Warehouse result =
                resource.replaceTheCurrentActiveWarehouse(
                        "BU001",
                        request
                );

        assertEquals("1", result.getId());
        assertEquals("BU001", result.getBusinessUnitCode());
        assertEquals("New Location", result.getLocation());
        assertEquals(200, result.getCapacity());
        assertEquals(50, result.getStock());
    }

    @Test
    void shouldThrowBadRequestWhenBusinessUnitCodesDoNotMatch() {

        com.warehouse.api.beans.Warehouse request =
                apiWarehouse("BU002", "Amsterdam", 100, 20);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> resource.replaceTheCurrentActiveWarehouse(
                        "BU001",
                        request
                )
        );

        assertEquals(
                "Business Unit Code in request body must match the path parameter",
                exception.getMessage()
        );

        verifyNoInteractions(replaceWarehouseOperation);
    }

    @Test
    void shouldUsePathBusinessUnitCodeWhenRequestCodeIsNull() {

        com.warehouse.api.beans.Warehouse request =
                apiWarehouse(null, "Amsterdam", 100, 20);

        resource.replaceTheCurrentActiveWarehouse(
                "BU001",
                request
        );

        ArgumentCaptor<
                com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                > captor =
                ArgumentCaptor.forClass(
                        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class
                );

        verify(replaceWarehouseOperation).replace(captor.capture());

        assertEquals(
                "BU001",
                captor.getValue().businessUnitCode
        );
    }

    @Test
    void shouldThrowNotFoundWhenActiveWarehouseDoesNotExist() {

        com.warehouse.api.beans.Warehouse request =
                apiWarehouse("BU001", "Amsterdam", 100, 20);

        doThrow(new IllegalArgumentException(
                "Active warehouse not found: BU001"
        )).when(replaceWarehouseOperation).replace(any());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> resource.replaceTheCurrentActiveWarehouse(
                        "BU001",
                        request
                )
        );

        assertEquals(
                "Active warehouse not found: BU001",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowBadRequestForOtherReplaceErrors() {

        com.warehouse.api.beans.Warehouse request =
                apiWarehouse("BU001", "Amsterdam", 100, 20);

        doThrow(new IllegalArgumentException(
                "Stock cannot exceed capacity"
        )).when(replaceWarehouseOperation).replace(any());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> resource.replaceTheCurrentActiveWarehouse(
                        "BU001",
                        request
                )
        );

        assertEquals(
                "Stock cannot exceed capacity",
                exception.getMessage()
        );
    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    private com.warehouse.api.beans.Warehouse apiWarehouse(
            String businessUnitCode,
            String location,
            Integer capacity,
            Integer stock) {

        com.warehouse.api.beans.Warehouse warehouse =
                new com.warehouse.api.beans.Warehouse();

        warehouse.setBusinessUnitCode(businessUnitCode);
        warehouse.setLocation(location);
        warehouse.setCapacity(capacity);
        warehouse.setStock(stock);

        return warehouse;
    }

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
    domainWarehouse(
            Long id,
            String businessUnitCode,
            String location,
            int capacity,
            int stock) {

        var warehouse =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

        warehouse.id = id;
        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;

        return warehouse;
    }
}