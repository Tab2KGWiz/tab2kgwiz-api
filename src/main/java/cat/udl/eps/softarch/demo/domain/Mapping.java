package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Mapping extends UriEntity<Long> {
    private static final int fileSize = 100 * 1024; // 100KB
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Title is mandatory")
    @Length(min = 1, max = 80)
    private String title;

    @Length(min = 1, max = 100)
    private String fileName;

    @Column(length = fileSize, columnDefinition = "TEXT")
    @Size(max = fileSize)
    private String fileContent;

    private String fileFormat;

    private String prefixesURIS;

    private String mainOntology;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIdentityReference(alwaysAsId = true)
    private Supplier providedBy;

    @Size(max = fileSize)
    private String yamlFile;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "columnBelongsTo", cascade = CascadeType.ALL)
    private List<cat.udl.eps.softarch.demo.domain.Column> columns;

    @Override
    public String toString() {
        return "Mapping{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileContent='" + fileContent + '\'' +
                ", fileFormat='" + fileFormat + '\'' +
                ", prefixesURIS='" + prefixesURIS + '\'' +
                ", mainOntology='" + mainOntology + '\'' +
                ", providedBy=" + providedBy +
                ", yamlFile='" + yamlFile + '\'' +
                '}';
    }
}
