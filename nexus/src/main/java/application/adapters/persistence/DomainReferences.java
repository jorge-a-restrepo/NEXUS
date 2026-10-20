package application.adapters.persistence;

import application.domain.Administrator;
import application.domain.Buyer;
import application.domain.Cart;
import application.domain.LogisticsOperator;
import application.domain.Order;
import application.domain.Product;
import application.domain.Seller;
import application.domain.Supervisor;
import application.domain.User;
import application.domain.UserRole;
import application.domain.Warehouse;

import java.util.UUID;

/**
 * Helpers shared by the persistence mappers.
 *
 * Relations are stored as the identifier of the related aggregate (as in the
 * reference project). When a mapper only needs the identity of a related
 * object, it rebuilds a reference: an instance holding only its identifier.
 */
public final class DomainReferences {

    private DomainReferences() {
    }

    /** Identifiers of User, Order, Cart, Shipment and ReturnRefund are generated here. */
    public static String newIdentifier() {
        return UUID.randomUUID().toString();
    }

    public static String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    public static <E extends Enum<E>> E toEnum(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }

    /**
     * User is abstract: the stored role decides the subclass, because the
     * authorization services check it (for example "user instanceof Buyer").
     */
    public static User newUserForRole(UserRole role) {
        if (role == null) {
            throw new IllegalStateException("A stored user must have a role.");
        }
        return switch (role) {
            case BUYER -> new Buyer();
            case SELLER -> new Seller();
            case LOGISTICS_OPERATOR -> new LogisticsOperator();
            case ADMINISTRATOR -> new Administrator();
            case SUPERVISOR -> new Supervisor();
        };
    }

    public static User user(String identifier, UserRole role) {
        if (identifier == null || role == null) {
            return null;
        }
        User user = newUserForRole(role);
        user.setIdentifier(identifier);
        user.setRole(role);
        return user;
    }

    public static Buyer buyer(String identifier) {
        return (Buyer) user(identifier, UserRole.BUYER);
    }

    public static Seller seller(String identifier) {
        return (Seller) user(identifier, UserRole.SELLER);
    }

    public static LogisticsOperator logisticsOperator(String identifier) {
        return (LogisticsOperator) user(identifier, UserRole.LOGISTICS_OPERATOR);
    }

    public static Product product(String identifier) {
        if (identifier == null) {
            return null;
        }
        Product product = new Product();
        product.setIdentifier(identifier);
        return product;
    }

    public static Warehouse warehouse(String identifier) {
        if (identifier == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier(identifier);
        return warehouse;
    }

    public static Order order(String identifier) {
        if (identifier == null) {
            return null;
        }
        Order order = new Order();
        order.setIdentifier(identifier);
        return order;
    }

    public static Cart cart(String identifier) {
        if (identifier == null) {
            return null;
        }
        Cart cart = new Cart();
        cart.setIdentifier(identifier);
        return cart;
    }

    public static String identifierOf(User user) {
        return user == null ? null : user.getIdentifier();
    }

    public static String identifierOf(Product product) {
        return product == null ? null : product.getIdentifier();
    }

    public static String identifierOf(Warehouse warehouse) {
        return warehouse == null ? null : warehouse.getIdentifier();
    }

    public static String identifierOf(Order order) {
        return order == null ? null : order.getIdentifier();
    }

    public static String identifierOf(Cart cart) {
        return cart == null ? null : cart.getIdentifier();
    }
}
