package com.mthsgimenez.fitcontrol.infra.exception;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public ExceptionHandler(MessageSource messageSource) {
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

    @org.springframework.web.bind.annotation.ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(status.getReasonPhrase());
        problem.setDetail(ex.getReason());

        return ResponseEntity.status(status).body(problem);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle(messageSource.getMessage("problem.login-failed.title", null, LocaleContextHolder.getLocale()));
        problem.setDetail(messageSource.getMessage("problem.login-failed.detail", null, LocaleContextHolder.getLocale()));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundWithIdentifierException.class)
    public ResponseEntity<Object> handleNotFoundWithIdentifierException(NotFoundWithIdentifierException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase());
        problem.setDetail(ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        problem.setDetail(ex.getMessage());

        return new ResponseEntity<>(problem, HttpStatus.UNAUTHORIZED);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(FKConstraintViolationException.class)
    public ResponseEntity<Object> handleFKConstraintViolationException(FKConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
        problem.setDetail(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    private static final Pattern POSTGRES_DETAIL_PATTERN = Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\)");
    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleUniqueConstraint(DataIntegrityViolationException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        String detailMessage = "";

        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException cve) {
            String sqlMessage = cve.getSQLException().getMessage();
            if (sqlMessage != null && sqlMessage.toLowerCase().contains("duplicate key value violates unique constraint")) {
                Matcher matcher = POSTGRES_DETAIL_PATTERN.matcher(sqlMessage);
                if (matcher.find()) {
                    String field = matcher.group(1);
                    String value = matcher.group(2);
                    detailMessage = String.format("Conflicting value: field '%s' with value '%s' already exists", field, value);
                }
            }
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detailMessage);
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }
}
