package cat.udl.eps.softarch.demo.domain;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Mapping extends UriEntity<Long> {
    private static final int fileSize = 16 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Length(min = 1, max = 80)
    private String title;

    @NotBlank
    @Length(min = 1, max = 100)
    private String fileName;

    @Size(max = fileSize)
    private String file;

    private String fileFormat;

    @Override
    public Long getId() {
        return null;
    }
}
