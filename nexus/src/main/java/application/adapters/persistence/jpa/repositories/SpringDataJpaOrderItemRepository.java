package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJpaOrderItemRepository extends JpaRepository<OrderItemJpaEntity, Long> {

    List<OrderItemJpaEntity> findAllByOrderIdentifier(String orderIdentifier);

    List<OrderItemJpaEntity> findAllByProductIdentifier(String productIdentifier);
}
