package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ReturnRefundJpaEntity;
import application.adapters.persistence.jpa.mappers.ReturnRefundJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaReturnRefundRepository;
import application.domain.Buyer;
import application.domain.Order;
import application.domain.ReturnRefund;
import application.domain.ReturnStatus;
import application.domain.ports.out.ReturnRefundRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for return and refund requests (MySQL, table
 * return_refunds). The requests of a buyer are found through the buyer's orders.
 */
@Repository
public class ReturnRefundJpaAdapter implements ReturnRefundRepositoryPort {

    private final SpringDataJpaReturnRefundRepository repository;
    private final SpringDataJpaOrderRepository orderRepository;
    private final JpaReferenceResolver resolver;

    public ReturnRefundJpaAdapter(SpringDataJpaReturnRefundRepository repository,
                                  SpringDataJpaOrderRepository orderRepository,
                                  JpaReferenceResolver resolver) {
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.resolver = resolver;
    }

    @Override
    public ReturnRefund save(ReturnRefund returnRefund) {
        if (returnRefund.getIdentifier() == null) {
            returnRefund.setIdentifier(DomainReferences.newIdentifier());
        }
        return toDomain(repository.save(ReturnRefundJpaMapper.toEntity(returnRefund)));
    }

    @Override
    public ReturnRefund update(ReturnRefund returnRefund) {
        return save(returnRefund);
    }

    @Override
    public Optional<ReturnRefund> findByOrder(Order order) {
        String orderId = DomainReferences.identifierOf(order);
        if (orderId == null) {
            return Optional.empty();
        }
        return repository.findFirstByOrderIdentifier(orderId).map(this::toDomain);
    }

    @Override
    public List<ReturnRefund> findAllByBuyer(Buyer buyer) {
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
    public List<ReturnRefund> findAllByStatus(ReturnStatus returnStatus) {
        if (returnStatus == null) {
            return List.of();
        }
        return repository.findAllByStatus(returnStatus.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ReturnRefund> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private ReturnRefund toDomain(ReturnRefundJpaEntity entity) {
        return ReturnRefundJpaMapper.toDomain(entity, resolver.order(entity.getOrderIdentifier()));
    }
}
