package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employee")
@PreAuthorize("hasRole('MANAGER')")
public class EmployeeController {

    private final EmployeeRegistrationService employeeRegistrationService;
    private final UserRepository userRepository;
    private final EmployeeService employeeService;

    public EmployeeController(
            EmployeeRegistrationService employeeRegistrationService,
            UserRepository userRepository,
            EmployeeService employeeService
    ) {
        this.employeeRegistrationService = employeeRegistrationService;
        this.userRepository = userRepository;
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> registerEmployee(
            @Valid @RequestBody EmployeeRegistrationRequestDTO data,
            Authentication auth
    ) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Unauthorized");
        }

        String userUUIDString = jwtAuth.getToken().getSubject();
        UUID userUUID = UUID.fromString(userUUIDString);
        User authUser = userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));

        EmployeeResponseDTO responseDTO = employeeRegistrationService.registerNewEmployeeAsDto(data, authUser);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> editEmployeeRoles(
            @PathVariable Integer id,
            @RequestBody @Valid EmployeeRolesRequestDTO data
    ) {
        return ResponseEntity.ok(employeeService.editEmployeeRolesAsDto(id, data.roles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.findByIdAsDto(id));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployees() {
        return ResponseEntity.ok(employeeService.findAllAsDto());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}