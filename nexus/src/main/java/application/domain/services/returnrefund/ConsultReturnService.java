package application.domain.services.returnrefund;

import application.domain.Buyer;
import application.domain.ReturnRefund;
import application.domain.ReturnStatus;
import application.domain.User;
import application.domain.ports.out.ReturnRefundRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves return and refund information.
 *
 * A buyer only sees its own requests (Domain 2). The administrator works the
 * pending queue and the supervisor follows the process up (OBJ-12).
 */
@Service
public class ConsultReturnService {

    private final ReturnRefundRepositoryPort returnRefundRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public ConsultReturnService(ReturnRefundRepositoryPort returnRefundRepositoryPort,
                                AuthorizeBuyerOperationService authorizeBuyerOperationService,
                                AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService) {
        this.returnRefundRepositoryPort = returnRefundRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.authorizeAdministrativeConsultationService = authorizeAdministrativeConsultationService;
    }

    public List<ReturnRefund> executeMyReturns(User user) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        return returnRefundRepositoryPort.findAllByBuyer(buyer);
    }

    public List<ReturnRefund> executePending(User user) {
        authorizeAdministrativeConsultationService.execute(user);
        return returnRefundRepositoryPort.findAllByStatus(ReturnStatus.REQUESTED);
    }

    public List<ReturnRefund> executeAll(User user) {
        authorizeAdministrativeConsultationService.execute(user);
        return returnRefundRepositoryPort.findAll();
    }
}
