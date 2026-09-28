package application.adapters.useCases;

import application.domain.Buyer;
import application.domain.Inventory;
import application.domain.Order;
import application.domain.ReturnRefund;
import application.domain.Seller;
import application.domain.User;
import application.domain.UserStatus;
import application.domain.Warehouse;
import application.domain.ports.in.AdministratorPort;
import application.domain.services.buyer.ChangeBuyerCommercialStatusService;
import application.domain.services.buyer.ConsultBuyersService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.order.ConsultAllOrdersService;
import application.domain.services.returnrefund.ApproveReturnService;
import application.domain.services.returnrefund.ConsultReturnService;
import application.domain.services.returnrefund.ProcessRefundService;
import application.domain.services.returnrefund.RejectReturnService;
import application.domain.services.seller.ConsultSellersService;
import application.domain.services.seller.RegisterSellerService;
import application.domain.services.user.ChangeUserStatusService;
import application.domain.services.user.ConsultUserService;
import application.domain.services.user.RegisterInternalUserService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.services.warehouse.RegisterWarehouseService;
import application.domain.services.warehouse.UpdateWarehouseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements the administrator input port by delegating to the domain services.
 */
@Service
public class AdministratorUseCaseImpl implements AdministratorPort {

    private final RegisterSellerService registerSellerService;
    private final RegisterWarehouseService registerWarehouseService;
    private final UpdateWarehouseService updateWarehouseService;
    private final ConsultWarehouseService consultWarehouseService;
    private final RegisterInternalUserService registerInternalUserService;
    private final ChangeUserStatusService changeUserStatusService;
    private final ConsultUserService consultUserService;
    private final ConsultBuyersService consultBuyersService;
    private final ConsultSellersService consultSellersService;
    private final ChangeBuyerCommercialStatusService changeBuyerCommercialStatusService;
    private final ApproveReturnService approveReturnService;
    private final RejectReturnService rejectReturnService;
    private final ProcessRefundService processRefundService;
    private final ConsultReturnService consultReturnService;
    private final ConsultAllOrdersService consultAllOrdersService;
    private final ConsultInventoryService consultInventoryService;

    public AdministratorUseCaseImpl(RegisterSellerService registerSellerService,
                                    RegisterWarehouseService registerWarehouseService,
                                    UpdateWarehouseService updateWarehouseService,
                                    ConsultWarehouseService consultWarehouseService,
                                    RegisterInternalUserService registerInternalUserService,
                                    ChangeUserStatusService changeUserStatusService,
                                    ConsultUserService consultUserService,
                                    ConsultBuyersService consultBuyersService,
                                    ConsultSellersService consultSellersService,
                                    ChangeBuyerCommercialStatusService changeBuyerCommercialStatusService,
                                    ApproveReturnService approveReturnService,
                                    RejectReturnService rejectReturnService,
                                    ProcessRefundService processRefundService,
                                    ConsultReturnService consultReturnService,
                                    ConsultAllOrdersService consultAllOrdersService,
                                    ConsultInventoryService consultInventoryService) {
        this.registerSellerService = registerSellerService;
        this.registerWarehouseService = registerWarehouseService;
        this.updateWarehouseService = updateWarehouseService;
        this.consultWarehouseService = consultWarehouseService;
        this.registerInternalUserService = registerInternalUserService;
        this.changeUserStatusService = changeUserStatusService;
        this.consultUserService = consultUserService;
        this.consultBuyersService = consultBuyersService;
        this.consultSellersService = consultSellersService;
        this.changeBuyerCommercialStatusService = changeBuyerCommercialStatusService;
        this.approveReturnService = approveReturnService;
        this.rejectReturnService = rejectReturnService;
        this.processRefundService = processRefundService;
        this.consultReturnService = consultReturnService;
        this.consultAllOrdersService = consultAllOrdersService;
        this.consultInventoryService = consultInventoryService;
    }

    @Override
    public Seller registerSeller(User user, Seller seller, Warehouse firstWarehouse) {
        return registerSellerService.execute(user, seller, firstWarehouse);
    }

    @Override
    public Warehouse registerWarehouse(User user, Warehouse warehouse) {
        return registerWarehouseService.execute(user, warehouse);
    }

    @Override
    public Warehouse updateWarehouse(User user, Warehouse warehouse) {
        return updateWarehouseService.execute(user, warehouse);
    }

    @Override
    public List<Warehouse> consultWarehouses(User user) {
        return consultWarehouseService.executeAll(user);
    }

    @Override
    public User registerUser(User user, User newUser) {
        return registerInternalUserService.execute(user, newUser);
    }

    @Override
    public User changeUserStatus(User user, User targetUser, UserStatus newStatus) {
        return changeUserStatusService.execute(user, targetUser, newStatus);
    }

    @Override
    public List<User> consultUsers(User user) {
        return consultUserService.executeAll(user);
    }

    @Override
    public List<Buyer> consultBuyers(User user) {
        return consultBuyersService.execute(user);
    }

    @Override
    public List<Seller> consultSellers(User user) {
        return consultSellersService.execute(user);
    }

    @Override
    public Buyer changeBuyerCommercialStatus(User user, Buyer buyer) {
        return changeBuyerCommercialStatusService.execute(user, buyer);
    }

    @Override
    public ReturnRefund approveReturn(User user, ReturnRefund returnRefund) {
        return approveReturnService.execute(user, returnRefund);
    }

    @Override
    public ReturnRefund rejectReturn(User user, ReturnRefund returnRefund) {
        return rejectReturnService.execute(user, returnRefund);
    }

    @Override
    public ReturnRefund processRefund(User user, ReturnRefund returnRefund) {
        return processRefundService.execute(user, returnRefund);
    }

    @Override
    public List<ReturnRefund> consultReturns(User user) {
        return consultReturnService.executeAll(user);
    }

    @Override
    public List<Order> consultAllOrders(User user) {
        return consultAllOrdersService.execute(user);
    }

    @Override
    public List<Inventory> consultAllInventory(User user) {
        return consultInventoryService.executeConsolidated(user);
    }
}
