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
        return buildProblem(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, ControlRoutingException.class})
    public ProblemDetail handleBadRequest(Exception exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    private ProblemDetail buildProblem(HttpStatus status, String message, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("https://example.com/problems/remote-control"));
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }
}


