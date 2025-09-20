package com.mayorman.employees.configuration;

import com.mayorman.employees.entities.Employee;
import com.mayorman.employees.models.data.EmployeeDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);

        // This prevents the request from setting the ID field
        TypeMap<EmployeeDto, Employee> dtoToEntityMap = modelMapper.createTypeMap(EmployeeDto.class, Employee.class);
        dtoToEntityMap.addMappings(mapper -> mapper.skip(Employee::setId));

        // This skips the 'roles' field so you can map it manually
        TypeMap<Employee, EmployeeDto> entityToDtoMap = modelMapper.createTypeMap(Employee.class, EmployeeDto.class);
        entityToDtoMap.addMappings(mapper -> mapper.skip(EmployeeDto::setRoles));

        return modelMapper;
    }
}