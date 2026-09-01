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
    private UserRole role;
    private UserStatus status;
}
