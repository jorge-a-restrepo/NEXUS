package application.domain.ports.out;

import application.domain.Buyer;
import application.domain.Order;
import application.domain.ReturnRefund;
import application.domain.ReturnStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port for ReturnRefund persistence and querying.
 *
 * OBJ-11 and the responsibility matrix: a return is requested by the Buyer and
 * resolved by the Administrator, so querying by status supports the
 * administrator's pending-requests view.
 */
public interface ReturnRefundRepositoryPort {

    ReturnRefund save(ReturnRefund returnRefund);

    ReturnRefund update(ReturnRefund returnRefund);

    Optional<ReturnRefund> findByOrder(Order order);

    List<ReturnRefund> findAllByBuyer(Buyer buyer);

    List<ReturnRefund> findAllByStatus(ReturnStatus returnStatus);

    List<ReturnRefund> findAll();
}
