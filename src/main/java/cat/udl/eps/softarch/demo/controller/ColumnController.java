package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.config.BasicUserDetailsImpl;
import cat.udl.eps.softarch.demo.domain.Column;
import cat.udl.eps.softarch.demo.domain.Mapping;
import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.exception.NotAuthorizedException;
import cat.udl.eps.softarch.demo.exception.NotFoundException;
import cat.udl.eps.softarch.demo.repository.ColumnRepository;
import cat.udl.eps.softarch.demo.repository.MappingRepository;
import cat.udl.eps.softarch.demo.repository.SupplierRepository;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RepositoryRestController
public class ColumnController {
    private final ColumnRepository columnRepository;
    private final MappingRepository mappingRepository;
    private final SupplierRepository supplierRepository;

    public ColumnController(ColumnRepository columnRepository, MappingRepository mappingRepository, SupplierRepository supplierRepository) {
        this.columnRepository = columnRepository;
        this.mappingRepository = mappingRepository;
        this.supplierRepository = supplierRepository;
    }

    private Supplier getAuthenticatedSupplier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new NotAuthorizedException();
        }
        BasicUserDetailsImpl userPrincipal = (BasicUserDetailsImpl) authentication.getPrincipal();
        return supplierRepository.findById(userPrincipal.getId()).orElseThrow(NotFoundException::new);
    }

    private Mapping getMapping(Long id, Supplier supplier) {
        Mapping mapping = mappingRepository.findById(id).orElseThrow(NotFoundException::new);
        if (!Objects.equals(mapping.getProvidedBy().getId(), supplier.getId())) {
            throw new NotAuthorizedException();
        }
        return mapping;
    }

    @GetMapping("/columns/{id}")
    public @ResponseBody PersistentEntityResource getColumn(PersistentEntityResourceAssembler resourceAssembler, @PathVariable Long id) {
        Supplier supplier = getAuthenticatedSupplier();
        Column column = columnRepository.findById(id).orElseThrow(NotFoundException::new);
        assert column.getColumnBelongsTo().getId() != null;
        Mapping mapping = getMapping(column.getColumnBelongsTo().getId(), supplier);
        return resourceAssembler.toFullResource(column);
    }

    @GetMapping("/mappings/{id}/columns")
    public @ResponseBody ResponseEntity<List<Column>> getColumns(@PathVariable Long id) {
        Supplier supplier = getAuthenticatedSupplier();
        Mapping mapping = getMapping(id, supplier);
        return new ResponseEntity<>(mapping.getColumns(), HttpStatus.OK);
    }

    @PostMapping("/mappings/{id}/columns")
    public @ResponseBody PersistentEntityResource createColumn(PersistentEntityResourceAssembler resourceAssembler,
                                                               @PathVariable Long id, @RequestBody Column column) throws MethodArgumentNotValidException {
        Supplier supplier = getAuthenticatedSupplier();
        Mapping mapping = getMapping(id, supplier);
        column.setColumnBelongsTo(mapping);

        try {
            column = columnRepository.save(column);
        } catch (Exception e) {
            throw new MethodArgumentNotValidException(null, new BeanPropertyBindingResult(column, "column"));
        }
        return resourceAssembler.toFullResource(column);
    }

    @DeleteMapping("/columns/{id}")
    public ResponseEntity<String> deleteColumns(@PathVariable Long id) {
        Supplier supplier = getAuthenticatedSupplier();
        Column column = columnRepository.findById(id).orElseThrow(NotFoundException::new);
        Mapping mapping = getMapping(column.getColumnBelongsTo().getId(), supplier);
        columnRepository.delete(column);
        return ResponseEntity.ok("Column deleted successfully.");
    }


    @PutMapping("/mappings/{id}/columns/{columnId}")
    public @ResponseBody PersistentEntityResource updateColumn(PersistentEntityResourceAssembler resourceAssembler,
                                                               @PathVariable Long id, @PathVariable Long columnId, @RequestBody Column column) throws MethodArgumentNotValidException {
        Supplier supplier = getAuthenticatedSupplier();
        Mapping mapping = getMapping(id, supplier);

        Column updatedColumn = columnRepository.findById(columnId).map(existingColumn -> {
            updateColumnDetails(existingColumn, column);
            return columnRepository.save(existingColumn);
        }).orElseGet(() -> {
            column.setColumnBelongsTo(mapping);
            return columnRepository.save(column);
        });

        return resourceAssembler.toFullResource(updatedColumn);
    }

    private void updateColumnDetails(Column existingColumn, Column column) {
        if (column.getTitle() != null) {
            existingColumn.setTitle(column.getTitle());
        }
        if (column.getDataType() != null) {
            existingColumn.setDataType(column.getDataType());
        }
        if (column.getRelatesToProperty() != null) {
            existingColumn.setRelatesToProperty(column.getRelatesToProperty());
        }
        if (column.getHasUnit() != null) {
            existingColumn.setHasUnit(column.getHasUnit());
        }
        if (column.getHasTimestamp() != null) {
            existingColumn.setHasTimestamp(column.getHasTimestamp());
        }
        if (column.getMeasurementMadeBy() != null) {
            existingColumn.setMeasurementMadeBy(column.getMeasurementMadeBy());
        }
        existingColumn.setIdentifier(column.isIdentifier());
        existingColumn.setMeasurement(column.isMeasurement());
        if (column.getOntologyType() != null) {
            existingColumn.setOntologyType(column.getOntologyType());
        }
        if (column.getOntologyURI() != null) {
            existingColumn.setOntologyURI(column.getOntologyURI());
        }
        if (column.getLabel() != null) {
            existingColumn.setLabel(column.getLabel());
        }
        if (column.getPrefix() != null) {
            existingColumn.setPrefix(column.getPrefix());
        }
        if (column.getIsMeasurementOf() != null) {
            existingColumn.setIsMeasurementOf(column.getIsMeasurementOf());
        }
        if (column.getRelatedTo() != null) {
            existingColumn.setRelatedTo(column.getRelatedTo());
        }
        if (column.getRelationShip() != null) {
            existingColumn.setRelationShip(column.getRelationShip());
        }
    }
}
