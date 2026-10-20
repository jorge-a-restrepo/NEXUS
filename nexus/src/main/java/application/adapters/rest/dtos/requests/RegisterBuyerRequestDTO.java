package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request body for the public buyer self-registration endpoint.
 *
 * Only text fields use @NotBlank: that constraint applies to character
 * sequences only. Role, status and commercial status are not accepted from the
 * client; the domain service assigns them.
 */
@Data
public class RegisterBuyerRequestDTO {

    @NotBlank
    @Size(max = 60)
    private String identityDocument;

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    /** BCrypt only considers the first 72 bytes of a password. */
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    @Size(max = 200)
    private String mainAddress;

    private List<@Size(max = 200) String> additionalAddresses;
}
