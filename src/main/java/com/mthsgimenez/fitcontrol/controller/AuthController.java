package com.mthsgimenez.fitcontrol.controller;

import com.mthsgimenez.fitcontrol.dto.EmailDTO;
import com.mthsgimenez.fitcontrol.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.dto.VerifyEmailDTO;
import com.mthsgimenez.fitcontrol.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/send-code")
    public ResponseEntity<Void> sendEmailVerification(@Valid @RequestBody EmailDTO email) {
        try {
            authService.sendVerificationCode(email.email());
            return ResponseEntity.ok().build();
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/register/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody VerifyEmailDTO data) {
        String registrationToken = authService.verifyEmail(data.email(), data.code());

        if (registrationToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid code");
        }

        return ResponseEntity.ok(registrationToken);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerTenant(@RequestBody TenantRegisterDTO data) {
        authService.registerTenant(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
