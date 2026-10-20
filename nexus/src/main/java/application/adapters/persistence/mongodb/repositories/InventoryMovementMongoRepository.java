package application.adapters.persistence.mongodb.repositories;

import application.adapters.persistence.mongodb.documents.InventoryMovementDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryMovementMongoRepository extends MongoRepository<InventoryMovementDocument, String> {

    List<InventoryMovementDocument> findAllByProductIdentifierAndWarehouseIdentifier(String productIdentifier,
                                                                                     String warehouseIdentifier);

    List<InventoryMovementDocument> findAllByMovementType(String movementType);

    List<InventoryMovementDocument> findAllByResponsibleUserIdentifier(String responsibleUserIdentifier);
}
