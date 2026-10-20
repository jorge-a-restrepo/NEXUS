package application.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Login response: the JWT, how to send it (Authorization: Bearer token), its
 * lifetime in seconds and the authenticated user.
 */
@Data
public class LoginResponseDTO {

    private String token;
    private String tokenType;
    private long expiresIn;
    private UserResponseDTO user;
}
