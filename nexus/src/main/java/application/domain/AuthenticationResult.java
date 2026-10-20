package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Result of a successful authentication (RG-01): the authenticated user and the
 * access token issued for it through TokenServicePort.
 *
 * It is not a business entity of the functional specification and is never
 * persisted; it is the value object returned by LoginService so that the
 * delivery adapter receives the token without calling an output port.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResult {

    private User user;
    private String token;
}
