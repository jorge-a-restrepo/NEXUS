package application.domain.ports.out;

import application.domain.LogisticsOperator;
import application.domain.Order;
import application.domain.Shipment;
import application.domain.ShipmentStatus;
import application.domain.Warehouse;

import java.util.List;

/**
 * Output port for Shipment persistence and querying.
 *
 * OBJ-10: shipments apply to physical products only. Because inventory is
 * distributed across warehouses (Domain 6), a single order may require more
 * than one shipment, so shipments are always retrieved as a list per order.
 */
public interface ShipmentRepositoryPort {

    Shipment save(Shipment shipment);

    Shipment update(Shipment shipment);

    List<Shipment> findAllByOrder(Order order);

    List<Shipment> findAllByWarehouse(Warehouse warehouse);

    List<Shipment> findAllByStatus(ShipmentStatus shipmentStatus);

    List<Shipment> findAllByLogisticsOperator(LogisticsOperator logisticsOperator);

    List<Shipment> findAll();
}
