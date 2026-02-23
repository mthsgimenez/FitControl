package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/employee")
@PreAuthorize("hasRole('MANAGER')")
public class EmployeeController {

    private final EmployeeRegistrationService employeeRegistrationService;
    private final UserRepository userRepository;

    public EmployeeController(
            EmployeeRegistrationService employeeRegistrationService,
            UserRepository userRepository
    ) {
        this.employeeRegistrationService = employeeRegistrationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Employee> registerEmployee(
            @Valid @RequestBody EmployeeRegistrationRequest data,
            Authentication auth
    ) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Unauthorized");
        }

        String userUUIDString = jwtAuth.getToken().getSubject();
        UUID userUUID = UUID.fromString(userUUIDString);

        User authUser = userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));

        Employee newEmployee = employeeRegistrationService.registerNewEmployee(data, authUser);
        return ResponseEntity.ok(newEmployee);
    }
}
