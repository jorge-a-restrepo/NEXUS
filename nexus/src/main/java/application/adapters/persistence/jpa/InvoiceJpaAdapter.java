package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInvoiceRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.domain.Buyer;
import application.domain.Invoice;
import application.domain.Order;
import application.domain.ports.out.InvoiceRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for invoices (MySQL, table invoices). The invoices
 * of a buyer are found through the buyer's orders.
 */
@Repository
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private final SpringDataJpaInvoiceRepository repository;
    private final SpringDataJpaOrderRepository orderRepository;
    private final JpaReferenceResolver resolver;

    public InvoiceJpaAdapter(SpringDataJpaInvoiceRepository repository,
                             SpringDataJpaOrderRepository orderRepository,
                             JpaReferenceResolver resolver) {
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.resolver = resolver;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return toDomain(repository.save(InvoiceJpaMapper.toEntity(invoice)));
    }

    @Override
    public Optional<Invoice> findByOrder(Order order) {
        String orderId = DomainReferences.identifierOf(order);
        if (orderId == null) {
            return Optional.empty();
        }
        return repository.findById(orderId).map(this::toDomain);
    }

    @Override
    public List<Invoice> findAllByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentifier() == null) {
            return List.of();
        }
        List<String> orderIds = orderRepository.findAllByBuyerIdentifier(buyer.getIdentifier()).stream()
                .map(OrderJpaEntity::getIdentifier)
                .toList();
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllByOrderIdentifierIn(orderIds).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Invoice> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private Invoice toDomain(InvoiceJpaEntity entity) {
        return InvoiceJpaMapper.toDomain(entity, resolver.order(entity.getOrderIdentifier()));
    }
}
