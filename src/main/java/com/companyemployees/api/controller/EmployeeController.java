package com.companyemployees.api.controller;

import com.companyemployees.api.request.CreateEmployeeRequest;
import com.companyemployees.api.request.UpdateEmployeeRequest;
import com.companyemployees.api.response.EmployeeApiResponse;
import com.companyemployees.application.employee.dto.CreateEmployeeCommand;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.dto.UpdateEmployeeCommand;
import com.companyemployees.application.employee.usecase.CreateEmployeeUseCase;
import com.companyemployees.application.employee.usecase.DeleteEmployeeUseCase;
import com.companyemployees.application.employee.usecase.GetEmployeeUseCase;
import com.companyemployees.application.employee.usecase.UpdateEmployeeUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final DeleteEmployeeUseCase deleteEmployeeUseCase;

    public EmployeeController(
            CreateEmployeeUseCase createEmployeeUseCase,
            GetEmployeeUseCase getEmployeeUseCase,
            UpdateEmployeeUseCase updateEmployeeUseCase,
            DeleteEmployeeUseCase deleteEmployeeUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.deleteEmployeeUseCase = deleteEmployeeUseCase;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeApiResponse>> getAll() {
        List<EmployeeApiResponse> response = getEmployeeUseCase.getAll().stream()
                .map(this::mapToApiResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeApiResponse> getById(@PathVariable String id) {
        EmployeeResponse employee = getEmployeeUseCase.getById(id);
        return ResponseEntity.ok(mapToApiResponse(employee));
    }

    @PostMapping
    public ResponseEntity<EmployeeApiResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        CreateEmployeeCommand command = new CreateEmployeeCommand(
                request.nombre(), request.apellido(), request.correo(),
                request.cargo(), request.salario(), request.companiaId()
        );
        EmployeeResponse created = createEmployeeUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/empleados/" + created.id()))
                .body(mapToApiResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeApiResponse> update(@PathVariable String id, @Valid @RequestBody UpdateEmployeeRequest request) {
        UpdateEmployeeCommand command = new UpdateEmployeeCommand(
                request.nombre(), request.apellido(), request.correo(),
                request.cargo(), request.salario(), request.status()
        );
        EmployeeResponse updated = updateEmployeeUseCase.execute(id, command);
        return ResponseEntity.ok(mapToApiResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteEmployeeUseCase.execute(id);
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
