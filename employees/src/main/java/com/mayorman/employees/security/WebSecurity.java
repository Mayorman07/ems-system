package com.mayorman.employees.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class WebSecurity {

    private final EmployeeService employeeService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final Environment environment;

    private final ObjectMapper objectMapper;

    public  WebSecurity(Environment environment, EmployeeService employeeService, BCryptPasswordEncoder bCryptPasswordEncoder, ObjectMapper objectMapper){
        this.environment = environment;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain configure (HttpSecurity http) throws Exception{
        //configure authentication manager builder, 37 -> which method {users service that implements userdetailsservice}
        // contains details that can be used for lookups in a DB
        //The passwordEncoder(bCryptPasswordEncoder) specifies the password encoder (bcrypt in this case)
        // to compare the stored password with the user-provided password during authentication.
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.userDetailsService(employeeService)
                .passwordEncoder(bCryptPasswordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        //Create Authentication filter
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(employeeService, environment,authenticationManager,objectMapper);
        authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url.path"));


        http
                // Disable CSRF protection since this application is stateless (e.g., using JWTs)
                .csrf(AbstractHttpConfigurer::disable);

        // Configure authorization requests
        http.authorizeHttpRequests(authorize ->
                        authorize
//                                .requestMatchers(HttpMethod.GET, "/status/check").authenticated()
                                .requestMatchers(HttpMethod.GET, "/status/check").permitAll()

//                                        .requestMatchers(HttpMethod.GET, "/users/status/check").permitAll()  // Allow all requests to /users/status/check
                                // Permit all POST requests to /users (e.g., for user registration) from a particular Ip address
//                                .requestMatchers(HttpMethod.POST, "/users")
//                                .access(new WebExpressionAuthorizationManager("hasIpAddress('"+environment.getProperty("gateway.ip")+"')"))
                                .requestMatchers(new AntPathRequestMatcher("/employees/**")).permitAll()


                                .requestMatchers(new AntPathRequestMatcher("/actuator/**", HttpMethod.GET.name())).permitAll()
//                                        .requestMatchers(new AntPathRequestMatcher("/actuator/circuitbreakerevents", HttpMethod.GET.name())).permitAll()

                                // Permit all requests to /h2-console/**
                                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                                // Any other requests must be authenticated
                                .anyRequest().authenticated())
                .addFilter(new AuthorizationFilter(authenticationManager,environment))
                // Add authentication filter
                .addFilter(authenticationFilter)

                .authenticationManager(authenticationManager)
                // Configure session management as stateless
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );


        // Disable X-Frame-Options to allow H2 Console to be displayed in a frame
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }

}
