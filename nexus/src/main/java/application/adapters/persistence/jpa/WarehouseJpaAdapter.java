package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.Seller;
import application.domain.Warehouse;
import application.domain.WarehouseType;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for warehouses (MySQL, table warehouses).
 * The identifier is provided by the client and validated by the domain.
 */
@Repository
public class WarehouseJpaAdapter implements WarehouseRepositoryPort {

    private final SpringDataJpaWarehouseRepository repository;

    public WarehouseJpaAdapter(SpringDataJpaWarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        return WarehouseJpaMapper.toDomain(repository.save(WarehouseJpaMapper.toEntity(warehouse)));
    }

    @Override
    public Warehouse update(Warehouse warehouse) {
        return save(warehouse);
    }

    @Override
    public Optional<Warehouse> findByIdentifier(Warehouse warehouse) {
        if (warehouse == null || warehouse.getIdentifier() == null) {
            return Optional.empty();
        }
        return repository.findById(warehouse.getIdentifier()).map(WarehouseJpaMapper::toDomain);
    }

    @Override
    public boolean existsByIdentifier(Warehouse warehouse) {
        return warehouse != null && warehouse.getIdentifier() != null
                && repository.existsById(warehouse.getIdentifier());
    }

    @Override
    public List<Warehouse> findAll() {
        return repository.findAll().stream().map(WarehouseJpaMapper::toDomain).toList();
    }

    @Override
    public List<Warehouse> findAllByType(WarehouseType warehouseType) {
        if (warehouseType == null) {
            return List.of();
        }
        return repository.findAllByWarehouseType(warehouseType.name()).stream()
                .map(WarehouseJpaMapper::toDomain).toList();
    }

    @Override
    public List<Warehouse> findAllBySeller(Seller seller) {
        if (seller == null || seller.getIdentifier() == null) {
            return List.of();
        }
        return repository.findAllBySellerIdentifier(seller.getIdentifier()).stream()
                .map(WarehouseJpaMapper::toDomain).toList();
    }
}
