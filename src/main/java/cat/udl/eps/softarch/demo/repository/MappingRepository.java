package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface MappingRepository extends PagingAndSortingRepository<Mapping, Long>, CrudRepository<Mapping, Long> {
    List<Mapping> findByTitle(@Param("title") String title);

    List<Mapping> findByTitleContaining(@Param("title") String title);

    @Override
    Iterable<Mapping> findAll(Sort sort);

    @Override
    Page<Mapping> findAll(Pageable pageable);

    Optional<Mapping> findById(Long id);

    // In the case that we have multiple mapping for each supplier
    List<Mapping> findByProvidedBy(Supplier supplier);

    List<Mapping> findByIsAccessibleTrueAndProvidedByNot(Supplier supplier);
}
