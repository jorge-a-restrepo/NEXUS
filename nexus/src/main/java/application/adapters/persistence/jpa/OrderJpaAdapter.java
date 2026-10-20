package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.domain.Buyer;
import application.domain.Order;
import application.domain.OrderStatus;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for orders (MySQL, table orders). The order
 * identifier is generated here when the cart is converted.
 */
@Repository
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final SpringDataJpaOrderRepository repository;

    public OrderJpaAdapter(SpringDataJpaOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        if (order.getIdentifier() == null) {
            order.setIdentifier(DomainReferences.newIdentifier());
        }
        return OrderJpaMapper.toDomain(repository.save(OrderJpaMapper.toEntity(order)));
    }

    @Override
    public Order update(Order order) {
        return OrderJpaMapper.toDomain(repository.save(OrderJpaMapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findByIdentifier(Order order) {
        if (order == null || order.getIdentifier() == null) {
            return Optional.empty();
        }
        return repository.findById(order.getIdentifier()).map(OrderJpaMapper::toDomain);
    }

    @Override
    public List<Order> findAllByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentifier() == null) {
            return List.of();
        }
        return repository.findAllByBuyerIdentifier(buyer.getIdentifier()).stream()
                .map(OrderJpaMapper::toDomain).toList();
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return List.of();
        }
        return repository.findAllByStatus(orderStatus.name()).stream().map(OrderJpaMapper::toDomain).toList();
    }

    @Override
    public List<Order> findAll() {
        return repository.findAll().stream().map(OrderJpaMapper::toDomain).toList();
    }
}
