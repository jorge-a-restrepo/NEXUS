package application.adapters.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity (repository DTO) for every user role.
 *
 * The User hierarchy is flattened into one table; the role column acts as the
 * discriminator (RG-02: one role per user). Email and identity document are
 * unique at database level as a second line of defense for RG-05. Buyer-only
 * columns stay null for the other roles. The domain never references this class.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;

    @Column(name = "identity_document", unique = true, length = 60)
    private String identityDocument;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    /** BCrypt hash, never the plain password. */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /** UserRole enum name; discriminator of the subclass. */
    @Column(name = "role", nullable = false, length = 30)
    private String role;

    /** UserStatus enum name. */
    @Column(name = "status", length = 20)
    private String status;

    /** Buyer.mainAddress. */
    @Column(name = "main_address", length = 200)
    private String mainAddress;

    /** Buyer.commercialStatus (CommercialStatus enum name). */
    @Column(name = "commercial_status", length = 20)
    private String commercialStatus;

    /** Buyer.additionalAddresses. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_additional_addresses", joinColumns = @JoinColumn(name = "user_identifier"))
    @Column(name = "address", length = 200)
    private List<String> additionalAddresses = new ArrayList<>();
}
