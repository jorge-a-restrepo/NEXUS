package application.domain.services.user;

import application.domain.Buyer;
import application.domain.UserRole;
import application.domain.UserStatus;
import application.domain.exceptions.DomainException;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

/**
 * Registers a new buyer in the platform.
 *
 * Section 3.1 lists buyer registration as a public process, in contrast with
 * sellers, who are onboarded by an administrator (Domain 3).
 *
 * Section 11 critical validation: identity document and email must be unique
 * across the whole platform, so uniqueness is checked against the complete user
 * base and not only against other buyers.
 *
 * RG-02: the buyer is created with exactly one role, BUYER, and starts in
 * ACTIVE status so it can operate immediately.
 *
 * The password never reaches the stored state in clear text: it is hashed
 * through the password output port, whose algorithm lives outside the domain.
 */
@Service
public class RegisterBuyerUserService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    public RegisterBuyerUserService(BuyerRepositoryPort buyerRepositoryPort,
                                    UserRepositoryPort userRepositoryPort,
                                    PasswordServicePort passwordServicePort) {
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.passwordServicePort = passwordServicePort;
    }

    public Buyer execute(Buyer buyer) {
        if (buyer == null) {
            throw new DomainException("Buyer information must be provided.");
        }
        if (buyer.getFullName() == null || buyer.getFullName().isBlank()) {
            throw new DomainException("Full name must be provided.");
        }
        if (buyer.getEmail() == null || buyer.getEmail().isBlank()) {
            throw new DomainException("Email must be provided.");
        }
        if (buyer.getIdentityDocument() == null || buyer.getIdentityDocument().isBlank()) {
            throw new DomainException("Identity document must be provided.");
        }
        if (buyer.getPassword() == null || buyer.getPassword().isBlank()) {
            throw new DomainException("A password must be provided.");
        }
        if (userRepositoryPort.existsByEmail(buyer)) {
            throw new DomainException("The email is already registered in the platform.");
        }
        if (userRepositoryPort.existsByIdentityDocument(buyer)) {
            throw new DomainException("The identity document is already registered in the platform.");
        }
        buyer.setPassword(passwordServicePort.hash(buyer.getPassword()));
        buyer.setRole(UserRole.BUYER);
        buyer.setStatus(UserStatus.ACTIVE);
        return buyerRepositoryPort.save(buyer);
    }
}
