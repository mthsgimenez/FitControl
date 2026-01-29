package com.mthsgimenez.fitcontrol.controller;

import com.mthsgimenez.fitcontrol.dto.EmailDTO;
import com.mthsgimenez.fitcontrol.dto.LoginDTO;
import com.mthsgimenez.fitcontrol.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.exception.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final MessageSource messageSource;

    public AuthController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> sendEmailVerificationCode(@Valid @RequestBody EmailDTO email) {
        try {
            UUID verificationId = authService.sendVerificationCode(email);
            return ResponseEntity.ok(Collections.singletonMap("verificationId", verificationId.toString()));
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(@Valid @RequestBody TenantRegisterDTO data) {
        try {
            authService.registerTenant(data);
        } catch (DataIntegrityViolationException e) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problem.setTitle(messageSource.getMessage("problem.tenant-registration-failed.title", null, LocaleContextHolder.getLocale()));
            problem.setDetail(messageSource.getMessage("problem.tenant-registration-failed.detail", null, LocaleContextHolder.getLocale()));
            return new ResponseEntity<ProblemDetail>(problem, HttpStatus.BAD_REQUEST);
        } catch (EmailNotVerifiedException e) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problem.setTitle(messageSource.getMessage("problem.email-verification-failed.title", null, LocaleContextHolder.getLocale()));
            problem.setDetail(e.getMessage());
            return new ResponseEntity<ProblemDetail>(problem, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDTO data) {
        String token = authService.login(data);

        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
