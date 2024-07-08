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
    private static final String BASE = "base";
    private static final String SAREF = "saref";
    private static final String RDFS = "rdfs";
    private static final String XSD = "xsd";
    private static final String S4AGR = "s4agri";
    private static final String CSV_NAME = "mappings.csv";
    private static final String RDF_TYPE = "rdf:type";
    private static final String RDFS_LABEL = "rdfs:label";

    public void generateYaml(MappingRepository mappingRepository, ColumnRepository columnRepository, Mapping mapping) throws IOException {
        Map<String, String> prefixes = initializePrefixes(mapping.getPrefixesURIS());

        YamlMapping yamlMapping = new YamlMapping();
        yamlMapping.setPrefixes(prefixes);

        Map<String, YamlMapping.Mappings> mappings = new HashMap<>();

        columnRepository.findByColumnBelongsTo(mapping).forEach(column -> {
            YamlMapping.Mappings yamlMappingsMap = new YamlMapping.Mappings();
            yamlMappingsMap.setSources(Collections.singletonList(new YamlMapping.Sources(CSV_NAME, mapping.getFileFormat())));

            if (column.isIdentifier()) {
                createIdentifierMappings(yamlMappingsMap, column);
                mappings.put(column.getTitle().toLowerCase().replaceAll("[\\s()]", ""), yamlMappingsMap);
            } else if (column.isMeasurement()) {
                createMeasurementMappings(columnRepository, yamlMappingsMap, column, mapping);
                mappings.put(column.getTitle().toLowerCase().replaceAll("[\\s()]", ""), yamlMappingsMap);
            }
        });
        yamlMapping.setMappings(mappings);

        ObjectMapper mapper = createYamlMapper();
        mapping.setYamlFile(mapper.writeValueAsString(yamlMapping));
        mappingRepository.save(mapping);
    }

    private Map<String, String> initializePrefixes(String prefixesURIS) {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put(SAREF, "https://saref.etsi.org/core/");
        prefixes.put(BASE, "https://tab2kgwiz.udl.cat/");
        prefixes.put(RDFS, "http://www.w3.org/2000/01/rdf-schema#");
        prefixes.put(XSD, "http://www.w3.org/2001/XMLSchema#");
        prefixes.put(S4AGR, "https://saref.etsi.org/saref4agri/");

        Arrays.stream(prefixesURIS.split(","))
                .map(prefix -> prefix.split(";"))
                .forEach(parts -> prefixes.put(parts[0], parts[1]));

        return prefixes;
    }

    private void createIdentifierMappings(YamlMapping.Mappings yamlMappingsMap, Column column) {
        List<YamlMapping.PredicateObject> poList = new ArrayList<>();
        String columnTitle = escapeColumnTitle(column.getTitle());

        yamlMappingsMap.setS(BASE + ":$(" + columnTitle + ")");
        poList.add(new YamlMapping.PredicateObject(RDF_TYPE,
                new YamlMapping.PropertyValue(column.getOntologyType() + "/" + column.getLabel(), "iri")));
        poList.add(new YamlMapping.PredicateObject(RDFS_LABEL, new YamlMapping.PropertyValue("$(" + columnTitle + ")")));

        if (column.getRelatedTo() != null) {
            poList.add(new YamlMapping.PredicateObject("s4agri:isLocatedIn", new YamlMapping.PropertyValue(
                    column.getRelationShip() + "/$(" + escapeColumnTitle(column.getRelatedTo()) + ")", "iri")));
        }

        yamlMappingsMap.setPo(poList);
    }

    private void createMeasurementMappings(ColumnRepository columnRepository, YamlMapping.Mappings yamlMappingsMap,
                                           Column column, Mapping mapping) {
        List<YamlMapping.PredicateObject> poList = new ArrayList<>();
        String columnTitle = escapeColumnTitle(column.getTitle());

        poList.add(new YamlMapping.PredicateObject(RDF_TYPE,
                new YamlMapping.PropertyValue("saref:Measurement", "iri")));
        poList.add(new YamlMapping.PredicateObject(RDFS_LABEL, new YamlMapping.PropertyValue(column.getLabel())));
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
                new YamlMapping.PropertyValue(BASE + ":" + column.getMeasurementMadeBy(), "iri")));
        poList.add(new YamlMapping.PredicateObject("saref:isMeasurementOf",
                new YamlMapping.PropertyValue(BASE + ":$(" + column.getIsMeasurementOf() + ")", "iri")));

        yamlMappingsMap.setPo(poList);
    }

    private String escapeColumnTitle(String title) {
        return title.replaceAll("[(]", "\\\\(").replaceAll("[)]", "\\\\)");
    }

    private ObjectMapper createYamlMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        // Remove default quotes
        yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        // Remove --- from the start of the file
        yamlFactory.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
        return new ObjectMapper(yamlFactory);
    }
}
