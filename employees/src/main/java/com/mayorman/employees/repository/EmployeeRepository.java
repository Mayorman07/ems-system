package com.mayorman.employees.repository;
import com.mayorman.employees.constants.Status;
import com.mayorman.employees.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
@Repository
public interface EmployeeRepository extends JpaRepository <Employee, Long>{

    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeId(String userId);
    Optional<Employee> findByVerificationToken(String token);
    Optional<Employee> findEmployeeByUsername(String username);
    List<Employee> findAllByDepartment(String department);
    List<Employee> findAllByStatusAndLastLoggedInBefore(Status status, Date cutoffDate);


}
