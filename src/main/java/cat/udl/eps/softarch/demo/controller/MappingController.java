package cat.udl.eps.softarch.demo.controller;
import cat.udl.eps.softarch.demo.config.BasicUserDetailsImpl;
import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.exception.NotAuthorizedException;
import cat.udl.eps.softarch.demo.exception.NotFoundException;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RepositoryRestController
public class MappingController {
    private final MappingRepository mappingRepository;

    private SupplierRepository supplierRepository;

    public MappingController(MappingRepository mappingRepository, SupplierRepository supplierRepository) {
        this.mappingRepository = mappingRepository;
        this.supplierRepository = supplierRepository;
    }

    @RequestMapping(value = "/mappings/{id}", method = RequestMethod.GET)
    public @ResponseBody PersistentEntityResource getMapping(PersistentEntityResourceAssembler resourceAssembler,
                                                             @PathVariable Long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }

        BasicUserDetailsImpl userPrincipal = (BasicUserDetailsImpl) authentication.getPrincipal();
        Supplier supplier = supplierRepository.findById(userPrincipal.getId()).orElseThrow(NotFoundException::new);
        Optional<Mapping> mapping = mappingRepository.findById(id);

        if (mapping.isEmpty()) {
            throw new NotFoundException();
        }

        Mapping m = mapping.get();

        if(m.getProvidedBy().getId() == null) {
            throw new NotFoundException();
        }

        if (m.getProvidedBy().getId().equals(supplier.getId())) {
            return resourceAssembler.toFullResource(m);
        } else {
            throw new NotAuthorizedException();
        }
    }

    @RequestMapping(value = "/mappings", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody PersistentEntityResource createMapping(PersistentEntityResourceAssembler resourceAssembler,
                                                                @RequestBody Mapping mapping) throws MethodArgumentNotValidException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }

        BasicUserDetailsImpl userPrincipal = (BasicUserDetailsImpl) authentication.getPrincipal();

        Supplier supplier = supplierRepository.findById(userPrincipal.getId()).orElseThrow(NotFoundException::new);

        mapping.setProvidedBy(supplier);
        mapping.setPrefixesURIS("http://www.example.com/,http://myontology.com/,http://schema.org/");

        try {
            mapping = mappingRepository.save(mapping);
        } catch (Exception e) {
            throw new MethodArgumentNotValidException(null, new BeanPropertyBindingResult(mapping, "mapping"));
        }

        return resourceAssembler.toFullResource(mapping);
    }

    @RequestMapping(value = "/mappings/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteMapping(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentSupplierUsername = authentication.getName();

        Optional<Mapping> mapping = mappingRepository.findById(id);

        if (mapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mapping not found.");
        }

        if (!mapping.get().getProvidedBy().getUsername().equals(currentSupplierUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this mapping.");
        }

        mappingRepository.delete(mapping.get());
        return ResponseEntity.ok("Mapping deleted successfully.");
    }


    @RequestMapping(value = "/mappings/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<String> updateMapping(@RequestBody Mapping mapping) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentSupplierUsername = authentication.getName();

        assert mapping.getId() != null;
        Optional<Mapping> mappingOptional = mappingRepository.findById(mapping.getId());

        if (mappingOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mapping not found.");
        }

        if (!mappingOptional.get().getProvidedBy().getUsername().equals(currentSupplierUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this mapping.");
        }

        mappingRepository.save(mapping);
        return ResponseEntity.ok("Mapping updated successfully.");
    }
}
