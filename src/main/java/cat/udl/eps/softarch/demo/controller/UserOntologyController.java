package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.domain.UserOntology;
import cat.udl.eps.softarch.demo.repository.UserOntologyRepository;
import cat.udl.eps.softarch.demo.service.SupplierAuthService;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserOntologyController {
    private final UserOntologyRepository userOntologyRepository;
    private final SupplierAuthService supplierAuthService;

    public UserOntologyController(UserOntologyRepository userOntologyRepository,
                                  SupplierAuthService supplierAuthService) {
        this.userOntologyRepository = userOntologyRepository;
        this.supplierAuthService = supplierAuthService;
    }

    @RequestMapping(value = "/userontologylists", method = RequestMethod.GET)
    public @ResponseBody List<UserOntology> getUserOntologyList() {
        Supplier supplier = supplierAuthService.getAuthenticatedSupplier();
        return userOntologyRepository.findByProvidedBy(supplier);
    }

    @PostMapping("/userontologylists")
    public ResponseEntity<?> updateUserOntologyList(@RequestBody List<UserOntology> userOntology) {
        Supplier supplier = supplierAuthService.getAuthenticatedSupplier();
        userOntologyRepository.deleteAll(userOntologyRepository.findByProvidedBy(supplier));

        for (UserOntology uo : userOntology) {
            uo.setProvidedBy(supplier);
            userOntologyRepository.save(uo);
        }
        return ResponseEntity.ok().body("User Ontology list updated");
    }
}
