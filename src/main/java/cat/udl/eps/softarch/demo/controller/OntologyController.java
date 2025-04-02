package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.service.OntologyService;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ontology")
public class OntologyController {
    @PostMapping("/load")
    public ResponseEntity<List<Map<String, String>>> loadOntology(@RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            OntologyService ontologyService = new OntologyService();
            ontologyService.loadOntology(inputStream);
            List<Map<String, String>> classesInfo = ontologyService.extractClassesInfo();

            return ResponseEntity.ok(classesInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList(
                    Collections.singletonMap("error", "Error processing ontology file.")));
        }
    }
}
