package application.domain.services.buyer;

import application.domain.Buyer;
import application.domain.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves the registered buyers.
 *
 * OBJ-03 and OBJ-12: buyer administration and consolidated consultation belong
 * to the administrator, with the supervisor following the operation up.
 */
@Service
public class ConsultBuyersService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public ConsultBuyersService(BuyerRepositoryPort buyerRepositoryPort,
                                AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService) {
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.authorizeAdministrativeConsultationService = authorizeAdministrativeConsultationService;
    }

    public List<Buyer> execute(User user) {
        authorizeAdministrativeConsultationService.execute(user);
        return buyerRepositoryPort.findAll();
    }
}
