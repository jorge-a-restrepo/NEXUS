package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.ProductType;

import java.util.ArrayList;

/**
 * Bidirectional mapper between Product and ProductJpaEntity.
 */
public final class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static ProductJpaEntity toEntity(Product domain) {
        if (domain == null) {
            return null;
        }
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setName(domain.getName());
        entity.setSellerIdentifier(DomainReferences.identifierOf(domain.getSeller()));
        entity.setProductType(DomainReferences.name(domain.getProductType()));
        entity.setStatus(DomainReferences.name(domain.getStatus()));
        entity.setBasePrice(domain.getBasePrice());
        entity.setVariants(domain.getVariants() != null ? new ArrayList<>(domain.getVariants()) : new ArrayList<>());
        return entity;
    }

    public static Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Product domain = new Product();
        domain.setIdentifier(entity.getIdentifier());
        domain.setName(entity.getName());
        domain.setSeller(DomainReferences.seller(entity.getSellerIdentifier()));
        domain.setProductType(DomainReferences.toEnum(ProductType.class, entity.getProductType()));
        domain.setStatus(DomainReferences.toEnum(ProductStatus.class, entity.getStatus()));
        domain.setBasePrice(entity.getBasePrice());
        domain.setVariants(entity.getVariants() != null ? new ArrayList<>(entity.getVariants()) : new ArrayList<>());
        return domain;
    }
}
