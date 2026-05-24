package com.companyemployees;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CompanyEmployeesApplication {

    private static final Logger log = LoggerFactory.getLogger(CompanyEmployeesApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CompanyEmployeesApplication.class, args);
        log.info("=== Company Employees API iniciada ===");
        log.info("=== API: http://localhost:8080/api/companias ===");
    }
}
