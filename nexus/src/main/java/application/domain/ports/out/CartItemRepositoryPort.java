package application.domain.ports.out;

import application.domain.Cart;
import application.domain.CartItem;
import application.domain.Product;

import java.util.List;
import java.util.Optional;

/**
 * Output port for CartItem persistence and querying.
 *
 * CartItem is currently modeled as an association class that references its
 * Cart, so its items are retrieved through this port. If Cart later holds its
 * items as a list attribute, this port is absorbed by CartRepositoryPort.
 */
public interface CartItemRepositoryPort {

    CartItem save(CartItem cartItem);

    CartItem update(CartItem cartItem);

    void delete(CartItem cartItem);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findAllByCart(Cart cart);
}
