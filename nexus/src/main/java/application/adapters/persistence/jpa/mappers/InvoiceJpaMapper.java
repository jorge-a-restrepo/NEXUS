package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import application.domain.Invoice;
import application.domain.Order;

/**
 * Bidirectional mapper between Invoice and InvoiceJpaEntity. The order is
 * resolved by the adapter because ownership checks read its buyer.
 */
public final class InvoiceJpaMapper {

    private InvoiceJpaMapper() {
    }

    public static InvoiceJpaEntity toEntity(Invoice domain) {
        if (domain == null) {
            return null;
        }
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setOrderIdentifier(DomainReferences.identifierOf(domain.getOrder()));
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setIssueDate(domain.getIssueDate());
        return entity;
    }

    public static Invoice toDomain(InvoiceJpaEntity entity, Order order) {
        if (entity == null) {
            return null;
        }
        Invoice domain = new Invoice();
        domain.setOrder(order);
        domain.setTotalAmount(entity.getTotalAmount());
        domain.setIssueDate(entity.getIssueDate());
        return domain;
    }
}
