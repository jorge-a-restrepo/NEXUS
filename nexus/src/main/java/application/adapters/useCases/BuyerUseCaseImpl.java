package application.adapters.useCases;

import application.domain.Buyer;
import application.domain.Cart;
import application.domain.CartItem;
import application.domain.Invoice;
import application.domain.Order;
import application.domain.Product;
import application.domain.ReturnRefund;
import application.domain.User;
import application.domain.ports.in.BuyerPort;
import application.domain.services.buyer.ConsultBuyerProfileService;
import application.domain.services.buyer.UpdateBuyerProfileService;
import application.domain.services.cart.AddCartItemService;
import application.domain.services.cart.CheckoutCartService;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.cart.RemoveCartItemService;
import application.domain.services.cart.UpdateCartItemQuantityService;
import application.domain.services.catalog.ConsultCatalogService;
import application.domain.services.catalog.ConsultProductService;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.services.order.ConfirmOrderPaymentService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.returnrefund.ConsultReturnService;
import application.domain.services.returnrefund.RequestReturnService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements the buyer input port by delegating to the domain services.
 *
 * Every call forwards the authenticated User, which each service uses to
 * authorize the operation and to resolve the buyer's own information.
 */
@Service
public class BuyerUseCaseImpl implements BuyerPort {

    private final ConsultBuyerProfileService consultBuyerProfileService;
    private final UpdateBuyerProfileService updateBuyerProfileService;
    private final ConsultCatalogService consultCatalogService;
    private final ConsultProductService consultProductService;
    private final ConsultCartService consultCartService;
    private final AddCartItemService addCartItemService;
    private final UpdateCartItemQuantityService updateCartItemQuantityService;
    private final RemoveCartItemService removeCartItemService;
    private final CheckoutCartService checkoutCartService;
    private final ConfirmOrderPaymentService confirmOrderPaymentService;
    private final ConsultOrderService consultOrderService;
    private final ConsultInvoiceService consultInvoiceService;
    private final RequestReturnService requestReturnService;
    private final ConsultReturnService consultReturnService;

    public BuyerUseCaseImpl(ConsultBuyerProfileService consultBuyerProfileService,
                            UpdateBuyerProfileService updateBuyerProfileService,
                            ConsultCatalogService consultCatalogService,
                            ConsultProductService consultProductService,
                            ConsultCartService consultCartService,
                            AddCartItemService addCartItemService,
                            UpdateCartItemQuantityService updateCartItemQuantityService,
                            RemoveCartItemService removeCartItemService,
                            CheckoutCartService checkoutCartService,
                            ConfirmOrderPaymentService confirmOrderPaymentService,
                            ConsultOrderService consultOrderService,
                            ConsultInvoiceService consultInvoiceService,
                            RequestReturnService requestReturnService,
                            ConsultReturnService consultReturnService) {
        this.consultBuyerProfileService = consultBuyerProfileService;
        this.updateBuyerProfileService = updateBuyerProfileService;
        this.consultCatalogService = consultCatalogService;
        this.consultProductService = consultProductService;
        this.consultCartService = consultCartService;
        this.addCartItemService = addCartItemService;
        this.updateCartItemQuantityService = updateCartItemQuantityService;
        this.removeCartItemService = removeCartItemService;
        this.checkoutCartService = checkoutCartService;
        this.confirmOrderPaymentService = confirmOrderPaymentService;
        this.consultOrderService = consultOrderService;
        this.consultInvoiceService = consultInvoiceService;
        this.requestReturnService = requestReturnService;
        this.consultReturnService = consultReturnService;
    }

    @Override
    public Buyer consultMyProfile(User user) {
        return consultBuyerProfileService.execute(user);
    }

    @Override
    public Buyer updateMyProfile(User user, Buyer buyer) {
        return updateBuyerProfileService.execute(user, buyer);
    }

    @Override
    public List<Product> consultCatalog(User user) {
        return consultCatalogService.execute();
    }

    @Override
    public Product consultProduct(User user, Product product) {
        return consultProductService.execute(product);
    }

    @Override
    public Cart consultMyCart(User user) {
        return consultCartService.execute(user);
    }

    @Override
    public Cart addItemToCart(User user, CartItem cartItem) {
        return addCartItemService.execute(user, cartItem);
    }

    @Override
    public Cart updateCartItemQuantity(User user, CartItem cartItem) {
        return updateCartItemQuantityService.execute(user, cartItem);
    }

    @Override
    public Cart removeItemFromCart(User user, CartItem cartItem) {
        return removeCartItemService.execute(user, cartItem);
    }

    @Override
    public Order checkoutCart(User user) {
        return checkoutCartService.execute(user);
    }

    @Override
    public Order confirmOrderPayment(User user, Order order) {
        return confirmOrderPaymentService.execute(user, order);
    }

    @Override
    public List<Order> consultMyOrders(User user) {
        return consultOrderService.executeMyOrders(user);
    }

    @Override
    public Order consultMyOrder(User user, Order order) {
        return consultOrderService.execute(user, order);
    }

    @Override
    public List<Invoice> consultMyInvoices(User user) {
        return consultInvoiceService.execute(user);
    }

    @Override
    public Invoice consultOrderInvoice(User user, Order order) {
        return consultInvoiceService.executeByOrder(user, order);
    }

    @Override
    public ReturnRefund requestReturn(User user, ReturnRefund returnRefund) {
        return requestReturnService.execute(user, returnRefund);
    }

    @Override
    public List<ReturnRefund> consultMyReturns(User user) {
        return consultReturnService.executeMyReturns(user);
    }
}
