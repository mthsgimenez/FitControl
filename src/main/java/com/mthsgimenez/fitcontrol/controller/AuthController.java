package com.mthsgimenez.fitcontrol.controller;

import com.mthsgimenez.fitcontrol.dto.EmailDTO;
import com.mthsgimenez.fitcontrol.dto.ErrorDTO;
import com.mthsgimenez.fitcontrol.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.exception.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
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
            ErrorDTO response = new ErrorDTO("Could not register tenant. Check the provided information");
            return new ResponseEntity<ErrorDTO>(response, HttpStatus.BAD_REQUEST);
        } catch (EmailNotVerifiedException e) {
            ErrorDTO response = new ErrorDTO(e.getMessage());
            return new ResponseEntity<ErrorDTO>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
