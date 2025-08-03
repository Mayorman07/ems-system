package com.mayorman.employees.services;

import com.mayorman.employees.entities.Employee;
import com.mayorman.employees.exceptions.ConflictException;
import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    // --- Dependencies are correctly defined here ---
    private final EmployeeRepository employeeRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PasswordEncoder passwordEncoder; // <-- IMPROVEMENT 1: Using the interface
    private final ModelMapper modelMapper;
    private final Environment environment;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDetails) {
        if (employeeRepository.findByEmail(employeeDetails.getEmail()).isPresent()) {
            logger.info("Employee with email {} already exists!", employeeDetails.getEmail());
            throw new ConflictException("Existing employee!");
        }

        employeeDetails.setEmployeeId(UUID.randomUUID().toString());
        // Using the injected passwordEncoder
        employeeDetails.setEncryptedPassword(passwordEncoder.encode(employeeDetails.getPassword()));
        String username = generateUsername(employeeDetails.getFirstName(), employeeDetails.getLastName());
        employeeDetails.setUsername(username);
        Employee employeeToBeCreated = modelMapper.map(employeeDetails, Employee.class);
        Employee savedEmployee = employeeRepository.save(employeeToBeCreated);

        rabbitTemplate.convertAndSend("user-events-exchange", "user.created", employeeDetails);
        logger.info("Published User Created event for email: {}", savedEmployee.getEmail());

        return modelMapper.map(savedEmployee, EmployeeDto.class);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployee(EmployeeDto employeeDetails) {
        Employee existingEmployee = employeeRepository.findByEmail(employeeDetails.getEmail())
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for update!", employeeDetails.getEmail());
                    return new NotFoundException("Employee not found!");
                });

        modelMapper.map(employeeDetails, existingEmployee);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return modelMapper.map(updatedEmployee, EmployeeDto.class);
    }

    @Override
    @Transactional
    public void deleteEmployee(String email) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for deletion!", email);
                    return new NotFoundException("Employee not found!");
                });

        employeeRepository.delete(existingEmployee);
        logger.info("The employee has been deleted");
    }

    @Override
    public EmployeeDto viewProfile(String email) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for viewing!", email);
                    return new NotFoundException("Employee not found!");
                });

        return modelMapper.map(existingEmployee, EmployeeDto.class);
    }

    @Override
    public List<EmployeeDto> viewEmployeeDetails() {
        List<Employee> existingEmployees = employeeRepository.findAll();
        if (existingEmployees.isEmpty()) {
            logger.info("No employees found in the database.");
            return List.of();
        }
        return existingEmployees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .collect(Collectors.toList());
    }

    // --- IMPROVEMENT 2: This method is now correct ---
    @Override
    public EmployeeDto getEmployeeDetailsByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return modelMapper.map(employee, EmployeeDto.class);
    }


    @Override
    public EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization) {
        // TODO: Implement this method
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Employee> employeeToBeLoggedIn = employeeRepository.findByEmail(username);
        if(employeeToBeLoggedIn == null){
            throw new UsernameNotFoundException(username);
        }
        return new User(employeeToBeLoggedIn.get().getEmail(), employeeToBeLoggedIn.get().getEncryptedPassword(),true,
                true, true,true,new ArrayList<>());
    }

    private String generateUsername(String firstName, String lastName) {
        // Safely get the first 3 letters of the first name
        String firstNamePart = firstName.length() < 3 ?
                firstName :
                firstName.substring(0, 3);

        // Safely get the last 2 letters of the last name
        String lastNamePart = lastName.length() < 2 ?
                lastName :
                lastName.substring(lastName.length() - 2);

        // Generate a random 3-digit number (100-999)
        int randomNumber = new Random().nextInt(900) + 100;

        // Combine, convert to lowercase, and return the result
        return (firstNamePart + lastNamePart).toLowerCase() + randomNumber;
    }
}