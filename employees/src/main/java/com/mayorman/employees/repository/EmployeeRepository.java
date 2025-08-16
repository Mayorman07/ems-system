package com.mayorman.employees.repository;
import com.mayorman.employees.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository <Employee, Long>{

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeId(String userId);

    Optional<Employee> findByVerificationToken(String token);

    Optional<Employee> findEmployeeByUsername(String username);

}
