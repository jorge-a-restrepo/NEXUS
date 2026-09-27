package application.domain.ports.out;

import application.domain.Buyer;
import application.domain.Invoice;
import application.domain.Order;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Invoice persistence and querying.
 *
 * OBJ-09: the invoice records the commercial information of a sale and is
 * generated once the order is paid. An invoice is immutable once issued, so the
 * contract exposes creation and querying only.
 */
public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findByOrder(Order order);

    List<Invoice> findAllByBuyer(Buyer buyer);

    List<Invoice> findAll();
}
