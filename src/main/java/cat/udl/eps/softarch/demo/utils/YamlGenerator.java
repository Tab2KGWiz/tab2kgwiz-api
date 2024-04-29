package cat.udl.eps.softarch.demo.utils;

import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.YamlMapping;
import cat.udl.eps.softarch.demo.repository.ColumnRepository;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.service.PrefixCCMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class YamlGenerator {

   public void generateYaml(MappingRepository mappingRepository, ColumnRepository columnRepository, String mappingName) throws IOException {

       PrefixCCMap prefixCCMap = new PrefixCCMap();

       Mapping mapping = mappingRepository.findByTitle(mappingName).get(0);

       YamlMapping.Mappings yamlMappingsMap = new YamlMapping.Mappings();

       Map<String, String> prefixes = new HashMap<>();

       Map<String, String> prefixes4Factory = new HashMap<>();

       ArrayList<String> prefixList = new ArrayList<>(Arrays.asList(mapping.getPrefixesURIS().split(",")));
       AtomicInteger count = new AtomicInteger(1);

       prefixList.forEach(prefix -> {
           prefixes.put(prefix, prefixCCMap.prefixCCReverseLookup(prefix));
           prefixes4Factory.put(prefixCCMap.prefixCCReverseLookup(prefix), prefix);
           count.getAndIncrement();
       });

       YamlMapping.Sources sources = new YamlMapping.Sources();
       sources.setAccess(mapping.getFileName());
       sources.setReferenceFormulation(mapping.getFileFormat());
       yamlMappingsMap.setSources(List.of(sources));

       ArrayList<YamlMapping.PredicateObject> poList = new ArrayList<>();

       columnRepository.findByColumnBelongsTo(mapping).forEach(column -> {
           if (column.getId() == 1) {
               yamlMappingsMap.setS(prefixes.get(column.getOntologyURI()) + ":$(" + column.getTitle() + ")");
               YamlMapping.PredicateObject po = new YamlMapping.PredicateObject();
               po.setP("a");
               po.setO(new YamlMapping.PropertyValue(column.getColumnBelongsTo().getMainOntology()));
               poList.add(po);

           } else {
               YamlMapping.PredicateObject po = new YamlMapping.PredicateObject();
               po.setP(prefixes.get(column.getOntologyURI()) + ":" + column.getOntologyType());
               po.setO(new YamlMapping.PropertyValue("$(" + column.getTitle() + ")", column.getDataType()));
               poList.add(po);

           }
       });
       yamlMappingsMap.setPo(poList);

       Map<String, YamlMapping.Mappings> mappings = new HashMap<>();
       mappings.put(mapping.getTitle(), yamlMappingsMap);

       YamlMapping yamlMapping = new YamlMapping();

       yamlMapping.setPrefixes(prefixes4Factory);
       yamlMapping.setMappings(mappings);

       YAMLFactory yamlFactory = new YAMLFactory();

       // Remove default quotes
       yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
       // Remove --- from the start of the file
       yamlFactory.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);

       ObjectMapper mapper = new ObjectMapper(yamlFactory);
       mapper.writeValue(new File("src/main/resources/mappings.yarrrml.yml"), yamlMapping);
   }
}
