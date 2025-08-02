package com.ConfigurationServer.ConfigServer.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private Environment environment;

    @Bean
    public InMemoryUserDetailsManager userDetailsManager(PasswordEncoder passwordEncoder) {
        UserDetails admin = User
                // Use environment properties to get user details
                .withUsername(environment.getProperty("spring.security.user.name"))
                .password(passwordEncoder.encode(environment.getProperty("spring.security.user.password")))
                // Make sure you have spring.security.user.roles=ADMIN in your properties file
                .roles(environment.getProperty("spring.security.user.roles"))
                .build();

        UserDetails client = User
                // Use environment properties to get user details
                .withUsername(environment.getProperty("mayor-spring.security.user.name"))
                .password(passwordEncoder.encode(environment.getProperty("mayor-spring.security.user.password")))
                // Make sure you have spring.security.user.roles=ADMIN in your properties file
                .roles(environment.getProperty("mayor-spring.security.user.roles"))
                .build();
        return new InMemoryUserDetailsManager(admin,client);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        // This rule correctly restricts the POST method on actuator endpoints to ADMINs.
                        .requestMatchers(HttpMethod.POST, "/actuator/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/encrypt").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/decrypt").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/**").hasAnyRole("CLIENT", "ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/actuator/**"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/actuator/busrefresh", "/encrypt","/decrypt"))
                .httpBasic(Customizer.withDefaults());

        return httpSecurity.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}