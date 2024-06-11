package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.config.BasicUserDetailsImpl;
import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.exception.NotAuthorizedException;
import cat.udl.eps.softarch.demo.exception.NotFoundException;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.repository.SupplierRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.List;
import java.util.Optional;

import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RepositoryRestController
public class MappingController {
    private final MappingRepository mappingRepository;

    private SupplierRepository supplierRepository;

    public MappingController(MappingRepository mappingRepository, SupplierRepository supplierRepository) {
        this.mappingRepository = mappingRepository;
        this.supplierRepository = supplierRepository;
    }

    @RequestMapping(value = "/mappings/{id}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Mapping> getMapping(PersistentEntityResourceAssembler resourceAssembler,
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

        if (m.getProvidedBy().getId() == null) {
            throw new NotFoundException();
        }

        if (m.getProvidedBy().getId().equals(supplier.getId())) {
            return new ResponseEntity<>(m, HttpStatus.OK);
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
        mapping.setPrefixesURIS("http://dbpedia.org/ontology/,http://schema.org/");

        try {
            mapping = mappingRepository.save(mapping);

        } catch (Exception e) {
            throw new MethodArgumentNotValidException(null, new BeanPropertyBindingResult(mapping, "mapping"));
        }

        return resourceAssembler.toFullResource(mapping);
    }

    @RequestMapping(value = "/mappings/{id}/generate", method = RequestMethod.POST)
    public ResponseEntity<String> generateMappingLinkedData(@PathVariable Long id,
                                                            @RequestParam("csvFile") MultipartFile csvFile) throws IOException {

        Optional<Mapping> mapping = mappingRepository.findById(id);

        if (mapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mapping not found.");
        }

        // Authorization logic
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //String currentSupplierUsername = authentication.getName();
        //if (!mapping.get().getProvidedBy().getUsername().equals(currentSupplierUsername)) {
        //    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to generate linked data for this mapping.");
        //}

        String content = mapping.get().getYamlFile();

        File yamlFile = File.createTempFile("temp", ".yaml");
        FileWriter yamlWriter = new FileWriter(yamlFile);
        yamlWriter.write(content);
        yamlWriter.close();

        File csvContent = File.createTempFile("temp", ".csv");
        csvFile.transferTo(csvContent);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("csvFile", new FileSystemResource(csvContent));
        body.add("yamlFile", new FileSystemResource(yamlFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String serverUrl = "http://165.232.127.94:8081/generateLinkedData";

        RestTemplate restTemplate = new RestTemplate();

        try {
            // Send POST request and handle potential exceptions
            ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
            return response;
        } catch (RestClientResponseException e) {
            // Handle error from the server (e.g., return appropriate HTTP status code)
            return ResponseEntity.status(e.getStatusCode()).body("Error generating linked data: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions during communication
            return ResponseEntity.internalServerError().body("Error generating linked data: " + e.getMessage());
        } finally {
            // Clean up temporary files
            if (yamlFile.exists()) {
                yamlFile.delete();
            }
            if (csvContent.exists()) {
                csvContent.delete();
            }
        }
    }

//    @RequestMapping(value = "/mappings/{id}/columns", method = RequestMethod.GET)
//    public ResponseEntity<String> getColumnsByMapping(@PathVariable Long id) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentSupplierUsername = authentication.getName();
//
//        Optional<Mapping> mapping = mappingRepository.findById(id);
//
//        if (mapping.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mapping not found.");
//        }
//
//        if (!mapping.get().getProvidedBy().getUsername().equals(currentSupplierUsername)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to get the columns of this mapping.");
//        }
//        mapping.get().getColumns();
//
//        return;
//    }


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
    public ResponseEntity<String> updateMapping(@PathVariable Long id, @RequestBody Mapping mapping) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentSupplierUsername = authentication.getName();

        Optional<Mapping> mappingOptional = mappingRepository.findById(id);

        if (mappingOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mapping not found.");
        }

        if (!mappingOptional.get().getProvidedBy().getUsername().equals(currentSupplierUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this mapping.");
        }

        Mapping existingMapping = mappingOptional.get();
        existingMapping.setAccessible(mapping.isAccessible());
        existingMapping.setTitle(mapping.getTitle());

        mappingRepository.save(existingMapping);
        return ResponseEntity.ok("Mapping updated successfully.");
    }


    @RequestMapping(value = "/mappings", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<List<Mapping>> getAllMappings() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }

        BasicUserDetailsImpl userPrincipal = (BasicUserDetailsImpl) authentication.getPrincipal();
        Supplier supplier = supplierRepository.findById(userPrincipal.getId()).orElseThrow(NotFoundException::new);

        List<Mapping> mappings = mappingRepository.findByProvidedBy(supplier);

        // List of mappings that are public and not provided by the current supplier
        mappings.addAll(mappingRepository.findByIsAccessibleTrueAndProvidedByNot(supplier));

        if (mappings.isEmpty()) {
            throw new NotFoundException();
        }
        return new ResponseEntity<>(mappings, HttpStatus.OK);
    }
}
