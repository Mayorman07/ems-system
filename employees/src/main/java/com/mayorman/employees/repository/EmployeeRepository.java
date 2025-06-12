package com.mayorman.employees.repository;
import com.mayorman.employees.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
public interface EmployeeRepository extends JpaRepository <Employee, Long>{

    Employee findByEmail (String email);

    Employee findByUserId(String userId);
}
