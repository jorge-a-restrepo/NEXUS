package application.adapters.security.dtos;

import application.domain.User;
import application.domain.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security principal wrapping the User rebuilt from the JWT.
 *
 * This is the only place where the domain user meets Spring Security: the
 * domain never depends on these types. Controllers receive the wrapped user with
 * {@code @AuthenticationPrincipal(expression = "user")}.
 */
public class AuthenticatedUserPrincipal implements UserDetails {

    private final User user;

    public AuthenticatedUserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    /** ROLE_ prefix is what hasRole("BUYER") checks in SecurityConfig. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null || user.getRole() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user == null ? null : user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user != null && !UserStatus.BLOCKED.equals(user.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && UserStatus.ACTIVE.equals(user.getStatus());
    }
}
