package com.mayorman.employees.controllers;

import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.requests.CreateEmployeeRequest;
import com.mayorman.employees.models.responses.CreateEmployeeResponse;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeesController {

    private final EmployeeService employeeService;
    ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class); // Use SLF4J logger


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest employeeDetails){

        logger.info("The incoming create employee request {} " , employeeDetails);
        EmployeeDto employeeDto = modelMapper.map(employeeDetails, EmployeeDto.class);
        EmployeeDto createdUserDto = employeeService.createEmployee(employeeDto);

        CreateEmployeeResponse returnValue = modelMapper.map(createdUserDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<CreateEmployeeResponse> updateEmployee(@Valid @RequestBody CreateEmployeeRequest employeeDetails){
        logger.info("The incoming update employee request {} " , employeeDetails);
        EmployeeDto employeeDto = modelMapper.map(employeeDetails, EmployeeDto.class);
        EmployeeDto updatedEmployeeDto = employeeService.updateEmployee(employeeDto);

        CreateEmployeeResponse returnValue = modelMapper.map(updatedEmployeeDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);

    }

    @DeleteMapping("/{email}") // Maps to DELETE /employees/{email}
    public ResponseEntity<Void> deleteEmployee(@PathVariable("email") String email) {
        // 1. Clearer log message
        logger.info("Received request to delete employee with email: {}", email);

        // 2. No ModelMapper configuration needed here. Use the injected service.
        try {
            employeeService.deleteEmployee(email); // Call the void service method
            logger.info("Employee with email {} deleted successfully.", email);
            // 3. Return 204 No Content for successful deletion (common practice)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            // Spring's @ControllerAdvice can also handle this globally, but direct handling is also possible.
            logger.warn("Attempt to delete non-existent employee with email {}. Reason: {}", email, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return 404 if not found
        } catch (Exception e) {
            logger.error("Error deleting employee with email {}: {}", email, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // General error
        }
    }

    public void  viewEmployeeDetails(){

    }

}
