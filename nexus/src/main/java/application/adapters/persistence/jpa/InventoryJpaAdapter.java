package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.domain.Inventory;
import application.domain.Product;
import application.domain.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for distributed inventory (MySQL, table inventories).
 * Records are located by the pair (product, warehouse), never by a client identifier.
 */
@Repository
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final SpringDataJpaInventoryRepository repository;
    private final JpaReferenceResolver resolver;

    public InventoryJpaAdapter(SpringDataJpaInventoryRepository repository, JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Inventory save(Inventory inventory) {
        return upsert(inventory);
    }

    @Override
    public Inventory update(Inventory inventory) {
        return upsert(inventory);
    }

    @Override
    public Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse) {
        String productId = DomainReferences.identifierOf(product);
        String warehouseId = DomainReferences.identifierOf(warehouse);
        if (productId == null || warehouseId == null) {
            return Optional.empty();
        }
        return repository.findByProductIdentifierAndWarehouseIdentifier(productId, warehouseId).map(this::toDomain);
    }

    @Override
    public boolean existsByProductAndWarehouse(Product product, Warehouse warehouse) {
        String productId = DomainReferences.identifierOf(product);
        String warehouseId = DomainReferences.identifierOf(warehouse);
        return productId != null && warehouseId != null
                && repository.existsByProductIdentifierAndWarehouseIdentifier(productId, warehouseId);
    }

    @Override
    public List<Inventory> findAllByProduct(Product product) {
        String productId = DomainReferences.identifierOf(product);
        if (productId == null) {
            return List.of();
        }
        return repository.findAllByProductIdentifier(productId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Inventory> findAllByWarehouse(Warehouse warehouse) {
        String warehouseId = DomainReferences.identifierOf(warehouse);
        if (warehouseId == null) {
            return List.of();
        }
        return repository.findAllByWarehouseIdentifier(warehouseId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Inventory> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    /** Inserts the record for the pair or updates the existing one, never duplicating it. */
    private Inventory upsert(Inventory inventory) {
        InventoryJpaEntity entity = InventoryJpaMapper.toEntity(inventory);
        repository.findByProductIdentifierAndWarehouseIdentifier(entity.getProductIdentifier(),
                        entity.getWarehouseIdentifier())
                .ifPresent(stored -> entity.setId(stored.getId()));
        return toDomain(repository.save(entity));
    }

    private Inventory toDomain(InventoryJpaEntity entity) {
        return InventoryJpaMapper.toDomain(entity,
                resolver.product(entity.getProductIdentifier()),
                resolver.warehouse(entity.getWarehouseIdentifier()));
    }
}
