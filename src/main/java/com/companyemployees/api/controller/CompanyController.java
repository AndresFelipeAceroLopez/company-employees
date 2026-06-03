package com.companyemployees.api.controller;

import com.companyemployees.api.request.CreateCompaniesBatchRequest;
import com.companyemployees.api.request.CreateCompanyRequest;
import com.companyemployees.api.request.CreateCompanyWithEmployeesRequest;
import com.companyemployees.api.request.PatchCompanyRequest;
import com.companyemployees.api.request.UpdateCompanyRequest;
import com.companyemployees.api.response.CompanyApiResponse;
import com.companyemployees.api.response.CompanyWithEmployeesApiResponse;
import com.companyemployees.api.response.EmployeeApiResponse;
import com.companyemployees.api.response.OptionApiResponse;
import com.companyemployees.api.response.PagedResponse;
import com.companyemployees.application.common.dto.OptionResponse;
import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.CreateCompanyCommand;
import com.companyemployees.application.company.dto.PatchCompanyCommand;
import com.companyemployees.application.company.dto.CreateCompanyWithEmployeesCommand;
import com.companyemployees.application.company.dto.UpdateCompanyCommand;
import com.companyemployees.application.company.usecase.CreateCompaniesBatchUseCase;
import com.companyemployees.application.company.usecase.CreateCompanyUseCase;
import com.companyemployees.application.company.usecase.CreateCompanyWithEmployeesUseCase;
import com.companyemployees.application.company.usecase.DeleteCompanyUseCase;
import com.companyemployees.application.company.usecase.GetCompanyOptionsUseCase;
import com.companyemployees.application.company.usecase.GetCompanyUseCase;
import com.companyemployees.application.company.usecase.PatchCompanyUseCase;
import com.companyemployees.application.company.usecase.UpdateCompanyUseCase;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.usecase.GetPagedCompanyEmployeesUseCase;
import com.companyemployees.api.support.AllowedMethodsResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/companias")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetCompanyUseCase getCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeleteCompanyUseCase deleteCompanyUseCase;
    private final GetPagedCompanyEmployeesUseCase getPagedCompanyEmployeesUseCase;
    private final CreateCompanyWithEmployeesUseCase createCompanyWithEmployeesUseCase;
    private final CreateCompaniesBatchUseCase createCompaniesBatchUseCase;
    private final GetCompanyOptionsUseCase getCompanyOptionsUseCase;
    private final PatchCompanyUseCase patchCompanyUseCase;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyUseCase getCompanyUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            DeleteCompanyUseCase deleteCompanyUseCase,
            GetPagedCompanyEmployeesUseCase getPagedCompanyEmployeesUseCase,
            CreateCompanyWithEmployeesUseCase createCompanyWithEmployeesUseCase,
            CreateCompaniesBatchUseCase createCompaniesBatchUseCase,
            GetCompanyOptionsUseCase getCompanyOptionsUseCase,
            PatchCompanyUseCase patchCompanyUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyUseCase = getCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
        this.getPagedCompanyEmployeesUseCase = getPagedCompanyEmployeesUseCase;
        this.createCompanyWithEmployeesUseCase = createCompanyWithEmployeesUseCase;
        this.createCompaniesBatchUseCase = createCompaniesBatchUseCase;
        this.getCompanyOptionsUseCase = getCompanyOptionsUseCase;
        this.patchCompanyUseCase = patchCompanyUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_compania:leer')")
    public ResponseEntity<List<CompanyApiResponse>> getAll() {
        List<CompanyApiResponse> response = getCompanyUseCase.getAll().stream()
                .map(this::mapToApiResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @RequestMapping(method = RequestMethod.HEAD)
    @PreAuthorize("hasAuthority('SCOPE_compania:leer')")
    public ResponseEntity<Void> head() {
        // HEAD: mismas cabeceras que GET pero sin cuerpo. Devuelve el total en X-Total-Count.
        return ResponseEntity.noContent()
                .header("X-Total-Count", String.valueOf(getCompanyUseCase.count()))
                .build();
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<List<String>> options(Authentication authentication) {
        // OPTIONS: informa que verbos puede usar ESTE usuario segun sus scopes.
        List<HttpMethod> permitidos = AllowedMethodsResolver.forResource("compania", authentication);
        return ResponseEntity.ok()
                .allow(permitidos.toArray(HttpMethod[]::new))
                .body(permitidos.stream().map(HttpMethod::name).toList());
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAuthority('SCOPE_compania:leer')")
    public ResponseEntity<List<OptionApiResponse>> getOptions() {
        List<OptionApiResponse> response = getCompanyOptionsUseCase.getOptions().stream()
                .map(this::mapToOptionApiResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_compania:leer')")
    public ResponseEntity<CompanyApiResponse> getById(@PathVariable String id) {
        CompanyResponse company = getCompanyUseCase.getById(id);
        return ResponseEntity.ok(mapToApiResponse(company));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_compania:crear')")
    public ResponseEntity<CompanyApiResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        CreateCompanyCommand command = new CreateCompanyCommand(
                request.nombre(), request.direccion(), request.telefono());
        CompanyResponse created = createCompanyUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/companias/" + created.id()))
                .body(mapToApiResponse(created));
    }

    @PostMapping("/lote")
    @PreAuthorize("hasAuthority('SCOPE_compania:crear')")
    public ResponseEntity<List<CompanyApiResponse>> createBatch(
            @Valid @RequestBody CreateCompaniesBatchRequest request) {
        List<CreateCompanyCommand> commands = request.companias().stream()
                .map(r -> new CreateCompanyCommand(r.nombre(), r.direccion(), r.telefono()))
                .toList();
        List<CompanyApiResponse> response = createCompaniesBatchUseCase.execute(commands).stream()
                .map(this::mapToApiResponse)
                .toList();
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_compania:actualizar')")
    public ResponseEntity<CompanyApiResponse> update(@PathVariable String id,
                                                     @Valid @RequestBody UpdateCompanyRequest request) {
        UpdateCompanyCommand command = new UpdateCompanyCommand(
                request.nombre(), request.direccion(), request.telefono());
        CompanyResponse updated = updateCompanyUseCase.execute(id, command);
        return ResponseEntity.ok(mapToApiResponse(updated));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_compania:actualizar')")
    public ResponseEntity<CompanyApiResponse> patch(@PathVariable String id,
                                                    @Valid @RequestBody PatchCompanyRequest request) {
        PatchCompanyCommand command = new PatchCompanyCommand(
                request.nombre(), request.direccion(), request.telefono());
        CompanyResponse patched = patchCompanyUseCase.execute(id, command);
        return ResponseEntity.ok(mapToApiResponse(patched));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_compania:eliminar')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteCompanyUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/empleados")
    @PreAuthorize("hasAuthority('SCOPE_empleado:leer')")
    public CompletableFuture<ResponseEntity<PagedResponse<EmployeeApiResponse>>> getEmployees(
            @PathVariable String id,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false) String dir,
            @RequestParam(required = false) String buscar) {
        PageCriteria criteria = PageCriteria.of(pagina, tamano, orden, dir, buscar);
        return getPagedCompanyEmployeesUseCase.getPagedAsync(id, criteria)
                .thenApply(result -> ResponseEntity.ok(PagedResponse.from(result, this::mapEmployeeToApiResponse)));
    }

    @PostMapping("/con-empleados")
    @PreAuthorize("hasAuthority('SCOPE_compania:crear')")
    public ResponseEntity<CompanyWithEmployeesApiResponse> createWithEmployees(
            @Valid @RequestBody CreateCompanyWithEmployeesRequest request) {

        CreateCompanyWithEmployeesCommand command = new CreateCompanyWithEmployeesCommand(
                request.nombre(),
                request.direccion(),
                request.telefono(),
                request.empleados().stream()
                        .map(e -> new CreateCompanyWithEmployeesCommand.EmployeeData(
                                e.nombre(), e.apellido(), e.correo(), e.cargo(), e.salario()))
                        .toList()
        );

        var result = createCompanyWithEmployeesUseCase.execute(command);

        CompanyWithEmployeesApiResponse response = new CompanyWithEmployeesApiResponse(
                mapToApiResponse(result.compania()),
                result.empleados().stream().map(this::mapEmployeeToApiResponse).toList()
        );

        return ResponseEntity.created(URI.create("/api/companias/" + result.compania().id()))
                .body(response);
    }

    private CompanyApiResponse mapToApiResponse(CompanyResponse dto) {
        return new CompanyApiResponse(
                dto.id(),
                dto.nombre(),
                dto.direccion(),
                dto.telefono(),
                dto.fechaCreacion(),
                dto.employeeCount()
        );
    }

    private OptionApiResponse mapToOptionApiResponse(OptionResponse dto) {
        return new OptionApiResponse(dto.label(), dto.value());
    }

    private EmployeeApiResponse mapEmployeeToApiResponse(EmployeeResponse dto) {
        return new EmployeeApiResponse(
                dto.nombre(),
                dto.apellido(),
                dto.correo(),
                dto.cargo(),
                dto.salario(),
                dto.status()
        );
    }
}
