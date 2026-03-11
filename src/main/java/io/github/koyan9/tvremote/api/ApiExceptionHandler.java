package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.service.ControlRoutingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, exception.getMessage(), request, "resource.not-found");
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, ControlRoutingException.class})
    public ProblemDetail handleBadRequest(Exception exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.BAD_REQUEST, exception.getMessage(), request, "request.invalid");
    }

    @ExceptionHandler(io.github.koyan9.tvremote.integration.IntegrationDisabledException.class)
    public ProblemDetail handleIntegrationDisabled(RuntimeException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, "integration.disabled");
    }

    @ExceptionHandler(io.github.koyan9.tvremote.integration.IntegrationConfigurationException.class)
    public ProblemDetail handleIntegrationConfiguration(RuntimeException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request, "integration.config.missing");
    }

    @ExceptionHandler(io.github.koyan9.tvremote.integration.IntegrationTransportException.class)
    public ProblemDetail handleIntegrationTransport(RuntimeException exception, HttpServletRequest request) {
        String code = exception instanceof io.github.koyan9.tvremote.integration.IntegrationTimeoutException
                ? "integration.transport.timeout"
                : "integration.transport.failure";
        return buildProblem(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, code);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request, "internal.error");
    }

    private ProblemDetail buildProblem(HttpStatus status, String message, HttpServletRequest request, String code) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("https://example.com/problems/remote-control"));
        problemDetail.setProperty("path", request.getRequestURI());
        if (code != null) {
            problemDetail.setProperty("code", code);
        }
        return problemDetail;
    }
}


