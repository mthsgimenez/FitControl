package com.mthsgimenez.fitcontrol.passwordtoken;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/password")
public class PasswordTokenController {

    private final PasswordTokenService passwordTokenService;

    public PasswordTokenController(PasswordTokenService passwordTokenService) {
        this.passwordTokenService = passwordTokenService;
    }

    @PostMapping("/set")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid SetPasswordRequestDTO data) {
        passwordTokenService.setNewPassword(data.token(), data.password());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-token")
    public ResponseEntity<Void> resendToken(@RequestBody @Valid ResendTokenRequestDTO data) {
        passwordTokenService.resendPasswordToken(data.email());
        return ResponseEntity.ok().build();
    }
}
