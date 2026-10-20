package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataJpaInvoiceRepository extends JpaRepository<InvoiceJpaEntity, String> {

    List<InvoiceJpaEntity> findAllByOrderIdentifierIn(Collection<String> orderIdentifiers);
}
