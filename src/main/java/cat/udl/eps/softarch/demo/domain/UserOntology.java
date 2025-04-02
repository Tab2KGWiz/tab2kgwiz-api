package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
public class UserOntology {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String uri;
    private String prefix;
    private String label;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIdentityReference(alwaysAsId = true)
    private Supplier providedBy;

}
