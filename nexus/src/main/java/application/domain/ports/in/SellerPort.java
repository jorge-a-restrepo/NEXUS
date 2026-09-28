package application.domain.ports.in;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.Order;
import application.domain.Product;
import application.domain.Seller;
import application.domain.Warehouse;
import application.domain.User;

import java.util.List;

/**
 * Input port for the SELLER role.
 *
 * Responsibility matrix: the seller registers products and manages inventory.
 * RG-03 restricts every operation to the seller's own products, warehouses and
 * related orders.
 */
public interface SellerPort {

    Seller consultMyProfile(User user);

    // Catalog (Domain 5)

    Product registerProduct(User user, Product product);

    Product updateProduct(User user, Product product);

    Product publishProduct(User user, Product product);

    Product suspendProduct(User user, Product product);

    Product discontinueProduct(User user, Product product);

    List<Product> consultMyProducts(User user);

    // Inventory (Domain 6)

    List<Warehouse> consultMyWarehouses(User user);

    Inventory registerInventoryInflow(User user, InventoryMovement inventoryMovement);

    Inventory adjustInventory(User user, InventoryMovement inventoryMovement);

    List<Inventory> consultMyInventory(User user);

    Inventory consultProductInventory(User user, Product product, Warehouse warehouse);

    // Orders containing the seller's products

    List<Order> consultMyOrders(User user);
}
