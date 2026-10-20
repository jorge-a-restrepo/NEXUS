package application.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Authenticated user data returned by the login endpoint. Never includes the
 * password hash nor the identity document.
 */
@Data
public class UserResponseDTO {

    private String identifier;
    private String fullName;
    private String email;
    private String role;
    private String status;
}
