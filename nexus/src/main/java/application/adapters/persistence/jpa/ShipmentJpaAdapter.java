package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import application.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaShipmentRepository;
import application.domain.LogisticsOperator;
import application.domain.Order;
import application.domain.Shipment;
import application.domain.ShipmentStatus;
import application.domain.Warehouse;
import application.domain.ports.out.ShipmentRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Output persistence adapter for shipments (MySQL, table shipments).
 */
@Repository
public class ShipmentJpaAdapter implements ShipmentRepositoryPort {

    private final SpringDataJpaShipmentRepository repository;
    private final JpaReferenceResolver resolver;

    public ShipmentJpaAdapter(SpringDataJpaShipmentRepository repository, JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Shipment save(Shipment shipment) {
        if (shipment.getIdentifier() == null) {
            shipment.setIdentifier(DomainReferences.newIdentifier());
        }
        return toDomain(repository.save(ShipmentJpaMapper.toEntity(shipment)));
    }

    @Override
    public Shipment update(Shipment shipment) {
        return save(shipment);
    }

    @Override
    public List<Shipment> findAllByOrder(Order order) {
        String orderId = DomainReferences.identifierOf(order);
        if (orderId == null) {
            return List.of();
        }
        return repository.findAllByOrderIdentifier(orderId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Shipment> findAllByWarehouse(Warehouse warehouse) {
        String warehouseId = DomainReferences.identifierOf(warehouse);
        if (warehouseId == null) {
            return List.of();
        }
        return repository.findAllByWarehouseIdentifier(warehouseId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Shipment> findAllByStatus(ShipmentStatus shipmentStatus) {
        if (shipmentStatus == null) {
            return List.of();
        }
        return repository.findAllByStatus(shipmentStatus.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Shipment> findAllByLogisticsOperator(LogisticsOperator logisticsOperator) {
        String operatorId = DomainReferences.identifierOf(logisticsOperator);
        if (operatorId == null) {
            return List.of();
        }
        return repository.findAllByLogisticsOperatorIdentifier(operatorId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Shipment> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private Shipment toDomain(ShipmentJpaEntity entity) {
        return ShipmentJpaMapper.toDomain(entity,
                resolver.order(entity.getOrderIdentifier()),
                resolver.warehouse(entity.getWarehouseIdentifier()));
    }
}
