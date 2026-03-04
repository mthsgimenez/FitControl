package com.mthsgimenez.fitcontrol.routine;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/routine")
public class RoutineController {

    private final RoutineService routineService;
    private final RoutineMapper routineMapper;
    private final UserRepository userRepository;

    public RoutineController(
            RoutineService routineService,
            RoutineMapper routineMapper,
            UserRepository userRepository
    ) {
        this.routineService = routineService;
        this.routineMapper = routineMapper;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<RoutineFullResponseDTO> createRoutine(
            @Valid @RequestBody RoutineDTO data,
            Authentication auth
    ) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Unauthorized");
        }

        String userUUIDString = jwtAuth.getToken().getSubject();
        UUID userUUID = UUID.fromString(userUUIDString);
        User authUser = userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));

        Routine newRoutine = routineService.createRoutine(data, authUser);
        RoutineFullResponseDTO response = routineMapper.toFullDto(newRoutine);

        return ResponseEntity.ok(response);
    }
}
