package cat.udl.eps.softarch.demo.utils;

import cat.udl.eps.softarch.demo.domain.Column;
import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.YamlMapping;
import cat.udl.eps.softarch.demo.repository.ColumnRepository;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class YamlGenerator {

    public void generateYaml(MappingRepository mappingRepository, ColumnRepository columnRepository, Mapping mapping) throws IOException {

        Map<String, String> prefixes4Factory = new HashMap<>();
        prefixes4Factory.put("saref", "https://saref.etsi.org/core/");
        prefixes4Factory.put("base", "https://tab2kgwiz.udl.cat/");
        prefixes4Factory.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prefixes4Factory.put("xsd", "http://www.w3.org/2001/XMLSchema#");

        Map<String, YamlMapping.Mappings> mappings = new HashMap<>();

        ArrayList<String> prefixList = new ArrayList<>(Arrays.asList(mapping.getPrefixesURIS().split(",")));

        prefixList.forEach(prefix -> prefixes4Factory.put(prefix.split(";")[0], prefix.split(";")[1]));

        YamlMapping yamlMapping = new YamlMapping();
        yamlMapping.setPrefixes(prefixes4Factory);

        columnRepository.findByColumnBelongsTo(mapping).forEach(column -> {
            YamlMapping.Mappings yamlMappingsMap = new YamlMapping.Mappings();
            ArrayList<YamlMapping.PredicateObject> poList = new ArrayList<>();
            String columnTitle = column.getTitle().replaceAll("[(]", "\\\\(");
            columnTitle = columnTitle.replaceAll("[)]", "\\\\)");

            if (column.isIdentifier()) {
                yamlMappingsMap.setSources(List.of(new YamlMapping.Sources("mappings.csv", mapping.getFileFormat())));
                yamlMappingsMap.setS("base" + ":$(" + columnTitle + ")");

                poList.add(new YamlMapping.PredicateObject("a", new YamlMapping.PropertyValue(column.getOntologyType()
                        + "/" + column.getLabel())));
                poList.add(new YamlMapping.PredicateObject("rdfs:label", new YamlMapping.PropertyValue("$("
                        + columnTitle + ")")));
                yamlMappingsMap.setPo(poList);
                mappings.put(column.getTitle().toLowerCase().replaceAll("[\\s()]", ""), yamlMappingsMap);

            } else if (column.isMeasurement()) {
                yamlMappingsMap.setSources(List.of(new YamlMapping.Sources("mappings.csv", mapping.getFileFormat())));
                poList.add(new YamlMapping.PredicateObject("rdf:type", new YamlMapping.PropertyValue(
                        "saref:Measurement", "iri")));
                poList.add(new YamlMapping.PredicateObject("rdfs:label", new YamlMapping.PropertyValue(
                        column.getLabel())));
                poList.add(new YamlMapping.PredicateObject("saref:relatesToProperty",
                        new YamlMapping.PropertyValue(column.getOntologyType(), "iri")));

                YamlMapping.PropertyValue hasValuePropertyValue = new YamlMapping.PropertyValue();
                hasValuePropertyValue.setValue("$(" + columnTitle + ")");
                hasValuePropertyValue.setDatatype(column.getDataType());
                poList.add(new YamlMapping.PredicateObject("saref:hasValue", hasValuePropertyValue));

                poList.add(new YamlMapping.PredicateObject("saref:hasUnit",
                        new YamlMapping.PropertyValue(column.getHasUnit(), "iri")));

                YamlMapping.PropertyValue hasTimestampPropertyValue = new YamlMapping.PropertyValue();
                hasTimestampPropertyValue.setValue("$(" + column.getHasTimestamp() + ")");
                Column hasTimestampColumn = columnRepository.findByTitleAndColumnBelongsTo(column.getHasTimestamp(), mapping);
                hasTimestampPropertyValue.setDatatype(hasTimestampColumn.getDataType());
                poList.add(new YamlMapping.PredicateObject("saref:hasTimestamp", hasTimestampPropertyValue));

                poList.add(new YamlMapping.PredicateObject("saref:measurementMadeBy",
                        new YamlMapping.PropertyValue("base:" + column.getMeasurementMadeBy(), "iri")));

                poList.add(new YamlMapping.PredicateObject("saref:isMeasurementOf",
                        new YamlMapping.PropertyValue("base" + ":$(" +
                                column.getIsMeasurementOf() + ")", "iri")));

                yamlMappingsMap.setPo(poList);
                mappings.put(column.getTitle().toLowerCase().replaceAll("[\\s()]", ""), yamlMappingsMap);
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
