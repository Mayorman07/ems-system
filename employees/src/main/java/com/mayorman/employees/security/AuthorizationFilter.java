package com.mayorman.employees.security;
import com.parser.JwtAuthorities.JwtClaimsParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
@Component
public class AuthorizationFilter extends BasicAuthenticationFilter {


    private final Environment environment;

    public AuthorizationFilter(AuthenticationManager authenticationManager,
                               Environment environment) {
        super(authenticationManager);
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        String authorizationHeader = req.getHeader(environment.getProperty("authorization.token.header.name"));

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(environment.getProperty("authorization.token.header.prefix"))) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest req) {
        String authorizationHeader = req.getHeader(environment.getProperty("authorization.token.header.name"));

        if (authorizationHeader == null) {
            return null;
        }

        String token = authorizationHeader.replace(Objects.requireNonNull(environment.getProperty("authorization.token.header.prefix")), "").trim();
        String tokenSecret = environment.getProperty("token.secret.key");

        System.out.println("DEBUG: token.secret.key retrieved: " + tokenSecret);

        if(tokenSecret==null) return null;

        JwtClaimsParser jwtClaimsParser = new JwtClaimsParser(token, tokenSecret);

        String userId = jwtClaimsParser.getJwtSubject();

//        byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
//        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
//
//        JwtParser parser = Jwts.parser()
//                .verifyWith(secretKey)
//                .build();

//        byte[] secretKeyBytes = tokenSecret.getBytes(StandardCharsets.UTF_8);
//        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
//        JwtParser parser = Jwts.parser()
//                .verifyWith(secretKey)
//                .build();
//
//        Claims claims = parser.parseSignedClaims(token).getPayload();
//        String userId = (String) claims.get("sub");

        if (userId == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(userId, null, jwtClaimsParser.getUserAuthorities());

    }
}
