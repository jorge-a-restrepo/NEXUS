package application.domain.ports.out;

import application.domain.Inventory;
import application.domain.Product;
import application.domain.Warehouse;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Inventory persistence and querying.
 *
 * Domain 6: inventory is distributed and must be linked to exactly one Product
 * and one Warehouse. The (Product, Warehouse) pair must not produce duplicate
 * records, which is why lookup and existence checks are expressed over that
 * pair instead of a single identifier.
 */
public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);

    Inventory update(Inventory inventory);

    Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);

    boolean existsByProductAndWarehouse(Product product, Warehouse warehouse);

    List<Inventory> findAllByProduct(Product product);

    List<Inventory> findAllByWarehouse(Warehouse warehouse);

    List<Inventory> findAll();
}
