package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    @NoArgsConstructor
    @AllArgsConstructor
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
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PredicateObject {
        @JsonProperty
        private String p;

        @JsonProperty
        private PropertyValue o;
    }

    @Setter
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PropertyValue {
        @JsonProperty
        private String value;

        @JsonProperty
        private String datatype;

        @JsonProperty
        private String type;

        public PropertyValue(String value, String type, String datatype) {
            this.value = value;
            this.type = type;
            this.datatype = datatype;
        }

        public PropertyValue(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public PropertyValue(String value) {
            this.value = value;
        }

        public PropertyValue() {}
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Sources {
        @JsonProperty
        private String access;

        @JsonProperty
        private String referenceFormulation;
    }
}
