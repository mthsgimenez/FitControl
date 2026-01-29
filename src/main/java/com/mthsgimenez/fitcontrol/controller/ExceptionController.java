package com.mthsgimenez.fitcontrol.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public ExceptionController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(status);

        problem.setTitle(messageSource.getMessage("problem.argument-not-valid.title", null, LocaleContextHolder.getLocale()));
        problem.setDetail(messageSource.getMessage("problem.argument-not-valid.detail", null, LocaleContextHolder.getLocale()));

        var errors = ex.getBindingResult().getFieldErrors().stream().map(
                err -> Map.of(
                        "field", err.getField(),
                        "message", messageSource.getMessage(err, LocaleContextHolder.getLocale())
                )
        ).toList();

        problem.setProperty("errors", errors);

        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle(messageSource.getMessage("problem.login-failed.title", null, LocaleContextHolder.getLocale()));
        problem.setDetail(messageSource.getMessage("problem.login-failed.detail", null, LocaleContextHolder.getLocale()));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }
}
