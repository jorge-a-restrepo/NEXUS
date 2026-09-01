package application.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user with an exclusive read-only, operational-monitoring
 * profile. Does not manage or modify business information. Extends User.
 * No additional attributes.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Supervisor extends User {
}
