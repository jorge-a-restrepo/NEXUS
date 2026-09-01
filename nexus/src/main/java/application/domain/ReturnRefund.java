package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * INFERRED CLASS.
 * Represents a buyer's request to return an order and receive
 * compensation. The source document includes "Return management" and
 * "Refund management" within scope (OBJ-11, section 3.1) but does not
 * develop a full domain with attributes.
 * DESIGN DECISION: Return and Refund are kept merged into a single class.
 * In a real system they could be modeled as independent concepts (one
 * logistics-related, one financial), but the source document does not
 * develop them with enough detail to justify two full entities.
 * ItemReturnRefund (per-product return detail) is intentionally NOT
 * modeled - the source document does not require partial returns at the
 * individual product level.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRefund {

    private Order order;
    private String reason;
    private ReturnStatus status;
    private BigDecimal refundedAmount;
}
