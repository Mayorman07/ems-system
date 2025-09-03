package com.mayorman.employees.services;

import com.mayorman.employees.constants.Status;
import com.mayorman.employees.entities.Authority;
import com.mayorman.employees.entities.Employee;
import com.mayorman.employees.entities.Role;
import com.mayorman.employees.exceptions.ConflictException;
import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.data.CustomUserDetails;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.models.data.EmployeeStatusDto;
import com.mayorman.employees.models.data.UserCreatedEventDto;
import com.mayorman.employees.models.requests.CreateAdminRequest;
import com.mayorman.employees.models.requests.EmployeeStatusCheckRequest;
import com.mayorman.employees.repository.EmployeeRepository;
import com.mayorman.employees.repository.RoleRepository;
import java.util.Calendar;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
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
        employeeDetails.setEncryptedPassword(passwordEncoder.encode(employeeDetails.getPassword()));
        String username = generateUsername(employeeDetails.getFirstName(), employeeDetails.getLastName());
        employeeDetails.setUsername(username);
        employeeDetails.setStatus(Status.INACTIVE);
        Employee employeeToBeCreated = modelMapper.map(employeeDetails, Employee.class);
        String verificationToken = UUID.randomUUID().toString();
        employeeToBeCreated.setVerificationToken(verificationToken);
        Employee savedEmployee = employeeRepository.save(employeeToBeCreated);
        publishUserCreatedEvent(savedEmployee, verificationToken);
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
    public EmployeeStatusDto viewProfile(String email) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for viewing!", email);
                    return new NotFoundException("Employee not found!");
                });

        return modelMapper.map(existingEmployee, EmployeeStatusDto.class);
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

    @Override
    public List<EmployeeDto> getEmployeesInDepartment(String department) {
        List<Employee> employeesInDepartment = employeeRepository.findAllByDepartment(department);
        if (employeesInDepartment.isEmpty()) {
            logger.info("No employees found for department: {}", department);
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<EmployeeDto>>() {}.getType();
        List<EmployeeDto> returnValue = new ModelMapper().map(employeesInDepartment, listType);
        return returnValue;
    }

    @Override
    public EmployeeDto getEmployeeDetailsByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return modelMapper.map(employee, EmployeeDto.class);
    }

    @Override
    @Transactional
    public void assignManagerRole(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + employeeId));
        Role managerRole = roleRepository.findByName("ROLE_MANAGER");
        if (managerRole == null) {
            throw new RuntimeException("Error: ROLE_MANAGER not found.");
        }
        Collection<Role> userRoles = employee.getRoles();
        if (!userRoles.contains(managerRole)) {
            userRoles.add(managerRole);
            employee.setRoles(userRoles);

            employeeRepository.save(employee);
        }
    }

    @Override
    public EmployeeStatusDto checkStatus(EmployeeStatusDto employeeStatusDto) {
        Employee foundEmployee = employeeRepository.findEmployeeByUsername(employeeStatusDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(employeeStatusDto.getUsername()));

        logger.info("Received request to fetch status of employee with username: {}", foundEmployee.getUsername());
        return modelMapper.map(foundEmployee, EmployeeStatusDto.class);
    }

    public EmployeeDto createInitialAdmin(EmployeeDto request) {
        if (employeeRepository.count() > 0) {
            throw new IllegalStateException("An initial admin user already exists. This endpoint cannot be used again.");
        }
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            throw new RuntimeException("Error: ROLE_ADMIN not found in the database.");
        }
        Employee adminEntity = modelMapper.map(request, Employee.class);

        // 4. Set the fields that require special logic
        adminEntity.setEmployeeId(UUID.randomUUID().toString());
        adminEntity.setEncryptedPassword(passwordEncoder.encode(request.getPassword()));
        adminEntity.setVerificationToken(UUID.randomUUID().toString());
        adminEntity.setStatus(Status.ACTIVE);
        adminEntity.setRoles(Arrays.asList(adminRole));
        String adminUsername = generateUsername(request.getFirstName(), request.getLastName());
        adminEntity.setUsername(adminUsername);
        Employee savedAdmin = employeeRepository.save(adminEntity);
        EmployeeDto returnDto = modelMapper.map(savedAdmin, EmployeeDto.class);
        List<String> roleNames = savedAdmin.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        returnDto.setRoles(roleNames);
        return returnDto;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Employee> employeeToBeLoggedIn = employeeRepository.findByEmail(username);
        if(employeeToBeLoggedIn.isEmpty()){
            throw new UsernameNotFoundException(username);
        }
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Collection<Role> roles = employeeToBeLoggedIn.get().getRoles();

        roles.forEach((role) ->{
            authorities.add(new SimpleGrantedAuthority((role.getName())));
            Collection<Authority> authorityEntities = role.getAuthorities();

            authorityEntities.forEach((authorityEntity -> {
                authorities.add(new SimpleGrantedAuthority(authorityEntity.getName()));
            }));
        });

        //enabled after password can be false until the user successfully verifys their email

        return new CustomUserDetails(employeeToBeLoggedIn.get().getEmail(), employeeToBeLoggedIn.get().getEncryptedPassword(),
                true, true, true,true,
                authorities,employeeToBeLoggedIn.get().getEmployeeId(),
                employeeToBeLoggedIn.get().getDepartment());
    }

    @Transactional
    public void updateLastLoggedIn(String employeeId) {
        // 1. Find the employee in the database
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + employeeId));

        // 2. Set the lastLoggedIn field to the current date and time
        employee.setLastLoggedIn(new Date());

        // 3. Save the updated entity. The @Transactional annotation ensures this is committed.
        employeeRepository.save(employee);
    }

    private String generateUsername(String firstName, String lastName) {
        String firstNamePart = firstName.length() < 3 ?
                firstName :
                firstName.substring(0, 3);
        String lastNamePart = lastName.length() < 2 ?
                lastName :
                lastName.substring(lastName.length() - 2);
        int randomNumber = new Random().nextInt(900) + 100;

        return (firstNamePart + lastNamePart).toLowerCase() + randomNumber;
    }
    public boolean verifyUser(String token) {
        Optional<Employee> employeeWithVerificationToken = employeeRepository.findByVerificationToken(token);
        if (employeeWithVerificationToken.isPresent()) {
            Employee employee = employeeWithVerificationToken.get();
            employee.setStatus(Status.ACTIVE);
            employee.setVerificationToken(null); // Clear the token so it can't be used again
            employeeRepository.save(employee);
            return true;
        }
        return false;
    }

    private void publishUserCreatedEvent(Employee savedEmployee, String verificationToken) {
        // Create the specific DTO for the event
        UserCreatedEventDto eventDto = new UserCreatedEventDto(
                savedEmployee.getFirstName(),
                savedEmployee.getEmail(),
                verificationToken
        );
        // Send the correct eventDto to RabbitMQ
        rabbitTemplate.convertAndSend("user-events-exchange", "user.created", eventDto);
        logger.info("Published User Created event for email: {}", savedEmployee.getEmail());
    }

    @Transactional
    public int deactivateInactiveUsers() {
        // 1. Calculate the cutoff date (e.g., 2 months ago)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        Date cutoffDate = cal.getTime();
        // 2. Find all active users who haven't logged in since the cutoff date
        List<Employee> inactiveUsers = employeeRepository.findAllByStatusAndLastLoggedInBefore(Status.ACTIVE, cutoffDate);

        if (inactiveUsers.isEmpty()) {
            return 0; // No users to deactivate
        }
        // 3. Loop through the inactive users and update their status
        for (Employee user : inactiveUsers) {
            user.setStatus(Status.DEACTIVATED);
        }
        // 4. Save all the changes to the database in one batch
        employeeRepository.saveAll(inactiveUsers);
        return inactiveUsers.size();
    }
}