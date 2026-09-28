package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents any person authorized to interact with the NexusMarket system.
 * Abstract class centralizing common identity and status information shared
 * by all participants of the platform, regardless of their role.
 * Cannot be instantiated directly.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    private String identifier;
    private String identityDocument;
    private String fullName;
    private String email;
    /**
     * SUPUESTO: RG-01 requires every operation to be executed by an
     * authenticated user, which implies stored credentials. The source
     * document lists the user attributes without naming a credential, so this
     * attribute is inferred from the authentication requirement. It stores the
     * hashed value produced by the password output port, never the raw one.
     */
    private String password;
    private UserRole role;
    private UserStatus status;
}
