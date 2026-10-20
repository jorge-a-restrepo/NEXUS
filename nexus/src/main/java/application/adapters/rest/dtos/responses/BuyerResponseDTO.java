package application.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Buyer data returned to the client.
 *
 * The password hash and the identity document are never returned: combined with
 * name and email, the identity document is personally identifiable information.
 */
@Data
public class BuyerResponseDTO {

    private String identifier;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private String commercialStatus;
}
