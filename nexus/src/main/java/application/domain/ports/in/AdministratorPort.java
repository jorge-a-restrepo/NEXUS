package application.domain.ports.in;

import application.domain.Buyer;
import application.domain.Inventory;
import application.domain.Order;
import application.domain.ReturnRefund;
import application.domain.Seller;
import application.domain.User;
import application.domain.UserStatus;
import application.domain.Warehouse;

import java.util.List;

/**
 * Input port for the ADMINISTRATOR role.
 *
 * Domain 3: sellers are onboarded exclusively by an Administrator, together
 * with their first warehouse, which is why registerSeller takes both models.
 * The responsibility matrix also assigns refund management to this role.
 */
public interface AdministratorPort {

    // Seller and warehouse onboarding

    Seller registerSeller(User user, Seller seller, Warehouse firstWarehouse);

    Warehouse registerWarehouse(User user, Warehouse warehouse);

    Warehouse updateWarehouse(User user, Warehouse warehouse);

    List<Warehouse> consultWarehouses(User user);

    // User administration (Domain 1)

    User registerUser(User user, User newUser);

    User changeUserStatus(User user, User targetUser, UserStatus newStatus);

    List<User> consultUsers(User user);

    List<Buyer> consultBuyers(User user);

    List<Seller> consultSellers(User user);

    /**
     * The new commercial status travels inside the Buyer domain model.
     * SUPUESTO: the commercial status is still modeled as an open String and
     * should become a bounded value object.
     */
    Buyer changeBuyerCommercialStatus(User user, Buyer buyer);

    // Returns and refunds

    ReturnRefund approveReturn(User user, ReturnRefund returnRefund);

    ReturnRefund rejectReturn(User user, ReturnRefund returnRefund);

    ReturnRefund processRefund(User user, ReturnRefund returnRefund);

    List<ReturnRefund> consultReturns(User user);

    // Administrative consolidation (OBJ-12)

    List<Order> consultAllOrders(User user);

    List<Inventory> consultAllInventory(User user);
}
