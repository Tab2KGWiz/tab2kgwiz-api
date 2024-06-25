package cat.udl.eps.softarch.demo.utils;

import cat.udl.eps.softarch.demo.domain.Column;
import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.YamlMapping;
import cat.udl.eps.softarch.demo.repository.ColumnRepository;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.service.PrefixCCMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class YamlGenerator {

    public void generateYaml(MappingRepository mappingRepository, ColumnRepository columnRepository, Mapping mapping) throws IOException {

        PrefixCCMap prefixCCMap = new PrefixCCMap();

        Map<String, String> prefixes = new HashMap<>();

        Map<String, String> prefixes4Factory = new HashMap<>();

        Map<String, YamlMapping.Mappings> mappings = new HashMap<>();

        ArrayList<String> prefixList = new ArrayList<>(Arrays.asList(mapping.getPrefixesURIS().split(",")));
        AtomicInteger count = new AtomicInteger(1);

        prefixList.forEach(prefix -> {
            prefixes.put(prefix, prefixCCMap.prefixCCReverseLookup(prefix));
            prefixes4Factory.put(prefixCCMap.prefixCCReverseLookup(prefix), prefix);
            count.getAndIncrement();
        });

        YamlMapping yamlMapping = new YamlMapping();
        yamlMapping.setPrefixes(prefixes4Factory);

        columnRepository.findByColumnBelongsTo(mapping).forEach(column -> {
            YamlMapping.Mappings yamlMappingsMap = new YamlMapping.Mappings();
            Random rand = new Random();
            ArrayList<YamlMapping.PredicateObject> poList = new ArrayList<>();

            if (column.isIdentifier()) {
                yamlMappingsMap.setSources(List.of(new YamlMapping.Sources("mappings.csv", mapping.getFileFormat())));
                yamlMappingsMap.setS("base" + ":$(" + column.getTitle() + ")");


                poList.add(new YamlMapping.PredicateObject("a", new YamlMapping.PropertyValue(column.getOntologyType()
                        + "/" + column.getLabel())));
                poList.add(new YamlMapping.PredicateObject("rdfs:label", new YamlMapping.PropertyValue("$(" + column.getTitle() + ")")));
                yamlMappingsMap.setPo(poList);
                mappings.put(column.getTitle(), yamlMappingsMap);

            } else if (column.isMeasurement()) {
                int randomNum = rand.nextInt(1000, 9999);
                yamlMappingsMap.setSources(List.of(new YamlMapping.Sources("mappings.csv", mapping.getFileFormat())));
                yamlMappingsMap.setS("base:measurements" + "/" + column.getLabel() + "-" + randomNum);
                poList.add(new YamlMapping.PredicateObject("a", new YamlMapping.PropertyValue("saref:Measurement")));
                poList.add(new YamlMapping.PredicateObject("rdfs:label",
                        new YamlMapping.PropertyValue(column.getLabel() + " " + randomNum)));
                poList.add(new YamlMapping.PredicateObject("saref:relatesToProperty",
                        new YamlMapping.PropertyValue("auto:" + column.getLabel() + "~iri")));
                poList.add(new YamlMapping.PredicateObject("saref:hasValue", new YamlMapping.PropertyValue(
                        "$(" + column.getTitle() + ")", column.getDataType())));
                poList.add(new YamlMapping.PredicateObject("saref:hasUnit",
                        new YamlMapping.PropertyValue("om:gram~iri")));
                poList.add(new YamlMapping.PredicateObject("saref:hasTimestamp",
                        new YamlMapping.PropertyValue(column.getHasTimestamp())));
                poList.add(new YamlMapping.PredicateObject("saref:measurementMadeBy",
                        new YamlMapping.PropertyValue(column.getMeasurementMadeBy())));

                Column identifierColumn = columnRepository.findByColumnBelongsToAndIsMeasurement(
                        mapping, true).get(0);
                poList.add(new YamlMapping.PredicateObject("saref:isMeasurementOf",
                        new YamlMapping.PropertyValue("base:" + identifierColumn.getLabel() + "/$(" +
                                identifierColumn.getTitle() + ")~iri")));

                yamlMappingsMap.setPo(poList);
                mappings.put(column.getTitle(), yamlMappingsMap);
            }
        });


        yamlMapping.setMappings(mappings);

        YAMLFactory yamlFactory = new YAMLFactory();

        // Remove default quotes
        yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        // Remove --- from the start of the file
        yamlFactory.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);

        ObjectMapper mapper = new ObjectMapper(yamlFactory);

        mapping.setYamlFile(mapper.writeValueAsString(yamlMapping));
        mappingRepository.save(mapping);
        //mapper.writeValue(new File("src/main/static/mappings.yarrrml.yml"), yamlMapping);
    }
}
