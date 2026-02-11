package com.mthsgimenez.fitcontrol.auth.controller;

import com.mthsgimenez.fitcontrol.auth.dto.*;
import com.mthsgimenez.fitcontrol.auth.exception.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.auth.exception.InvalidTokenException;
import com.mthsgimenez.fitcontrol.auth.service.EmailVerificationService;
import com.mthsgimenez.fitcontrol.auth.service.LoginService;
import com.mthsgimenez.fitcontrol.auth.service.RefreshTokenService;
import com.mthsgimenez.fitcontrol.auth.service.RegisterTenantService;
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

    private final EmailVerificationService emailVerificationService;
    private final LoginService loginService;
    private final RegisterTenantService registerTenantService;
    private final MessageSource messageSource;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            EmailVerificationService emailVerificationService,
            LoginService loginService,
            RegisterTenantService registerTenantService,
            MessageSource messageSource,
            RefreshTokenService refreshTokenService
    ) {
        this.emailVerificationService = emailVerificationService;
        this.loginService = loginService;
        this.registerTenantService = registerTenantService;
        this.messageSource = messageSource;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> sendEmailVerificationCode(@Valid @RequestBody EmailDTO email) {
            UUID verificationId = emailVerificationService.sendVerificationEmail(email);
            return ResponseEntity.ok(Collections.singletonMap("verificationId", verificationId.toString()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(@Valid @RequestBody TenantRegisterDTO data) {
        try {
            registerTenantService.registerNewTenant(data);
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
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO data) {
        TokenDTO token = loginService.login(data);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO data) {
        String refreshToken = data.refreshToken();
        try {
            TokenDTO refreshedTokens = refreshTokenService.refreshTokens(refreshToken);
            return ResponseEntity.ok(refreshedTokens);
        } catch (InvalidTokenException e) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            problem.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
            problem.setDetail(e.getMessage());

            return new ResponseEntity<>(problem, HttpStatus.UNAUTHORIZED);
        }
    }
}
