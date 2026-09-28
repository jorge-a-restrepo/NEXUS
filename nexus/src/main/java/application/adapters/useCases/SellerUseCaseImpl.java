package application.adapters.useCases;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.Order;
import application.domain.Product;
import application.domain.Seller;
import application.domain.User;
import application.domain.Warehouse;
import application.domain.ports.in.SellerPort;
import application.domain.services.catalog.ConsultSellerProductsService;
import application.domain.services.catalog.DiscontinueProductService;
import application.domain.services.catalog.PublishProductService;
import application.domain.services.catalog.RegisterProductService;
import application.domain.services.catalog.SuspendProductService;
import application.domain.services.catalog.UpdateProductService;
import application.domain.services.inventory.AdjustInventoryService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.inventory.RegisterInventoryInflowService;
import application.domain.services.order.ConsultSellerOrdersService;
import application.domain.services.seller.ConsultSellerProfileService;
import application.domain.services.warehouse.ConsultWarehouseService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the seller input port by delegating to the domain services.
 *
 * consultMyInventory walks the seller's own warehouses and collects their
 * inventory: the scope comes from the warehouse consultation, which is already
 * restricted to the authenticated seller.
 */
@Service
public class SellerUseCaseImpl implements SellerPort {

    private final ConsultSellerProfileService consultSellerProfileService;
    private final RegisterProductService registerProductService;
    private final UpdateProductService updateProductService;
    private final PublishProductService publishProductService;
    private final SuspendProductService suspendProductService;
    private final DiscontinueProductService discontinueProductService;
    private final ConsultSellerProductsService consultSellerProductsService;
    private final ConsultWarehouseService consultWarehouseService;
    private final RegisterInventoryInflowService registerInventoryInflowService;
    private final AdjustInventoryService adjustInventoryService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultSellerOrdersService consultSellerOrdersService;

    public SellerUseCaseImpl(ConsultSellerProfileService consultSellerProfileService,
                             RegisterProductService registerProductService,
                             UpdateProductService updateProductService,
                             PublishProductService publishProductService,
                             SuspendProductService suspendProductService,
                             DiscontinueProductService discontinueProductService,
                             ConsultSellerProductsService consultSellerProductsService,
                             ConsultWarehouseService consultWarehouseService,
                             RegisterInventoryInflowService registerInventoryInflowService,
                             AdjustInventoryService adjustInventoryService,
                             ConsultInventoryService consultInventoryService,
                             ConsultSellerOrdersService consultSellerOrdersService) {
        this.consultSellerProfileService = consultSellerProfileService;
        this.registerProductService = registerProductService;
        this.updateProductService = updateProductService;
        this.publishProductService = publishProductService;
        this.suspendProductService = suspendProductService;
        this.discontinueProductService = discontinueProductService;
        this.consultSellerProductsService = consultSellerProductsService;
        this.consultWarehouseService = consultWarehouseService;
        this.registerInventoryInflowService = registerInventoryInflowService;
        this.adjustInventoryService = adjustInventoryService;
        this.consultInventoryService = consultInventoryService;
        this.consultSellerOrdersService = consultSellerOrdersService;
    }

    @Override
    public Seller consultMyProfile(User user) {
        return consultSellerProfileService.execute(user);
    }

    @Override
    public Product registerProduct(User user, Product product) {
        return registerProductService.execute(user, product);
    }

    @Override
    public Product updateProduct(User user, Product product) {
        return updateProductService.execute(user, product);
    }

    @Override
    public Product publishProduct(User user, Product product) {
        return publishProductService.execute(user, product);
    }

    @Override
    public Product suspendProduct(User user, Product product) {
        return suspendProductService.execute(user, product);
    }

    @Override
    public Product discontinueProduct(User user, Product product) {
        return discontinueProductService.execute(user, product);
    }

    @Override
    public List<Product> consultMyProducts(User user) {
        return consultSellerProductsService.execute(user);
    }

    @Override
    public List<Warehouse> consultMyWarehouses(User user) {
        return consultWarehouseService.executeMyWarehouses(user);
    }

    @Override
    public Inventory registerInventoryInflow(User user, InventoryMovement inventoryMovement) {
        return registerInventoryInflowService.execute(user, inventoryMovement);
    }

    @Override
    public Inventory adjustInventory(User user, InventoryMovement inventoryMovement) {
        return adjustInventoryService.execute(user, inventoryMovement);
    }

    @Override
    public List<Inventory> consultMyInventory(User user) {
        List<Warehouse> warehouses = consultWarehouseService.executeMyWarehouses(user);
        List<Inventory> inventories = new ArrayList<>();
        if (warehouses == null) {
            return inventories;
        }
        for (Warehouse warehouse : warehouses) {
            List<Inventory> warehouseInventories = consultInventoryService.executeByWarehouse(user, warehouse);
            if (warehouseInventories != null) {
                inventories.addAll(warehouseInventories);
            }
        }
        return inventories;
    }

    @Override
    public Inventory consultProductInventory(User user, Product product, Warehouse warehouse) {
        return consultInventoryService.execute(user, product, warehouse);
    }

    @Override
    public List<Order> consultMyOrders(User user) {
        return consultSellerOrdersService.execute(user);
    }
}
