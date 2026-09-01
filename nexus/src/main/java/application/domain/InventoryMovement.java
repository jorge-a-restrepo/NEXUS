package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a recorded change over the stock of a specific Inventory.
 * Domain 6 explicitly lists the possible movement types.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    private Inventory inventory;
    private MovementType movementType;
    private Integer quantity;
    private LocalDateTime date;
    private User responsibleUser;
}
