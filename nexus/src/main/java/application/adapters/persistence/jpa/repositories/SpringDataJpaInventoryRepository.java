package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaInventoryRepository extends JpaRepository<InventoryJpaEntity, Long> {

    Optional<InventoryJpaEntity> findByProductIdentifierAndWarehouseIdentifier(String productIdentifier,
                                                                             String warehouseIdentifier);

    boolean existsByProductIdentifierAndWarehouseIdentifier(String productIdentifier, String warehouseIdentifier);

    List<InventoryJpaEntity> findAllByProductIdentifier(String productIdentifier);

    List<InventoryJpaEntity> findAllByWarehouseIdentifier(String warehouseIdentifier);
}
