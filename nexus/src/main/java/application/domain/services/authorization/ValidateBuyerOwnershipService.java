package application.domain.services.authorization;

import application.domain.Buyer;
import application.domain.Cart;
import application.domain.Invoice;
import application.domain.Order;
import application.domain.ReturnRefund;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.OwnershipViolationException;
import org.springframework.stereotype.Service;

/**
 * Validates that a buyer only operates over information that belongs to it.
 *
 * Domain 2: "El comprador nunca administrará información de otros compradores."
 * RG-03: no participant may manage information outside its role scope.
 *
 * Ownership is compared through the buyer identifier, which is the identity
 * attribute of the User hierarchy.
 */
@Service
public class ValidateBuyerOwnershipService {

    public void execute(Buyer buyer, Cart cart) {
        if (cart == null) {
            throw new EntityNotFoundException("Cart");
        }
        requireSameBuyer(buyer, cart.getBuyer(), "cart");
    }

    public void execute(Buyer buyer, Order order) {
        if (order == null) {
            throw new EntityNotFoundException("Order");
        }
        requireSameBuyer(buyer, order.getBuyer(), "order");
    }

    public void execute(Buyer buyer, Invoice invoice) {
        if (invoice == null) {
            throw new EntityNotFoundException("Invoice");
        }
        if (invoice.getOrder() == null) {
            throw new EntityNotFoundException("Order of the invoice");
        }
        requireSameBuyer(buyer, invoice.getOrder().getBuyer(), "invoice");
    }

    public void execute(Buyer buyer, ReturnRefund returnRefund) {
        if (returnRefund == null) {
            throw new EntityNotFoundException("Return request");
        }
        if (returnRefund.getOrder() == null) {
            throw new EntityNotFoundException("Order of the return request");
        }
        requireSameBuyer(buyer, returnRefund.getOrder().getBuyer(), "return request");
    }

    private void requireSameBuyer(Buyer buyer, Buyer ownerBuyer, String resourceName) {
        if (buyer == null || buyer.getIdentifier() == null) {
            throw new OwnershipViolationException("The requesting buyer could not be identified.");
        }
        if (ownerBuyer == null || ownerBuyer.getIdentifier() == null) {
            throw new OwnershipViolationException("The owner of the " + resourceName + " could not be identified.");
        }
        if (!buyer.getIdentifier().equals(ownerBuyer.getIdentifier())) {
            throw new OwnershipViolationException("The " + resourceName + " does not belong to the requesting buyer.");
        }
    }
}
