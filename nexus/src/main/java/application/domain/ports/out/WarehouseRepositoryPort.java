package application.domain.ports.out;

import application.domain.Seller;
import application.domain.Warehouse;
import application.domain.WarehouseType;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Warehouse persistence and querying.
 *
 * Domain 4 distinguishes marketplace-owned warehouses from seller-owned
 * warehouses, which is why querying by type is part of the contract.
 *
 * SUPUESTO: findAllBySeller requires the Warehouse domain model to reference
 * its owning Seller. That association is described in the domain model but is
 * not yet an attribute of the Warehouse class.
 */
public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);

    Warehouse update(Warehouse warehouse);

    Optional<Warehouse> findByIdentifier(Warehouse warehouse);

    boolean existsByIdentifier(Warehouse warehouse);

    List<Warehouse> findAll();

    List<Warehouse> findAllByType(WarehouseType warehouseType);

    List<Warehouse> findAllBySeller(Seller seller);
}
