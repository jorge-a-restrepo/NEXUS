package application.domain.services.warehouse;

import application.domain.Seller;
import application.domain.User;
import application.domain.Warehouse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Retrieves warehouse information.
 *
 * The administrator and the supervisor consult every warehouse (OBJ-04,
 * OBJ-12), while a seller only reaches its own ones (RG-03).
 *
 * SUPUESTO: the restriction for sellers depends on Warehouse referencing its
 * owning Seller, which the domain model does not declare yet.
 */
@Service
public class ConsultWarehouseService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;

    public ConsultWarehouseService(WarehouseRepositoryPort warehouseRepositoryPort,
                                   AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService,
                                   AuthorizeSellerOperationService authorizeSellerOperationService) {
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.authorizeAdministrativeConsultationService = authorizeAdministrativeConsultationService;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
    }

    public List<Warehouse> executeAll(User user) {
        authorizeAdministrativeConsultationService.execute(user);
        return warehouseRepositoryPort.findAll();
    }

    public List<Warehouse> executeMyWarehouses(User user) {
        Seller seller = authorizeSellerOperationService.execute(user);
        return warehouseRepositoryPort.findAllBySeller(seller);
    }

    public Warehouse execute(User user, Warehouse warehouse) {
        authorizeAdministrativeConsultationService.execute(user);
        Optional<Warehouse> storedOptional = warehouseRepositoryPort.findByIdentifier(warehouse);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Warehouse");
        }
        return storedOptional.get();
    }
}
