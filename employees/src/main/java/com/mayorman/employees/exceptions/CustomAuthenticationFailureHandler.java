package com.mayorman.employees.exceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // Check if the reason for failure was a disabled account
        if (exception instanceof DisabledException) {
            // If so, redirect to the custom "deactivated" page
            getRedirectStrategy().sendRedirect(request, response, "/account-deactivated.html");
        } else {
            // For all other login failures (e.g., bad password), redirect to a generic error page
            super.setDefaultFailureUrl("/login-error.html");
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
