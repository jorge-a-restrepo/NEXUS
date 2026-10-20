package application.adapters.security.mappers;

import application.domain.Administrator;
import application.domain.Buyer;
import application.domain.CommercialStatus;
import application.domain.LogisticsOperator;
import application.domain.Seller;
import application.domain.Supervisor;
import application.domain.User;
import application.domain.UserRole;
import application.domain.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;

/**
 * Maps the User domain model to JWT claims and back.
 *
 * The token carries only what the domain needs to rebuild the authenticated
 * user: identifier (sub), role, status, email, full name and, for buyers, the
 * commercial status. Identity document, addresses and password are never placed
 * in the token, because anyone holding it can read its payload.
 *
 * The rebuilt user is an instance of the subclass that matches its role
 * (BUYER -> Buyer, SELLER -> Seller...), because the authorization services
 * check the subclass, for example "user instanceof Buyer".
 */
public final class JwtClaimsMapper {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_STATUS = "status";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_FULL_NAME = "fullName";
    public static final String CLAIM_COMMERCIAL_STATUS = "commercialStatus";

    private JwtClaimsMapper() {
    }

    public static void applyClaims(JwtBuilder builder, User user) {
        builder.claim(CLAIM_ROLE, user.getRole() != null ? user.getRole().name() : null);
        builder.claim(CLAIM_STATUS, user.getStatus() != null ? user.getStatus().name() : null);
        builder.claim(CLAIM_EMAIL, user.getEmail());
        builder.claim(CLAIM_FULL_NAME, user.getFullName());
        if (user instanceof Buyer buyer && buyer.getCommercialStatus() != null) {
            builder.claim(CLAIM_COMMERCIAL_STATUS, buyer.getCommercialStatus().name());
        }
    }

    public static User toDomain(Claims claims) {
        UserRole role = toEnum(UserRole.class, claims.get(CLAIM_ROLE, String.class));
        if (role == null) {
            throw new IllegalArgumentException("The token does not carry a valid role.");
        }
        User user = newUserForRole(role);
        user.setIdentifier(claims.getSubject());
        user.setRole(role);
        user.setStatus(toEnum(UserStatus.class, claims.get(CLAIM_STATUS, String.class)));
        user.setEmail(claims.get(CLAIM_EMAIL, String.class));
        user.setFullName(claims.get(CLAIM_FULL_NAME, String.class));
        if (user instanceof Buyer buyer) {
            buyer.setCommercialStatus(
                    toEnum(CommercialStatus.class, claims.get(CLAIM_COMMERCIAL_STATUS, String.class)));
        }
        return user;
    }

    private static User newUserForRole(UserRole role) {
        return switch (role) {
            case BUYER -> new Buyer();
            case SELLER -> new Seller();
            case LOGISTICS_OPERATOR -> new LogisticsOperator();
            case ADMINISTRATOR -> new Administrator();
            case SUPERVISOR -> new Supervisor();
        };
    }

    private static <E extends Enum<E>> E toEnum(Class<E> type, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
