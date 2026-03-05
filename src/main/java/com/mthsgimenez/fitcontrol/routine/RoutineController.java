package com.mthsgimenez.fitcontrol.routine;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        User authUser = getAuthUser(auth);

        Routine newRoutine = routineService.createRoutine(data, authUser);
        RoutineFullResponseDTO response = routineMapper.toFullDto(newRoutine);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<RoutineResponseDTO>> getRoutinesByMember(@PathVariable Integer memberId) {
        List<Routine> routines = routineService.findByMemberId(memberId);

        List<RoutineResponseDTO> response = routines.stream()
                .map(routineMapper::toSimpleDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineFullResponseDTO> getRoutine(
            @PathVariable Integer id,
            Authentication auth
    ) {
        User authUser = getAuthUser(auth);

        Routine routine = routineService.findById(id, authUser);
        RoutineFullResponseDTO response = routineMapper.toFullDto(routine);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineFullResponseDTO> updateRoutine(
            @PathVariable Integer id,
            @Valid @RequestBody RoutineDTO dto,
            Authentication auth
    ) {
        User authUser = getAuthUser(auth);

        Routine updatedRoutine = routineService.updateRoutine(id, dto, authUser);
        RoutineFullResponseDTO response = routineMapper.toFullDto(updatedRoutine);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutine(
            @PathVariable Integer id,
            Authentication auth
    ) {
        User authUser = getAuthUser(auth);

        routineService.deleteById(id, authUser);

        return ResponseEntity.noContent().build();
    }

    private User getAuthUser(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Unauthorized");
        }

        String userUUIDString = jwtAuth.getToken().getSubject();
        UUID userUUID = UUID.fromString(userUUIDString);

        return userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));
    }
}
