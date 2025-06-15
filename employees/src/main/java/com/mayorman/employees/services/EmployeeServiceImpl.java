package com.mayorman.employees.services;

import com.mayorman.employees.entities.Employee;
import com.mayorman.employees.exceptions.ConflictException;
import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.repository.EmployeeRepository;
import com.mayorman.employees.utils.EmployeeManagementBeanUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements  EmployeeService{

    EmployeeRepository employeeRepository;

    BCryptPasswordEncoder bCryptPasswordEncoder;

    ModelMapper modelMapper; // Autowire the shared ModelMapper bean for one instance creation and sharing

    Environment environment;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class); // Use SLF4J logger



    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDetails) {
        // 1. Check for existence FIRST
        if (employeeRepository.findByEmail(employeeDetails.getEmail()).isPresent()){ // Use Optional for clarity
            logger.info("Employee with email {} already exists!", employeeDetails.getEmail());
            throw new ConflictException("Existing employee!");
        }

        // 2. Set necessary fields before mapping
        employeeDetails.setEmployeeId(UUID.randomUUID().toString());
        // Assuming employeeDetails.getPassword() holds the raw password for encoding
        employeeDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(employeeDetails.getPassword()));


        // 3. Map DTO to Entity
        Employee employeeToBeCreated = modelMapper.map(employeeDetails, Employee.class);

        // 4. Save the new entity
        Employee savedEmployee = employeeRepository.save(employeeToBeCreated);

        // 5. Map the saved entity back to DTO for return
        EmployeeDto returnValue = modelMapper.map(savedEmployee, EmployeeDto.class);
        return returnValue;
    }




    public EmployeeDto updateEmployee(EmployeeDto employeeDetails) {
        // 1. Find the existing employee by email
        Employee existingEmployee = employeeRepository.findByEmail(employeeDetails.getEmail())
                .orElseThrow(() -> { // Use Optional and throw directly
                    logger.info("Employee with email {} not found for update!", employeeDetails.getEmail());
                    return new NotFoundException("Employee not found!");
                });

        // 2. Update specific fields from DTO onto the EXISTING entity
        // ModelMapper.map(source, destination) is designed for this
        modelMapper.map(employeeDetails, existingEmployee);

        // Handle password update if necessary
        // If employeeDetails has a 'rawPassword' field for updates:
        // if (employeeDetails.getRawPassword() != null && !employeeDetails.getRawPassword().isEmpty()) {
        //     existingEmployee.setEncryptedPassword(bCryptPasswordEncoder.encode(employeeDetails.getRawPassword()));
        // }

        // 3. Save the updated existing entity
        Employee updatedEmployee = employeeRepository.save(existingEmployee); // Save the modified existing entity

        // 4. Map the updated entity back to DTO for return
        EmployeeDto returnValue = modelMapper.map(updatedEmployee, EmployeeDto.class);
        return returnValue;
    }

    @Override
    public void deleteEmployee() {

    }

    @Override
    public void viewEmployeeDetails() {

    }

    @Override
    public void viewProfile() {

    }

    @Override
    public EmployeeDto getEmployeeDetailsByEmail(String email) {
        return null;
    }

    @Override
    public EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization) {
        return null;
    }
}

