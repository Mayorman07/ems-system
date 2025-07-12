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
        // This configuration is essential to prevent the error
        TypeMap<EmployeeDto, Employee> typeMap = modelMapper.createTypeMap(EmployeeDto.class, Employee.class);
        typeMap.addMappings(mapper -> mapper.skip(Employee::setId));
        // You can add specific mappings here if needed
        return modelMapper;
    }
}