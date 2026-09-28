package application.adapters.useCases;

import application.domain.Buyer;
import application.domain.Product;
import application.domain.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.services.catalog.ConsultCatalogService;
import application.domain.services.catalog.ConsultProductService;
import application.domain.services.user.LoginService;
import application.domain.services.user.LogoutService;
import application.domain.services.user.RegisterBuyerUserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements the public access input port by delegating to the domain services.
 *
 * Use case classes contain no business rules: they translate a role port call
 * into the corresponding domain service invocation.
 */
@Service
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final LoginService loginService;
    private final LogoutService logoutService;
    private final RegisterBuyerUserService registerBuyerUserService;
    private final ConsultCatalogService consultCatalogService;
    private final ConsultProductService consultProductService;

    public PublicAccessUseCaseImpl(LoginService loginService,
                                   LogoutService logoutService,
                                   RegisterBuyerUserService registerBuyerUserService,
                                   ConsultCatalogService consultCatalogService,
                                   ConsultProductService consultProductService) {
        this.loginService = loginService;
        this.logoutService = logoutService;
        this.registerBuyerUserService = registerBuyerUserService;
        this.consultCatalogService = consultCatalogService;
        this.consultProductService = consultProductService;
    }

    @Override
    public User login(User user) {
        return loginService.execute(user);
    }

    @Override
    public void logout(User user) {
        logoutService.execute(user);
    }

    @Override
    public Buyer registerBuyer(Buyer buyer) {
        return registerBuyerUserService.execute(buyer);
    }

    @Override
    public List<Product> consultPublicCatalog() {
        return consultCatalogService.execute();
    }

    @Override
    public Product consultPublicProduct(Product product) {
        return consultProductService.execute(product);
    }
}
