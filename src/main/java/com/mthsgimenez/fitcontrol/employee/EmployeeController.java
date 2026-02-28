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
    private final EmployeeMapper employeeMapper;
    private final EmployeeService employeeService;

    public EmployeeController(
            EmployeeRegistrationService employeeRegistrationService,
            UserRepository userRepository,
            EmployeeMapper employeeMapper,
            EmployeeService employeeService
    ) {
        this.employeeRegistrationService = employeeRegistrationService;
        this.userRepository = userRepository;
        this.employeeMapper = employeeMapper;
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

        Employee newEmployee = employeeRegistrationService.registerNewEmployee(data, authUser);
        EmployeeResponseDTO responseDTO = employeeMapper.toResponseDTO(newEmployee);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> editEmployeeRoles(@PathVariable Integer id, @RequestBody @Valid EmployeeRolesRequestDTO data) {
        Employee updatedEmployee = employeeService.editEmployeeRoles(id, data.roles());
        var response = employeeMapper.toResponseDTO(updatedEmployee);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable Integer id) {
        Employee employee = employeeService.findById(id);
        var response = employeeMapper.toResponseDTO(employee);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployees() {
        List<Employee> employees = employeeService.findAll();
        var responseList = employees.stream().map(employeeMapper::toResponseDTO).toList();

        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
