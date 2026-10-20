package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ReturnRefundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataJpaReturnRefundRepository extends JpaRepository<ReturnRefundJpaEntity, String> {

    Optional<ReturnRefundJpaEntity> findFirstByOrderIdentifier(String orderIdentifier);

    List<ReturnRefundJpaEntity> findAllByOrderIdentifierIn(Collection<String> orderIdentifiers);

    List<ReturnRefundJpaEntity> findAllByStatus(String status);
}
