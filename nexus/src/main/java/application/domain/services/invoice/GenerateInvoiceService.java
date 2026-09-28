package application.domain.services.invoice;

import application.domain.Invoice;
import application.domain.Order;
import application.domain.OrderItem;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OrderItemRepositoryPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Issues the invoice of a paid order.
 *
 * OBJ-09: invoicing records the commercial information of the sale. The total
 * is calculated from the order lines using the unit price frozen at purchase
 * time, never from the current catalog price.
 *
 * An order can only be invoiced once: the invoice is an immutable record.
 *
 * Invoked by the payment confirmation flow; authorization is performed by the
 * calling service.
 */
@Service
public class GenerateInvoiceService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;

    public GenerateInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort,
                                  OrderItemRepositoryPort orderItemRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.orderItemRepositoryPort = orderItemRepositoryPort;
    }

    public Invoice execute(Order order) {
        if (order == null) {
            throw new EntityNotFoundException("Order");
        }
        if (invoiceRepositoryPort.findByOrder(order).isPresent()) {
            throw new DomainException("The order has already been invoiced.");
        }
        List<OrderItem> orderItems = orderItemRepositoryPort.findAllByOrder(order);
        if (orderItems == null || orderItems.isEmpty()) {
            throw new DomainException("An order without items cannot be invoiced.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getUnitPrice() == null || orderItem.getQuantity() == null) {
                throw new DomainException("Every order item must have a unit price and a quantity.");
            }
            total = total.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("The invoice total must be greater than zero.");
        }
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setTotalAmount(total);
        invoice.setIssueDate(LocalDateTime.now());
        return invoiceRepositoryPort.save(invoice);
    }
}
