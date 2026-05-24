package com.companyemployees.application.ports.repository;

import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para Empleado — vive en Application.
 */
public interface EmployeeRepository {
    Optional<Employee> findById(EmployeeId id);
    Optional<Employee> findByCorreo(String correo);
    List<Employee> findByCompaniaId(CompanyId companiaId);
    List<Employee> findAll();
    Employee save(Employee employee);
    void deleteById(EmployeeId id);
    boolean existsById(EmployeeId id);
}
