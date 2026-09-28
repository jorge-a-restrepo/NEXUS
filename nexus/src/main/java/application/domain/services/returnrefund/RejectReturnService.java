package application.domain.services.returnrefund;

import application.domain.ReturnRefund;
import application.domain.ReturnStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.ReturnRefundRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Rejects a return request.
 *
 * A rejected request is final: no refund is produced and no stock returns to
 * the inventory.
 */
@Service
public class RejectReturnService {

    private final ReturnRefundRepositoryPort returnRefundRepositoryPort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public RejectReturnService(ReturnRefundRepositoryPort returnRefundRepositoryPort,
                               AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.returnRefundRepositoryPort = returnRefundRepositoryPort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public ReturnRefund execute(User user, ReturnRefund returnRefund) {
        authorizeAdministratorOperationService.execute(user);
        if (returnRefund == null || returnRefund.getOrder() == null) {
            throw new DomainException("The return request must reference an order.");
        }
        Optional<ReturnRefund> storedOptional = returnRefundRepositoryPort.findByOrder(returnRefund.getOrder());
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Return request");
        }
        ReturnRefund stored = storedOptional.get();
        if (!ReturnStatus.REQUESTED.equals(stored.getStatus())) {
            throw new DomainException("Only a requested return can be rejected.");
        }
        stored.setStatus(ReturnStatus.REJECTED);
        return returnRefundRepositoryPort.update(stored);
    }
}
