package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

    private final WarehouseStore warehouseStore;
    private final LocationResolver locationResolver;

    public ReplaceWarehouseUseCase(
            WarehouseStore warehouseStore, LocationResolver locationResolver) {
        this.warehouseStore = warehouseStore;
        this.locationResolver = locationResolver;
    }

    @Override
    @Transactional
    public void replace(Warehouse newWarehouse) {
        // TODO implement this method
        validateWarehouseData(newWarehouse);

        Warehouse oldWarehouse =
                warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);

        if (oldWarehouse == null) {
            throw new IllegalArgumentException(
                    "Active warehouse not found: " + newWarehouse.businessUnitCode);
        }

        Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
        if (location == null) {
            throw new IllegalArgumentException("Invalid location: " + newWarehouse.location);
        }

        if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
            throw new IllegalArgumentException(
                    "Replacement warehouse stock must match the previous warehouse stock");
        }

        if (newWarehouse.capacity < oldWarehouse.stock) {
            throw new IllegalArgumentException(
                    "Replacement warehouse capacity cannot accommodate previous stock");
        }

//    var otherWarehousesAtLocation =
//            warehouseStore.getAll().stream()
//                    .filter(existing -> !existing.businessUnitCode.equals(oldWarehouse.businessUnitCode))
//                    .filter(existing -> existing.location.equals(newWarehouse.location))
//                    .toList();

        var otherWarehousesAtLocation =
                warehouseStore.getAll().stream()
                        .filter(existing -> !existing.id.equals(oldWarehouse.id))
                        .filter(existing -> existing.location.equals(newWarehouse.location))
                        .toList();

        if (otherWarehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
            throw new IllegalArgumentException(
                    "Maximum number of warehouses reached for location: " + newWarehouse.location);
        }

        int totalCapacity =
                otherWarehousesAtLocation.stream()
                        .mapToInt(existing -> existing.capacity)
                        .sum()
                        + newWarehouse.capacity;

        if (totalCapacity > location.maxCapacity) {
            throw new IllegalArgumentException(
                    "Maximum capacity exceeded for location: " + newWarehouse.location);
        }

        oldWarehouse.archivedAt = LocalDateTime.now();
        warehouseStore.update(oldWarehouse);

        newWarehouse.createdAt = LocalDateTime.now();
        newWarehouse.archivedAt = null;
        warehouseStore.create(newWarehouse);
    }

    private void validateWarehouseData(Warehouse warehouse) {
        if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
            throw new IllegalArgumentException("Business Unit Code is required");
        }

        if (warehouse.location == null || warehouse.location.isBlank()) {
            throw new IllegalArgumentException("Location is required");
        }

        if (warehouse.capacity == null || warehouse.capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }

        if (warehouse.stock == null || warehouse.stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        if (warehouse.stock > warehouse.capacity) {
            throw new IllegalArgumentException("Stock cannot exceed capacity");
        }
    }
}