package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Column;
import cat.udl.eps.softarch.demo.domain.CustomMapping;
import cat.udl.eps.softarch.demo.domain.Mapping;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(excerptProjection = CustomMapping.class)
public interface ColumnRepository extends PagingAndSortingRepository<Column, Long>, CrudRepository<Column, Long> {
    @Override
    Iterable<Column> findAll(Sort sort);

    @Override
    Page<Column> findAll(Pageable pageable);

    Optional<Column> findById(Long id);

    Column findByTitleAndColumnBelongsTo(String title, Mapping mapping);

    List<Column> findByColumnBelongsTo(Mapping mapping);

    List<Column> findByColumnBelongsToAndIsMeasurement(Mapping mapping, boolean isMeasurement);
}

