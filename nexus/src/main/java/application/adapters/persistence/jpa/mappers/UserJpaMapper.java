package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.domain.Buyer;
import application.domain.CommercialStatus;
import application.domain.User;
import application.domain.UserRole;
import application.domain.UserStatus;

import java.util.ArrayList;

/**
 * Bidirectional mapper between the User hierarchy and UserJpaEntity.
 */
public final class UserJpaMapper {

    private UserJpaMapper() {
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity entity = new UserJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setIdentityDocument(domain.getIdentityDocument());
        entity.setFullName(domain.getFullName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setRole(DomainReferences.name(domain.getRole()));
        entity.setStatus(DomainReferences.name(domain.getStatus()));
        if (domain instanceof Buyer buyer) {
            entity.setMainAddress(buyer.getMainAddress());
            entity.setCommercialStatus(DomainReferences.name(buyer.getCommercialStatus()));
            entity.setAdditionalAddresses(buyer.getAdditionalAddresses() != null
                    ? new ArrayList<>(buyer.getAdditionalAddresses())
                    : new ArrayList<>());
        }
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        UserRole role = DomainReferences.toEnum(UserRole.class, entity.getRole());
        User domain = DomainReferences.newUserForRole(role);
        domain.setIdentifier(entity.getIdentifier());
        domain.setIdentityDocument(entity.getIdentityDocument());
        domain.setFullName(entity.getFullName());
        domain.setEmail(entity.getEmail());
        domain.setPassword(entity.getPassword());
        domain.setRole(role);
        domain.setStatus(DomainReferences.toEnum(UserStatus.class, entity.getStatus()));
        if (domain instanceof Buyer buyer) {
            buyer.setMainAddress(entity.getMainAddress());
            buyer.setCommercialStatus(DomainReferences.toEnum(CommercialStatus.class, entity.getCommercialStatus()));
            buyer.setAdditionalAddresses(entity.getAdditionalAddresses() != null
                    ? new ArrayList<>(entity.getAdditionalAddresses())
                    : new ArrayList<>());
        }
        return domain;
    }
}
