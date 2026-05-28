package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.common.pagination.PagedResult;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import com.companyemployees.infrastructure.persistence.mongo.document.EmployeeDocument;
import com.companyemployees.infrastructure.persistence.mongo.mapper.EmployeeDocumentMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
public class MongoEmployeeRepository implements EmployeeRepository {

    private final SpringDataEmployeeMongoRepository mongoRepository;
    private final EmployeeDocumentMapper mapper;
    private final MongoTemplate mongoTemplate;

    public MongoEmployeeRepository(SpringDataEmployeeMongoRepository mongoRepository,
                                   EmployeeDocumentMapper mapper,
                                   MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
    }

    // ----- Operaciones individuales -----

    @Override
    public List<Employee> findAll() {
        return mongoRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Employee> findById(EmployeeId id) {
        return mongoRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByCorreo(String correo) {
        return mongoRepository.findByCorreo(correo).map(mapper::toDomain);
    }

    @Override
    public List<Employee> findByCompaniaId(CompanyId companiaId) {
        return mongoRepository.findByCompaniaId(companiaId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeDocument document = mapper.toDocument(employee);
        EmployeeDocument saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(EmployeeId id) {
        mongoRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(EmployeeId id) {
        return mongoRepository.existsById(id.value());
    }

    // ----- Operaciones de coleccion -----

    @Override
    public PagedResult<Employee> findPaged(PageCriteria criteria) {
        return runPaged(buildBaseCriteria(criteria), criteria);
    }

    @Override
    public PagedResult<Employee> findPagedByCompaniaId(CompanyId companiaId, PageCriteria criteria) {
        Criteria base = buildBaseCriteria(criteria).and("companiaId").is(companiaId.value());
        return runPaged(base, criteria);
    }

    @Override
    public List<Employee> saveAll(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) return List.of();
        List<EmployeeDocument> docs = employees.stream().map(mapper::toDocument).toList();
        Iterable<EmployeeDocument> saved = mongoRepository.saveAll(docs);
        List<Employee> result = new ArrayList<>();
        saved.forEach(d -> result.add(mapper.toDomain(d)));
        return result;
    }

    @Override
    public void deleteAllById(List<EmployeeId> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<String> raw = ids.stream().map(EmployeeId::value).toList();
        mongoRepository.deleteAllById(raw);
    }

    @Override
    public List<Employee> findAllByIds(List<EmployeeId> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<String> raw = ids.stream().map(EmployeeId::value).toList();
        return mongoRepository.findAllById(raw).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Employee> findByCorreoIn(List<String> correos) {
        if (correos == null || correos.isEmpty()) return List.of();
        Query query = new Query(Criteria.where("correo").in(correos));
        return mongoTemplate.find(query, EmployeeDocument.class).stream()
                .map(mapper::toDomain)
                .toList();
    }

    // ----- Helpers -----

    /**
     * Construye un Criteria base con el filtro de busqueda libre (si aplica).
     * El filtro busca coincidencia parcial e insensible a mayusculas sobre
     * nombre, apellido o correo.
     */
    private Criteria buildBaseCriteria(PageCriteria criteria) {
        if (criteria.buscar() == null) {
            return new Criteria();
        }
        String safe = Pattern.quote(criteria.buscar());
        Criteria buscar = new Criteria().orOperator(
                Criteria.where("nombre").regex(safe, "i"),
                Criteria.where("apellido").regex(safe, "i"),
                Criteria.where("correo").regex(safe, "i")
        );
        return buscar;
    }

    private PagedResult<Employee> runPaged(Criteria criteria, PageCriteria pageCriteria) {
        Query countQuery = Query.query(criteria);
        long total = mongoTemplate.count(countQuery, EmployeeDocument.class);

        Sort.Direction direction = pageCriteria.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        // Tie-break por _id para garantizar orden estable entre paginas cuando el campo principal empata.
        Sort sort = Sort.by(direction, pageCriteria.orden()).and(Sort.by(Sort.Direction.ASC, "_id"));
        Query dataQuery = Query.query(criteria)
                .with(sort)
                .skip(pageCriteria.offset())
                .limit(pageCriteria.tamano());

        List<EmployeeDocument> docs = mongoTemplate.find(dataQuery, EmployeeDocument.class);
        List<Employee> contenido = docs.isEmpty()
                ? Collections.emptyList()
                : docs.stream().map(mapper::toDomain).toList();

        return new PagedResult<>(contenido, pageCriteria.pagina(), pageCriteria.tamano(), total);
    }
}
