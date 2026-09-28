package application.domain.services.invoice;

import application.domain.Buyer;
import application.domain.Invoice;
import application.domain.Order;
import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Retrieves the invoices of the authenticated buyer.
 *
 * Domain 2 and RG-03: a buyer only accesses its own commercial information.
 */
@Service
public class ConsultInvoiceService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public ConsultInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort,
                                 AuthorizeBuyerOperationService authorizeBuyerOperationService,
                                 ValidateBuyerOwnershipService validateBuyerOwnershipService) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateBuyerOwnershipService = validateBuyerOwnershipService;
    }

    public List<Invoice> execute(User user) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        return invoiceRepositoryPort.findAllByBuyer(buyer);
    }

    public Invoice executeByOrder(User user, Order order) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        validateBuyerOwnershipService.execute(buyer, order);
        Optional<Invoice> invoiceOptional = invoiceRepositoryPort.findByOrder(order);
        if (invoiceOptional.isEmpty()) {
            throw new EntityNotFoundException("Invoice");
        }
        Invoice invoice = invoiceOptional.get();
        validateBuyerOwnershipService.execute(buyer, invoice);
        return invoice;
    }
}
