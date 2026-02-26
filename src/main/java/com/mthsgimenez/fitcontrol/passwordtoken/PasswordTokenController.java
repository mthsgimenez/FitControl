package com.mthsgimenez.fitcontrol.passwordtoken;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordTokenController {

    private final PasswordTokenService passwordTokenService;

    public PasswordTokenController(PasswordTokenService passwordTokenService) {
        this.passwordTokenService = passwordTokenService;
    }

    @PostMapping("/set-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid PasswordTokenRequestDTO data) {
        passwordTokenService.setNewPassword(data.token(), data.password());

        return ResponseEntity.ok().build();
    }
}
