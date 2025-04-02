package cat.udl.eps.softarch.demo.service;

import cat.udl.eps.softarch.demo.config.BasicUserDetailsImpl;
import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.exception.NotAuthorizedException;
import cat.udl.eps.softarch.demo.repository.SupplierRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import cat.udl.eps.softarch.demo.exception.NotFoundException;

@Service
public class SupplierAuthService {

    private final SupplierRepository supplierRepository;

    public SupplierAuthService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier getAuthenticatedSupplier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }
        BasicUserDetailsImpl userPrincipal = (BasicUserDetailsImpl) authentication.getPrincipal();
        return supplierRepository.findById(userPrincipal.getId())
                .orElseThrow(NotFoundException::new);
    }
}