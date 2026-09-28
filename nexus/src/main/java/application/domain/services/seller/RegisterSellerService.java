package application.domain.services.seller;

import application.domain.Seller;
import application.domain.User;
import application.domain.UserRole;
import application.domain.UserStatus;
import application.domain.Warehouse;
import application.domain.WarehouseType;
import application.domain.exceptions.DomainException;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

/**
 * Onboards a seller together with its first warehouse.
 *
 * Domain 3 business rule: sellers cannot self-register; they are incorporated
 * by the ADMINISTRATOR. The business flow step "Incorporación" states that the
 * administrator registers the seller and its first warehouse, so both are
 * created in the same operation and a seller never exists without a warehouse.
 *
 * Section 11: identity document and email must be unique platform-wide.
 * Domain 4: the warehouse created here is of type SELLER.
 */
@Service
public class RegisterSellerService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public RegisterSellerService(SellerRepositoryPort sellerRepositoryPort,
                                 UserRepositoryPort userRepositoryPort,
                                 WarehouseRepositoryPort warehouseRepositoryPort,
                                 PasswordServicePort passwordServicePort,
                                 AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.passwordServicePort = passwordServicePort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public Seller execute(User user, Seller seller, Warehouse firstWarehouse) {
        authorizeAdministratorOperationService.execute(user);
        if (seller == null) {
            throw new DomainException("Seller information must be provided.");
        }
        if (firstWarehouse == null) {
            throw new DomainException("A seller must be registered together with its first warehouse.");
        }
        if (seller.getFullName() == null || seller.getFullName().isBlank()) {
            throw new DomainException("Full name must be provided.");
        }
        if (seller.getEmail() == null || seller.getEmail().isBlank()) {
            throw new DomainException("Email must be provided.");
        }
        if (seller.getIdentityDocument() == null || seller.getIdentityDocument().isBlank()) {
            throw new DomainException("Identity document must be provided.");
        }
        if (seller.getPassword() == null || seller.getPassword().isBlank()) {
            throw new DomainException("A password must be provided for the seller.");
        }
        if (firstWarehouse.getIdentifier() == null || firstWarehouse.getIdentifier().isBlank()) {
            throw new DomainException("Warehouse identifier must be provided.");
        }
        if (userRepositoryPort.existsByEmail(seller)) {
            throw new DomainException("The email is already registered in the platform.");
        }
        if (userRepositoryPort.existsByIdentityDocument(seller)) {
            throw new DomainException("The identity document is already registered in the platform.");
        }
        if (warehouseRepositoryPort.existsByIdentifier(firstWarehouse)) {
            throw new DomainException("A warehouse with the same identifier already exists.");
        }
        seller.setPassword(passwordServicePort.hash(seller.getPassword()));
        seller.setRole(UserRole.SELLER);
        seller.setStatus(UserStatus.ACTIVE);
        Seller storedSeller = sellerRepositoryPort.save(seller);
        firstWarehouse.setWarehouseType(WarehouseType.SELLER);
        firstWarehouse.setSeller(storedSeller);
        warehouseRepositoryPort.save(firstWarehouse);
        return storedSeller;
    }
}
