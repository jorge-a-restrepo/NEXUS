package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * INFERRED CLASS.
 * Represents the commercial information associated with the sale of an
 * order. The source document includes "Billing management" within scope
 * (OBJ-09, section 3.1) but does not develop a full domain with its own
 * attribute table. Attributes are limited to the minimum required for the
 * concept of "invoice" to make sense - no elaborate attributes (taxes,
 * subtotal, payment method, tax id) that the document does not mention.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    private Order order;
    private BigDecimal totalAmount;
    private LocalDateTime issueDate;
}
