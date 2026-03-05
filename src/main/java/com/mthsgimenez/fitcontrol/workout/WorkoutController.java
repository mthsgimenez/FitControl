package com.mthsgimenez.fitcontrol.workout;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/workout")
@PreAuthorize("hasRole('MEMBER')")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final WorkoutMapper workoutMapper;
    private final UserRepository userRepository;

    public WorkoutController(
            WorkoutService workoutService,
            WorkoutMapper workoutMapper,
            UserRepository userRepository
    ) {
        this.workoutService = workoutService;
        this.workoutMapper = workoutMapper;
        this.userRepository = userRepository;
    }

    private User getUserFromAuthentication(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Unauthorized");
        }

        String userUUIDString = jwtAuth.getToken().getSubject();
        UUID userUUID = UUID.fromString(userUUIDString);

        return userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));
    }

    @PostMapping
    public ResponseEntity<WorkoutFullResponseDTO> createWorkout(Authentication auth) {
        User user = getUserFromAuthentication(auth);

        Workout newWorkout = workoutService.createWorkout(user);
        WorkoutFullResponseDTO response = workoutMapper.toFullDto(newWorkout);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{workoutId}/exercise")
    public ResponseEntity<WorkoutFullResponseDTO> addPerformedExercise(
            @PathVariable Integer workoutId,
            @RequestBody @Valid PerformedExerciseDTO data,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        Workout workout = workoutService.addExerciseToWorkout(workoutId, data.exerciseId(), user);
        WorkoutFullResponseDTO response = workoutMapper.toFullDto(workout);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{workoutId}/exercise/{exerciseId}")
    public ResponseEntity<WorkoutFullResponseDTO> addPerformedExercise(
            @PathVariable Integer workoutId,
            @PathVariable Integer exerciseId,
            @RequestBody @Valid SetDTO data,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        Workout workout = workoutService.addSetToExercise(workoutId, exerciseId, data, user);
        WorkoutFullResponseDTO response = workoutMapper.toFullDto(workout);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
