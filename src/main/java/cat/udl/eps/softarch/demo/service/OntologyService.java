package cat.udl.eps.softarch.demo.service;

import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OntologyService {

    private OntModel model;

    public OntologyService() {
        this.model = ModelFactory.createOntologyModel();
    }

    public void loadOntology(InputStream inputStream) {
        try {
            model.read(inputStream, null, "RDF/XML");
            saveModel(model);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> extractClassesInfo() {
        List<Map<String, String>> classesInfo = new ArrayList<>();

        model.listClasses().forEachRemaining(ontClass -> {
            Map<String, String> classInfo = new HashMap<>();

            String uri = ontClass.getURI();
            if (uri != null) {
                classInfo.put("URI", uri);

                String prefix = model.getNsURIPrefix(ontClass.getNameSpace());
                classInfo.put("Prefix", prefix);

                String localName = ontClass.getLocalName();
                classInfo.put("Term", localName);

                Literal label = ontClass.getPropertyValue(RDFS.label).asLiteral();
                if (label != null) {
                    classInfo.put("Description", label.getString());
                } else {
                    Resource oboDescription = model.getResource("http://purl.obolibrary.org/obo/IAO_0000115");
                    Literal oboLabel = ontClass.getPropertyValue((Property) oboDescription).asLiteral();
                    if (oboLabel != null) {
                        classInfo.put("Description", oboLabel.getString());
                    }
                }

                classesInfo.add(classInfo);
            }
        });

        return classesInfo;
    }

    public void saveModel(OntModel model) {
        try {
//            model.write(System.out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
