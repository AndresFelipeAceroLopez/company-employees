package com.companyemployees.api.controller;

import com.companyemployees.api.request.CreateEmployeeRequest;
import com.companyemployees.api.request.CreateEmployeesBatchRequest;
import com.companyemployees.api.request.DeleteEmployeesBatchRequest;
import com.companyemployees.api.request.PatchEmployeeRequest;
import com.companyemployees.api.request.UpdateEmployeeRequest;
import com.companyemployees.api.response.EmployeeApiResponse;
import com.companyemployees.api.response.PagedResponse;
import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.employee.dto.CreateEmployeeCommand;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.dto.PatchEmployeeCommand;
import com.companyemployees.application.employee.dto.UpdateEmployeeCommand;
import com.companyemployees.application.employee.usecase.CreateEmployeeUseCase;
import com.companyemployees.application.employee.usecase.CreateEmployeesBatchUseCase;
import com.companyemployees.application.employee.usecase.DeleteEmployeeUseCase;
import com.companyemployees.application.employee.usecase.DeleteEmployeesBatchUseCase;
import com.companyemployees.application.employee.usecase.GetEmployeeUseCase;
import com.companyemployees.application.employee.usecase.GetPagedEmployeesUseCase;
import com.companyemployees.application.employee.usecase.PatchEmployeeUseCase;
import com.companyemployees.application.employee.usecase.UpdateEmployeeUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/empleados")
public class EmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final DeleteEmployeeUseCase deleteEmployeeUseCase;
    private final GetPagedEmployeesUseCase getPagedEmployeesUseCase;
    private final CreateEmployeesBatchUseCase createEmployeesBatchUseCase;
    private final PatchEmployeeUseCase patchEmployeeUseCase;
    private final DeleteEmployeesBatchUseCase deleteEmployeesBatchUseCase;

    public EmployeeController(
            CreateEmployeeUseCase createEmployeeUseCase,
            GetEmployeeUseCase getEmployeeUseCase,
            UpdateEmployeeUseCase updateEmployeeUseCase,
            DeleteEmployeeUseCase deleteEmployeeUseCase,
            GetPagedEmployeesUseCase getPagedEmployeesUseCase,
            CreateEmployeesBatchUseCase createEmployeesBatchUseCase,
            PatchEmployeeUseCase patchEmployeeUseCase,
            DeleteEmployeesBatchUseCase deleteEmployeesBatchUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.deleteEmployeeUseCase = deleteEmployeeUseCase;
        this.getPagedEmployeesUseCase = getPagedEmployeesUseCase;
        this.createEmployeesBatchUseCase = createEmployeesBatchUseCase;
        this.patchEmployeeUseCase = patchEmployeeUseCase;
        this.deleteEmployeesBatchUseCase = deleteEmployeesBatchUseCase;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<PagedResponse<EmployeeApiResponse>>> getAll(
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false) String dir,
            @RequestParam(required = false) String buscar) {
        // La validacion de parametros ocurre aqui (sincrona); si falla -> 400.
        PageCriteria criteria = PageCriteria.of(pagina, tamano, orden, dir, buscar);
        return getPagedEmployeesUseCase.getPagedAsync(criteria)
                .thenApply(result -> ResponseEntity.ok(PagedResponse.from(result, this::mapToApiResponse)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeApiResponse> getById(@PathVariable String id) {
        EmployeeResponse employee = getEmployeeUseCase.getById(id);
        return ResponseEntity.ok(mapToApiResponse(employee));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<EmployeeApiResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        CreateEmployeeCommand command = new CreateEmployeeCommand(
                request.nombre(), request.apellido(), request.correo(),
                request.cargo(), request.salario(), request.companiaId()
        );
        EmployeeResponse created = createEmployeeUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/empleados/" + created.id()))
                .body(mapToApiResponse(created));
    }

    @PostMapping("/lote")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<java.util.List<EmployeeApiResponse>> createBatch(
            @Valid @RequestBody CreateEmployeesBatchRequest request) {
        java.util.List<CreateEmployeeCommand> commands = request.empleados().stream()
                .map(r -> new CreateEmployeeCommand(
                        r.nombre(), r.apellido(), r.correo(),
                        r.cargo(), r.salario(), r.companiaId()))
                .toList();
        java.util.List<EmployeeApiResponse> response = createEmployeesBatchUseCase.execute(commands).stream()
                .map(this::mapToApiResponse).toList();
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @employeeAuthorizationService.canModifyEmployee(authentication, #id)")
    public ResponseEntity<EmployeeApiResponse> update(@PathVariable String id,
                                                      @Valid @RequestBody UpdateEmployeeRequest request) {
        UpdateEmployeeCommand command = new UpdateEmployeeCommand(
                request.nombre(), request.apellido(), request.correo(),
                request.cargo(), request.salario(), request.status()
        );
        EmployeeResponse updated = updateEmployeeUseCase.execute(id, command);
        return ResponseEntity.ok(mapToApiResponse(updated));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @employeeAuthorizationService.canModifyEmployee(authentication, #id)")
    public ResponseEntity<EmployeeApiResponse> patch(@PathVariable String id,
                                                     @Valid @RequestBody PatchEmployeeRequest request) {
        PatchEmployeeCommand command = new PatchEmployeeCommand(
                request.nombre(), request.apellido(), request.correo(),
                request.cargo(), request.salario(), request.status()
        );
        EmployeeResponse patched = patchEmployeeUseCase.execute(id, command);
        return ResponseEntity.ok(mapToApiResponse(patched));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @employeeAuthorizationService.canModifyEmployee(authentication, #id)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteEmployeeUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lote")
    @PreAuthorize("hasRole('ADMIN') or @employeeAuthorizationService.canModifyEmployees(authentication, #request.ids())")
    public ResponseEntity<Void> deleteBatch(@Valid @RequestBody DeleteEmployeesBatchRequest request) {
        deleteEmployeesBatchUseCase.execute(request.ids());
        return ResponseEntity.noContent().build();
    }

    private EmployeeApiResponse mapToApiResponse(EmployeeResponse dto) {
        return new EmployeeApiResponse(
                dto.id(),
                dto.nombre(),
                dto.apellido(),
                dto.correo(),
                dto.cargo(),
                dto.salario(),
                dto.companiaId(),
                dto.status()
        );
    }
}
