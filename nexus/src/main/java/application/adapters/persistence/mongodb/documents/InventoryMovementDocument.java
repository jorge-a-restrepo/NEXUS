package application.adapters.persistence.mongodb.documents;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB persistence document (repository DTO) for InventoryMovement.
 *
 * SUPUESTO (decision D1): inventory movements are the immutable history of every
 * stock change. They are only inserted and queried, never updated, which is the
 * same role the operation log plays in the reference project, so they are stored
 * in MongoDB (database nexus_audit_db, collection inventory_movements).
 *
 * The inventory (product + warehouse) and the responsible user are denormalized
 * into plain values, so a movement can be read without loading other aggregates.
 */
@Document(collection = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementDocument {

    @Id
    private String id;

    /** MovementType enum name. */
    private String movementType;

    private Integer quantity;

    private LocalDateTime date;

    /** InventoryMovement.inventory, denormalized by its real key. */
    private String productIdentifier;
    private String warehouseIdentifier;

    /** InventoryMovement.responsibleUser, denormalized. */
    private String responsibleUserIdentifier;
    private String responsibleUserRole;
}
