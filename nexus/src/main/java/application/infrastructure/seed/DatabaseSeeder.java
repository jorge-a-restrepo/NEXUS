package application.infrastructure.seed;

import application.domain.Administrator;
import application.domain.UserRole;
import application.domain.UserStatus;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first Administrator when none exists.
 *
 * SUPUESTO: internal users (including administrators) can only be registered by
 * an authenticated Administrator, and sellers are onboarded only by an
 * Administrator (RG-09). Without an initial administrator nobody could start
 * those flows, so one is seeded at startup. Its credentials come from
 * nexus.seed.admin.* (academic defaults, overridable with environment variables).
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminIdentityDocument;
    private final String adminFullName;

    public DatabaseSeeder(UserRepositoryPort userRepositoryPort,
                          PasswordServicePort passwordServicePort,
                          @Value("${nexus.seed.admin.email}") String adminEmail,
                          @Value("${nexus.seed.admin.password}") String adminPassword,
                          @Value("${nexus.seed.admin.identity-document}") String adminIdentityDocument,
                          @Value("${nexus.seed.admin.full-name}") String adminFullName) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordServicePort = passwordServicePort;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminIdentityDocument = adminIdentityDocument;
        this.adminFullName = adminFullName;
    }

    @Override
    public void run(String... args) {
        if (!userRepositoryPort.findAllByRole(UserRole.ADMINISTRATOR).isEmpty()) {
            return;
        }
        Administrator administrator = new Administrator();
        administrator.setIdentityDocument(adminIdentityDocument);
        administrator.setFullName(adminFullName);
        administrator.setEmail(adminEmail);
        administrator.setPassword(passwordServicePort.hash(adminPassword));
        administrator.setRole(UserRole.ADMINISTRATOR);
        administrator.setStatus(UserStatus.ACTIVE);
        userRepositoryPort.save(administrator);
        log.info("Initial administrator created: {} (academic default credentials, change them outside the course).",
                adminEmail);
    }
}
