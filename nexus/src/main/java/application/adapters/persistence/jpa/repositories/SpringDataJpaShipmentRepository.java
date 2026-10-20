package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJpaShipmentRepository extends JpaRepository<ShipmentJpaEntity, String> {

    List<ShipmentJpaEntity> findAllByOrderIdentifier(String orderIdentifier);

    List<ShipmentJpaEntity> findAllByWarehouseIdentifier(String warehouseIdentifier);

    List<ShipmentJpaEntity> findAllByStatus(String status);

    List<ShipmentJpaEntity> findAllByLogisticsOperatorIdentifier(String logisticsOperatorIdentifier);
}
