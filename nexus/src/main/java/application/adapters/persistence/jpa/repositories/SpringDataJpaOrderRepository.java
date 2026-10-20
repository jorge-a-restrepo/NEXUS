package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJpaOrderRepository extends JpaRepository<OrderJpaEntity, String> {

    List<OrderJpaEntity> findAllByBuyerIdentifier(String buyerIdentifier);

    List<OrderJpaEntity> findAllByStatus(String status);
}
