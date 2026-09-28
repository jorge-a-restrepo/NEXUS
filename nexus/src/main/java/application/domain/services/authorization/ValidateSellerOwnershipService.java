package application.domain.services.authorization;

import application.domain.Product;
import application.domain.Seller;
import application.domain.Warehouse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.OwnershipViolationException;
import org.springframework.stereotype.Service;

/**
 * Validates that a seller only operates over its own products and warehouses.
 *
 * RG-03: no participant may administer information outside its role scope, and
 * Domain 3 makes each seller responsible for its own catalog.
 *
 * Ownership is compared through the seller identifier, which is the identity
 * attribute of the User hierarchy.
 */
@Service
public class ValidateSellerOwnershipService {

    public void execute(Seller seller, Product product) {
        if (product == null) {
            throw new EntityNotFoundException("Product");
        }
        requireSameSeller(seller, product.getSeller(), "product");
    }

    public void execute(Seller seller, Warehouse warehouse) {
        if (warehouse == null) {
            throw new EntityNotFoundException("Warehouse");
        }
        requireSameSeller(seller, warehouse.getSeller(), "warehouse");
    }

    private void requireSameSeller(Seller seller, Seller ownerSeller, String resourceName) {
        if (seller == null || seller.getIdentifier() == null) {
            throw new OwnershipViolationException("The requesting seller could not be identified.");
        }
        if (ownerSeller == null || ownerSeller.getIdentifier() == null) {
            throw new OwnershipViolationException("The owner of the " + resourceName + " could not be identified.");
        }
        if (!seller.getIdentifier().equals(ownerSeller.getIdentifier())) {
            throw new OwnershipViolationException("The " + resourceName + " does not belong to the requesting seller.");
        }
    }
}
