package application.domain.ports.in;

import application.domain.Inventory;
import application.domain.Order;
import application.domain.Product;
import application.domain.ReturnRefund;
import application.domain.Shipment;
import application.domain.User;

import java.util.List;

/**
 * Input port for the SUPERVISOR role.
 *
 * Section 5 defines the supervisor as a consultation and operational follow-up
 * profile, so this port exposes read operations exclusively. No method here may
 * modify business state.
 */
public interface SupervisorPort {

    List<Order> consultOrders(User user);

    Order consultOrder(User user, Order order);

    List<Inventory> consultInventory(User user);

    List<Shipment> consultShipments(User user);

    List<ReturnRefund> consultReturns(User user);

    List<Product> consultCatalog(User user);
}
