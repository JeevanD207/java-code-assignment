package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WarehouseRepositoryTest {

    @Test
    void shouldGetAllActiveWarehouses() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.id = 1L;
        dbWarehouse.businessUnitCode = "MWH-100";
        dbWarehouse.location = "AMSTERDAM-001";
        dbWarehouse.capacity = 50;
        dbWarehouse.stock = 10;

        doReturn(List.of(dbWarehouse))
                .when(repository)
                .list("archivedAt is null");

        List<Warehouse> result = repository.getAll();

        assertEquals(1, result.size());
        assertEquals("MWH-100", result.get(0).businessUnitCode);
        assertEquals("AMSTERDAM-001", result.get(0).location);
        assertEquals(50, result.get(0).capacity);
        assertEquals(10, result.get(0).stock);
    }

    @Test
    void shouldCreateWarehouse() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH-101";
        warehouse.location = "AMSTERDAM-002";
        warehouse.capacity = 40;
        warehouse.stock = 5;
        warehouse.createdAt = LocalDateTime.now();

        doAnswer(invocation -> {
            DbWarehouse dbWarehouse = invocation.getArgument(0);
            dbWarehouse.id = 10L;
            return null;
        }).when(repository).persist(any(DbWarehouse.class));

        repository.create(warehouse);

        ArgumentCaptor<DbWarehouse> captor =
                ArgumentCaptor.forClass(DbWarehouse.class);

        verify(repository).persist(captor.capture());

        DbWarehouse saved = captor.getValue();

        assertEquals("MWH-101", saved.businessUnitCode);
        assertEquals("AMSTERDAM-002", saved.location);
        assertEquals(40, saved.capacity);
        assertEquals(5, saved.stock);

        assertEquals(10L, warehouse.id);
    }

    @Test
    void shouldUpdateActiveWarehouse() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.businessUnitCode = "MWH-100";
        dbWarehouse.location = "OLD-LOCATION";
        dbWarehouse.capacity = 20;
        dbWarehouse.stock = 5;

        PanacheQuery<DbWarehouse> query = mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find(
                        "businessUnitCode = ?1 and archivedAt is null",
                        "MWH-100");

        when(query.firstResult()).thenReturn(dbWarehouse);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH-100";
        warehouse.location = "NEW-LOCATION";
        warehouse.capacity = 50;
        warehouse.stock = 25;
        warehouse.archivedAt = LocalDateTime.now();

        repository.update(warehouse);

        assertEquals("NEW-LOCATION", dbWarehouse.location);
        assertEquals(50, dbWarehouse.capacity);
        assertEquals(25, dbWarehouse.stock);
        assertEquals(warehouse.archivedAt, dbWarehouse.archivedAt);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingWarehouse() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        PanacheQuery<DbWarehouse> query = mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find(
                        "businessUnitCode = ?1 and archivedAt is null",
                        "MWH-999");

        when(query.firstResult()).thenReturn(null);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH-999";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.update(warehouse)
        );

        assertEquals(
                "Active warehouse not found: MWH-999",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionWhenRemovingWarehouse() {
        WarehouseRepository repository = new WarehouseRepository();

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH-100";

        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> repository.remove(warehouse)
        );

        assertEquals(
                "Unimplemented method 'remove'",
                exception.getMessage()
        );
    }

    @Test
    void shouldFindActiveWarehouseByBusinessUnitCode() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.id = 1L;
        dbWarehouse.businessUnitCode = "MWH-100";
        dbWarehouse.location = "AMSTERDAM-001";
        dbWarehouse.capacity = 50;
        dbWarehouse.stock = 10;

        PanacheQuery<DbWarehouse> query = mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find(
                        "businessUnitCode = ?1 and archivedAt is null",
                        "MWH-100");

        when(query.firstResultOptional())
                .thenReturn(Optional.of(dbWarehouse));

        Warehouse result =
                repository.findByBusinessUnitCode("MWH-100");

        assertNotNull(result);
        assertEquals("MWH-100", result.businessUnitCode);
        assertEquals("AMSTERDAM-001", result.location);
    }

    @Test
    void shouldReturnNullWhenBusinessUnitCodeNotFound() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        PanacheQuery<DbWarehouse> query = mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find(
                        "businessUnitCode = ?1 and archivedAt is null",
                        "MWH-999");

        when(query.firstResultOptional())
                .thenReturn(Optional.empty());

        Warehouse result =
                repository.findByBusinessUnitCode("MWH-999");

        assertNull(result);
    }

    @Test
    void shouldFindActiveWarehouseById() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.id = 1L;
        dbWarehouse.businessUnitCode = "MWH-100";
        dbWarehouse.location = "AMSTERDAM-001";
        dbWarehouse.capacity = 50;
        dbWarehouse.stock = 10;
        dbWarehouse.archivedAt = null;

        doReturn(dbWarehouse)
                .when(repository)
                .findById(1L);

        Warehouse result = repository.findWarehouseById(1L);

        assertNotNull(result);
        assertEquals("MWH-100", result.businessUnitCode);
    }

    @Test
    void shouldReturnNullWhenWarehouseIdDoesNotExist() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        doReturn(null)
                .when(repository)
                .findById(999L);

        Warehouse result = repository.findWarehouseById(999L);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenWarehouseIsArchived() {
        WarehouseRepository repository = spy(new WarehouseRepository());

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.id = 1L;
        dbWarehouse.archivedAt = LocalDateTime.now();

        doReturn(dbWarehouse)
                .when(repository)
                .findById(1L);

        Warehouse result = repository.findWarehouseById(1L);

        assertNull(result);
    }
}