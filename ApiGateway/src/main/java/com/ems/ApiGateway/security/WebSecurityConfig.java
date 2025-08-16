//package com.ems.ApiGateway.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//@EnableWebFluxSecurity // Use the reactive security annotation
//public class WebSecurityConfig {
//
//    private static final String LOGIN_URL = "/employees/login";
//
//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//        http
//                .authorizeExchange(exchanges -> exchanges
//                        // --- THIS IS THE CORRECTED PART ---
//                        // The path must match the full path on the gateway
//                        .pathMatchers(HttpMethod.POST, "/employees/employees/create").permitAll()
//                        .pathMatchers(HttpMethod.POST, "/employees/employees/login").permitAll()
//                        .pathMatchers(HttpMethod.GET, "/employees/employees/verify").permitAll()
//                        // ------------------------------------
//
//                        // All other requests must be authenticated.
//                        .anyExchange().authenticated()
//                )
//                .csrf(ServerHttpSecurity.CsrfSpec::disable);
//
//        return http.build();
//    }
//}