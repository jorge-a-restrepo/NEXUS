package application.domain;

/**
 * Status of a ReturnRefund request. Inferred - necessary to manage the
 * Requested/Approved/Rejected/Refunded cycle.
 */
public enum ReturnStatus {
    REQUESTED,
    APPROVED,
    REJECTED,
    REFUNDED
}
