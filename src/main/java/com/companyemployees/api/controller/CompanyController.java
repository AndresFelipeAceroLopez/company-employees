package com.companyemployees.api.controller;

import com.companyemployees.api.request.CreateCompanyRequest;
import com.companyemployees.api.request.CreateCompanyWithEmployeesRequest;
import com.companyemployees.api.request.UpdateCompanyRequest;
import com.companyemployees.api.response.CompanyApiResponse;
import com.companyemployees.api.response.CompanyWithEmployeesApiResponse;
import com.companyemployees.api.response.EmployeeApiResponse;
import com.companyemployees.api.response.PagedResponse;
import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.CreateCompanyCommand;
import com.companyemployees.application.company.dto.CreateCompanyWithEmployeesCommand;
import com.companyemployees.application.company.dto.UpdateCompanyCommand;
import com.companyemployees.application.company.usecase.CreateCompanyUseCase;
import com.companyemployees.application.company.usecase.CreateCompanyWithEmployeesUseCase;
import com.companyemployees.application.company.usecase.DeleteCompanyUseCase;
import com.companyemployees.application.company.usecase.GetCompanyUseCase;
import com.companyemployees.application.company.usecase.UpdateCompanyUseCase;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.usecase.GetPagedCompanyEmployeesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyUseCase getCompanyUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            DeleteCompanyUseCase deleteCompanyUseCase,
            GetPagedCompanyEmployeesUseCase getPagedCompanyEmployeesUseCase,
            CreateCompanyWithEmployeesUseCase createCompanyWithEmployeesUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyUseCase = getCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
        this.getPagedCompanyEmployeesUseCase = getPagedCompanyEmployeesUseCase;
        this.createCompanyWithEmployeesUseCase = createCompanyWithEmployeesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CompanyApiResponse>> getAll() {
        List<CompanyApiResponse> response = getCompanyUseCase.getAll().stream()
                .map(this::mapToApiResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyApiResponse> getById(@PathVariable String id) {
        CompanyResponse company = getCompanyUseCase.getById(id);
        return ResponseEntity.ok(mapToApiResponse(company));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<CompanyApiResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        CreateCompanyCommand command = new CreateCompanyCommand(
                request.nombre(), request.direccion(), request.telefono());
        CompanyResponse created = createCompanyUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/companias/" + created.id()))
                .body(mapToApiResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<CompanyApiResponse> update(@PathVariable String id,
                                                     @Valid @RequestBody UpdateCompanyRequest request) {
        UpdateCompanyCommand command = new UpdateCompanyCommand(
                request.nombre(), request.direccion(), request.telefono());
        CompanyResponse updated = updateCompanyUseCase.execute(id, command);
        return ResponseEntity.ok(mapToApiResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteCompanyUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/empleados")
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
    @PreAuthorize("hasRole('ADMIN')")
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
                dto.nombre(),
                dto.direccion(),
                dto.telefono(),
                dto.fechaCreacion(),
                dto.employeeCount()
        );
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
