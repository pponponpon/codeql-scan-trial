package com.example.trial.support;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class ApiErrorHandler {

    // BAIT-01: 401 / 403 handler writes a JSON body built only from fixed string literals via PrintWriter.
    // Nothing here is attacker-controlled; a tool that flags every PrintWriter.write as XSS is wrong.
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex, HttpServletResponse response) throws IOException {
        response.setStatus(ex.getStatus());
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        if (ex.getStatus() == 401) {
            writer.write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}");
        } else {
            writer.write("{\"code\":\"FORBIDDEN\",\"message\":\"Access denied\"}");
        }
        writer.flush();
    }
}
