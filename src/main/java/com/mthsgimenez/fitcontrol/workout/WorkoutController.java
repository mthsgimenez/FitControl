package com.mthsgimenez.fitcontrol.workout;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

        Workout workout = workoutService.createWorkout(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutMapper.toFullDto(workout));
    }

    @PostMapping("/{workoutId}/exercise")
    public ResponseEntity<WorkoutFullResponseDTO> addPerformedExercise(
            @PathVariable Integer workoutId,
            @RequestBody @Valid PerformedExerciseDTO data,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        Workout workout = workoutService.addExerciseToWorkout(
                workoutId,
                data.exerciseId(),
                user
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutMapper.toFullDto(workout));
    }

    @PostMapping("/{workoutId}/exercise/{performedExerciseId}/set")
    public ResponseEntity<WorkoutFullResponseDTO> addSet(
            @PathVariable Integer workoutId,
            @PathVariable Integer performedExerciseId,
            @RequestBody @Valid SetDTO data,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        Workout workout = workoutService.addSetToExercise(
                workoutId,
                performedExerciseId,
                data,
                user
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutMapper.toFullDto(workout));
    }

    @PutMapping("/{workoutId}/exercise/{performedExerciseId}/set/{setId}")
    public ResponseEntity<WorkoutFullResponseDTO> updateSet(
            @PathVariable Integer workoutId,
            @PathVariable Integer performedExerciseId,
            @PathVariable Integer setId,
            @RequestBody @Valid SetDTO data,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        Workout workout = workoutService.updateSet(
                workoutId,
                performedExerciseId,
                setId,
                data,
                user
        );

        return ResponseEntity.ok(workoutMapper.toFullDto(workout));
    }

    @DeleteMapping("/{workoutId}")
    public ResponseEntity<Void> deleteWorkout(
            @PathVariable Integer workoutId,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        workoutService.deleteWorkout(workoutId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{workoutId}/exercise/{performedExerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Integer workoutId,
            @PathVariable Integer performedExerciseId,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        workoutService.deleteExercise(workoutId, performedExerciseId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{workoutId}/exercise/{performedExerciseId}/set/{setId}")
    public ResponseEntity<Void> deleteSet(
            @PathVariable Integer workoutId,
            @PathVariable Integer performedExerciseId,
            @PathVariable Integer setId,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        workoutService.deleteSet(workoutId, performedExerciseId, setId, user);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'INSTRUCTOR')")
    @GetMapping("/{workoutId}")
    public ResponseEntity<WorkoutFullResponseDTO> getWorkout(
            @PathVariable Integer workoutId,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        Workout workout = workoutService.findWorkout(workoutId, user);
        return ResponseEntity.ok(workoutMapper.toFullDto(workout));
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'INSTRUCTOR')")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<Page<WorkoutResponseDTO>> getMemberWorkouts(
            @PathVariable Integer memberId,
            @PageableDefault(sort = "workoutDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication auth
    ) {
        User user = getUserFromAuthentication(auth);

        Page<WorkoutResponseDTO> response =
                workoutService.getMemberWorkouts(memberId, pageable, user)
                        .map(workoutMapper::toSimpleDto);

        return ResponseEntity.ok(response);
    }
}