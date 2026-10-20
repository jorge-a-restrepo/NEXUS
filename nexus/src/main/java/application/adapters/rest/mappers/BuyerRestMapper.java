package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.domain.Buyer;

import java.util.ArrayList;

/**
 * Converts between the buyer REST DTOs and the Buyer domain model.
 */
public final class BuyerRestMapper {

    private BuyerRestMapper() {
    }

    public static Buyer toDomain(RegisterBuyerRequestDTO dto) {
        Buyer buyer = new Buyer();
        buyer.setIdentityDocument(dto.getIdentityDocument());
        buyer.setFullName(dto.getFullName());
        buyer.setEmail(dto.getEmail());
        buyer.setPassword(dto.getPassword());
        buyer.setMainAddress(dto.getMainAddress());
        buyer.setAdditionalAddresses(dto.getAdditionalAddresses() != null
                ? new ArrayList<>(dto.getAdditionalAddresses())
                : new ArrayList<>());
        return buyer;
    }

    public static BuyerResponseDTO toResponseDTO(Buyer buyer) {
        if (buyer == null) {
            return null;
        }
        BuyerResponseDTO dto = new BuyerResponseDTO();
        dto.setIdentifier(buyer.getIdentifier());
        dto.setFullName(buyer.getFullName());
        dto.setEmail(buyer.getEmail());
        dto.setRole(buyer.getRole() != null ? buyer.getRole().name() : null);
        dto.setStatus(buyer.getStatus() != null ? buyer.getStatus().name() : null);
        dto.setCommercialStatus(buyer.getCommercialStatus() != null ? buyer.getCommercialStatus().name() : null);
        return dto;
    }
}
