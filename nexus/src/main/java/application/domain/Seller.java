package application.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user responsible for registering and managing their own
 * products in the catalog. Cannot self-register: is onboarded exclusively
 * by an Administrator, together with their first warehouse. Extends User.
 * No additional attributes beyond User (relations to Administrator and
 * Warehouse are represented as UML associations, not as attributes).
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Seller extends User {
}
