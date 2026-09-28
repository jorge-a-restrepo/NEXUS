package application.domain.services.returnrefund;

import application.domain.Invoice;
import application.domain.ReturnRefund;
import application.domain.ReturnStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.ReturnRefundRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Settles the refund of an approved return.
 *
 * OBJ-11: the refund closes the after-sales process. The amount is taken from
 * the invoice issued for the order, so the refund can never exceed what was
 * actually charged.
 */
@Service
public class ProcessRefundService {

    private final ReturnRefundRepositoryPort returnRefundRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public ProcessRefundService(ReturnRefundRepositoryPort returnRefundRepositoryPort,
                                InvoiceRepositoryPort invoiceRepositoryPort,
                                AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.returnRefundRepositoryPort = returnRefundRepositoryPort;
        this.invoiceRepositoryPort = invoiceRepositoryPort;
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
        if (!ReturnStatus.APPROVED.equals(stored.getStatus())) {
            throw new DomainException("Only an approved return can be refunded.");
        }
        Optional<Invoice> invoiceOptional = invoiceRepositoryPort.findByOrder(stored.getOrder());
        if (invoiceOptional.isEmpty()) {
            throw new EntityNotFoundException("Invoice of the order");
        }
        BigDecimal invoicedAmount = invoiceOptional.get().getTotalAmount();
        if (invoicedAmount == null || invoicedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("The invoiced amount is not valid for a refund.");
        }
        BigDecimal requestedAmount = returnRefund.getRefundedAmount();
        BigDecimal amountToRefund = requestedAmount == null ? invoicedAmount : requestedAmount;
        if (amountToRefund.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("The refunded amount must be greater than zero.");
        }
        if (amountToRefund.compareTo(invoicedAmount) > 0) {
            throw new DomainException("The refunded amount cannot exceed the invoiced amount.");
        }
        stored.setRefundedAmount(amountToRefund);
        stored.setStatus(ReturnStatus.REFUNDED);
        return returnRefundRepositoryPort.update(stored);
    }
}
