package application.domain.ports.in;

import application.domain.Buyer;
import application.domain.Product;
import application.domain.User;

import java.util.List;

/**
 * Input port for operations that do not require a previously authenticated user.
 *
 * Covers authentication (RG-01), buyer self-registration (Section 3.1) and the
 * public catalog, which exposes only products in PUBLISHED status (Domain 5).
 *
 * Sellers are intentionally absent from this port: Domain 3 states they cannot
 * self-register and are onboarded exclusively by an Administrator.
 */
public interface PublicAccessPort {

    User login(User user);

    void logout(User user);

    Buyer registerBuyer(Buyer buyer);

    List<Product> consultPublicCatalog();

    Product consultPublicProduct(Product product);
}
