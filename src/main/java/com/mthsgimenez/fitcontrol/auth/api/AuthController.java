package com.mthsgimenez.fitcontrol.auth.api;

import com.mthsgimenez.fitcontrol.auth.login.LoginDTO;
import com.mthsgimenez.fitcontrol.auth.login.LoginService;
import com.mthsgimenez.fitcontrol.auth.refreshtokens.InvalidTokenException;
import com.mthsgimenez.fitcontrol.auth.refreshtokens.RefreshTokenService;
import com.mthsgimenez.fitcontrol.auth.refreshtokens.TokenDTO;
import com.mthsgimenez.fitcontrol.emailverification.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.emailverification.EmailVerificationService;
import com.mthsgimenez.fitcontrol.tenant.TenantRegisterRequestDTO;
import com.mthsgimenez.fitcontrol.tenant.TenantRegistrationService;
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

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final LoginService loginService;
    private final TenantRegistrationService tenantRegistrationService;
    private final MessageSource messageSource;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            EmailVerificationService emailVerificationService,
            LoginService loginService,
            TenantRegistrationService tenantRegistrationService,
            MessageSource messageSource,
            RefreshTokenService refreshTokenService
    ) {
        this.emailVerificationService = emailVerificationService;
        this.loginService = loginService;
        this.tenantRegistrationService = tenantRegistrationService;
        this.messageSource = messageSource;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> sendEmailVerificationCode(@Valid @RequestBody EmailRequestDTO email) {
            emailVerificationService.sendVerificationEmail(email.email());
            return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(@Valid @RequestBody TenantRegisterRequestDTO data) {
        try {
            tenantRegistrationService.registerNewTenant(data);
        } catch (DataIntegrityViolationException e) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problem.setTitle(messageSource.getMessage("problem.tenant-registration-failed.title", null, LocaleContextHolder.getLocale()));
            problem.setDetail(messageSource.getMessage("problem.tenant-registration-failed.detail", null, LocaleContextHolder.getLocale()));
            return new ResponseEntity<ProblemDetail>(problem, HttpStatus.BAD_REQUEST);
        } catch (EmailNotVerifiedException e) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
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
