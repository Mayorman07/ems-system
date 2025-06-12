package com.mayorman.employees.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
public interface EmployeeRepository extends JpaRepository <Employee, Long>{

    UserEntity findByEmail (String email);

    UserEntity findByUserId(String userId);
}
