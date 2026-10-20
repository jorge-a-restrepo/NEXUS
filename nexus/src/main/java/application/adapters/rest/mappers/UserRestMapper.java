package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.domain.AuthenticationResult;
import application.domain.User;

/**
 * Converts between the authentication REST DTOs and the domain models.
 */
public final class UserRestMapper {

    public static final String TOKEN_TYPE = "Bearer";

    private UserRestMapper() {
    }

    /**
     * User is abstract and the role is unknown before authenticating, so the
     * credentials travel in this minimal subclass. LoginService only reads the
     * email and the password, and returns the stored user of the real subclass.
     */
    private static final class LoginCredentials extends User {
    }

    public static User toDomain(LoginRequestDTO dto) {
        User credentials = new LoginCredentials();
        credentials.setEmail(dto.getEmail());
        credentials.setPassword(dto.getPassword());
        return credentials;
    }

    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDTO dto = new UserResponseDTO();
        dto.setIdentifier(user.getIdentifier());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        return dto;
    }

    public static LoginResponseDTO toLoginResponseDTO(AuthenticationResult result, long expiresInSeconds) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setToken(result.getToken());
        dto.setTokenType(TOKEN_TYPE);
        dto.setExpiresIn(expiresInSeconds);
        dto.setUser(toResponseDTO(result.getUser()));
        return dto;
    }
}
