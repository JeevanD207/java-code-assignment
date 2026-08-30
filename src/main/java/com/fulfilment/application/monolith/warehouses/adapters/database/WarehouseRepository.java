package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

    @Override
    public List<Warehouse> getAll() {
        return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
    }

    @Override
    public void create(Warehouse warehouse) {
        // TODO Auto-generated method stub
        var dbWarehouse = new DbWarehouse();

        dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
        dbWarehouse.location = warehouse.location;
        dbWarehouse.capacity = warehouse.capacity;
        dbWarehouse.stock = warehouse.stock;
        dbWarehouse.createdAt = warehouse.createdAt;
        dbWarehouse.archivedAt = warehouse.archivedAt;

        persist(dbWarehouse);

        warehouse.id = dbWarehouse.id;

        // throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public void update(Warehouse warehouse) {
        // TODO Auto-generated method stub
      var dbWarehouse =
              find(
                              "businessUnitCode = ?1 and archivedAt is null",
                              warehouse.businessUnitCode)
                      .firstResult();

      if (dbWarehouse == null) {
        throw new IllegalArgumentException(
                "Active warehouse not found: " + warehouse.businessUnitCode);
      }

      dbWarehouse.location = warehouse.location;
      dbWarehouse.capacity = warehouse.capacity;
      dbWarehouse.stock = warehouse.stock;
      dbWarehouse.archivedAt = warehouse.archivedAt;
       // throw new UnsupportedOperationException("Unimplemented method 'replace'");
    }

    @Override
    public void remove(Warehouse warehouse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
       return this.find("businessUnitCode = ?1 and archivedAt is null", buCode)
            .firstResultOptional()
            .map(DbWarehouse::toWarehouse)
            .orElse(null);
  }

  @Override
  public Warehouse findWarehouseById(Long id) {
    var dbWarehouse = findById(id);

    if (dbWarehouse == null || dbWarehouse.archivedAt != null) {
      return null;
    }

    return dbWarehouse.toWarehouse();
  }
}
