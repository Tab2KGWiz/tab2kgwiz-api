package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class SupplierEventHandler {
    final Logger logger = LoggerFactory.getLogger(Supplier.class);

    @HandleBeforeDelete
    public void handleSupplierPreDelete(Supplier supplier) {
        logger.info("Before delete: {}", supplier.toString());
        checkAuthentication(supplier);
    }

    private void checkAuthentication(Supplier supplier) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Username: {}", authentication.getAuthorities());

        Supplier curr_supplier = ((Supplier)authentication.getPrincipal());

        if (curr_supplier.getId() == null) {
            throw new ForbiddenException();
        }

        if (!curr_supplier.getId().equals(supplier.getId())){
            throw new ForbiddenException();
        }
    }

    @HandleBeforeSave
    public void handleSupplierPreUpdate(Supplier supplier) {
        logger.info("Before save: {}", supplier.toString());
        checkAuthentication(supplier);
    }
}
