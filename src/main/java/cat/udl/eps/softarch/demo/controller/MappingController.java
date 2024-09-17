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

import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RepositoryRestController
public class MappingController {
    private final MappingRepository mappingRepository;
    private final SupplierRepository supplierRepository;
    private static final String DEFAULT_PREFIXES = "https://saref.etsi.org/core/,https://ai4pork.angliru.udl.cat/schauer/,https://ai4pork.angliru.udl.cat/,https://saref.etsi.org/saref4agri/,https://saref.etsi.org/saref4city/,https://saref.etsi.org/saref4auto/,http://www.ontology-of-units-of-measure.org/resource/om-2/,http://www.w3.org/2006/time#,http://www.w3.org/2000/01/rdf-schema#,http://www.w3.org/2001/XMLSchema#";

    public MappingController(MappingRepository mappingRepository, SupplierRepository supplierRepository) {
        this.mappingRepository = mappingRepository;
        this.supplierRepository = supplierRepository;
    }

    private Supplier getAuthenticatedSupplier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }
        BasicUserDetailsImpl userPrincipal = (BasicUserDetailsImpl) authentication.getPrincipal();
        return supplierRepository.findById(userPrincipal.getId()).orElseThrow(NotFoundException::new);
    }

    private void validateMappingOwnership(Mapping mapping, Supplier supplier) {
        if (!mapping.isAccessible()) {
            assert mapping.getProvidedBy().getId() != null;
            if (!mapping.getProvidedBy().getId().equals(supplier.getId())) {
                throw new NotAuthorizedException();
            }
        }
    }

    @RequestMapping(value = "/mappings/{id}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Mapping> getMapping(@PathVariable Long id) {
        Supplier supplier = getAuthenticatedSupplier();
        Mapping mapping = mappingRepository.findById(id).orElseThrow(NotFoundException::new);
        validateMappingOwnership(mapping, supplier);
        return ResponseEntity.ok(mapping);
    }

    @PostMapping("/mappings")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody PersistentEntityResource createMapping(PersistentEntityResourceAssembler resourceAssembler,
                                                                @RequestBody Mapping mapping) throws MethodArgumentNotValidException {
        Supplier supplier = getAuthenticatedSupplier();
        mapping.setProvidedBy(supplier);
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

        Mapping mapping = mappingRepository.findById(id).orElseThrow(() -> new NotFoundException("Mapping not found."));

        File yamlFile = createTempFile(mapping.getYamlFile());
        File csvContent = createTempFileFromMultipart(csvFile);

        try {
            // Send POST request and handle potential exceptions
            ResponseEntity<String> response = sendPostRequest(yamlFile, csvContent);
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (RestClientResponseException e) {
            // Handle error from the server (e.g., return appropriate HTTP status code)
            return ResponseEntity.status(e.getStatusCode()).body("Error generating linked data: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions during communication
            return ResponseEntity.internalServerError().body("Error generating linked data: " + e.getMessage());
        } finally {
            yamlFile.delete();
            csvContent.delete();
        }
    }

    private File createTempFile(String content) throws IOException {
        File file = File.createTempFile("temp", ".yaml");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    private File createTempFileFromMultipart(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp", ".csv");
        file.transferTo(tempFile);
        return tempFile;
    }

        private ResponseEntity<String> sendPostRequest(File yamlFile, File csvContent) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("csvFile", new FileSystemResource(csvContent));
        body.add("yamlFile", new FileSystemResource(yamlFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String serverUrl = "http://104.248.240.80:8081/generateLinkedData";

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(serverUrl, requestEntity, String.class);
    }

    @DeleteMapping("/mappings/{id}")
    public ResponseEntity<String> deleteMapping(@PathVariable Long id) {
        Supplier supplier = getAuthenticatedSupplier();
        Mapping mapping = mappingRepository.findById(id).orElseThrow(() -> new NotFoundException("Mapping not found."));

        if (!mapping.getProvidedBy().getUsername().equals(supplier.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this mapping.");
        }

        mappingRepository.delete(mapping);
        return ResponseEntity.ok("Mapping deleted successfully.");
    }


    @PatchMapping("/mappings/{id}")
    public ResponseEntity<String> updateMapping(@PathVariable Long id, @RequestBody Mapping mapping) {
        Supplier supplier = getAuthenticatedSupplier();
        Mapping existingMapping = mappingRepository.findById(id).orElseThrow(() -> new NotFoundException("Mapping not found."));

        if (!existingMapping.getProvidedBy().getUsername().equals(supplier.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this mapping.");
        }

        updateMappingDetails(existingMapping, mapping);
        mappingRepository.save(existingMapping);
        return ResponseEntity.ok("Mapping updated successfully.");
    }

    private void updateMappingDetails(Mapping existingMapping, Mapping newMapping) {
        existingMapping.setAccessible(newMapping.isAccessible());
        existingMapping.setTitle(newMapping.getTitle());
        if (newMapping.getPrefixesURIS() != null) {
            existingMapping.setPrefixesURIS(newMapping.getPrefixesURIS());
        } else if (existingMapping.getPrefixesURIS() == null) {
            existingMapping.setPrefixesURIS(DEFAULT_PREFIXES);
        }
    }


    @GetMapping("/mappings")
    public @ResponseBody ResponseEntity<List<Mapping>> getAllMappings() {
        Supplier supplier = getAuthenticatedSupplier();
        List<Mapping> mappings = mappingRepository.findByProvidedBy(supplier);
        mappings.addAll(mappingRepository.findByIsAccessibleTrueAndProvidedByNot(supplier));

        if (mappings.isEmpty()) {
            throw new NotFoundException();
        }
        return ResponseEntity.ok(mappings);
    }
}
