package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.domain.UserOntology;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface UserOntologyRepository extends PagingAndSortingRepository<UserOntology, Long>,
        CrudRepository<UserOntology, Long> {

    @Override
    List<UserOntology> findAll();

    List<UserOntology> findByProvidedBy(Supplier supplier);
}
