package com.mthsgimenez.fitcontrol.workout;

import com.mthsgimenez.fitcontrol.exercise.Exercise;
import com.mthsgimenez.fitcontrol.exercise.ExerciseRepository;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.member.MemberRepository;
import com.mthsgimenez.fitcontrol.user.RoleType;
import com.mthsgimenez.fitcontrol.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class WorkoutService {

    private final MemberRepository memberRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutMapper workoutMapper;

    public WorkoutService(
            MemberRepository memberRepository,
            WorkoutRepository workoutRepository,
            ExerciseRepository exerciseRepository,
            WorkoutMapper workoutMapper
    ) {
        this.memberRepository = memberRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutMapper = workoutMapper;
    }

    private Member getMemberFromUser(User authUser) {
        return memberRepository.findByPerson_User(authUser)
                .orElseThrow(() -> new IllegalStateException("Member entity not found for user"));
    }

    private Workout getWorkout(Integer workoutId, User authUser) {
        Member member = getMemberFromUser(authUser);

        return workoutRepository.findByIdAndMemberId(workoutId, member.getId())
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Workout.class.getSimpleName(), workoutId));
    }

    private PerformedExercise getPerformedExercise(Workout workout, Integer performedExerciseId) {
        return workout.getExercises().stream()
                .filter(e -> e.getId().equals(performedExerciseId))
                .findFirst()
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        PerformedExercise.class.getSimpleName(), performedExerciseId));
    }

    private PerformedSet getPerformedSet(PerformedExercise exercise, Integer setId) {
        return exercise.getSets().stream()
                .filter(s -> s.getId().equals(setId))
                .findFirst()
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        PerformedSet.class.getSimpleName(), setId));
    }

    public Workout createWorkout(User authUser) {
        Member member = getMemberFromUser(authUser);

        Workout newWorkout = new Workout();
        newWorkout.setWorkoutDate(LocalDate.now());
        newWorkout.setMember(member);

        return workoutRepository.save(newWorkout);
    }

    public Workout addExerciseToWorkout(Integer workoutId, Integer exerciseId, User authUser)  {
        Workout workout = getWorkout(workoutId, authUser);
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Exercise.class.getSimpleName(), exerciseId
                ));

        PerformedExercise performedExercise = new PerformedExercise();
        performedExercise.setExercise(exercise);
        workout.addExercise(performedExercise);

        return workout;
    }

    public Workout addSetToExercise(Integer workoutId, Integer performedExerciseId, SetDTO data, User authUser) {
        Workout workout = getWorkout(workoutId, authUser);
        PerformedExercise exercise = getPerformedExercise(workout, performedExerciseId);

        PerformedSet set = new PerformedSet();
        set.setRepetitions(data.repetitions());
        set.setWeight(data.weight());
        set.setNotes(data.notes());
        exercise.addSet(set);

        return workout;
    }

    public Workout updateSet(
            Integer workoutId, Integer performedExerciseId, Integer setId,
            SetDTO data, User authUser
    ) {
        Workout workout = getWorkout(workoutId, authUser);
        PerformedExercise exercise = getPerformedExercise(workout, performedExerciseId);
        PerformedSet set = getPerformedSet(exercise, setId);

        set.setNotes(data.notes());
        set.setRepetitions(data.repetitions());
        set.setWeight(data.weight());

        return workout;
    }

    public void deleteWorkout(Integer workoutId, User authUser) {
        Workout workout = getWorkout(workoutId, authUser);

        workoutRepository.delete(workout);
    }

    public void deleteExercise(Integer workoutId, Integer performedExerciseId, User authUser) {
        Workout workout = getWorkout(workoutId, authUser);
        PerformedExercise exercise = getPerformedExercise(workout, performedExerciseId);

        workout.getExercises().remove(exercise);
    }

    public void deleteSet(Integer workoutId, Integer performedExerciseId, Integer setId, User authUser) {
        Workout workout = getWorkout(workoutId, authUser);
        PerformedExercise exercise = getPerformedExercise(workout, performedExerciseId);
        PerformedSet set = getPerformedSet(exercise, setId);

        exercise.getSets().remove(set);
    }

    @Transactional(readOnly = true)
    public Workout findWorkout(Integer workoutId, User authUser) {
        if (authUser.hasRole(RoleType.INSTRUCTOR) || authUser.hasRole(RoleType.OWNER)) {
            return workoutRepository.findById(workoutId)
                    .orElseThrow(() -> new NotFoundWithIdentifierException(Workout.class.getSimpleName(), workoutId));
        }

        return getWorkout(workoutId, authUser);
    }

    @Transactional(readOnly = true)
    public Page<Workout> getMemberWorkouts(Integer memberId, Pageable pageable, User authUser) {
        if (authUser.hasRole(RoleType.INSTRUCTOR) || authUser.hasRole(RoleType.OWNER)) {
            return workoutRepository.findByMemberId(memberId, pageable);
        }

        Member member = getMemberFromUser(authUser);

        if (!member.getId().equals(memberId)) {
            throw new AccessDeniedException("Not allowed");
        }

        return workoutRepository.findByMemberId(memberId, pageable);
    }

    public WorkoutFullResponseDTO createWorkoutAsDto(User authUser) {
        return workoutMapper.toFullDto(createWorkout(authUser));
    }

    public WorkoutFullResponseDTO addExerciseToWorkoutAsDto(
            Integer workoutId,
            Integer exerciseId,
            User authUser
    ) {
        return workoutMapper.toFullDto(addExerciseToWorkout(
                workoutId,
                exerciseId,
                authUser
        ));
    }

    public WorkoutFullResponseDTO addSetToExerciseAsDto(
            Integer workoutId,
            Integer performedExerciseId,
            SetDTO data,
            User authUser
    ) {
        return workoutMapper.toFullDto(addSetToExercise(
                workoutId,
                performedExerciseId,
                data,
                authUser
        ));
    }

    public WorkoutFullResponseDTO updateSetAsDto(
            Integer workoutId,
            Integer performedExerciseId,
            Integer setId,
            SetDTO data,
            User user
    ) {
        return workoutMapper.toFullDto(updateSet(
                workoutId,
                performedExerciseId,
                setId,
                data,
                user
        ));
    }

    @Transactional(readOnly = true)
    public WorkoutFullResponseDTO findWorkoutAsDto(Integer workoutId, User authUser) {
        return workoutMapper.toFullDto(
                findWorkout(workoutId, authUser)
        );
    }

    @Transactional(readOnly = true)
    public Page<WorkoutResponseDTO> getMemberWorkoutsAsDto(
            Integer memberId,
            Pageable pageable,
            User authUser
    ) {
        return getMemberWorkouts(memberId, pageable, authUser)
                .map(workoutMapper::toSimpleDto);
    }
}