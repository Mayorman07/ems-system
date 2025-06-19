package com.mayorman.employees.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.models.requests.LoginRequest;
import com.mayorman.employees.services.EmployeeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;


@Component
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final EmployeeService employeeService;
    private final Environment environment;

    public AuthenticationFilter(EmployeeService employeeService,Environment environment, AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.environment = environment;
        this.employeeService = employeeService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        try {

            LoginRequest loginCredentials = new ObjectMapper().readValue(req.getInputStream(), LoginRequest.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(loginCredentials.getEmail(), loginCredentials.getPassword(), new ArrayList<>()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res, FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        String username = ((User)auth.getPrincipal()).getUsername();
        EmployeeDto userDetails = employeeService.getEmployeeDetailsByEmail(username);
        String tokenSecret = environment.getProperty("token.secret.key");
        if (tokenSecret == null) {
            throw new RuntimeException("Token secret key is missing in the configuration!");
        }
        // Generate a secure key (replace with securely generated key bytes)
//        assert tokenSecret != null;
        byte[] secretKeyBytes = tokenSecret.getBytes(StandardCharsets.UTF_8);
        // Create a SecretKey using Keys.hMacShaKeyFor -> length of string determines algorithm to be used
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

        Instant now = Instant.now();
        // Use the SecretKey to sign a JWT
        String token = Jwts.builder()
                .claim("scope", auth.getAuthorities())
                .subject(userDetails.getUserId())
                .expiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("token.expiration.time")))))
                .issuedAt(Date.from(now))
                .signWith(secretKey)
                .compact();


        // Set the JWT in the response header (optional)
        res.addHeader("token", token);
        res.addHeader("userId", userDetails.getUserId());
    }
}

}
