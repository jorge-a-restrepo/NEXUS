package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJpaProductRepository extends JpaRepository<ProductJpaEntity, String> {

    List<ProductJpaEntity> findAllByStatus(String status);

    List<ProductJpaEntity> findAllByProductType(String productType);

    List<ProductJpaEntity> findAllBySellerIdentifier(String sellerIdentifier);
}
