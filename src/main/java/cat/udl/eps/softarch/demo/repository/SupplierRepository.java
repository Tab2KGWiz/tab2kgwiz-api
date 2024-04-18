package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Supplier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface SupplierRepository extends PagingAndSortingRepository<Supplier, String>, CrudRepository<Supplier, String> {

    List<Supplier> findByUsernameContaining(@Param("text") String text);
    Boolean existsByEmail(String email);
}