package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Column extends UriEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Title is mandatory")
    @Length(min = 1, max = 80)
    private String title;

    @NotBlank
    private String dataType;

    private String ontologyURI;

    private String ontologyType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIdentityReference(alwaysAsId = true)
    private Mapping columnBelongsTo;

    private boolean isMeasurement;

    private boolean isIdentifier;

    private String hasUnit;

    private String hasTimestamp;

    private String measurementMadeBy;

    private String label;

    private String prefix;

    private String isMeasurementOf;

    private String relatedTo;

    private String relationShip;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Column{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dataType='" + dataType + '\'' +
                ", ontologyURI='" + ontologyURI + '\'' +
                ", ontologyType='" + ontologyType + '\'' +
                ", isMeasurement=" + isMeasurement + '\'' +
                ", isIdentifier=" + isIdentifier + '\'' +
                ", hasUnit='" + hasUnit + '\'' +
                ", hasTimestamp='" + hasTimestamp + '\'' +
                ", measurementMadeBy='" + measurementMadeBy + '\'' +
                ", label='" + label + '\'' +
                ", prefix='" + prefix + '\'' +
                ", isMeasurementOf='" + isMeasurementOf + '\'' +
                ", relatedTo='" + relatedTo + '\'' +
                ", relationShip='" + relationShip + '\'' +
                '}';
    }
}
