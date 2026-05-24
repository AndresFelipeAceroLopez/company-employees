package com.companyemployees.api.controller;

import com.companyemployees.api.request.CreateCompanyRequest;
import com.companyemployees.api.request.CreateCompanyWithEmployeesRequest;
import com.companyemployees.api.request.UpdateCompanyRequest;
import com.companyemployees.api.response.CompanyApiResponse;
import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.CreateCompanyCommand;
import com.companyemployees.application.company.dto.UpdateCompanyCommand;
import com.companyemployees.application.company.usecase.CreateCompanyUseCase;
import com.companyemployees.application.company.usecase.CreateCompanyWithEmployeesUseCase;
import com.companyemployees.application.company.usecase.DeleteCompanyUseCase;
import com.companyemployees.application.company.usecase.GetCompanyUseCase;
import com.companyemployees.application.company.usecase.UpdateCompanyUseCase;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.usecase.GetCompanyEmployeesUseCase;
import com.companyemployees.api.response.EmployeeApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/companias")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetCompanyUseCase getCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeleteCompanyUseCase deleteCompanyUseCase;
    private final GetCompanyEmployeesUseCase getCompanyEmployeesUseCase;
    private final CreateCompanyWithEmployeesUseCase createCompanyWithEmployeesUseCase;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyUseCase getCompanyUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            DeleteCompanyUseCase deleteCompanyUseCase,
            GetCompanyEmployeesUseCase getCompanyEmployeesUseCase,
            CreateCompanyWithEmployeesUseCase createCompanyWithEmployeesUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyUseCase = getCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
        this.getCompanyEmployeesUseCase = getCompanyEmployeesUseCase;
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
    public ResponseEntity<CompanyApiResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        CreateCompanyCommand command = new CreateCompanyCommand(request.nombre(), request.direccion(), request.telefono());
        CompanyResponse created = createCompanyUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/companias/" + created.id()))
                .body(mapToApiResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyApiResponse> update(@PathVariable String id, @Valid @RequestBody UpdateCompanyRequest request) {
        UpdateCompanyCommand command = new UpdateCompanyCommand(request.nombre(), request.direccion(), request.telefono());
        CompanyResponse updated = updateCompanyUseCase.execute(id, command);
        return ResponseEntity.ok(mapToApiResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteCompanyUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/empleados")
    public ResponseEntity<List<EmployeeApiResponse>> getEmployees(@PathVariable String id) {
        List<EmployeeApiResponse> response = getCompanyEmployeesUseCase.execute(id).stream()
                .map(this::mapEmployeeToApiResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint transaccional obligatorio: Crear compañía con empleados.
     * Demuestra el uso de Unit of Work.
     * Si falla la creación de un empleado, no se guarda nada.
     */
    @PostMapping("/con-empleados")
    public ResponseEntity<CompanyWithEmployeesApiResponse> createWithEmployees(
            @Valid @RequestBody CreateCompanyWithEmployeesRequest request) {
        
        var command = new CreateCompanyWithEmployeesUseCase.CreateCompanyWithEmployeesCommand(
                request.getNombre(),
                request.getDireccion(),
                request.getTelefono(),
                request.getEmpleados().stream()
                        .map(e -> new CreateCompanyWithEmployeesUseCase.EmployeeData(
                                e.getNombre(),
                                e.getApellido(),
                                e.getCorreo(),
                                e.getCargo(),
                                e.getSalario()
                        ))
                        .toList()
        );

        var result = createCompanyWithEmployeesUseCase.execute(command);

        var response = new CompanyWithEmployeesApiResponse(
                mapToApiResponse(result.compania()),
                result.empleados().stream()
                        .map(this::mapEmployeeToApiResponse)
                        .toList()
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

    private EmployeeApiResponse mapEmployeeToApiResponse(EmployeeResponse dto) {
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

    // DTO para respuesta del endpoint transaccional
    public record CompanyWithEmployeesApiResponse(
            CompanyApiResponse compania,
            List<EmployeeApiResponse> empleados
    ) {}
}
