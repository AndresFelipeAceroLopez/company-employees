package com.companyemployees.api.controller;

import com.companyemployees.api.request.CreateEmployeeRequest;
import com.companyemployees.api.request.CreateEmployeesBatchRequest;
import com.companyemployees.api.request.DeleteEmployeesBatchRequest;
import com.companyemployees.api.request.PatchEmployeeRequest;
import com.companyemployees.api.request.UpdateEmployeeRequest;
import com.companyemployees.api.response.EmployeeApiResponse;
import com.companyemployees.api.response.OptionApiResponse;
import com.companyemployees.api.response.PagedResponse;
import com.companyemployees.application.common.dto.OptionResponse;
import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.employee.dto.CreateEmployeeCommand;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.dto.PatchEmployeeCommand;
import com.companyemployees.application.employee.dto.UpdateEmployeeCommand;
import com.companyemployees.application.employee.usecase.CreateEmployeeUseCase;
import com.companyemployees.application.employee.usecase.CreateEmployeesBatchUseCase;
import com.companyemployees.application.employee.usecase.DeleteEmployeeUseCase;
import com.companyemployees.application.employee.usecase.DeleteEmployeesBatchUseCase;
import com.companyemployees.application.employee.usecase.GetEmployeeOptionsUseCase;
import com.companyemployees.application.employee.usecase.GetEmployeeUseCase;
import com.companyemployees.application.employee.usecase.GetPagedEmployeesUseCase;
import com.companyemployees.application.employee.usecase.PatchEmployeeUseCase;
import com.companyemployees.application.employee.usecase.UpdateEmployeeUseCase;
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
    private final GetEmployeeOptionsUseCase getEmployeeOptionsUseCase;

    public EmployeeController(
            CreateEmployeeUseCase createEmployeeUseCase,
            GetEmployeeUseCase getEmployeeUseCase,
            UpdateEmployeeUseCase updateEmployeeUseCase,
            DeleteEmployeeUseCase deleteEmployeeUseCase,
            GetPagedEmployeesUseCase getPagedEmployeesUseCase,
            CreateEmployeesBatchUseCase createEmployeesBatchUseCase,
            PatchEmployeeUseCase patchEmployeeUseCase,
            DeleteEmployeesBatchUseCase deleteEmployeesBatchUseCase,
            GetEmployeeOptionsUseCase getEmployeeOptionsUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.deleteEmployeeUseCase = deleteEmployeeUseCase;
        this.getPagedEmployeesUseCase = getPagedEmployeesUseCase;
        this.createEmployeesBatchUseCase = createEmployeesBatchUseCase;
        this.patchEmployeeUseCase = patchEmployeeUseCase;
        this.deleteEmployeesBatchUseCase = deleteEmployeesBatchUseCase;
        this.getEmployeeOptionsUseCase = getEmployeeOptionsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_empleado:leer')")
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

    @RequestMapping(method = RequestMethod.HEAD)
    @PreAuthorize("hasAuthority('SCOPE_empleado:leer')")
    public ResponseEntity<Void> head() {
        // HEAD: mismas cabeceras que GET pero sin cuerpo. Devuelve el total en X-Total-Count.
        return ResponseEntity.noContent()
                .header("X-Total-Count", String.valueOf(getEmployeeUseCase.count()))
                .build();
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<java.util.List<String>> options(Authentication authentication) {
        // OPTIONS: informa que verbos puede usar ESTE usuario segun sus scopes.
        java.util.List<HttpMethod> permitidos = AllowedMethodsResolver.forResource("empleado", authentication);
        return ResponseEntity.ok()
                .allow(permitidos.toArray(HttpMethod[]::new))
                .body(permitidos.stream().map(HttpMethod::name).toList());
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAuthority('SCOPE_empleado:leer')")
    public ResponseEntity<java.util.List<OptionApiResponse>> getOptions() {
        java.util.List<OptionApiResponse> response = getEmployeeOptionsUseCase.getOptions().stream()
                .map(this::mapToOptionApiResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_empleado:leer')")
    public ResponseEntity<EmployeeApiResponse> getById(@PathVariable String id) {
        EmployeeResponse employee = getEmployeeUseCase.getById(id);
        return ResponseEntity.ok(mapToApiResponse(employee));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_empleado:crear')")
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
    @PreAuthorize("hasAuthority('SCOPE_empleado:crear')")
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
    @PreAuthorize("hasAuthority('SCOPE_empleado:actualizar')")
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
    @PreAuthorize("hasAuthority('SCOPE_empleado:actualizar')")
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
    @PreAuthorize("hasAuthority('SCOPE_empleado:eliminar')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteEmployeeUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lote")
    @PreAuthorize("hasAuthority('SCOPE_empleado:eliminar')")
    public ResponseEntity<Void> deleteBatch(@Valid @RequestBody DeleteEmployeesBatchRequest request) {
        deleteEmployeesBatchUseCase.execute(request.ids());
        return ResponseEntity.noContent().build();
    }

    private OptionApiResponse mapToOptionApiResponse(OptionResponse dto) {
        return new OptionApiResponse(dto.label(), dto.value());
    }

    private EmployeeApiResponse mapToApiResponse(EmployeeResponse dto) {
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
