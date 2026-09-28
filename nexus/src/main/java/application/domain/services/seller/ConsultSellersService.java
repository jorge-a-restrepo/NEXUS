package application.domain.services.seller;

import application.domain.Seller;
import application.domain.User;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves the registered sellers.
 *
 * OBJ-02 and OBJ-12: seller administration belongs to the administrator, with
 * the supervisor following the operation up.
 */
@Service
public class ConsultSellersService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public ConsultSellersService(SellerRepositoryPort sellerRepositoryPort,
                                 AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService) {
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.authorizeAdministrativeConsultationService = authorizeAdministrativeConsultationService;
    }

    public List<Seller> execute(User user) {
        authorizeAdministrativeConsultationService.execute(user);
        return sellerRepositoryPort.findAll();
    }
}
