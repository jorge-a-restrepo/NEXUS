package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.domain.Warehouse;
import application.domain.WarehouseType;

/**
 * Bidirectional mapper between Warehouse and WarehouseJpaEntity.
 */
public final class WarehouseJpaMapper {

    private WarehouseJpaMapper() {
    }

    public static WarehouseJpaEntity toEntity(Warehouse domain) {
        if (domain == null) {
            return null;
        }
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setWarehouseType(DomainReferences.name(domain.getWarehouseType()));
        entity.setSellerIdentifier(DomainReferences.identifierOf(domain.getSeller()));
        return entity;
    }

    public static Warehouse toDomain(WarehouseJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Warehouse domain = new Warehouse();
        domain.setIdentifier(entity.getIdentifier());
        domain.setWarehouseType(DomainReferences.toEnum(WarehouseType.class, entity.getWarehouseType()));
        domain.setSeller(DomainReferences.seller(entity.getSellerIdentifier()));
        return domain;
    }
}
