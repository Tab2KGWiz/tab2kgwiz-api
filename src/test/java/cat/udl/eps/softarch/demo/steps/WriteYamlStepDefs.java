package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.YamlMapping;
import cat.udl.eps.softarch.demo.repository.ColumnRepository;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.utils.YamlGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WriteYamlStepDefs {
    final MappingRepository mappingRepository;
    final ColumnRepository columnRepository;


    public WriteYamlStepDefs(MappingRepository mappingRepository, ColumnRepository columnRepository) {
        this.mappingRepository = mappingRepository;
        this.columnRepository = columnRepository;
    }

    @Given("Generate the Yaml file")
    public void generateTheYamlFile() throws IOException {

        YamlMapping.Mappings mapping = new YamlMapping.Mappings();

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("ex", "http://www.example.com/");
        prefixes.put("e", "http://myontology.com/");

        //mapping.setSources(List.of("CEP-2021-S1-WEIGHT.csv~csv"));

        YamlMapping.Sources sources = new YamlMapping.Sources();
        sources.setAccess("CEP-2021-S1-WEIGHT.csv");
        sources.setReferenceFormulation("csv");
        mapping.setSources(List.of(sources));

        mapping.setS("ex:$(Animal ID)");

        YamlMapping.PredicateObject po1 = new YamlMapping.PredicateObject();
        po1.setP("a");
        po1.setO(new YamlMapping.PropertyValue("schema:Pork"));

        YamlMapping.PredicateObject po2 = new YamlMapping.PredicateObject();
        po2.setP("schema:date");
        po2.setO(new YamlMapping.PropertyValue("$(Date)"));

        YamlMapping.PredicateObject po3 = new YamlMapping.PredicateObject();
        po3.setP("ex:weight");
        po3.setO(new YamlMapping.PropertyValue("$(Weight)", "xsd:integer"));

        mapping.setPo(Arrays.asList(po1, po2, po3));

        Map<String, YamlMapping.Mappings> mappings = new HashMap<>();
        mappings.put("animalWeight", mapping);

        YamlMapping yamlMapping = new YamlMapping();
        yamlMapping.setPrefixes(prefixes);

        yamlMapping.setMappings(mappings);

        YAMLFactory yamlFactory = new YAMLFactory();

        // Remove default quotes
        yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        // Remove --- from the start of the file
        yamlFactory.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);

        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.writeValue(new File("src/main/resources/pork.yaml"), yamlMapping);
    }

//    @Then("Write the yaml file with mapping name {string}")
//    public void writeTheYamlFile(String mappingName) throws IOException {
//
//        YamlGenerator yamlGenerator = new YamlGenerator();
//        yamlGenerator.generateYaml(mappingRepository, columnRepository, mappingName);
//
//    }

}
