package application.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user responsible for the administration of sellers and
 * warehouses. The only role authorized to onboard new sellers. Extends
 * User. No additional attributes.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Administrator extends User {
}
