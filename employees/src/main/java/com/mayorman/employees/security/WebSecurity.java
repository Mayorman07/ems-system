package com.mayorman.employees.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayorman.employees.exceptions.CustomAuthenticationFailureHandler;
import com.mayorman.employees.services.EmployeeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

//@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurity {

    private final EmployeeService employeeService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final CustomAuthenticationFailureHandler failureHandler;

    public WebSecurity(Environment environment, EmployeeService employeeService, BCryptPasswordEncoder bCryptPasswordEncoder
            , ObjectMapper objectMapper, CustomAuthenticationFailureHandler failureHandler){
        this.environment = environment;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
        this.failureHandler = failureHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain configure (HttpSecurity http) throws Exception{

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);


        //method that will be used to look up in a database
        authenticationManagerBuilder.userDetailsService(employeeService)
                .passwordEncoder(bCryptPasswordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        //Create Authentication filter
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(employeeService, environment,authenticationManager,objectMapper);
        authenticationFilter.setAuthenticationFailureHandler(failureHandler);

        authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url.path"));

        http
                // Disable CSRF protection since this application is stateless (e.g., using JWTs)
                .csrf(AbstractHttpConfigurer::disable);

        // Configure authorization requests
        http.authorizeHttpRequests(authorize ->
                        authorize
//                                .requestMatchers(HttpMethod.GET, "/status/check").authenticated()
                                .requestMatchers("/api/setup/create-admin").permitAll()
                                .requestMatchers(HttpMethod.GET, "/status/check").permitAll()
                                .requestMatchers(HttpMethod.GET, "/verify").permitAll()
                                .requestMatchers("/verification_success.html", "/verification_failure.html").permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/employees/**")).permitAll()


                                .requestMatchers(new AntPathRequestMatcher("/actuator/**", HttpMethod.GET.name())).permitAll()
//                                        .requestMatchers(new AntPathRequestMatcher("/actuator/circuitbreakerevents", HttpMethod.GET.name())).permitAll()

                                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                                .anyRequest().authenticated())
                .addFilter(new AuthorizationFilter(authenticationManager,environment))
                .addFilter(authenticationFilter)

                .authenticationManager(authenticationManager)
                // Configure session management as stateless
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }

}
