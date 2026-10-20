package application.adapters.security;

import application.adapters.security.mappers.JwtClaimsMapper;
import application.domain.User;
import application.domain.ports.out.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Output adapter for TokenServicePort backed by JSON Web Tokens (jjwt, HS256).
 *
 * A JWT has three dot-separated parts: header (algorithm), payload (claims that
 * identify the user) and signature (HMAC of header and payload with the secret).
 * The secret and the lifetime come from jwt.secret and jwt.expiration-ms, which
 * Docker overrides with JWT_SECRET and JWT_EXPIRATION_MS.
 *
 * The authenticated User is rebuilt from the claims on every request without a
 * database round trip; role or status changes take effect on the next login.
 */
@Component
public class JwtProvider implements TokenServicePort {

    private final String secret;
    private final long expirationMs;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        if (user == null || user.getIdentifier() == null) {
            throw new IllegalArgumentException("A stored user with an identifier is required to issue a token.");
        }
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .subject(user.getIdentifier())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)));
        JwtClaimsMapper.applyClaims(builder, user);
        return builder.signWith(signingKey()).compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public User reconstructUser(String token) {
        return JwtClaimsMapper.toDomain(parseClaims(token));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
