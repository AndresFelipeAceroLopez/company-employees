package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.domain.company.CompanyId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetCompanyEmployeesUseCase {

    private final EmployeeRepository employeeRepository;

    public GetCompanyEmployeesUseCase(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeResponse> execute(String companyId) {
        return employeeRepository.findByCompaniaId(new CompanyId(companyId))
                .stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }
}
