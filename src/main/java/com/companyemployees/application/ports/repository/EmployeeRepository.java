package com.companyemployees.application.ports.repository;

import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.common.pagination.PagedResult;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para Empleado — vive en Application.
 * Define el contrato que Infrastructure debe implementar.
 */
public interface EmployeeRepository {

    // ----- Operaciones individuales (preservadas) -----
    Optional<Employee> findById(EmployeeId id);
    Optional<Employee> findByCorreo(String correo);
    List<Employee> findByCompaniaId(CompanyId companiaId);
    List<Employee> findAll();
    long count();
    Employee save(Employee employee);
    void deleteById(EmployeeId id);
    boolean existsById(EmployeeId id);

    // ----- Operaciones de coleccion -----
    PagedResult<Employee> findPaged(PageCriteria criteria);
    PagedResult<Employee> findPagedByCompaniaId(CompanyId companiaId, PageCriteria criteria);
    List<Employee> saveAll(List<Employee> employees);
    void deleteAllById(List<EmployeeId> ids);
    List<Employee> findAllByIds(List<EmployeeId> ids);
    List<Employee> findByCorreoIn(List<String> correos);
}
