package cat.udl.eps.softarch.demo.service;

import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class PrefixCCMap extends PrefixMapStd {
    private static final Logger logger = LoggerFactory.getLogger(PrefixCCMap.class);

    @Override
    public boolean containsPrefix(String prefix) {
        if (super.containsPrefix(prefix))
            return true;

        String prefixccUri = prefixCCNamespaceLookup(prefix);
        if (prefixccUri != null) {
            this.add(prefix, prefixccUri);
            return true;
        }
        else
            return false;
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        Pair<String, String> curiePair = super.abbrev(uriStr);
        if (curiePair == null) {
            Resource uri = ResourceFactory.createResource(uriStr);
            String prefixcc = prefixCCReverseLookup(uri.getNameSpace());
            if (prefixcc != null)
                this.add(prefixcc, uri.getNameSpace());
            else
                try { this.add(generatePrefix(uriStr), uri.getNameSpace()); }
                catch (URISyntaxException e) { return null; }
            curiePair = super.abbrev(uriStr);
        }
        return curiePair;
    }

    @Override
    public String abbreviate(String uriStr) {
        Pair<String, String> curiePair = this.abbrev(uriStr);
        if (curiePair == null)
            return null;
        return curiePair.getLeft()+":"+curiePair.getRight();
    }

    @Override
    public String expand(String prefixedName) {
        String uriStr = super.expand(prefixedName);
        if (uriStr == null) {
            String[] curiePair = prefixedName.split(":");
            if (curiePair.length == 2) {
                String prefix = curiePair[0];
                String namespace = prefixCCNamespaceLookup(prefix);
                if (namespace != null) {
                    super.add(prefix, namespace);
                    uriStr = super.expand(prefixedName);
                }
            }
        }
        return uriStr;
    }

    public String generatePrefix(String uriStr) throws URISyntaxException {
        java.net.URI uri = new java.net.URI(uriStr);
        String[] candidateParts;
        if (uri.getHost() != null) {
            String host = uri.getHost();
            candidateParts = host.split("\\.");
        } else {
            String path = uri.getPath();
            candidateParts = path.split("/");
        }

        String candidatePrefix = null;
        if (candidateParts.length > 1)
            candidatePrefix = candidateParts[candidateParts.length - 2];
        else
            candidatePrefix = candidateParts[0];

        if (!this.containsPrefix(candidatePrefix))
            return candidatePrefix;
        else {
            int version = 1;
            while(this.containsPrefix(candidatePrefix+"_"+version))
                version++;
            return candidatePrefix+"_"+version;
        }
    }

    public String prefixCCNamespaceLookup(String prefix) {
        switch (prefix) {
            case "rdf": return RDF.getURI();
            case "rdfs": return RDFS.getURI();
            case "owl": return OWL.getURI();
            case "xsd": return XSD.getURI();
            case "foaf": return FOAF.getURI();
            default: {
                RestTemplate restTemplate = new RestTemplate();
                try {
                    String response = restTemplate.getForObject("http://prefix.cc/{prefix}.file.{format}", String.class, prefix, "txt");
                    String[] pair = response.split("\\s");
                    if (pair.length == 2)
                        return pair[1];
                } catch (RestClientException e) {
                    logger.info("Prefix {} not found in http://prefix.cc \n", prefix);
                }
            }
        }
        return null;
    }

    public String prefixCCReverseLookup(String uri) {
        switch (uri) {
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#": return "rdf";
            case "http://www.w3.org/2000/01/rdf-schema#": return "rdfs";
            case "http://www.w3.org/2002/07/owl#": return "owl";
            case "http://xmlns.com/foaf/0.1/": return "foaf";
            case "http://purl.org/net/schemas/space/": return "space";
            case "https://ai4pork.angliru.udl.cat/schauer/": return "base";
            case "https://ai4pork.angliru.udl.cat/": return "ai4pork";
            case "https://saref.etsi.org/saref4agri/": return "s4agri";
            case "https://saref.etsi.org/saref4auto/": return "auto";
            case "https://saref.etsi.org/saref4city/": return "s4city";
            case "http://www.ontology-of-units-of-measure.org/resource/om-2/": return "om";
            case "http://www.w3.org/2006/time#": return "time";
            case "http://www.w3.org/2001/XMLSchema#": return "xsd";

            default: {
                RestTemplate restTemplate = new RestTemplate();
                try {
                    String response = restTemplate.getForObject("http://prefix.cc/reverse?uri={uri}&format={format}", String.class, uri, "txt");
                    String[] pair = response.split("\\s");
                    if (pair.length == 2)
                        return pair[0];
                } catch (RestClientException e) {
                    logger.info("Prefix for URI {} not found in http://prefix.cc", uri);
                }
            }
        }
        return null;
    }

    public boolean isCurie(String k) {
        return Pattern.matches("[\\w_][\\w\\d\\.\\-_]+:[\\w\\d\\.\\-_+#']+", k);
    }

    public String localName(String uriString) {
        if (uriString.contains("#"))
            return uriString.substring(uriString.lastIndexOf('#')+1);
        else if (uriString.contains("/"))
            return uriString.substring(uriString.lastIndexOf('/')+1);
        else if (uriString.contains(":"))
            return uriString.substring(uriString.lastIndexOf(':')+1);
        else
            return uriString;
    }
}