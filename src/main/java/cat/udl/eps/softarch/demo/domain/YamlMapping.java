package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YamlMapping {

    @JsonProperty
    private Map<String, String> prefixes;
    @JsonProperty
    private Map<String, Mappings> mappings;

    @OneToOne
    @JsonIdentityReference(alwaysAsId = true)
    private Mapping extendsOf;

    @Setter
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Mappings {
        @JsonProperty
        private List<Sources> sources;

        @JsonProperty
        private String s;
        @JsonProperty
        private List<PredicateObject> po;

    }

    @Setter
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PredicateObject {
        @JsonProperty
        private String p;
        @JsonProperty
        private PropertyValue o;

        public PredicateObject(String a, PropertyValue propertyValue) {
            this.p = a;
            this.o = propertyValue;
        }
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PropertyValue {
        @JsonProperty
        private String value;
        @JsonProperty
        private String datatype;

        public PropertyValue(String value, String datatype) {
            this.value = value;
            this.datatype = datatype;
        }

        public PropertyValue(String value) {
            this.value = value;
        }

    }

    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Sources {
        @JsonProperty
        private String access;
        @JsonProperty
        private String referenceFormulation;

        public Sources(String s, String fileFormat) {
            this.access = s;
            this.referenceFormulation = fileFormat;
        }
    }
}