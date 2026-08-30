package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseRepository warehouseRepository;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream()
            .map(this::toWarehouseResponse)
            .toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var warehouse = toDomainWarehouse(data);

    try {
      createWarehouseOperation.create(warehouse);
    } catch (IllegalArgumentException exception) {
      throw new BadRequestException(exception.getMessage(), exception);
    }

    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findWarehouseById(parseId(id));

    if (warehouse == null) {
      throw new NotFoundException("Warehouse not found: " + id);
    }

    return toWarehouseResponse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findWarehouseById(parseId(id));

    if (warehouse == null) {
      throw new NotFoundException("Warehouse not found: " + id);
    }

    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode, @NotNull Warehouse data) {
    if (data.getBusinessUnitCode() != null
            && !data.getBusinessUnitCode().equals(businessUnitCode)) {
      throw new BadRequestException(
              "Business Unit Code in request body must match the path parameter");
    }

    var warehouse = toDomainWarehouse(data);
    warehouse.businessUnitCode = businessUnitCode;

    try {
      replaceWarehouseOperation.replace(warehouse);
    } catch (IllegalArgumentException exception) {
      if (exception.getMessage().startsWith("Active warehouse not found")) {
        throw new NotFoundException(exception.getMessage());
      }

      throw new BadRequestException(exception.getMessage(), exception);
    }

    return toWarehouseResponse(warehouse);
  }

  private Long parseId(String id) {
    try {
      return Long.valueOf(id);
    } catch (NumberFormatException exception) {
      throw new BadRequestException("Warehouse ID must be a number");
    }
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
  toDomainWarehouse(Warehouse source) {
    var warehouse =
            new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

    warehouse.businessUnitCode = source.getBusinessUnitCode();
    warehouse.location = source.getLocation();
    warehouse.capacity = source.getCapacity();
    warehouse.stock = source.getStock();

    return warehouse;
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();

    if (warehouse.id != null) {
      response.setId(warehouse.id.toString());
    }

    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}