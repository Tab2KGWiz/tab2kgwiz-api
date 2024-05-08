package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.exception.NotAuthorizedException;
import cat.udl.eps.softarch.demo.repository.ColumnRepository;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.utils.ExternalCommandExecutor;
import cat.udl.eps.softarch.demo.utils.YamlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

//@RequestMapping("/home/yaml")
@RestController
public class YamlController {

    private final YamlGenerator yamlGenerator;
    private final ColumnRepository columnRepository;
    private final MappingRepository mappingRepository;

    public YamlController(YamlGenerator yamlGenerator, ColumnRepository columnRepository, MappingRepository mappingRepository) {
        this.yamlGenerator = yamlGenerator;
        this.columnRepository = columnRepository;
        this.mappingRepository = mappingRepository;
    }

    @PostMapping("/mappings/{id}/yaml/generate")
    public ResponseEntity<?> generateYaml(@PathVariable Long id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }

        if (mappingRepository.findById(id).isEmpty()) {
            throw new IOException("Mapping not found");
        }

        Mapping mapping = mappingRepository.findById(id).get();

        try {
            yamlGenerator.generateYaml(mappingRepository, columnRepository, mapping);
            return ResponseEntity.ok().body("Yaml file generated successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error generating the Yaml file");
        }
    }


    @PostMapping("/yaml/yarrrmlmapper")
    public ResponseEntity<?> yarrrmlMapper() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }

        ExternalCommandExecutor.executeYARRRMLParser();
        return ResponseEntity.ok().body("YARRRML parser executed successfully");
    }

    @PostMapping("/generateLinkedData")
    public ResponseEntity<?> generateLinkedData(@RequestParam("yamlFile") MultipartFile yamlFile,
                                                @RequestParam("csvFile") MultipartFile csvFile) throws IOException {
        byte[] linkedData = ExternalCommandExecutor.generateLinkedData(yamlFile, csvFile);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=linked-data.txt")
                .body(linkedData);
    }
}