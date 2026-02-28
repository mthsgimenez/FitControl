package com.mthsgimenez.fitcontrol.passwordtoken;

import com.mthsgimenez.fitcontrol.infra.exception.TokenOnCooldownException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

    @PostMapping("/request-token")
    public ResponseEntity<?> requestNewToken(@RequestBody @Valid RequestNewTokenDTO data) {
        try {
            passwordTokenService.sendPasswordTokenEmail(data.email(), EmailType.PASSWORD_RESET);
        } catch (TokenOnCooldownException e) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
            problem.setDetail("A token has already been sent to your email. You can send another in " + e.getCooldownRemaining() + " seconds");
            problem.setProperty("retryAfter", e.getCooldownRemaining());

            return new ResponseEntity<ProblemDetail>(problem, HttpStatus.TOO_MANY_REQUESTS);
        }
        return ResponseEntity.ok().build();
    }
}
