package application.domain;

/**
 * Status of a Shipment. Inferred - necessary to know whether the shipment
 * is still in progress or already completed.
 */
public enum ShipmentStatus {
    IN_PROGRESS,
    COMPLETED
}
