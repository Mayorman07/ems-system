package com.mayorman.employees.controllers;

import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.requests.CreateEmployeeRequest;
import com.mayorman.employees.models.responses.CreateEmployeeResponse;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.services.EmployeeService;
import com.mayorman.employees.validations.InputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/employees")
@RequiredArgsConstructor
public class EmployeesController {

    private final EmployeeService employeeService;
    ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class); // Use SLF4J logger


    @GetMapping(path="/status/check")
    public String status()
    {
        return "Working hard on my new api gateway route on the port of Dee";
    }

    @PostMapping(path ="/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest employeeDetails, BindingResult bindingResult){

        logger.info("The incoming create employee request {} " , employeeDetails);
        InputValidator.validate(bindingResult);
        EmployeeDto employeeDto = modelMapper.map(employeeDetails, EmployeeDto.class);
        EmployeeDto createdUserDto = employeeService.createEmployee(employeeDto);

        CreateEmployeeResponse returnValue = modelMapper.map(createdUserDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }
    @PostMapping(path ="/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateEmployeeResponse> updateEmployee(@Valid @RequestBody CreateEmployeeRequest employeeDetails){
        logger.info("The incoming update employee request {} " , employeeDetails);
        EmployeeDto employeeDto = modelMapper.map(employeeDetails, EmployeeDto.class);
        EmployeeDto updatedEmployeeDto = employeeService.updateEmployee(employeeDto);

        CreateEmployeeResponse returnValue = modelMapper.map(updatedEmployeeDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }

    @DeleteMapping(path ="delete/{email}") // Maps to DELETE /employees/{email}
    public ResponseEntity<Void> deleteEmployee(@PathVariable("email") String email) {
        // 1. Clearer log message
        logger.info("Received request to delete employee with email: {}", email);
        employeeService.deleteEmployee(email); // Call the void service method
        logger.info("Employee with email {} deleted successfully.", email);
        // 3. Return 204 No Content for successful deletion (common practice)
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping(path ="/view")
    public ResponseEntity<Void> viewProfile(@PathVariable("email") String email){
        logger.info("Received request to delete employee with email: {}", email);

        employeeService.viewProfile(email);
        logger.info("Employee with email {} viewed successfully.", email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path ="/list")
    public ResponseEntity<List<CreateEmployeeResponse>> viewEmployeeDetails(){
        logger.info("Received request to fetch all employees available.");

        // 1. Capture the List<EmployeeDto> returned by the service
        List<EmployeeDto> employeesDto = employeeService.viewEmployeeDetails();

        // 2. Map the List<EmployeeDto> to List<CreateEmployeeResponse>
        // Use ModelMapper and Java Streams for collection mapping
        List<CreateEmployeeResponse> returnValue = employeesDto.stream()
                .map(employeeDto -> modelMapper.map(employeeDto, CreateEmployeeResponse.class))
                .collect(Collectors.toList());

        logger.info("Successfully fetched {} employees.", returnValue.size());
        // 3. Return the List<CreateEmployeeResponse> in the response body with HttpStatus.OK
        return new ResponseEntity<>(returnValue, HttpStatus.OK);
    }

}
