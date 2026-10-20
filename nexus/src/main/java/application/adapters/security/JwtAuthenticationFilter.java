package application.adapters.security;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.User;
import application.domain.UserStatus;
import application.domain.ports.out.TokenServicePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs before every endpoint. Reads the "Authorization: Bearer token" header,
 * validates the token, rebuilds the User from its claims and places it in the
 * Spring Security context. Without a valid token the context stays empty and
 * SecurityConfig rejects protected routes with 403.
 *
 * Logout is stateless: the client discards the token and it expires on its own
 * (SUPUESTO: the specification defines no token version attribute for User).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenServicePort tokenServicePort;

    public JwtAuthenticationFilter(TokenServicePort tokenServicePort) {
        this.tokenServicePort = tokenServicePort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token != null && tokenServicePort.validateToken(token)) {
            try {
                User user = tokenServicePort.reconstructUser(token);
                if (UserStatus.ACTIVE.equals(user.getStatus())) {
                    AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
