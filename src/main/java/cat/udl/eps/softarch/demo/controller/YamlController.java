package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.domain.Column;
import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.exception.NotAuthorizedException;
import cat.udl.eps.softarch.demo.repository.ColumnRepository;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.utils.YamlGenerator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        Supplier supplier = mappingRepository.findById(id).get().getProvidedBy();

        if (!authentication.getName().equals(supplier.getId())) {
            throw new NotAuthorizedException();
        }

        Mapping mainMapping = mappingRepository.findById(id).get();
        List<Column> mainColumns = mainMapping.getColumns();

        Set<String> mainColumnDataTypes = mainColumns.stream()
                .map(Column::getDataType)
                .collect(Collectors.toSet());
        Set<String> mainColumnOntologies = mainColumns.stream()
                .map(Column::getOntologyType)
                .collect(Collectors.toSet());

        Stream<Mapping> comparedMappings = mappingRepository.findByProvidedBy(supplier)
                .stream()
                .filter(m -> !Objects.equals(m.getId(), id) && m.getYamlFile() != null);

        Iterable<Mapping> mappings = comparedMappings.collect(Collectors.toList());

        for (Mapping m : mappings) {
            if (!Objects.equals(m.getId(), id) && m.getYamlFile() != null) {
                Set<String> columnDataTypes = m.getColumns().stream().map(Column::getDataType).collect(Collectors.toSet());
                Set<String> columnOntologies = m.getColumns().stream().map(Column::getOntologyType).collect(Collectors.toSet());

                if (mainColumnDataTypes.equals(columnDataTypes) && mainColumnOntologies.equals(columnOntologies)) {
                    mainMapping.setYamlFile(m.getYamlFile());
                    mappingRepository.save(mainMapping);
                    return ResponseEntity.ok().body("There is already a mapping with the same columns");
                }
            }
        }

        Mapping mapping = mappingRepository.findById(id).get();

        try {
            yamlGenerator.generateYaml(mappingRepository, columnRepository, mapping);
            return ResponseEntity.ok().body(mapping.getYamlFile());
//            return ResponseEntity.ok().body("Yaml file generated successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error generating the Yaml file");
        }
    }
}